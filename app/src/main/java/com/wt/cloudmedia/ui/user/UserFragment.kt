package com.wt.cloudmedia.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.wt.cloudmedia.BaseFragment
import com.wt.cloudmedia.databinding.FragmentUserBinding
import com.wt.cloudmedia.ui.event.SharedViewModel

class UserFragment : BaseFragment() {

    private lateinit var userViewModel:SharedViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userViewModel = getApplicationScopeViewModel(SharedViewModel::class.java)
    }

    private lateinit var binding: FragmentUserBinding
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentUserBinding.inflate(inflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        userViewModel.userRequest.userLiveData.observe(viewLifecycleOwner, { t ->
            if (t.responseStatus.isSuccess) {
                binding.userName.text = t.result.account.username
            } else {
                showToast(t.responseStatus.responseCode)
            }
        })

        binding.login.setOnClickListener {
            userViewModel.userRequest.requestLogin(this.getBaseActivity())
        }

        binding.logout.setOnClickListener {
            binding.userName.text = null
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = UserFragment()
    }
}