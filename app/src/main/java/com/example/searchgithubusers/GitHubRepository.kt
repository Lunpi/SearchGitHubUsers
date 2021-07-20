package com.example.searchgithubusers

import android.util.Log
import com.loopj.android.http.JsonHttpResponseHandler
import cz.msebera.android.httpclient.Header
import kotlinx.coroutines.suspendCancellableCoroutine
import org.json.JSONObject
import java.net.UnknownHostException

class GitHubRepository {

    private val githubClient = GitHubHttpClient()

    suspend fun search(keyword: String, page: Int): Pair<Int, List<GitHubUser>?> {
        return suspendCancellableCoroutine {
            githubClient.searchUser(keyword, page, object : JsonHttpResponseHandler() {

                override fun onSuccess(statusCode: Int, headers: Array<out Header>?, response: JSONObject?) {
                    super.onSuccess(statusCode, headers, response)

                    val totalCount = response?.optInt("total_count") ?: 0
                    val items = response?.optJSONArray("items")
                    if (items == null) {
                        it.resumeWith(Result.success(Pair(totalCount, null)))
                        return
                    }

                    val results = ArrayList<GitHubUser>()
                    for (i in 0 until items.length()) {
                        GitHubUser.parseJson(items.getJSONObject(i))?.let { user ->
                            results.add(user)
                        }
                    }
                    it.resumeWith(Result.success(Pair(totalCount, results)))
                }

                override fun onFailure(statusCode: Int, headers: Array<out Header>?, throwable: Throwable?, errorResponse: JSONObject?) {
                    super.onFailure(statusCode, headers, throwable, errorResponse)
                    Log.e(TAG, "Search onFailure: statusCode: $statusCode, throwable: $throwable, errorResponse: $errorResponse")

                    errorResponse?.optString("message")?.let { message ->
                        if (message.contains("API rate limit")) {
                            it.resumeWith(Result.failure(ApiRateExceededException(message)))
                            return
                        }
                    }
                    throwable?.message?.let { message ->
                        if (message.contains("UnknownHostException")) {
                            it.resumeWith(Result.failure(UnknownHostException(message)))
                            return
                        }
                    }
                    it.resumeWith(Result.failure(Exception(throwable)))
                }

                override fun getUseSynchronousMode() = false
            })
        }
    }


    class ApiRateExceededException(message: String) : java.lang.Exception(message)


    companion object {
        private const val TAG = "GitHubRepository"
    }
}