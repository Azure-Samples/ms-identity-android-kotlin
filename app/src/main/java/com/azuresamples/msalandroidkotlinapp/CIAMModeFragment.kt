package com.azuresamples.msalandroidkotlinapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.microsoft.identity.client.AcquireTokenParameters
import com.microsoft.identity.client.AcquireTokenSilentParameters
import com.microsoft.identity.client.AuthenticationCallback
import com.microsoft.identity.client.IAccount
import com.microsoft.identity.client.IAuthenticationResult
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication.RemoveAccountCallback
import com.microsoft.identity.client.IPublicClientApplication.IMultipleAccountApplicationCreatedListener
import com.microsoft.identity.client.IPublicClientApplication.LoadAccountsCallback
import com.microsoft.identity.client.PublicClientApplication
import com.microsoft.identity.client.SilentAuthenticationCallback
import com.microsoft.identity.client.exception.MsalClientException
import com.microsoft.identity.client.exception.MsalException
import com.microsoft.identity.client.exception.MsalServiceException
import com.microsoft.identity.client.exception.MsalUiRequiredException
import java.util.Arrays
import java.util.Locale


/**
 * This fragment support CIAM Authority in Multiple Account mode. Dedicated CIAM Authority support has not been fully released yet, this fragment
 * and it's json configuration file will be updated once that release is complete (ETA Q2 2023)
 */
class CIAMModeFragment() : Fragment() {
    /* UI & Debugging Variables */
    lateinit var removeAccountButton: Button
    lateinit var callAcquireTokenInteractiveButton: Button
    lateinit var callAcquireTokenSilentButton: Button
    lateinit var scopeTextView: TextView
    lateinit var logTextView: TextView
    lateinit var accountListSpinner: Spinner

