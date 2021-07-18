package com.example.searchgithubusers

import org.json.JSONObject

data class GitHubUser(
    val name: String,
    val avatar: String
) {
    companion object {
        fun parseJson(json: JSONObject): GitHubUser? {
            val name = json.optString("login", "")
            val avatar = json.optString("avatar_url", "")
            return if (name.isNotEmpty() && avatar.isNotEmpty()) GitHubUser(name, avatar) else null
        }
    }
}
