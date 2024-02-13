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

object B2CConfiguration {
    /**
     * Name of the policies/user flows in your B2C tenant.
     * See https://docs.microsoft.com/en-us/azure/active-directory-b2c/active-directory-b2c-reference-policies for more info.
     */
    @JvmField
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
    @JvmStatic
    fun getAuthorityFromPolicyName(policyName: String): String {
        return "https://" + azureAdB2CHostName + "/tfp/" + tenantName + "/" + policyName + "/"
    }

    @JvmStatic
    val scopes: List<String>
        /**
         * Returns an array of scopes you wish to acquire as part of the returned token result.
         * These scopes must be added in your B2C application page.
         */
        get() = mutableListOf(
            "https://fabrikamb2c.onmicrosoft.com/helloapi/demo.read"
        )
}
