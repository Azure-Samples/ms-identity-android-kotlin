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
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import com.azuresamples.msalandroidkotlinapp.B2CConfiguration.getAuthorityFromPolicyName
import com.microsoft.identity.client.*
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication.RemoveAccountCallback
import com.microsoft.identity.client.IPublicClientApplication.IMultipleAccountApplicationCreatedListener
import com.microsoft.identity.client.IPublicClientApplication.LoadAccountsCallback
import com.microsoft.identity.client.exception.MsalClientException
import com.microsoft.identity.client.exception.MsalException
import com.microsoft.identity.client.exception.MsalServiceException
import com.microsoft.identity.client.exception.MsalUiRequiredException
import kotlinx.android.synthetic.main.fragment_b2c_mode.*
import java.util.*

/**
 * Implementation sample for 'B2C' mode.
 */
class B2CModeFragment : Fragment() {
    /* UI & Debugging Variables */
    /*var removeAccountButton: Button? = null
    var runUserFlowButton: Button? = null
    var acquireTokenSilentButton: Button? = null
    var graphResourceTextView: TextView? = null
    var logTextView: TextView? = null
    var policyListSpinner: Spinner? = null
    var b2cUserList: Spinner? = null*/
    private var users: List<B2CUser>? = null

    /* Azure AD Variables */
    private var b2cApp: IMultipleAccountPublicClientApplication? = null


    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_b2c_mode, container, false)


        // Creates a PublicClientApplication object with res/raw/auth_config_single_account.json
        PublicClientApplication.createMultipleAccountPublicClientApplication(context!!,
                R.raw.auth_config_b2c,
                object : IMultipleAccountApplicationCreatedListener {
                    override fun onCreated(application: IMultipleAccountPublicClientApplication) {
                        b2cApp = application
                        loadAccounts()
                    }

                    override fun onError(exception: MsalException) {
                        displayError(exception)


                        btn_removeAccount!!.isEnabled = false
                        btn_runUserFlow!!.isEnabled = false
                        btn_acquireTokenSilently!!.isEnabled = false
                    }
                })
        return view
    }

    /**
     * Initializes UI variables and callbacks.
     */
    private fun initializeUI() {

        val dataAdapter = ArrayAdapter<String>(
                context, android.R.layout.simple_spinner_item,
                object : ArrayList<String?>() {
                    init {
                        for (policyName in B2CConfiguration.Policies) add(policyName)
                    }
                }
        )
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        policy_list.setAdapter(dataAdapter)
        dataAdapter.notifyDataSetChanged()
        btn_runUserFlow.setOnClickListener(View.OnClickListener {
            if (b2cApp == null) {
                return@OnClickListener
            }
            /**
             * Runs user flow interactively.
             *
             *
             * Once the user finishes with the flow, you will also receive an access token containing the claims for the scope you passed in (see B2CConfiguration.getScopes()),
             * which you can subsequently use to obtain your resources.
             */
            /**
             * Runs user flow interactively.
             *
             *
             * Once the user finishes with the flow, you will also receive an access token containing the claims for the scope you passed in (see B2CConfiguration.getScopes()),
             * which you can subsequently use to obtain your resources.
             */
            val parameters = AcquireTokenParameters.Builder()
                    .startAuthorizationFromActivity(activity)
                    .fromAuthority(getAuthorityFromPolicyName(policy_list.getSelectedItem().toString()))
                    .withScopes(B2CConfiguration.scopes)
                    .withPrompt(Prompt.LOGIN)
                    .withCallback(authInteractiveCallback)
                    .build()

            b2cApp!!.acquireToken(parameters)
        })

        btn_acquireTokenSilently.setOnClickListener(View.OnClickListener {
            if (b2cApp == null) {
                return@OnClickListener
            }
            val selectedUser = users!![user_list.getSelectedItemPosition()]
            selectedUser.acquireTokenSilentAsync(b2cApp!!,
                    policy_list.getSelectedItem().toString(),
                    B2CConfiguration.scopes,
                    authSilentCallback)
        })

        btn_removeAccount.setOnClickListener(View.OnClickListener {
            if (b2cApp == null) {
                return@OnClickListener
            }
            val selectedUser = users!![user_list.getSelectedItemPosition()]
            selectedUser.signOutAsync(b2cApp!!,
                    object : RemoveAccountCallback {
                        override fun onRemoved() {
                            txt_log.setText("Signed Out.")
                            loadAccounts()
                        }

                        override fun onError(exception: MsalException) {
                            displayError(exception)
                        }
                    })
        })
    }

    override fun onResume() {
        super.onResume()

        initializeUI()

    }

    /**
     * Load signed-in accounts, if there's any.
     */
    private fun loadAccounts() {
        if (b2cApp == null) {
            return
        }
        b2cApp!!.getAccounts(object : LoadAccountsCallback {
            override fun onTaskCompleted(result: List<IAccount>) {
                users = B2CUser.getB2CUsersFromAccountList(result)
                updateUI(users)
            }

            override fun onError(exception: MsalException) {
                displayError(exception)
            }
        })
    }/* Tokens expired or no session, retry with interactive *//* Exception when communicating with the STS, likely config issue *//* Exception inside MSAL, more info inside MsalError.java *//* Failed to acquireToken *//* Successfully got a token. */

    /**
     * Callback used in for silent acquireToken calls.
     */
    private val authSilentCallback: SilentAuthenticationCallback
        private get() = object : SilentAuthenticationCallback {
            override fun onSuccess(authenticationResult: IAuthenticationResult) {
                Log.d(TAG, "Successfully authenticated")

                /* Successfully got a token. */displayResult(authenticationResult)
            }

            override fun onError(exception: MsalException) {
                /* Failed to acquireToken */
                Log.d(TAG, "Authentication failed: $exception")
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

    /* display result info */

    /* Reload account asynchronously to get the up-to-date list. */
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

                /* display result info */displayResult(authenticationResult)

                /* Reload account asynchronously to get the up-to-date list. */loadAccounts()
            }

            override fun onError(exception: MsalException) {
                val B2C_PASSWORD_CHANGE = "AADB2C90118"
                if (exception.message!!.contains(B2C_PASSWORD_CHANGE)) {
                    txt_log!!.text = """
                        The user clicks the 'Forgot Password' link in a sign-up or sign-in user flow.
                        Your application needs to handle this error code by running a specific user flow that resets the password.
                        """.trimIndent()
                    return
                }

                /* Failed to acquireToken */Log.d(TAG, "Authentication failed: $exception")
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
    //
    // Helper methods manage UI updates
    // ================================
    // displayResult() - Display the authentication result.
    // displayError() - Display the token error.
    // updateSignedInUI() - Updates UI when the user is signed in
    // updateSignedOutUI() - Updates UI when app sign out succeeds
    //
    /**
     * Display the graph response
     */
    private fun displayResult(result: IAuthenticationResult) {
        val output = """
         Access Token :${result.accessToken}
         Scope : ${result.scope}
         Expiry : ${result.expiresOn}
         Tenant ID : ${result.tenantId}
         
         """.trimIndent()
        Log.d(TAG, output)
        txt_log!!.text = output
    }

    /**
     * Display the error message
     */
    private fun displayError(exception: Exception) {
        txt_log!!.text = exception.toString()
    }

    /**
     * Updates UI based on the obtained user list.
     */
    private fun updateUI(users: List<B2CUser>?) {
        if (users!!.size != 0) {
            btn_removeAccount!!.isEnabled = true
            btn_acquireTokenSilently!!.isEnabled = true
        } else {
            btn_removeAccount!!.isEnabled = false
            btn_acquireTokenSilently!!.isEnabled = false
        }
        val dataAdapter = ArrayAdapter<String>(
                context, android.R.layout.simple_spinner_item,
                object : ArrayList<String?>() {
                    init {
                        for (user in users) add(user.displayName)
                    }
                }
        )
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        user_list!!.adapter = dataAdapter
        dataAdapter.notifyDataSetChanged()
    }

    companion object {
        private val TAG = B2CModeFragment::class.java.simpleName
    }
}