package com.android.masterdistributormdl.gskDistributor.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.OfferBinding
import com.android.masterdistributormdl.databinding.TrainingStatusBinding
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.addFragment
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.view.MainActivity


class TrainingStatus : Fragment() {
    lateinit var model: OffersModel
    private lateinit var binding: TrainingStatusBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.training_status, container, false)
        model = ViewModelProvider(this).get(OffersModel::class.java)
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
            Loading.showHide2(requireContext(), it)
        }
        model.isDialogVisible.observe(viewLifecycleOwner) {
            Loading.showHide(requireActivity(), it)
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            getTrainingStatus()
        }

        model.offerAdapter.setOnclickListener {
            val bundle = bundleOf("data" to it)
            addFragment(requireActivity(), OfferInfo(), bundle)
        }
//        model.offerimage()

        getTrainingStatus()
    }

    private fun getTrainingStatus() {
        model.getTrainingStatus{
            if (it.status==0) {
                binding.txtDay1Value.text=it.data.day_1
                binding.txtDay2Value.text=it.data.day_2
                binding.txtDay3Value.text=it.data.day_3
                binding.txtDay4Value.text=it.data.day_4
                binding.txtDay5Value.text=it.data.day_5
                binding.txtDay6Value.text=it.data.day_6
                binding.txtDay7Value.text=it.data.day_7
            }
        }
    }



}