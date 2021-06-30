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

import java.util.*

/**
 * The value in this class has to map with the json configuration file (auth_config_b2c.json).
 * i.e. If you are using the following json file.
 * {
 * "client_id" : "90c0fe63-bcf2-44d5-8fb7-b8bbc0b29dc6",
 * "redirect_uri" : "msauth://com.azuresamples.msalandroidapp/1wIqXSqBj7w%2Bh11ZifsnqwgyKrY%3D",
 * "account_mode" : "MULTIPLE",
 * "broker_redirect_uri_registered": false,
 * "authorities": [
 * {
 * "type": "B2C",
 * "authority_url": "https://fabrikamb2c.b2clogin.com/tfp/fabrikamb2c.onmicrosoft.com/b2c_1_susi/",
 * "default": true
 * },
 * {
 * "type": "B2C",
 * "authority_url": "https://fabrikamb2c.b2clogin.com/tfp/fabrikamb2c.onmicrosoft.com/b2c_1_edit_profile/"
 * },
 * {
 * "type": "B2C",
 * "authority_url": "https://fabrikamb2c.b2clogin.com/tfp/fabrikamb2c.onmicrosoft.com/b2c_1_reset/"
 * }
 * ]
 * }
 *
 *
 * This file contains 2 B2C policies, namely "b2c_1_susi", "b2c_1_edit_profile" and "b2c_1_reset"
 * Its azureAdB2CHostName is "fabrikamb2c.b2clogin.com"
 * Its tenantName is "fabrikamb2c.onmicrosoft.com"
 */
object B2CConfiguration {
    /**
     * Name of the policies/user flows in your B2C tenant.
     * See https://docs.microsoft.com/en-us/azure/active-directory-b2c/active-directory-b2c-reference-policies for more info.
     */
    val Policies = arrayOf(
            "b2c_1_susi",
            "b2c_1_edit_profile",
            "b2c_1_reset"
    )

    /**
     * Name of your B2C tenant hostname.
     */
    const val azureAdB2CHostName = "fabrikamb2c.b2clogin.com"

    /**
     * Name of your B2C tenant.
     */
    const val tenantName = "fabrikamb2c.onmicrosoft.com"

    /**
     * Returns an authority for the given policy name.
     *
     * @param policyName name of a B2C policy.
     */
    fun getAuthorityFromPolicyName(policyName: String): String {
        return "https://" + azureAdB2CHostName + "/" + tenantName + "/" + policyName + "/"
    }

    /**
     * Returns an array of scopes you wish to acquire as part of the returned token result.
     * These scopes must be added in your B2C application page.
     */
    val scopes: List<String>
        get() = Arrays.asList(
                "https://fabrikamb2c.onmicrosoft.com/helloapi/demo.read")
}