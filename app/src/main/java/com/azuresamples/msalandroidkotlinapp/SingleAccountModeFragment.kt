// Copyright (c) Microsoft Corporation.
// All rights reserved.
//
// This code is licensed under the MIT License.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy
// of this software and associated documentation files(the "Software"), to deal
// in the Software without restriction, including without limitation the rights
// to use, copy, modify, merge, publish, distribute, sublicense, and / or sell
// copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions :
//
// The above copyright notice and this permission notice shall be included in
// all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
// THE SOFTWARE.
package com.azuresamples.msalandroidkotlinapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.azuresamples.msalandroidkotlinapp.MSGraphRequestWrapper.callGraphAPIUsingVolley
import com.microsoft.identity.client.*
import com.microsoft.identity.client.IPublicClientApplication.ISingleAccountApplicationCreatedListener
import com.microsoft.identity.client.ISingleAccountPublicClientApplication.CurrentAccountCallback
import com.microsoft.identity.client.ISingleAccountPublicClientApplication.SignOutCallback
import com.microsoft.identity.client.exception.MsalClientException
import com.microsoft.identity.client.exception.MsalException
import com.microsoft.identity.client.exception.MsalServiceException
import com.microsoft.identity.client.exception.MsalUiRequiredException
import org.json.JSONObject
import java.util.*

/**
 * Implementation sample for 'Single account' mode.
 *
 *
 * If your app only supports one account being signed-in at a time, this is for you.
 * This requires "account_mode" to be set as "SINGLE" in the configuration file.
 * (Please see res/raw/auth_config_single_account.json for more info).
 *
 *
 * Please note that switching mode (between 'single' and 'multiple' might cause a loss of data.
 */
class SingleAccountModeFragment : Fragment() {
    /* UI & Debugging Variables */
    lateinit var signInButton: Button
    lateinit var signOutButton: Button
    lateinit var callGraphApiInteractiveButton: Button
    lateinit var callGraphApiSilentButton: Button
    lateinit var scopeTextView: TextView
    lateinit var graphResourceTextView: TextView
    lateinit var logTextView: TextView
    lateinit var currentUserTextView: TextView
    lateinit var deviceModeTextView: TextView

