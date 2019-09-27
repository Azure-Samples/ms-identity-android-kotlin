---
languages:
- kotlin
page_type: sample
description: "Integrate Microsoft Identity Platform authentication in your Android application."
products:
- azure
- azure-active-directory
- office-ms-graph
urlFragment:
---

# Use MSAL in an Android app to sign-in users and call Microsoft Graph

| [Getting Started](https://docs.microsoft.com/azure/active-directory/develop/guidedsetups/active-directory-android)| [Library](https://github.com/AzureAD/microsoft-authentication-library-for-android) | [API Reference](http://javadoc.io/doc/com.microsoft.identity.client/msal) | [Support](README.md#community-help-and-support)
| --- | --- | --- | --- |

## About the Sample

The MSAL Android library gives your app the ability to begin using the [Microsoft identity platform](https://aka.ms/aaddev) by supporting [Azure Active Directory](https://azure.microsoft.com/services/active-directory/) and [Microsoft Accounts](https://account.microsoft.com) in a converged experience using industry standard OAuth2 and OpenID Connect protocols.

This sample walks you through the process of integrating authentication with Microsoft Identity Platform (formerly Azure Active Directory for developers) in your android application. In this sample we'll walk you through the code you need to write in the various lifecycle events of your app to achieve the following objectives.

* Sign-in a user
* Device-wide SSO and Conditional Access support through the Auth Broker
* Select between Single Account Mode and Multiple Account Mode
* Get a token for the [Microsoft Graph](https://graph.microsoft.com)
* Call the [Microsoft Graph](https://graph.microsoft.com)
* Sign out the user

## Scenario

This sample app is a [multi-tenant](https://docs.microsoft.com/en-us/azure/active-directory/develop/setup-multi-tenant-app) app, which means that it can sign-in users from any Azure AD tenant and Microsoft Accounts.  It also demonstrates how a developer can build apps to connect with enterprise users and access their Azure + O365 data via [Microsoft Graph](https://docs.microsoft.com/en-us/graph/overview).
During the auth flow, the users will be required to sign-in first, if it's their first time signing-in to the app, the user would be asked to consent to the [permissions](https://docs.microsoft.com/en-us/azure/active-directory/develop/v1-permissions-and-consent) required by the application.

The majority of the logic in this sample shows how to sign-in an end user and make a call to the Microsoft Graph to get basic information about the signed-in user.

![Flowchart](ReadmeFiles/image1.png)

## Broker Authentication using MSAL

Microsoft provides applications for every mobile platform that allow for the bridging of credentials across applications from different vendors and for enhanced features that require a single secure place from where to validate credentials. These are called brokers. The brokers available for Android are **Microsoft Authenticator** and **Company Portal**.
[Learn more about Brokers here.](https://docs.microsoft.com/azure/active-directory/develop/brokered-auth)

The MSAL for Android will automatically use the broker if they are present on the device.

> If you have older versions of **Microsoft Authenticator** app or [Company portal app](https://docs.microsoft.com/en-us/intune-user-help/install-and-sign-in-to-the-intune-company-portal-app-ios) installed in the device where this sample application will be run, then the user might not be able to test the scenarios presented here. Please make sure that you have installed the latest version of Microsoft Authenticator or Company Portal on your device.

### Single Account Mode

In the `Single Account` Mode, only one user can sign into the application at a time. If the app wants to support just one signed-in user, it is recommended to use the `Single Account` Mode.

The following code snippet from **SingleAccountModeFragment** class shows how the application is set to the `Single Account` Mode in the code:

```java
PublicClientApplication.createSingleAccountPublicClientApplication(
    getContext(),
    R.raw.auth_config_single_account);
```

In the **auth_config_single_account.json** file, the `account_mode` is set as following:

```json
"account_mode" : "SINGLE",
```

## Multiple Account Mode

In the `Multiple Account` Mode, the application supports multiple accounts and can switch between user accounts and get data from that user's account.

Code snippet from **MultipleAccountModeFragment** class shows how the application is set in the `Multiple Account` Mode in the code:

```java
PublicClientApplication.createMultipleAccountPublicClientApplication(getContext(),
    R.raw.auth_config_multiple_account);
```

## How to run this sample

To run this sample, you'll need:

* Android SDK
* An internet connection
* An Azure Active Directory (Azure AD) tenant. For more information on how to get an Azure AD tenant, see [How to get an Azure AD tenant](https://azure.microsoft.com/en-us/documentation/articles/active-directory-howto-tenant/)
* One or more user accounts in your Azure AD tenant.

## Steps to Run the app

> This sample ships with a default `redirect_uri` configured in the `AndroidManifest.xml`. In order for the default `redirect_uri` to work, this project must be built with the `debug.keystore` located in the `gradle/` directory. To configure signing in Android Studio, see [Sign Your App](https://developer.android.com/studio/publish/app-signing).

### Step 1: Clone the code

  From your shell or command line:

```Shell
   git clone https://github.com/Azure-Samples/ms-identity-android-kotlin.git
  ```

   The following steps have been carried out for android studio, but you can choose and work with any editor of your choice.

   Open Android Studio, and select *open an existing Android Studio project*. Find the cloned project and open it.

### Step 2: Run the sample

From menu, select *Run* > *Run 'app'*. Once the app launches,

1. Click on the hamburger icon

    * Single Account: Select this to explore Single Account Mode

    * Multiple Account: Select this to explore Multiple Account Mode.

2. Click on sign-in, it takes you to `add account` page.

3. Add one or more accounts as per the device mode, and sign in with your test account.

4. Once successfully signed-in, basic user details will be displayed.

To explore more about the application, follow on screen options.

> This sample application is configured to run out-of-the-box. To register your own application and run the sample with those settings, follow below steps.

## Register your Own Application (Optional)  

To begin registering your app, start at the [Azure portal](https://aka.ms/MobileAppReg)

To **create** an app registration,  

1. Click `New Registration`.

2. Name your app, select the audience you're targeting, and click `Register`.

3. In the `Overview` > `Sign in users in 5 minutes` > `Android`.
    * Click on `Make this changes for me`.
    * Enter the Package Name from your Android Manifest.
    * Generate a Signature Hash. Follow the instructions in the portal.

4. Hit the `Make updates` button. Note the ***MSAL Configuration*** as it is used later in `AndroidManifest.xml` and `auth_config.json`.

**Configure** the sample application with your app registration by replacing the sample code in `auth_config.json` and `AndroidManifest.xml`

1. Copy and paste the ***MSAL Configuration*** JSON from the Azure portal into `auth_config.json`.

2. Inside the `AndroidManifest.xml`, replace `android:host` and `android:path` with the same info saved in above step.
        - `auth_config.json` contains this information as a reference inside the `redirect_uri` field.
        - The Signature Hash should NOT be URL encoded in the `AndroidManifest.xml`.
    Refer [Azure Active Directory Android Quickstart](https://docs.microsoft.com/en-us/azure/active-directory/develop/quickstart-v2-android) for more details

From menu, select *Build* > *Clean Project* and *Run* > *Run 'app'*.

## About the code

The following code fragments walk through features that MSAL can implement.

### SingleAccountModeFragment class

   Contains code showing how the `Single Account` Mode is implemented. The includes authentication, obtaining the token, and making a Graph API call using the obtained token.

   The following steps give you more details.

1. Create a SingleAccount PublicClientApplication:
    ```java
    PublicClientApplication.createSingleAccountPublicClientApplication(
        context as Context,
        R.raw.auth_config_single_account,
        object : IPublicClientApplication.ISingleAccountApplicationCreatedListener {
            override fun onCreated(application: ISingleAccountPublicClientApplication) {
            }

            override fun onError(exception: MsalException) {
            }
        })
    ```

2. Signing in a user:
    ```java
    mSingleAccountApp!!.signIn(activity as Activity, "", getScopes(), getAuthInteractiveCallback())
    ```

3. Acquiring token:
    * Interactive:
    ```java
    mSingleAccountApp!!.acquireToken(activity!!, getScopes(), getAuthInteractiveCallback())
    ```

    * Silent:
    ```java
    mSingleAccountApp!!.acquireTokenSilentAsync(getScopes(), AUTHORITY, getAuthSilentCallback())
    ```

4. Calling Graph API to get basic user details and displaying data:
    ```java
    private fun callGraphAPI(authenticationResult: IAuthenticationResult) {
        MSGraphRequestWrapper.callGraphAPIWithVolley(
            context as Context,
            msgraph_url.text.toString(),
            authenticationResult.accessToken,
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
    ```

5. Sign-out:
    ```java
    mSingleAccountApp!!.signOut(object : ISingleAccountPublicClientApplication.SignOutCallback {
        override fun onSignOut() {
        }

        override fun onError(exception: MsalException) {
        }
    });
    ```  

    When sign-out is performed it removes the signed-in account and cached tokens from this app.

### MultipleAccountModeFragment class

   Contains code showing how the `Multiple Account` Mode is implemented. The includes authentication, obtaining the token, and making a Graph API call using the obtained token.

1. Create a MultipleAccount PublicClientApplication:

    ```java
    PublicClientApplication.createMultipleAccountPublicClientApplication(
        context as Context,
        R.raw.auth_config_multiple_account,
        object : IPublicClientApplication.IMultipleAccountApplicationCreatedListener {
            override fun onCreated(application: IMultipleAccountPublicClientApplication) {
                mMultipleAccountApp = application
            }

            override fun onError(error: MsalException){
            }
        });
    ```

2. Acquiring token:

    * Interactive:
    ```java
    mMultipleAccountApp!!.acquireToken(activity as Activity, getScopes(), getAuthInteractiveCallback())
    ```

    * Silent:
    ```java
    mMultipleAccountApp.acquireTokenSilentAsync(getScopes(),
        accountList.get(accountListSpinner.getSelectedItemPosition()),
            AUTHORITY,
            getAuthSilentCallback());
    ```

3. Get Accounts:
    ```java
    mMultipleAccountApp!!.acquireTokenSilentAsync(
                getScopes(),
                accountList!![account_list.selectedItemPosition],
                AUTHORITY,
                getAuthSilentCallback()
            )
    ```

4. Remove account:
    ```java
     mMultipleAccountApp!!.removeAccount(
        accountList!![account_list.selectedItemPosition],
        object : IMultipleAccountPublicClientApplication.RemoveAccountCallback {
            override fun onRemoved() {

            }

            override fun onError(exception: MsalException) {

            }
        })
    ```



## Feedback, Community Help, and Support

We use [Stack Overflow](http://stackoverflow.com/questions/tagged/msal) with the community to
provide support. We highly recommend you ask your questions on Stack Overflow first and browse
existing issues to see if someone has asked your question before.

If you find and bug or have a feature request, please raise the issue
on [GitHub Issues](../../issues).

To provide a recommendation, visit
our [User Voice page](https://feedback.azure.com/forums/169401-azure-active-directory).

## Contribute

We enthusiastically welcome contributions and feedback. You can clone the repo and start
contributing now. Read our [Contribution Guide](Contributing.md) for more information.

This project has adopted the
[Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see
the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or contact
[opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

## Security Library

This library controls how users sign-in and access services. We recommend you always take the
latest version of our library in your app when possible. We
use [semantic versioning](http://semver.org) so you can control the risk associated with updating
your app. As an example, always downloading the latest minor version number (e.g. x.*y*.x) ensures
you get the latest security and feature enhancements but our API surface remains the same. You
can always see the latest version and release notes under the Releases tab of GitHub.

## Security Reporting

If you find a security issue with our libraries or services please report it
to [secure@microsoft.com](mailto:secure@microsoft.com) with as much detail as possible. Your
submission may be eligible for a bounty through the [Microsoft Bounty](https://aka.ms/bugbounty)
program. Please do not post security issues to GitHub Issues or any other public site. We will
contact you shortly upon receiving the information. We encourage you to get notifications of when
security incidents occur by
visiting [this page](https://technet.microsoft.com/en-us/security/dd252948) and subscribing
to Security Advisory Alerts.

## Other samples and documentation

* The documentation for the Microsoft identity platform is available from [Microsoft identity platform (v2.0) overview](https://aka.ms/aadv2).

* Other samples for the Microsoft identity platform are available from [Microsoft identity platform code samples](https://aka.ms/aaddevsamplesv2).

* The conceptual documentation for MSAL Android is available from [Microsoft authentication library for android conceptual documentation](https://aka.ms/msalandroid).

* [Learn more about Brokers](https://docs.microsoft.com/azure/active-directory/develop/brokered-auth)
