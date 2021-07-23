package com.loan555.mvvm_musicapp.ui.activity

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.SearchView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.loan555.mvvm_musicapp.R
import com.loan555.mvvm_musicapp.databinding.SearchActivityBinding
import com.loan555.mvvm_musicapp.model.SongCustom
import com.loan555.mvvm_musicapp.ui.adapter.SongAdapter
import com.loan555.mvvm_musicapp.ui.search.SearchViewModel

class SearchActivity : AppCompatActivity() {

    private lateinit var binding: SearchActivityBinding
    private lateinit var viewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.search_activity)

        viewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        binding = DataBindingUtil.setContentView(this, R.layout.search_activity)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_close)

        binding.recyclerSearch.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerSearch.adapter = SongAdapter(this, application, onItemClick)
        viewModel.getList().observe(this, {
            binding.recyclerSearch.adapter = SongAdapter(this, application, onItemClick).apply {
                setSongs(it)
            }
            binding.swipeRefresh.isRefreshing = false
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_search, menu)
        val search = menu?.findItem(R.id.menu_search)
        val searchView = search?.actionView as SearchView
        searchView.queryHint = "Search"
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                Log.d("aaa", "search list submit")
                if (query != null) {
                    binding.swipeRefresh.isRefreshing = true
                    viewModel.searchList(query)
                } else Toast.makeText(this@SearchActivity, "Bạn chưa nhập gì", Toast.LENGTH_SHORT)
                    .show()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            this.finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private val onItemClick: (Int, List<SongCustom>) -> Unit = { pos, songs ->
        val myIntent = Intent()
        val bundle = Bundle()
        bundle.putSerializable("songSearch", songs[pos])
        myIntent.putExtra("bundle", bundle)
        setResult(Activity.RESULT_OK, myIntent)
        this.finish()
    }
}