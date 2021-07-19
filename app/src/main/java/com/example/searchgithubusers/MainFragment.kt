package com.example.searchgithubusers

import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var noResult: TextView
    private val resultAdapter = ResultAdapter()
    private val progressBar = ListItemProgressBar()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.main_fragment, container, false).apply {
            val editTextKeyword = findViewById<EditText>(R.id.edit_text_keyword)
            val buttonSearch = findViewById<Button>(R.id.button_search).apply {
                setOnClickListener {
                    resultAdapter.apply {
                        items.clear()
                        notifyDataSetChanged()
                    }
                    viewModel.apply {
                        keyword = editTextKeyword.text.toString().trim()
                        page = 0
                        startSearch()
                    }
                    (requireActivity().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).apply {
                        if (isAcceptingText) {
                            hideSoftInputFromWindow(windowToken, 0)
                        }
                    }
                    noResult.visibility = View.GONE
                }
            }
            recyclerView = findViewById<RecyclerView>(R.id.recyclerview_result).apply {
                adapter = resultAdapter
            }
            noResult = findViewById(R.id.text_no_result)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        viewModel.searchResult.observe(this) { result ->
            noResult.apply {
                visibility = if (result.isEmpty()) View.VISIBLE else View.GONE
                text = String.format(getString(R.string.no_search_result), viewModel.keyword)
            }
            resultAdapter.apply {
                items.addAll(result)
                recyclerView.post {
                    if (result.isEmpty()) {
                        notifyItemRemoved(itemCount)
                    } else {
                        notifyItemRangeInserted(itemCount, result.size)
                    }
                }
            }
        }

        viewModel.progressing.observe(this) {
            resultAdapter.apply {
                if (it) {
                    items.add(progressBar)
                    recyclerView.post { notifyItemInserted(itemCount) }
                } else {
                    items.remove(progressBar)
                }
            }
        }
    }


    inner class ResultAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        val items = ArrayList<Any>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            return when (viewType) {
                ITEM_TYPE_USER -> UserViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_user, parent, false))
                else -> ProgressBarViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.list_item_progress_bar, parent, false))
            }
        }

        override fun getItemCount() = items.size

        override fun getItemViewType(position: Int) = when (items[position]) {
            is GitHubUser -> ITEM_TYPE_USER
            else -> ITEM_TYPE_PROGRESS_BAR
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            if (holder.itemViewType == ITEM_TYPE_USER) {
                val item = items[position]
                if (item is GitHubUser) {
                    (holder as UserViewHolder).bind(item)
                }
                if (itemCount < viewModel.totalCount && position > itemCount - 5 && viewModel.progressing.value == false) {
                    viewModel.startSearch()
                }
            }
        }


        inner class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

            val avatar: ImageView = itemView.findViewById(R.id.image_avatar)
            val name: TextView = itemView.findViewById(R.id.text_name)

            fun bind(item: GitHubUser) {
                Glide.with(itemView)
                    .load(item.avatar)
                    .circleCrop()
                    .into(avatar)
                name.text = item.name
            }
        }

        inner class ProgressBarViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
    }


    inner class ListItemProgressBar


    companion object {
        private const val ITEM_TYPE_USER = 0
        private const val ITEM_TYPE_PROGRESS_BAR = 1
        fun newInstance() = MainFragment()
    }
}