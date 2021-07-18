package com.example.searchgithubusers

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

class MainViewModel : ViewModel() {

    private val gitHubRepository = GitHubRepository()

    val searchResult = MutableLiveData<List<GitHubUser>>()
    val progressing = MutableLiveData(false)
    var totalCount = 0
    var keyword = ""
    var page = 0

    fun startSearch() {
        progressing.value = true
        viewModelScope.launch(Dispatchers.IO) {
            val result = gitHubRepository.search(keyword, ++page)
            totalCount = result.first
            searchResult.postValue(result.second)
            progressing.postValue(false)
        }
    }
}