    /* Azure AD Variables */
    private var mCiamApp: IMultipleAccountPublicClientApplication? = null
    private var accountList: List<IAccount>? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_ciam_mode, container, false)
        initializeUI(view)

        // Creates a PublicClientApplication object with res/raw/auth_config_ciam.json
        context?.let {
            PublicClientApplication.createMultipleAccountPublicClientApplication(
                it,
                R.raw.auth_config_ciam,
                object : IMultipleAccountApplicationCreatedListener {
                    override fun onCreated(application: IMultipleAccountPublicClientApplication) {
                        mCiamApp = application
                        loadAccounts()
                    }

                    override fun onError(exception: MsalException) {
                        displayError(exception)
                        removeAccountButton!!.isEnabled = false
                        callAcquireTokenInteractiveButton!!.isEnabled = false
                        callAcquireTokenSilentButton!!.isEnabled = false
                    }
                })
        }
        return view
    }

    /**
     * Initializes UI variables and callbacks.
     */
    private fun initializeUI(view: View) {
        removeAccountButton = view.findViewById<Button>(R.id.btn_removeAccount)
        callAcquireTokenInteractiveButton =
            view.findViewById<Button>(R.id.btn_acquireTokenInteractively)
        callAcquireTokenSilentButton = view.findViewById<Button>(R.id.btn_acquireTokenSilently)
        scopeTextView = view.findViewById<TextView>(R.id.scope)
        logTextView = view.findViewById<TextView>(R.id.txt_log)
        accountListSpinner = view.findViewById<Spinner>(R.id.account_list)
        removeAccountButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (mCiamApp == null) {
                    return
                }
                /**
                 * Removes the selected account and cached tokens from this app (or device, if the device is in shared mode).
                 */
                mCiamApp!!.removeAccount(
                    accountList!!.get(accountListSpinner.getSelectedItemPosition()),
                    object : RemoveAccountCallback {
                        override fun onRemoved() {
                            Toast.makeText(context, "Account removed.", Toast.LENGTH_SHORT)
                                .show()

                            /* Reload account asynchronously to get the up-to-date list. */loadAccounts()
                        }

                        override fun onError(exception: MsalException) {
                            displayError(exception)
                        }
                    })
            }
        })
        callAcquireTokenInteractiveButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (mCiamApp == null) {
                    return
                }
                displayTokenResult("")
                /**
                 * Acquire token interactively. It will also create an account object for the silent call as a result (to be obtained by getAccount()).
                 *
                 * If acquireTokenSilent() returns an error that requires an interaction,
                 * invoke acquireToken() to have the user resolve the interrupt interactively.
                 *
                 * Some example scenarios are
                 * - password change
                 * - the resource you're acquiring a token for has a stricter set of requirement than your SSO refresh token.
                 * - you're introducing a new scope which the user has never consented for.
                 */
                val parameters: AcquireTokenParameters = AcquireTokenParameters.Builder()
                    .startAuthorizationFromActivity(activity)
                    .withScopes(Arrays.asList<String>(*scopes))
                    .withCallback(authInteractiveCallback)
                    .build()
                mCiamApp!!.acquireToken(parameters)
            }
        })
        callAcquireTokenSilentButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (mCiamApp == null) {
                    return
                }
                displayTokenResult("")
                val selectedAccount: IAccount =
                    accountList!!.get(accountListSpinner.getSelectedItemPosition())

                /**
                 * Performs acquireToken without interrupting the user.
                 *
                 * This requires an account object of the account you're obtaining a token for.
                 * (can be obtained via getAccount()).
                 */
                val silentParameters: AcquireTokenSilentParameters =
                    AcquireTokenSilentParameters.Builder()
                        .forAccount(selectedAccount)
                        .fromAuthority(selectedAccount.authority)
                        .withScopes(Arrays.asList<String>(*scopes))
                        .forceRefresh(false)
                        .withCallback(authSilentCallback)
                        .build()
                mCiamApp!!.acquireTokenSilentAsync(silentParameters)
            }
        })
    }

    private val scopes: Array<String>
        /**
         * Extracts a scope array from a text field,
         * i.e. from "User.Read User.ReadWrite" to ["user.read", "user.readwrite"]
         */
        private get() = scopeTextView!!.text.toString().lowercase(Locale.getDefault())
            .split(" ".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()

    /**
     * Load currently signed-in accounts, if there's any.
     */
    private fun loadAccounts() {
        if (mCiamApp == null) {
            return
        }
        mCiamApp!!.getAccounts(object : LoadAccountsCallback {
            override fun onTaskCompleted(result: List<IAccount>) {
                // You can use the account data to update your UI or your app database.
                accountList = result
                updateUI(accountList!!)
            }

            override fun onError(exception: MsalException) {
                displayError(exception)
            }
        })
    }

    private val authSilentCallback: SilentAuthenticationCallback
        /**
         * Callback used in for silent acquireToken calls.
         */
        private get() {
            return object : SilentAuthenticationCallback {
                override fun onSuccess(authenticationResult: IAuthenticationResult) {
                    Log.d(TAG, "Successfully authenticated")

                    /* Display Access Token */displayTokenResult("Silent Request Success:\n" + authenticationResult.accessToken)
                }

                override fun onError(exception: MsalException) {
                    /* Failed to acquireToken */
                    Log.d(
                        TAG,
                        "Authentication failed: $exception"
                    )
                    displayError(exception)
                    if (exception is MsalClientException) {
                        /* Exception inside MSAL, more info inside MsalError.java */
                    } else if (exception is MsalServiceException) {
                        /* Exception when communicating with the STS, likely config issue */
                    } else if (exception is MsalUiRequiredException) {
                        /* Tokens expired or no session, retry with interactive */
                    }
                }
            }
        }
    private val authInteractiveCallback: AuthenticationCallback
        /**
         * Callback used for interactive request.
         * If succeeds, we display the access token
         * Does not check cache.
         */
        private get() {
            return object : AuthenticationCallback {
                override fun onSuccess(authenticationResult: IAuthenticationResult) {
                    /* Successfully got a token */
                    Log.d(TAG, "Successfully authenticated")
                    Log.d(
                        TAG, "ID Token: " + authenticationResult.account.claims!!
                            .get("id_token")
                    )

                    /* Display Access Token */displayTokenResult("Interactive Request Success:\n" + authenticationResult.accessToken)

                    /* Reload account asynchronously to get the up-to-date list. */loadAccounts()
                }

                override fun onError(exception: MsalException) {
                    /* Failed to acquireToken */
                    Log.d(
                        TAG,
                        "Authentication failed: $exception"
                    )
                    displayError(exception)
                    if (exception is MsalClientException) {
                        /* Exception inside MSAL, more info inside MsalError.java */
                    } else if (exception is MsalServiceException) {
                        /* Exception when communicating with the STS, likely config issue */
                    }
                }

                override fun onCancel() {
                    /* User canceled the authentication */
                    Log.d(TAG, "User cancelled login.")
                }
            }
        }
    //
    // Helper methods manage UI updates
    // ================================
    // displayError() - Display the error message
    // updateUI() - Updates UI based on account list
    //
    /**
     * Display the access token
     */
    private fun displayTokenResult(accessToken: String) {
        logTextView!!.text = accessToken
    }

    /**
     * Display the error message
     */
    private fun displayError(exception: Exception) {
        logTextView!!.text = exception.toString()
    }

    /**
     * Updates UI based on the obtained account list.
     */
    private fun updateUI(result: List<IAccount>) {
        if (result.size > 0) {
            removeAccountButton!!.isEnabled = true
            callAcquireTokenInteractiveButton!!.isEnabled = true
            callAcquireTokenSilentButton!!.isEnabled = true
        } else {
            removeAccountButton!!.isEnabled = false
            callAcquireTokenInteractiveButton!!.isEnabled = true
            callAcquireTokenSilentButton!!.isEnabled = false
        }
        val dataAdapter: ArrayAdapter<String> = ArrayAdapter(
            (context)!!, android.R.layout.simple_spinner_item,
            object : ArrayList<String?>() {
                init {
                    for (account: IAccount in result) add(account.username)
                }
            }
        )
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        accountListSpinner!!.adapter = dataAdapter
        dataAdapter.notifyDataSetChanged()
    }

    companion object {
        private val TAG: String = CIAMModeFragment::class.java.simpleName
    }
}