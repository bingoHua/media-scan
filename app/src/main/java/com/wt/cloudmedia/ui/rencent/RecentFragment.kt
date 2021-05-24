package com.wt.cloudmedia.ui.rencent

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import cn.jzvd.Jzvd
import com.wt.cloudmedia.BaseFragment
import com.wt.cloudmedia.R
import com.wt.cloudmedia.databinding.FragmentMainBinding
import com.wt.cloudmedia.databinding.RecentFragmentBinding
import com.wt.cloudmedia.ui.main.MovieViewModel
import com.wt.cloudmedia.ui.main.MovieViewModelFactory
import com.wt.cloudmedia.ui.main.RecyclerViewAdapter

class RecentFragment : BaseFragment() {

    companion object {
        fun newInstance() = RecentFragment()
    }

    private lateinit var binding: RecentFragmentBinding

    private val viewModel: RecentViewModel by viewModels {
        RecentMovieViewModelFactory(getMediaApplication().repository)
    }

    private var adapter: RecyclerViewAdapter? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        binding = RecentFragmentBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        adapter = RecyclerViewAdapter()
        binding.recyclerView.adapter = adapter
        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        binding.recyclerView.addOnChildAttachStateChangeListener(object : RecyclerView.OnChildAttachStateChangeListener {
            override fun onChildViewAttachedToWindow(view: View) {}
            override fun onChildViewDetachedFromWindow(view: View) {
                val jzvd: Jzvd = view.findViewById(R.id.videoplayer)
                if (Jzvd.CURRENT_JZVD != null && jzvd.jzDataSource.containsTheUrl(Jzvd.CURRENT_JZVD.jzDataSource.currentUrl)) {
                    if (Jzvd.CURRENT_JZVD != null && Jzvd.CURRENT_JZVD.screen != Jzvd.SCREEN_FULLSCREEN) {
                        Jzvd.releaseAllVideos()
                    }
                }
            }
        })
        viewModel.getRecentMovies().observe(this) {
            it.let { adapter?.addItems(it) }
        }
    }

}