    /* Azure AD Variables */
    private var mSingleAccountApp: ISingleAccountPublicClientApplication? = null
    private var mAccount: IAccount? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_single_account_mode, container, false)
        initializeUI(view)

        // Creates a PublicClientApplication object with res/raw/auth_config_single_account.json
        PublicClientApplication.createSingleAccountPublicClientApplication(
            requireContext(),
            R.raw.auth_config_single_account,
            object : ISingleAccountApplicationCreatedListener {
                override fun onCreated(application: ISingleAccountPublicClientApplication) {
                    /*
                         * This test app assumes that the app is only going to support one account.
                         * This requires "account_mode" : "SINGLE" in the config json file.
                         */
                    mSingleAccountApp = application
                    loadAccount()
                }

                override fun onError(exception: MsalException) {
                    displayError(exception)
                }
            })
        return view
    }

    /**
     * Initializes UI variables and callbacks.
     */
    private fun initializeUI(view: View) {
        signInButton = view.findViewById(R.id.btn_signIn)
        signOutButton = view.findViewById(R.id.btn_removeAccount)
        callGraphApiInteractiveButton = view.findViewById(R.id.btn_callGraphInteractively)
        callGraphApiSilentButton = view.findViewById(R.id.btn_callGraphSilently)
        scopeTextView = view.findViewById(R.id.scope)
        graphResourceTextView = view.findViewById(R.id.msgraph_url)
        logTextView = view.findViewById(R.id.txt_log)
        currentUserTextView = view.findViewById(R.id.current_user)
        deviceModeTextView = view.findViewById(R.id.device_mode)
        val defaultGraphResourceUrl = MSGraphRequestWrapper.MS_GRAPH_ROOT_ENDPOINT + "v1.0/me"
        graphResourceTextView.setText(defaultGraphResourceUrl)
        signInButton.setOnClickListener(View.OnClickListener {
            if (mSingleAccountApp == null) {
                return@OnClickListener
            }
            val signInParameters: SignInParameters = SignInParameters.builder()
                .withActivity(requireActivity())
                .withLoginHint(null)
                .withScopes(Arrays.asList(*scopes))
                .withCallback(authInteractiveCallback)
                .build()
            mSingleAccountApp!!.signIn(signInParameters)
        })
        signOutButton.setOnClickListener(View.OnClickListener {
            if (mSingleAccountApp == null) {
                return@OnClickListener
            }

            /*
                     * Removes the signed-in account and cached tokens from this app (or device, if the device is in shared mode).
                     */mSingleAccountApp!!.signOut(object : SignOutCallback {
            override fun onSignOut() {
                mAccount = null
                updateUI()
                showToastOnSignOut()
            }

            override fun onError(exception: MsalException) {
                displayError(exception)
            }
        })
        })
        callGraphApiInteractiveButton.setOnClickListener(View.OnClickListener {
            if (mSingleAccountApp == null) {
                return@OnClickListener
            }
            val parameters = AcquireTokenParameters.Builder()
                .startAuthorizationFromActivity(activity)
                .withScopes(Arrays.asList(*scopes))
                .withCallback(authInteractiveCallback)
                .forAccount(mAccount)
                .build()
            /*
                     * If acquireTokenSilent() returns an error that requires an interaction (MsalUiRequiredException),
                     * invoke acquireToken() to have the user resolve the interrupt interactively.
                     *
                     * Some example scenarios are
                     *  - password change
                     *  - the resource you're acquiring a token for has a stricter set of requirement than your Single Sign-On refresh token.
                     *  - you're introducing a new scope which the user has never consented for.
                     */mSingleAccountApp!!.acquireToken(parameters)
        })
        callGraphApiSilentButton.setOnClickListener(View.OnClickListener {
            if (mSingleAccountApp == null) {
                return@OnClickListener
            }
            val silentParameters = AcquireTokenSilentParameters.Builder()
                .fromAuthority(mAccount!!.authority)
                .forAccount(mAccount)
                .withScopes(Arrays.asList(*scopes))
                .withCallback(authSilentCallback)
                .build()
            /*
                     * Once you've signed the user in,
                     * you can perform acquireTokenSilent to obtain resources without interrupting the user.
                     */mSingleAccountApp!!.acquireTokenSilentAsync(silentParameters)
        })
    }

    override fun onResume() {
        super.onResume()

        /*
         * The account may have been removed from the device (if broker is in use).
         *
         * In shared device mode, the account might be signed in/out by other apps while this app is not in focus.
         * Therefore, we want to update the account state by invoking loadAccount() here.
         */loadAccount()
    }

    /**
     * Extracts a scope array from a text field,
     * i.e. from "User.Read User.ReadWrite" to ["user.read", "user.readwrite"]
     */
    private val scopes: Array<String>
        private get() = scopeTextView!!.text.toString().toLowerCase().split(" ").toTypedArray()

    /**
     * Load the currently signed-in account, if there's any.
     */
    private fun loadAccount() {
        if (mSingleAccountApp == null) {
            return
        }
        mSingleAccountApp!!.getCurrentAccountAsync(object : CurrentAccountCallback {
            override fun onAccountLoaded(activeAccount: IAccount?) {
                // You can use the account data to update your UI or your app database.
                mAccount = activeAccount
                updateUI()
            }

            override fun onAccountChanged(priorAccount: IAccount?, currentAccount: IAccount?) {
                if (currentAccount == null) {
                    // Perform a cleanup task as the signed-in account changed.
                    showToastOnSignOut()
                }
            }

            override fun onError(exception: MsalException) {
                displayError(exception)
            }
        })
    }/* Tokens expired or no session, retry with interactive *//* Exception when communicating with the STS, likely config issue *//* Exception inside MSAL, more info inside MsalError.java *//* Failed to acquireToken *//* Successfully got a token, use it to call a protected resource - MSGraph */

    /**
     * Callback used in for silent acquireToken calls.
     */
    private val authSilentCallback: SilentAuthenticationCallback
        private get() = object : SilentAuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                Log.d(TAG, "Successfully authenticated")

                /* Successfully got a token, use it to call a protected resource - MSGraph */callGraphAPI(
                    authenticationResult
                )
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
        }/* User canceled the authentication *//* Exception when communicating with the STS, likely config issue *//* Exception inside MSAL, more info inside MsalError.java *//* Failed to acquireToken *//* Successfully got a token, use it to call a protected resource - MSGraph */

    /* Update account */

    /* call graph */
    /**
     * Callback used for interactive request.
     * If succeeds we use the access token to call the Microsoft Graph.
     * Does not check cache.
     */
    private val authInteractiveCallback: AuthenticationCallback
        private get() = object : AuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                /* Successfully got a token, use it to call a protected resource - MSGraph */
                Log.d(TAG, "Successfully authenticated")
                Log.d(TAG, "ID Token: " + authenticationResult.account.claims!!["id_token"])

                /* Update account */mAccount = authenticationResult.account
                updateUI()

                /* call graph */callGraphAPI(authenticationResult)
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

    /**
     * Make an HTTP request to obtain MSGraph data
     */
    private fun callGraphAPI(authenticationResult: IAuthenticationResult) {
        callGraphAPIUsingVolley(
            context!!,
            graphResourceTextView!!.text.toString(),
            authenticationResult.accessToken,
            Response.Listener<JSONObject> { response -> /* Successfully called graph, process data and send to UI */
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
        logTextView!!.text = graphResponse.toString()
    }

    /**
     * Display the error message
     */
    private fun displayError(exception: Exception) {
        logTextView!!.text = exception.toString()
    }

    /**
     * Updates UI based on the current account.
     */
    private fun updateUI() {
        if (mAccount != null) {
            signInButton!!.isEnabled = false
            signOutButton!!.isEnabled = true
            callGraphApiInteractiveButton!!.isEnabled = true
            callGraphApiSilentButton!!.isEnabled = true
            currentUserTextView!!.text = mAccount!!.username
        } else {
            signInButton!!.isEnabled = true
            signOutButton!!.isEnabled = false
            callGraphApiInteractiveButton!!.isEnabled = false
            callGraphApiSilentButton!!.isEnabled = false
            currentUserTextView!!.text = "None"
        }
        deviceModeTextView!!.text =
            if (mSingleAccountApp!!.isSharedDevice) "Shared" else "Non-shared"
    }

    /**
     * Updates UI when app sign out succeeds
     */
    private fun showToastOnSignOut() {
        val signOutText = "Signed Out."
        currentUserTextView!!.text = ""
        Toast.makeText(context, signOutText, Toast.LENGTH_SHORT)
            .show()
    }

    companion object {
        private val TAG = SingleAccountModeFragment::class.java.simpleName
    }
}