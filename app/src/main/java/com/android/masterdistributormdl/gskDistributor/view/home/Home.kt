package com.android.masterdistributormdl.gskDistributor.view.home

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import com.android.masterdistributormdl.utils.SharedPreference
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.viewpager.widget.ViewPager
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.addLead.ManageLead
import com.android.masterdistributormdl.databinding.HomeDistBinding
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.android.masterdistributormdl.gskDistributor.adapter.HomeAdapter
import com.android.masterdistributormdl.gskDistributor.adapter.HomeSlider
import com.gsk.distributor.model.DashCount
import com.gsk.distributor.model.DashResult
import com.gsk.distributor.model.ErrorAlert
import com.gsk.distributor.model.OfferItem
import com.android.masterdistributormdl.gskDistributor.model.User
import com.gsk.distributor.model.UserResult
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.OFFERS
import com.android.masterdistributormdl.gskDistributor.utils.addFragment
import com.android.masterdistributormdl.gskDistributor.utils.dash_count
import com.android.masterdistributormdl.gskDistributor.utils.getPriceFormat
import com.android.masterdistributormdl.gskDistributor.utils.gson
import com.android.masterdistributormdl.gskDistributor.utils.is_sales
import com.android.masterdistributormdl.gskDistributor.utils.replaceFragment
import com.android.masterdistributormdl.gskDistributor.utils.session_id
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_data
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.android.masterdistributormdl.gskDistributor.view.SplashActivity
import com.android.masterdistributormdl.gskDistributor.view.onboarding.OnboardActivity
import com.android.masterdistributormdl.gskDistributor.view.onboarding.OnboardBasicDetails
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class Home : Fragment() {
    lateinit var model: HomeModel
    private lateinit var binding: HomeDistBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.home_dist, container, false)
        model = ViewModelProvider(this)[HomeModel::class.java]
        binding.model = model
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        return binding.root
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (hidden) {
            stopAutoScroll()
        } else {
            iniApi()
            setHeader()
        }
    }

    private fun setHeader() {
        try {
            shooterFragment = this
            (requireActivity() as MainActivity).setHeader2("")

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
            if (!it.isNullOrEmpty()) showToastShort(it)
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = it
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            dashCount()
            if (model.getUser() != null) {
//                (requireActivity() as MainActivity).getBlockStatus()
            }

        }
        (requireActivity() as MainActivity).getAppVersion()
        binding.withdraw.visibility = View.GONE
        binding.sliderImage.visibility = View.GONE
//        binding.support.setOnClickListener { (requireActivity() as MainActivity).openSupport() }
        binding.support.setOnClickListener { (requireActivity() as MainActivity).openConsultant() }
        binding.txtConsultant.setOnClickListener { (requireActivity() as MainActivity).openConsultant() }
        binding.menu.setOnClickListener { (requireActivity() as MainActivity).openDrawer() }
        binding.notification.setOnClickListener { addFragment(requireActivity(), Notification()) }
//        model.offerimage()
//        (requireActivity() as MainActivity).getAppVersion()
        if (model.getUser() != null) {
//            (requireActivity() as MainActivity).getBlockStatus()
        }
        setSliderAdapter()
        setData()
        model.sliderAdapter.setOnclickListener {
            val bundle = bundleOf("data" to it)
            addFragment(requireActivity(), OfferInfo(), bundle)
        }
        val count = model.getDashCount()
        if (count != null) {
            setDashCount(count)
        }

        binding.withdraw.setOnClickListener {
            if (binding.withdrawComi.tag == "withdraw") {
                addFragment(requireActivity(), Withdraw())
            } else {
                addFragment(requireActivity(), KycUpdate())
            }
        }
        binding.orders.setOnClickListener {
            addFragment(requireActivity(), ReportOrder())
        }

        binding.shops.setOnClickListener {
            val bundle = bundleOf("type" to "report", "title" to "Retailer Report")
            addFragment(requireActivity(), Shops(), bundle)
        }

        binding.distributors.setOnClickListener {
            val bundle = bundleOf("type" to "report", "title" to "Distributor Report")
            addFragment(requireActivity(), GskDistributor(), bundle)
        }

        binding.agents.setOnClickListener {
            val bundle = bundleOf("type" to "report", "title" to "Agent Report")
            addFragment(requireActivity(), AgentListScreen(), bundle)
        }

        binding.flSocial.setOnClickListener {
            val url =
                "https://whatsapp.com/channel/0029VazAP4tLo4hjk40Fh50X" // Replace with your dynamic Telegram link
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
//            val url = "https://whatsapp.com/channel/0029VazAP4tLo4hjk40Fh50X"
//            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
//            if (intent.resolveActivity(requireContext().packageManager) != null) {
//                startActivity(intent)
//            } else {
//                showToastShort("No app found to open the link")
//            }

        }
        binding.flSocialTwitter.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/gstsuvidhakendratm"))
            startActivity(intent)


            // Check if Telegram is installed
//            val telegramInstalled = try {
//                requireActivity().packageManager.getPackageInfo("org.telegram.messenger", 0)
//                true
//            } catch (e: PackageManager.NameNotFoundException) {
//                false
//            }
//
//            if (telegramInstalled) {
//                // Open in Telegram app if installed
//                intent.setPackage("org.telegram.messenger")
//            }
//
//            try {
//                startActivity(intent)
//            } catch (e: Exception) {
//                showToastShort("Unable to open Telegram link")
//                e.printStackTrace()
//            }
        }


        binding.downloads.setOnClickListener {
            addFragment(requireActivity(), ReportDownloads(), bundleOf("reference" to "home"))
        }
        binding.earnings.setOnClickListener {
            addFragment(requireActivity(), ReportEarning())
        }

        binding.frameLeads.setOnClickListener {
            val intent = Intent(
                requireActivity(), com.android.masterdistributormdl.main.MainActivity::class.java
            )
            startActivity(intent)
//            addFragment(requireActivity(),com.android.masterdistributormdl.gskDistributor.view.home.ManageLead())
        }
