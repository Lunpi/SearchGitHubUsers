package com.example.searchgithubusers

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*
import java.net.UnknownHostException

class MainViewModel : ViewModel() {

    private val gitHubRepository = GitHubRepository()

    val searchResult = MutableLiveData<List<GitHubUser>>()
    val progressing = MutableLiveData(false)
    val toastMessage = MutableLiveData("")
    var totalCount = 0
    var keyword = ""
    var page = 0

    fun startSearch() {
        progressing.value = true
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = gitHubRepository.search(keyword, ++page)
                totalCount = result.first
                progressing.postValue(false)
                searchResult.postValue(result.second)
            } catch (e: GitHubRepository.ApiRateExceededException) {
                progressing.postValue(false)
                toastMessage.postValue(TOAST_MESSAGE_API_RATE_EXCEEDED)
            } catch (e: UnknownHostException) {
                progressing.postValue(false)
                toastMessage.postValue(TOAST_MESSAGE_NETWORK_ERROR)
            } catch (e: Exception) {
                progressing.postValue(false)
                toastMessage.postValue(TOAST_MESSAGE_OTHERS)
            }
        }
    }


    companion object {
        const val TOAST_MESSAGE_API_RATE_EXCEEDED = "ApiRateExceededException"
        const val TOAST_MESSAGE_NETWORK_ERROR = "network"
        const val TOAST_MESSAGE_OTHERS = "others"
    }
}