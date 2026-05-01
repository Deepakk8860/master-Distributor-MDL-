package com.android.masterdistributormdl.gskDistributor.view.home


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewOutlineProvider
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.Services5Binding
import com.android.masterdistributormdl.gskDistributor.utils.BASE_URL
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.addFragment
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.view.Content
import com.android.masterdistributormdl.gskDistributor.view.MainActivity


class Services5 : Fragment() {
    lateinit var model: Services1Model
    private lateinit var binding: Services5Binding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding=
            DataBindingUtil.inflate(inflater, R.layout.services5, container, false)
        model = ViewModelProvider(this)[Services1Model::class.java]
        binding.model = model
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        return binding.root
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            setHeader()
        }
    }

    private fun setHeader() {
        try {
            shooterFragment = this
            (activity as MainActivity).setHeader("", STATUS_COLOR2)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        model.setViewType(5)
        setHeader()
        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }
        model.retrofitError.observe(viewLifecycleOwner) {
            if (it.status == 0) {
                showToastShort(it.message)
            } else {
                InternetError.show(requireContext())
            }
        }
        model.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) showToastShort(it)
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = it
            Loading.showHide2(requireActivity(), it)
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            model.getServices()
        }
        model.getServices()
        model.services1Adapter.setOnclickListener {

        }
        binding.save.setOnClickListener {

            val bundle = bundleOf("url" to "$BASE_URL/commission-structure/master_distributor","title" to "")
            addFragment(requireActivity(), Content(), bundle)
//            val url = "https://docs.google.com/spreadsheets/d/1oDCzhC2vJDzCxJrQVSqcPaoKdIrvHsWo-9gw4SDOQUs/edit?gid=0#gid=0" // Replace with your dynamic link
//            val intent = Intent(Intent.ACTION_VIEW)
//            intent.data = Uri.parse(url)
//            startActivity(intent)


        }

        binding.swipeRefreshLayout.outlineProvider = ViewOutlineProvider.BACKGROUND;
        binding.swipeRefreshLayout.clipToOutline = true;

    }

}







