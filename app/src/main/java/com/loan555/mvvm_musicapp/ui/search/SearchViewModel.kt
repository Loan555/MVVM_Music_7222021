package com.loan555.mvvm_musicapp.ui.search

import androidx.lifecycle.ViewModel
import com.loan555.mvvm_musicapp.repository.SearchSongRepository

class SearchViewModel : ViewModel() {
    private val searchRepository = SearchSongRepository()

    fun getList() = searchRepository.getList()

    fun searchList(query: String) = searchRepository.searchList(query)

}