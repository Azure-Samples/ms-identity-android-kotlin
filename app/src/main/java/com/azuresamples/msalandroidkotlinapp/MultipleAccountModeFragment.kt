package com.azuresamples.msalandroidkotlinapp

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.microsoft.identity.client.*
import com.microsoft.identity.client.exception.MsalClientException
import com.microsoft.identity.client.exception.MsalException
import com.microsoft.identity.client.exception.MsalServiceException
import com.microsoft.identity.client.exception.MsalUiRequiredException
import kotlinx.android.synthetic.main.fragment_multiple_account_mode.*
import org.json.JSONObject
import java.util.*

class MultipleAccountModeFragment : Fragment() {
    private val TAG = MultipleAccountModeFragment::class.java.simpleName

    /* Azure AD v2 Configs */
    private val AUTHORITY = "https://login.microsoftonline.com/common"

    /* Azure AD Variables */
    private var mMultipleAccountApp: IMultipleAccountPublicClientApplication? = null

    private var accountList: List<IAccount>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_multiple_account_mode, container, false)

        // Creates a PublicClientApplication object with res/raw/auth_config_single_account.json
        PublicClientApplication.createMultipleAccountPublicClientApplication(
            context as Context,
            R.raw.auth_config_multiple_account,
            object : IPublicClientApplication.IMultipleAccountApplicationCreatedListener {
                override fun onCreated(application: IMultipleAccountPublicClientApplication) {
                    mMultipleAccountApp = application
                    loadAccount()
                }

                override fun onError(error: MsalException){
                    txt_log.text = "Please switch to 'Single Account' mode."
                    btn_removeAccount.isEnabled = false
                    btn_callGraphInteractively.isEnabled = false
                    btn_callGraphSilently.isEnabled = false
                }
            })

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    /**
     * Initializes UI variables and callbacks.
     */
    private fun initializeUI() {
       btn_removeAccount.setOnClickListener(View.OnClickListener {
            if (mMultipleAccountApp == null) {
                return@OnClickListener
            }

            /**
             * Removes the selected account and cached tokens from this app.
             */
            mMultipleAccountApp!!.removeAccount(
                accountList!![account_list.selectedItemPosition],
                object : IMultipleAccountPublicClientApplication.RemoveAccountCallback {
                    override fun onRemoved() {
                        Toast.makeText(context, "Account removed.", Toast.LENGTH_SHORT)
                            .show()

                        /* Reload account asynchronously to get the up-to-date list. */
                        loadAccount()
                    }

                    override fun onError(exception: MsalException) {
                        displayError(exception)
                    }
                })
        })

        btn_callGraphInteractively.setOnClickListener(View.OnClickListener {
            if (mMultipleAccountApp == null) {
                return@OnClickListener
            }

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
            mMultipleAccountApp!!.acquireToken(activity as Activity, getScopes(), getAuthInteractiveCallback())
        })

        btn_callGraphSilently.setOnClickListener(View.OnClickListener {
            if (mMultipleAccountApp == null) {
                return@OnClickListener
            }

            /**
             * Performs acquireToken without interrupting the user.
             *
             * This requires an account object of the account you're obtaining a token for.
             * (can be obtained via getAccount()).
             */

            /**
             * Performs acquireToken without interrupting the user.
             *
             * This requires an account object of the account you're obtaining a token for.
             * (can be obtained via getAccount()).
             */
            mMultipleAccountApp!!.acquireTokenSilentAsync(
                getScopes(),
                accountList!![account_list.selectedItemPosition],
                AUTHORITY,
                getAuthSilentCallback()
            )
        })

    }

    /**
     * Extracts a scope array from a text field,
     * i.e. from "User.Read User.ReadWrite" to ["user.read", "user.readwrite"]
     */
    private fun getScopes(): Array<String> {
        return scope.text.toString().toLowerCase().split(" ".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()
    }

    /**
     * Load the currently signed-in account, if there's any.
     * If the account is removed from the device, the app can also perform the clean-up work in onAccountChanged().
     */
    private fun loadAccount() {
        if (mMultipleAccountApp == null) {
            return
        }

        mMultipleAccountApp!!.getAccounts(object : IPublicClientApplication.LoadAccountsCallback {
            override fun onTaskCompleted(result: List<IAccount>) {
                accountList = result
                updateUI(accountList!!)
            }

            override fun onError(exception: MsalException) {
                txt_log.setText(exception.toString())
            }
        })
    }

    /**
     * Callback used in for silent acquireToken calls.
     * Looks if tokens are in the cache (refreshes if necessary and if we don't forceRefresh)
     * else errors that we need to do an interactive request.
     */
    private fun getAuthSilentCallback(): AuthenticationCallback {
        return object : AuthenticationCallback {

            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                Log.d(TAG, "Successfully authenticated")

                /* Successfully got a token, use it to call a protected resource - MSGraph */
                callGraphAPI(authenticationResult)
            }

            override fun onError(exception: MsalException) {
                /* Failed to acquireToken */
                Log.d(TAG, "Authentication failed: " + exception.toString())
                displayError(exception)

                if (exception is MsalClientException) {
                    /* Exception inside MSAL, more info inside MsalError.java */
                } else if (exception is MsalServiceException) {
                    /* Exception when communicating with the STS, likely config issue */
                } else if (exception is MsalUiRequiredException) {
                    /* Tokens expired or no session, retry with interactive */
                }
            }

            override fun onCancel() {
                /* User cancelled the authentication */
                Log.d(TAG, "User cancelled login.")
            }
        }
    }

    override fun onResume() {
        super.onResume()

        initializeUI()

    }

    /**
     * Callback used for interactive request.
     * If succeeds we use the access token to call the Microsoft Graph.
     * Does not check cache.
     */
    private fun getAuthInteractiveCallback(): AuthenticationCallback {
        return object : AuthenticationCallback {

            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                /* Successfully got a token, use it to call a protected resource - MSGraph */
                Log.d(TAG, "Successfully authenticated")
                Log.d(TAG, "ID Token: " + authenticationResult.getAccount().getClaims()?.get("id_token"))

                /* call graph */
                callGraphAPI(authenticationResult)

                /* Reload account asynchronously to get the up-to-date list. */
                loadAccount()
            }

            override fun onError(exception: MsalException) {
                /* Failed to acquireToken */
                Log.d(TAG, "Authentication failed: " + exception.toString())
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

    /**
     * Make an HTTP request to obtain MSGraph data
     */
    private fun callGraphAPI(authenticationResult: IAuthenticationResult) {
        MSGraphRequestWrapper.callGraphAPIWithVolley(
            context as Context,
            msgraph_url.text.toString(),
            authenticationResult.getAccessToken(),
            Response.Listener<JSONObject> { response ->
                /* Successfully called graph, process data and send to UI */
                Log.d(TAG, "Response: $response")
                displayGraphResult(response)
            },
            Response.ErrorListener { error ->
                Log.d(TAG, "Error: $error")
                displayError(error)
            })
    }

    //
    // Helper methods manage UI updates
    // ================================
    // displayGraphResult() - Display the graph response
    // displayError() - Display the graph response
    // updateSignedInUI() - Updates UI when the user is signed in
    // updateSignedOutUI() - Updates UI when app sign out succeeds
    //

    /**
     * Display the graph response
     */
    private fun displayGraphResult(graphResponse: JSONObject) {
        txt_log.text = graphResponse.toString()
    }

    /**
     * Display the error message
     */
    private fun displayError(exception: Exception) {
        txt_log.text = exception.toString()
    }

    /**
     * Updates UI based on the obtained account list.
     */
    private fun updateUI(result: List<IAccount>) {

        if (result.size > 0) {
            btn_removeAccount.isEnabled = true
            btn_callGraphInteractively.isEnabled = true
            btn_callGraphSilently.isEnabled = true
        } else {
            btn_removeAccount.isEnabled = false
            btn_callGraphInteractively.isEnabled = true
            btn_callGraphSilently.isEnabled = false
        }

        val dataAdapter = ArrayAdapter(
            context!!, android.R.layout.simple_spinner_item,
            object : ArrayList<String>() {
                init {
                    for (account in result)
                        add(account.getUsername())
                }
            }
        )

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        account_list.adapter = dataAdapter
        dataAdapter.notifyDataSetChanged()
    }
}