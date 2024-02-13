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
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.azuresamples.msalandroidkotlinapp.B2CConfiguration.getAuthorityFromPolicyName
import com.azuresamples.msalandroidkotlinapp.B2CConfiguration.scopes
import com.microsoft.identity.client.*
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication.RemoveAccountCallback
import com.microsoft.identity.client.IPublicClientApplication.IMultipleAccountApplicationCreatedListener
import com.microsoft.identity.client.IPublicClientApplication.LoadAccountsCallback
import com.microsoft.identity.client.exception.MsalClientException
import com.microsoft.identity.client.exception.MsalException
import com.microsoft.identity.client.exception.MsalServiceException
import com.microsoft.identity.client.exception.MsalUiRequiredException

/**
 * Implementation sample for 'B2C' mode.
 */
class B2CModeFragment() : Fragment() {
    /* UI & Debugging Variables */
    lateinit var removeAccountButton: Button
    lateinit var runUserFlowButton: Button
    lateinit var acquireTokenSilentButton: Button
    lateinit var logTextView: TextView
    lateinit var policyListSpinner: Spinner
    lateinit var b2cUserList: Spinner
    private var users: List<B2CUser>? = null

    /* Azure AD Variables */
    private lateinit var b2cApp: IMultipleAccountPublicClientApplication
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_b2c_mode, container, false)
        initializeUI(view)

        // Creates a PublicClientApplication object with res/raw/auth_config_single_account.json
        context?.let {
            PublicClientApplication.createMultipleAccountPublicClientApplication(
                it,
                R.raw.auth_config_b2c,
                object : IMultipleAccountApplicationCreatedListener {
                    override fun onCreated(application: IMultipleAccountPublicClientApplication) {
                        b2cApp = application
                        loadAccounts()
                    }

                    override fun onError(exception: MsalException) {
                        displayError(exception)
                        removeAccountButton!!.isEnabled = false
                        runUserFlowButton!!.isEnabled = false
                        acquireTokenSilentButton!!.isEnabled = false
                    }
                })
        }
        return view
    }

    /**
     * Initializes UI variables and callbacks.
     */
    private fun initializeUI(view: View) {
        removeAccountButton = view.findViewById(R.id.btn_removeAccount)
        runUserFlowButton = view.findViewById(R.id.btn_runUserFlow)
        acquireTokenSilentButton = view.findViewById(R.id.btn_acquireTokenSilently)
        logTextView = view.findViewById(R.id.txt_log)
        policyListSpinner = view.findViewById(R.id.policy_list)
        b2cUserList = view.findViewById(R.id.user_list)
        val dataAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            (context)!!, android.R.layout.simple_spinner_item,
            object : ArrayList<String?>() {
                init {
                    for (policyName: String in B2CConfiguration.Policies) add(policyName)
                }
            }
        )
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        policyListSpinner.setAdapter(dataAdapter)
        dataAdapter.notifyDataSetChanged()
        runUserFlowButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (b2cApp == null) {
                    return
                }
                /**
                 * Runs user flow interactively.
                 *
                 *
                 * Once the user finishes with the flow, you will also receive an access token containing the claims for the scope you passed in (see B2CConfiguration.getScopes()),
                 * which you can subsequently use to obtain your resources.
                 */
                val parameters: AcquireTokenParameters = AcquireTokenParameters.Builder()
                    .startAuthorizationFromActivity(activity)
                    .fromAuthority(
                        getAuthorityFromPolicyName(
                            policyListSpinner.getSelectedItem().toString()
                        )
                    )
                    .withScopes(scopes)
                    .withPrompt(Prompt.LOGIN)
                    .withCallback(authInteractiveCallback)
                    .build()
                b2cApp!!.acquireToken(parameters)
            }
        })
        acquireTokenSilentButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (b2cApp == null) {
                    return
                }
                val selectedUser: B2CUser = users!!.get(b2cUserList.getSelectedItemPosition())
                selectedUser.acquireTokenSilentAsync(
                    b2cApp,
                    policyListSpinner.getSelectedItem().toString(),
                    scopes,
                    authSilentCallback
                )
            }
        })
        removeAccountButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (b2cApp == null) {
                    return
                }
                val selectedUser: B2CUser = users!!.get(b2cUserList.getSelectedItemPosition())
                selectedUser.signOutAsync(b2cApp,
                    object : RemoveAccountCallback {
                        override fun onRemoved() {
                            logTextView.setText("Signed Out.")
                            loadAccounts()
                        }

                        override fun onError(exception: MsalException) {
                            displayError(exception)
                        }
                    })
            }
        })
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

    /* display result info */

    /* Reload account asynchronously to get the up-to-date list. */
    /**
     * Callback used for interactive request.
     * If succeeds we use the access token to call the Microsoft Graph.
     * Does not check cache.
     */
    private val authInteractiveCallback: AuthenticationCallback
        private get() {
            return object : AuthenticationCallback {
                override fun onSuccess(authenticationResult: IAuthenticationResult) {
                    /* Successfully got a token, use it to call a protected resource - MSGraph */
                    Log.d(TAG, "Successfully authenticated")

                    /* display result info */displayResult(authenticationResult)

                    /* Reload account asynchronously to get the up-to-date list. */loadAccounts()
                }

                override fun onError(exception: MsalException) {
                    val B2C_PASSWORD_CHANGE: String = "AADB2C90118"
                    if (exception.message!!.contains(B2C_PASSWORD_CHANGE)) {
                        logTextView!!.text =
                            "The user clicks the 'Forgot Password' link in a sign-up or sign-in user flow.\n" +
                                    "Your application needs to handle this error code by running a specific user flow that resets the password."
                        return
                    }

                    /* Failed to acquireToken */Log.d(
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
    // displayResult() - Display the authentication result.
    // displayError() - Display the token error.
    // updateSignedInUI() - Updates UI when the user is signed in
    // updateSignedOutUI() - Updates UI when app sign out succeeds
    //
    /**
     * Display the graph response
     */
    private fun displayResult(result: IAuthenticationResult) {
        val output: String = ("Access Token :" + result.accessToken + "\n" +
                "Scope : " + result.scope + "\n" +
                "Expiry : " + result.expiresOn + "\n" +
                "Tenant ID : " + result.tenantId + "\n")
        logTextView!!.text = output
    }

    /**
     * Display the error message
     */
    private fun displayError(exception: Exception) {
        logTextView!!.text = exception.toString()
    }

    /**
     * Updates UI based on the obtained user list.
     */
    private fun updateUI(users: List<B2CUser>?) {
        if (users!!.size != 0) {
            removeAccountButton!!.isEnabled = true
            acquireTokenSilentButton!!.isEnabled = true
        } else {
            removeAccountButton!!.isEnabled = false
            acquireTokenSilentButton!!.isEnabled = false
        }
        val dataAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            (context)!!, android.R.layout.simple_spinner_item,
            object : ArrayList<String?>() {
                init {
                    for (user: B2CUser in users) add(user.displayName)
                }
            }
        )
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        b2cUserList!!.adapter = dataAdapter
        dataAdapter.notifyDataSetChanged()
    }

    companion object {
        private val TAG: String = B2CModeFragment::class.java.simpleName
    }
}