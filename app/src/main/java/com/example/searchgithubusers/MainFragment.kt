package com.example.searchgithubusers

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide


class MainFragment : Fragment() {

    private lateinit var viewModel: MainViewModel
    private lateinit var progressBar: ProgressBar
    private val resultAdapter = ResultAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.main_fragment, container, false).apply {
            val editTextKeyword = findViewById<EditText>(R.id.edit_text_keyword)
            val buttonSearch = findViewById<Button>(R.id.button_search).apply {
                setOnClickListener {
                    resultAdapter.users.clear()
                    resultAdapter.notifyDataSetChanged()
                    viewModel.keyword = editTextKeyword.text.toString().trim()
                    viewModel.page = 0
                    viewModel.startSearch()
                }
            }
            val recyclerView = findViewById<RecyclerView>(R.id.recyclerview_result).apply {
                adapter = resultAdapter
            }
            progressBar = findViewById(R.id.progress_bar)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        viewModel.searchResult.observe(this) {
            resultAdapter.users.addAll(it)
            resultAdapter.notifyItemRangeInserted(resultAdapter.itemCount, it.size)
        }

        viewModel.progressing.observe(this) {
            progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }
    }


    inner class ResultAdapter : RecyclerView.Adapter<ResultAdapter.ViewHolder>() {

        val users = ArrayList<GitHubUser>()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            return ViewHolder(
                LayoutInflater.from(parent.context).inflate(R.layout.list_item_user, parent, false)
            )
        }

        override fun getItemCount() = users.size

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            holder.bind(users[position])
            if (itemCount < viewModel.totalCount && position > itemCount - 5 && viewModel.progressing.value == false) {
                viewModel.startSearch()
            }
        }


        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

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
    }


    companion object {
        fun newInstance() = MainFragment()
    }
}