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

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

object MSGraphRequestWrapper {
    private val TAG = MSGraphRequestWrapper::class.java.simpleName

    // See: https://docs.microsoft.com/en-us/graph/deployments#microsoft-graph-and-graph-explorer-service-root-endpoints
    const val MS_GRAPH_ROOT_ENDPOINT = "https://graph.microsoft.com/"

    /**
     * Use Volley to make an HTTP request with
     * 1) a given MSGraph resource URL
     * 2) an access token
     * to obtain MSGraph data.
     */
    fun callGraphAPIUsingVolley(
        context: Context,
        graphResourceUrl: String,
        accessToken: String,
        responseListener: Response.Listener<JSONObject>,
        errorListener: Response.ErrorListener
    ) {
        Log.d(TAG, "Starting volley request to graph")

        /* Make sure we have a token to send to graph */if (accessToken == null || accessToken.length == 0) {
            return
        }
        val queue = Volley.newRequestQueue(context)
        val parameters = JSONObject()
        try {
            parameters.put("key", "value")
        } catch (e: Exception) {
            Log.d(TAG, "Failed to put parameters: $e")
        }
        val request: JsonObjectRequest = object : JsonObjectRequest(
            Method.GET, graphResourceUrl,
            parameters, responseListener, errorListener
        ) {
            override fun getHeaders(): Map<String, String> {
                val headers: MutableMap<String, String> = HashMap()
                headers["Authorization"] = "Bearer $accessToken"
                return headers
            }
        }
        Log.d(TAG, "Adding HTTP GET to Queue, Request: $request")
        request.retryPolicy = DefaultRetryPolicy(
            3000,
            DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
            DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        )
        queue.add(request)
    }
}