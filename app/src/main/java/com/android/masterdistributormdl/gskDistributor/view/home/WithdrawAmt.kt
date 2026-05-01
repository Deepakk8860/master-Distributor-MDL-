package com.android.masterdistributormdl.gskDistributor.view.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.WithdrawAmtBinding
import com.google.gson.JsonObject
import com.gsk.distributor.model.DashCount
import com.android.masterdistributormdl.gskDistributor.utils.AlertError
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.SuccessAlert
import com.android.masterdistributormdl.gskDistributor.utils.getDialog
import com.android.masterdistributormdl.gskDistributor.utils.getNumberFormat
import com.android.masterdistributormdl.gskDistributor.utils.getPriceFormat
import com.android.masterdistributormdl.gskDistributor.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.gskDistributor.utils.setClearError
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.view.MainActivity


class WithdrawAmt : Fragment() {
    private lateinit var binding: WithdrawAmtBinding
    lateinit var model: WithdrawModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.withdraw_amt, container, false)
        model = ViewModelProvider(this)[WithdrawModel::class.java]
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

        model.retrofitError.observe(viewLifecycleOwner) {
            if (it.status == 0) {
                showToastShort(it.message)
            } else {
                InternetError.show(requireContext())
            }
        }
        model.errorMessage.observe(viewLifecycleOwner) {
            AlertError.show(requireContext(), it) {}
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) {
            Loading.showHide(requireActivity(), it)
        }
        binding.save.setOnClickListener { withdrawCommission() }
        binding.back.setOnClickListener {
            hideSoftKeyBoard(requireContext())
            requireActivity().onBackPressed()
        }
        setDashCount(model.getDashCount())
        binding.save.visibility = View.GONE
        binding.message.visibility = View.GONE
        setClearError(binding.amountLay, binding.amount)
        setAccountData()
    }

    private fun setAccountData() {
        model.getAccountDetails{
            if (it.status==0) {
                binding.cardAccountDetails.visibility=View.VISIBLE
                binding.txtAccountNumber.text=it.account_details.account_number
                binding.txtAccountName.text=it.account_details.account_name
                binding.txtIfscCode.text=it.account_details.ifsc_code
            }else{
                binding.cardAccountDetails.visibility=View.GONE
            }
        }
    }

    private fun setDashCount(count: DashCount?) {
        if (count == null) return
        binding.comiAmt.text = getPriceFormat(count.Earning)
        binding.amount.setText(getNumberFormat(count.Earning).replace(",", ""))
        binding.amount.isEnabled = false
        payoutcharge()
    }


    fun check() {
        if (binding.amount.text.toString().isEmpty()) {
            binding.amountLay.requestFocus()
            binding.amountLay.error = " "
        } else {
            val amt = binding.amount.text.toString().toDouble()
            if (amt > 0) {
                payoutcharge()
            } else {
                showToastShort("Invalid amount")
            }
        }

    }

    private fun payoutcharge() {
        val param = JsonObject()
        param.addProperty("amount", binding.amount.text.toString())
        model.payoutcharge(param) {
            binding.message.text = it.message
            binding.message.visibility = View.VISIBLE
            if (it.status == 0) {
                binding.save.visibility = View.VISIBLE
            }
        }
    }

    private fun withdraw_amount_alert(message: String) {
        val dialog = getDialog(requireContext(), R.layout.withdraw_amount_alert)
        dialog.setCancelable(false)
        val cancel = dialog.findViewById<Button>(R.id.cancel)
        val confirm = dialog.findViewById<Button>(R.id.confirm)
        val msg = dialog.findViewById<TextView>(R.id.msg)
        msg.text = message
        cancel.setOnClickListener { dialog.dismiss() }
        confirm.setOnClickListener {
            dialog.dismiss()
            withdrawCommission()
        }
        dialog.show()
    }

    private fun withdrawCommission() {
        val param = JsonObject()
        param.addProperty("amount", binding.amount.text.toString())
        model.withdrawCommission(param) {
            if (it.status == 0) {
                SuccessAlert.show(requireContext(), it.message) {
                    requireActivity().onBackPressed()
                }
            } else {
                model.errorMessage.value = it.message
            }
        }
    }

}





