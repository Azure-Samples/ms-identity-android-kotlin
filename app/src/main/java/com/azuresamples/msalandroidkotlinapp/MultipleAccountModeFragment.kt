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

//import android.R
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.volley.Response
import com.android.volley.VolleyError
import com.azuresamples.msalandroidkotlinapp.MSGraphRequestWrapper.callGraphAPIUsingVolley
import com.microsoft.identity.client.*
//import com.microsoft.identity.client.IAccount.client.exception.MsalUiRequiredException
import com.microsoft.identity.client.IMultipleAccountPublicClientApplication.RemoveAccountCallback
import com.microsoft.identity.client.IPublicClientApplication.IMultipleAccountApplicationCreatedListener
import com.microsoft.identity.client.IPublicClientApplication.LoadAccountsCallback
import com.microsoft.identity.client.exception.MsalClientException
import com.microsoft.identity.client.exception.MsalException
import com.microsoft.identity.client.exception.MsalServiceException
import com.microsoft.identity.client.exception.MsalUiRequiredException
import org.json.JSONObject
import java.util.*

/**
 * Implementation sample for 'Multiple account' mode.
 */
class MultipleAccountModeFragment() : Fragment() {
    /* UI & Debugging Variables */
    lateinit var removeAccountButton: Button
    lateinit var  callGraphApiInteractiveButton: Button
    lateinit var  callGraphApiSilentButton: Button
    lateinit var  scopeTextView: TextView
    lateinit var  graphResourceTextView: TextView
    lateinit var  logTextView: TextView
    lateinit var  accountListSpinner: Spinner

    /* Azure AD Variables */
    private var mMultipleAccountApp: IMultipleAccountPublicClientApplication? = null
    private var accountList: List<IAccount>? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val view: View = inflater.inflate(R.layout.fragment_multiple_account_mode, container, false)
        initializeUI(view)

