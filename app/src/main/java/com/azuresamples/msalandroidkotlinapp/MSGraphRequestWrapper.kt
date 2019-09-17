package com.azuresamples.msalandroidkotlinapp

import android.content.Context
import android.util.Log
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject
import java.util.*

class MSGraphRequestWrapper {


    /**
     * Use Volley to make an HTTP request with
     * 1) a given MSGraph resource URL
     * 2) an access token
     * to obtain MSGraph data.
     */
    companion object {

        private val TAG = MSGraphRequestWrapper::class.java.simpleName

        fun callGraphAPIWithVolley(
            context: Context,
            graphResourceUrl: String,
            accessToken: String,
            responseListener: Response.Listener<JSONObject>,
            errorListener: Response.ErrorListener
        ) {
            Log.d(TAG, "Starting volley request to graph")

            /* Make sure we have a token to send to graph */
            if (accessToken == null || accessToken.length == 0) {
                return
            }

            val queue = Volley.newRequestQueue(context)
            val parameters = JSONObject()

            try {
                parameters.put("key", "value")
            } catch (e: Exception) {
                Log.d(TAG, "Failed to put parameters: $e")
            }

            val request = object : JsonObjectRequest(
                Request.Method.GET, graphResourceUrl,
                parameters, responseListener, errorListener
            ) {
                override fun getHeaders(): MutableMap<String, String> {
                    val headers = HashMap<String, String>()
                    headers["Authorization"] = "Bearer $accessToken"
                    return headers
                }
            }

            Log.d(TAG, "Adding HTTP GET to Queue, Request: $request")

            request.retryPolicy =
                DefaultRetryPolicy(
                    3000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
                )

            queue.add(request)
        }
    }
}