//        (requireActivity() as MainActivity).checkNotifications()

    }

    private fun checkOnboarding() {
//        || !key.pan_status || !key.aadhaar_status
        val user = model.getUser()!!
        (requireActivity() as MainActivity).handleMenuVisibility()
        val key = user.kyc_status ?: return
        if (!key.basic_status || !key.address_status) {
            replaceFragment(requireActivity(), OnboardBasicDetails())
//            val intent = Intent(requireActivity(), OnboardActivity::class.java)
//            onboardLauncher.launch(intent)
        }
    }

    private val onboardLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
            } else {
                requireActivity().finish()
            }
        }

    private fun setSliderAdapter() {
        binding.viewPager.pageMargin = resources.getDimensionPixelOffset(R.dimen._10sp)
        binding.viewPager.adapter = model.sliderAdapter
        binding.viewPager.startAutoScroll()
        binding.viewPager.interval = 3000
        binding.viewPager.isCycle = true
        binding.viewPager.isStopScrollWhenTouch = true
        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int, positionOffset: Float, positionOffsetPixels: Int
            ) {

            }


            override fun onPageSelected(position: Int) {
                model.indicatorAdapter.selected = position
                model.indicatorAdapter.updateAdapter()
            }

            override fun onPageScrollStateChanged(state: Int) {

            }
        })
        binding.viewPager.currentItem = 1
    }

    override fun onResume() {
        super.onResume()
        iniApi()
    }

    private fun iniApi() {
        getUserProfile()
        dashCount()
    }

    override fun onPause() {
        super.onPause()
        stopAutoScroll()
    }

    fun stopAutoScroll() {
        if (binding.viewPager != null) binding.viewPager.stopAutoScroll()
    }

    private fun setData() {
        val user = model.getUser()!!
        binding.userName.text = user.fullname
        if (user.add_agent) {
            binding.agents.visibility = View.VISIBLE
        } else {
            binding.agents.visibility = View.GONE
        }
        (requireActivity() as MainActivity).setUserData(user)
    }

    private fun dashCount() {
        model.dashCount {
            if (it.status == 0) {
                model.sharedPreference.putString(dash_count, gson.toJson(it.data))
                setDashCount(it.data)
            }
        }
    }

    private fun setDashCount(dash: DashCount) {
        binding.earningCount.text = getPriceFormat(dash.Earning)
        binding.txtTotalEarningValue.text = getPriceFormat(dash.total_earning)
        binding.downloadCount.text = dash.Downloads.toString()
        binding.distCount.text = dash.gsk_cnt.toString()
        binding.txtLeadsCount.text = dash.lead_count.toString()
        binding.agentCount.text = dash.agent_count.toString()
        binding.shopCount.text = "${dash.Shops}"
        binding.orderCount.text = dash.Orders.toString()
        binding.agentCount.text = (dash.agent_cnt ?: dash.agent_count ?: 0).toString()
        binding.withdraw.visibility = View.VISIBLE
        if (dash.isWithdraw) {
            binding.withdrawComi.text = "Withdraw Commission"
            binding.withdrawComi.tag = "withdraw"
        } else {
            binding.withdrawComi.text = "KYC"
            binding.withdrawComi.tag = "kyc"
        }
        is_sales = dash.isAddSales
        (requireActivity() as MainActivity).setAddSale()
    }

    fun getUserProfile() {
        model.getUserProfile {
            if (it.status == 0) {
                model.save(it.data)
                setData()
                checkOnboarding()
            } else if (it.status == 100) {
                //handle maintenance
            } else {
                (requireActivity() as MainActivity).unregisterPushNotifications()
                model.sharedPreference.clearSharedPrefernce()
                showToastShort(it.message)
                val intent = Intent(
                    requireActivity(),
                    com.android.masterdistributormdl.main.SplashActivity::class.java
                )
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }

}


class HomeModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()

    val indicatorAdapter = HomeAdapter(this, 2)
    val sliderAdapter = HomeSlider()

    init {

        val json = sharedPreference.getString(OFFERS)
        if (json!!.isNotEmpty()) {
            val it: ArrayList<OfferItem> =
                gson.fromJson(json, object : TypeToken<ArrayList<OfferItem>>() {}.type)
            sliderAdapter.updateAdapter(it as ArrayList<Any>)
            indicatorAdapter.updateAdapter(it as ArrayList<Any>)
        }
    }

    fun getDashCount(): DashCount? {
        val json = sharedPreference.getString(dash_count)
        var count: DashCount? = null
        if (json!!.isNotEmpty()) {
            count = gson.fromJson(json, object : TypeToken<DashCount>() {}.type)
        }
        return count
    }

    fun save(user: User) {

        sharedPreference.putString(user_data, gson.toJson(user))

    }

    fun getUser(): User? {
        val user = sharedPreference.getUserDist()
        return user
    }

    fun getUserProfile(result: (UserResult) -> Unit) {
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))

        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getUserProfile(param).body()!!
                }
            }.onSuccess {
                result.invoke(it)
            }.onFailure {

                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }

    fun dashCount(result: (DashResult) -> Unit) {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))

        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().dashCount(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value = false
                result.invoke(it)
            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }

    fun offerimage() {
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().offerimage(param).body()!!
                }
            }.onSuccess {
                var array = ArrayList<OfferItem>()
                if (it.status == 0) {
                    array = it.data
                } else {
                    errorMessage.value = it.message
                }
                sharedPreference.putString(OFFERS, gson.toJson(array))
                sliderAdapter.updateAdapter(array as ArrayList<Any>)
                indicatorAdapter.updateAdapter(array as ArrayList<Any>)
            }.onFailure {
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }


}