        // Creates a PublicClientApplication object with res/raw/auth_config_single_account.json
        PublicClientApplication.createMultipleAccountPublicClientApplication(
            requireContext(),
            R.raw.auth_config_multiple_account,
            object : IMultipleAccountApplicationCreatedListener {
                override fun onCreated(application: IMultipleAccountPublicClientApplication) {
                    mMultipleAccountApp = application
                    loadAccounts()
                }

                override fun onError(exception: MsalException) {
                    displayError(exception)
                    removeAccountButton.isEnabled = false
                    callGraphApiInteractiveButton.isEnabled = false
                    callGraphApiSilentButton.isEnabled = false
                }
            })
        return view
    }

    /**
     * Initializes UI variables and callbacks.
     */
    private fun initializeUI(view: View) {
        removeAccountButton = view.findViewById(R.id.btn_removeAccount)
        callGraphApiInteractiveButton = view.findViewById(R.id.btn_callGraphInteractively)
        callGraphApiSilentButton = view.findViewById(R.id.btn_callGraphSilently)
        scopeTextView = view.findViewById(R.id.scope)
        graphResourceTextView = view.findViewById(R.id.msgraph_url)
        logTextView = view.findViewById(R.id.txt_log)
        accountListSpinner = view.findViewById(R.id.account_list)
        val defaultGraphResourceUrl: String =
            MSGraphRequestWrapper.MS_GRAPH_ROOT_ENDPOINT + "v1.0/me"
        with(graphResourceTextView) { setText(defaultGraphResourceUrl) }
        removeAccountButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (mMultipleAccountApp == null) {
                    return
                }

                /*
                 * Removes the selected account and cached tokens from this app (or device, if the device is in shared mode).
                 */mMultipleAccountApp!!.removeAccount(
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
        callGraphApiInteractiveButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (mMultipleAccountApp == null) {
                    return
                }
                val parameters: AcquireTokenParameters = AcquireTokenParameters.Builder()
                    .startAuthorizationFromActivity(activity)
                    .withScopes(Arrays.asList(*scopes))
                    .withCallback(authInteractiveCallback)
                    .build()
                /*
                 * Acquire token interactively. It will also create an account object for the silent call as a result (to be obtained by getAccount()).
                 *
                 * If acquireTokenSilent() returns an error that requires an interaction,
                 * invoke acquireToken() to have the user resolve the interrupt interactively.
                 *
                 * Some example scenarios are
                 *  - password change
                 *  - the resource you're acquiring a token for has a stricter set of requirement than your SSO refresh token.
                 *  - you're introducing a new scope which the user has never consented for.
                 */mMultipleAccountApp!!.acquireToken(parameters)
            }
        })
        callGraphApiSilentButton.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View) {
                if (mMultipleAccountApp == null) {
                    return
                }
                val selectedAccount: IAccount =
                    accountList!!.get(accountListSpinner.getSelectedItemPosition())
                val silentParameters: AcquireTokenSilentParameters =
                    AcquireTokenSilentParameters.Builder()
                        .forAccount(selectedAccount)
                        .fromAuthority(selectedAccount.authority)
                        .withScopes(Arrays.asList(*scopes))
                        .forceRefresh(false)
                        .withCallback(authSilentCallback)
                        .build()

                /*
                 * Performs acquireToken without interrupting the user.
                 *
                 * This requires an account object of the account you're obtaining a token for.
                 * (can be obtained via getAccount()).
                 */mMultipleAccountApp!!.acquireTokenSilentAsync(silentParameters)
            }
        })
    }

    /**
     * Extracts a scope array from a text field,
     * i.e. from "User.Read User.ReadWrite" to ["user.read", "user.readwrite"]
     */
    private val scopes: Array<String>
        private get() = scopeTextView!!.text.toString().toLowerCase().split(" ").toTypedArray()

    /**
     * Load currently signed-in accounts, if there's any.
     */
    private fun loadAccounts() {
        if (mMultipleAccountApp == null) {
            return
        }
        mMultipleAccountApp!!.getAccounts(object : LoadAccountsCallback {
            override fun onTaskCompleted(result: List<IAccount>) {
                // You can use the account data to update your UI or your app database.
                accountList = result
                updateUI(accountList!!)
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

    /* call graph */

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
                    Log.d(
                        TAG, "ID Token: " + authenticationResult.account.claims!!
                            .get("id_token")
                    )

                    /* call graph */callGraphAPI(authenticationResult)

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

    /**
     * Make an HTTP request to obtain MSGraph data
     *
     * The sample is using the global service cloud as a default.
     * If you're developing an app for sovereign cloud users, please change the Microsoft Graph Resource URL accordingly.
     * https://docs.microsoft.com/en-us/graph/deployments#microsoft-graph-and-graph-explorer-service-root-endpoints
     */
    private fun callGraphAPI(authenticationResult: IAuthenticationResult) {
        callGraphAPIUsingVolley(
            (context)!!,
            graphResourceTextView!!.text.toString(),
            authenticationResult.accessToken,
            object : Response.Listener<JSONObject> {
                override fun onResponse(response: JSONObject) {
                    /* Successfully called graph, process data and send to UI */
                    Log.d(TAG, "Response: $response")
                    displayGraphResult(response)
                }
            },
            object : Response.ErrorListener {
                override fun onErrorResponse(error: VolleyError) {
                    Log.d(TAG, "Error: $error")
                    displayError(error)
                }
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
     * Updates UI based on the obtained account list.
     */
    private fun updateUI(result: List<IAccount>) {
        if (result.size > 0) {
            removeAccountButton!!.isEnabled = true
            callGraphApiInteractiveButton!!.isEnabled = true
            callGraphApiSilentButton!!.isEnabled = true
        } else {
            removeAccountButton!!.isEnabled = false
            callGraphApiInteractiveButton!!.isEnabled = true
            callGraphApiSilentButton!!.isEnabled = false
        }
        val dataAdapter: ArrayAdapter<String> = ArrayAdapter<String>(
            context!!, android.R.layout.simple_spinner_item,
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
        private val TAG: String = MultipleAccountModeFragment::class.java.simpleName
    }
}