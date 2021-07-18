package com.example.searchgithubusers

import com.loopj.android.http.SyncHttpClient
import com.loopj.android.http.TextHttpResponseHandler

class GitHubHttpClient {

    private val client = SyncHttpClient()

    fun searchUser(keyword: String, page: Int, handler: TextHttpResponseHandler) {
        val url = GITHUB_API_URL + GITHUB_API_SEARCH_USERS + keyword + GITHUB_API_QUALIFIER + GITHUB_API_PAGE + page
        client.addHeader("User-Agent", "Lunpi")
        client.get(url, handler)
    }


    companion object {
        private const val GITHUB_API_URL = "https://api.github.com/"
        private const val GITHUB_API_SEARCH_USERS = "search/users?q="
        private const val GITHUB_API_QUALIFIER = "+type:user+in:login"
        private const val GITHUB_API_PAGE = "&page="
    }
}