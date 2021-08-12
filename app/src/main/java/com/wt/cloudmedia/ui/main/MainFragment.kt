package com.wt.cloudmedia.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.jzvd.Jzvd
import com.microsoft.graph.requests.extensions.GraphServiceClient
import com.wt.cloudmedia.BaseFragment
import com.wt.cloudmedia.R
import com.wt.cloudmedia.databinding.FragmentMainBinding
import com.wt.cloudmedia.repository.DataRepository
import com.wt.cloudmedia.request.ResponseStatus
import com.wt.cloudmedia.ui.event.SharedViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlin.coroutines.coroutineContext

/**
 * A simple [Fragment] subclass.
 * Use the [MainFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class MainFragment : BaseFragment() {

    private val viewModel: MovieViewModel by viewModels {
        MovieViewModelFactory(getMediaApplication().dataRepository)
    }
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var binding: FragmentMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedViewModel = getApplicationScopeViewModel(SharedViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = FragmentMainBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    private var adapter: RecyclerViewAdapter? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter = RecyclerViewAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {}
            override fun onChildViewDetachedFromWindow(view: View) {
                val jzvd: Jzvd = view.findViewById(R.id.videoplayer) ?: return
                if (Jzvd.CURRENT_JZVD != null && jzvd.jzDataSource.containsTheUrl(Jzvd.CURRENT_JZVD.jzDataSource.currentUrl)) {
                    if (Jzvd.CURRENT_JZVD != null && Jzvd.CURRENT_JZVD.screen != Jzvd.SCREEN_FULLSCREEN) {
                        Jzvd.releaseAllVideos()
                    }
                }
            }
        })
        /*adapter?.setItemClicked {
            viewModel.saveRecent(it)
        }*/
        /*viewModel.movies.observe(this) { movies ->
            movies.let { adapter?.addItems(it) }
        }*/
        viewModel.dataRequest.requestMovie().observe(this) {
            if (it.responseStatus.isSuccess) {
                adapter?.addItems(it.result)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}