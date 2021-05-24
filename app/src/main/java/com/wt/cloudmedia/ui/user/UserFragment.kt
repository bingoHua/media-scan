package com.wt.cloudmedia.ui.user

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import com.wt.cloudmedia.BaseFragment
import com.wt.cloudmedia.constant.OK
import com.wt.cloudmedia.databinding.FragmentUserBinding

class UserFragment : BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    private lateinit var binding: FragmentUserBinding
    private lateinit var userName: MutableLiveData<String>
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        binding = FragmentUserBinding.inflate(inflater)
        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        userName = getMediaApplication().getUserName()
        userName.observe(this) {
            binding.userName.text = it
        }

        binding.login.setOnClickListener {
            getMediaApplication().login(getBaseActivity(), object : (Int, String?) -> Unit {
                override fun invoke(errorCode: Int, errorMessage: String?) {
                    if (errorCode == OK) {
                        userName.value = errorMessage
                    } else {
                        showToast("${errorCode}:${errorMessage}")
                    }
                }
            })
        }
        binding.logout.setOnClickListener {
            getMediaApplication().loginOut()
            userName.value = null
        }
    }

    companion object {
        @JvmStatic
        fun newInstance() = UserFragment()
    }
}