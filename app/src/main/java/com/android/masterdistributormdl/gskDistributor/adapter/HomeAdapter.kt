package com.android.masterdistributormdl.gskDistributor.adapter

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.BottomAdapterDistBinding
import com.android.masterdistributormdl.databinding.CertificateImageBinding
import com.android.masterdistributormdl.databinding.ChatAdapterBinding
import com.android.masterdistributormdl.databinding.DefaultQuotaAdapterBinding
import com.android.masterdistributormdl.databinding.DistributorAdapterBinding
import com.android.masterdistributormdl.databinding.DistributorReportAdapterBinding
import com.android.masterdistributormdl.databinding.DownloadAdapterBinding
import com.android.masterdistributormdl.databinding.DroupAdapterBinding
import com.android.masterdistributormdl.databinding.NotiAdapterBinding
import com.android.masterdistributormdl.databinding.OfferAdapterBinding
import com.android.masterdistributormdl.databinding.PendingShopAdapterBinding
import com.android.masterdistributormdl.databinding.ReportEarnAdapterBinding
import com.android.masterdistributormdl.databinding.ReportOrderAdapterBinding
import com.android.masterdistributormdl.databinding.ReportReferralAdapterBinding
import com.android.masterdistributormdl.databinding.SalesAgentAdapterBinding
import com.android.masterdistributormdl.databinding.Service1AdapterBinding
import com.android.masterdistributormdl.databinding.Service2AdapterBinding
import com.android.masterdistributormdl.databinding.Service3AdapterBinding
import com.android.masterdistributormdl.databinding.ServiceItemAdapterBinding
import com.android.masterdistributormdl.databinding.SettlementAdapterBinding
import com.android.masterdistributormdl.databinding.ShopAdapterBinding
import com.android.masterdistributormdl.databinding.ShopReportAdapterBinding
import com.android.masterdistributormdl.databinding.SliderDotAdapterBinding
import com.android.masterdistributormdl.databinding.TicketAdapterBinding
import com.android.masterdistributormdl.gskDistributor.model.referral.Data
import com.android.masterdistributormdl.gskDistributor.model.referral.ReferralEarnResult
import com.android.masterdistributormdl.gskDistributor.utils.TAG
import com.android.masterdistributormdl.gskDistributor.utils.Utils
import com.android.masterdistributormdl.gskDistributor.utils.currency
import com.android.masterdistributormdl.gskDistributor.utils.formatPrice
import com.android.masterdistributormdl.gskDistributor.utils.getHtmlSpanned
import com.android.masterdistributormdl.gskDistributor.utils.getPriceFormat
import com.android.masterdistributormdl.gskDistributor.utils.indiaDate
import com.android.masterdistributormdl.gskDistributor.utils.indiaDateComa
import com.android.masterdistributormdl.gskDistributor.utils.indiaTimeFormat
import com.android.masterdistributormdl.gskDistributor.utils.is_sales
import com.android.masterdistributormdl.gskDistributor.utils.loadImage
import com.android.masterdistributormdl.gskDistributor.utils.setImageApp
import com.android.masterdistributormdl.gskDistributor.utils.setMargins
import com.android.masterdistributormdl.gskDistributor.utils.setTextColor2
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.gsk.distributor.model.*
import com.android.masterdistributormdl.gskDistributor.view.home.NotiModel
import com.android.masterdistributormdl.gskDistributor.view.home.ReportEarningModel


class HomeAdapter(val model: ViewModel, var viewType: Int) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var arrayList = ArrayList<Any>()
    var selected = -1
    private var clickListener: ((Any) -> Unit?)? = null
    private var originalList = ArrayList<Any>()

    fun setOnclickListener(clickListener: ((Any) -> Unit?)) {
        this.clickListener = clickListener
    }

    override fun getItemViewType(position: Int): Int {
        return viewType
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter() {
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(arrayList: java.util.ArrayList<Any>) {
        this.arrayList = arrayList
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter1(arrayList: ArrayList<Any>) {
        originalList.clear()
        this.arrayList = arrayList
        originalList.addAll(arrayList)
        notifyDataSetChanged()
    }
    fun clearSearch() {
        arrayList.clear()
        arrayList.addAll(originalList)
        notifyDataSetChanged()
    }

    override fun getItemCount() = arrayList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        var holder: RecyclerView.ViewHolder? = null
        if (viewType == 1) {
            holder = BottomHolder(BottomAdapterDistBinding.inflate(inflater, parent, false))
        } else if (viewType == 2) {
            holder = SliderDotHolder(SliderDotAdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 3 || viewType == 4) {
            holder = ShopHolder(ShopAdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 6 || viewType == 23) {
            holder = ShopReportHolder(ShopReportAdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 5) {
            holder = ReportOrderHolder(ReportOrderAdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 7) {
            holder = AppDownLoadHolder(DownloadAdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 8) {
            holder = ReportEarnHolder(ReportEarnAdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 9) {
            holder = NotiHolder(NotiAdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 10) {
            holder = Service1Holder(Service1AdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 11) {
            holder = Service2Holder(Service2AdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 12) {
            holder = Service3Holder(Service3AdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 13) {
            holder = ServiceItemHolder(ServiceItemAdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 14) {
            holder = TicketHolder(TicketAdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 15) {
            holder = ChatHolder(ChatAdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 16) {
            holder = PendingShopHolder(PendingShopAdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 17) {
            holder = OfferHolder(OfferAdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 18) {
            holder = DefaultQuotaHolder(DefaultQuotaAdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 19) {
            holder = DroupHolder(DroupAdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 20) {
            holder = SettlementHolder(SettlementAdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 21) {
            holder =
                SalesAgentReportHolder(SalesAgentAdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 22) {
            holder = CertificateHolder(CertificateImageBinding.inflate(inflater, parent, false))
        } else if (viewType == 24) {
            holder = Service4Holder(Service2AdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 25) {
            holder =
                ServiceItemFinanceHolder(ServiceItemAdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 26) {
            holder = Service5Holder(Service2AdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 27) {
            holder = ServiceItem5FinanceHolder(
                ServiceItemAdapterBinding.inflate(
                    inflater,
                    parent,
                    false
                )
            )
        }
        else if (viewType == 28) {
            holder = DistributorHolder(DistributorReportAdapterBinding.inflate(inflater, parent, false))
        }
        else if (viewType == 29) {
            holder = ReportReferralHolder(ReportReferralAdapterBinding.inflate(inflater, parent, false))
        }


        return holder!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BottomHolder) holder.bindHolder(position)
        else if (holder is SliderDotHolder) holder.bindHolder(position)
        else if (holder is ShopHolder) holder.bindHolder(position)
        else if (holder is ShopReportHolder) holder.bindHolder(position)
        else if (holder is ReportOrderHolder) holder.bindHolder(position)
        else if (holder is AppDownLoadHolder) holder.bindHolder(position)
        else if (holder is ReportEarnHolder) holder.bindHolder(position)
        else if (holder is NotiHolder) holder.bindHolder(position)
        else if (holder is Service1Holder) holder.bindHolder(position)
        else if (holder is Service2Holder) holder.bindHolder(position)
        else if (holder is ServiceItemHolder) holder.bindHolder(position)
        else if (holder is Service3Holder) holder.bindHolder(position)
        else if (holder is TicketHolder) holder.bindHolder(position)
        else if (holder is ChatHolder) holder.bindHolder(position)
        else if (holder is PendingShopHolder) holder.bindHolder(position)
        else if (holder is OfferHolder) holder.bindHolder(position)
        else if (holder is DefaultQuotaHolder) holder.bindHolder(position)
        else if (holder is DroupHolder) holder.bindHolder(position)
        else if (holder is SettlementHolder) holder.bindHolder(position)
        else if (holder is SalesAgentReportHolder) holder.bindHolder(position)
        else if (holder is CertificateHolder) holder.bindHolder(position)
        else if (holder is Service4Holder) holder.bindHolder(position)
        else if (holder is ServiceItemFinanceHolder) holder.bindHolder(position)
        else if (holder is Service5Holder) holder.bindHolder(position)
        else if (holder is ServiceItem5FinanceHolder) holder.bindHolder(position)
        else if (holder is DistributorHolder) holder.bindHolder(position)
        else if (holder is ReportReferralHolder) holder.bindHolder(position)
    }

    inner class CertificateHolder(val binding: CertificateImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as Images
            val bitmap = BitmapFactory.decodeFile(item.path)
            binding.image.setImageBitmap(bitmap)
        }
    }




    inner class SalesAgentReportHolder(val binding: SalesAgentAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as SalesAgentItem
            binding.name.text = item.name
            binding.address.text = item.address
            binding.mobile.text = item.mobile
            binding.email.text = item.email
            binding.count.text = item.nofranchise
            binding.viewCertificate.setOnClickListener {
                clickListener?.invoke(item)
            }
        }
    }

    inner class SettlementHolder(val binding: SettlementAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as SettlementItem
            binding.tnxid.text = "TNX ID : " + item.txnid
            binding.settledIn.text = item.txn_type
            binding.rrn.text = item.rrn
            binding.date.text = indiaDateComa(item.txn_dt)
            binding.requestAmount.text = getPriceFormat(item.txn_amount)
            binding.transferAmount.text = getPriceFormat(item.debitamount)
            binding.serviceCharge.text = getPriceFormat(item.txn_charge)
            val status = item.txn_status
            if (status.equals("Success")) {
                binding.status.setTextColor2(("#34A853"))
            } else if (status.equals("Pending")) {
                binding.status.setTextColor2(("#F4A11E"))
            } else if (status.equals("Failed")) {
                binding.status.setTextColor2(("#F86202"))
            } else {
                binding.status.setTextColor2(("#1D3667"))
            }
            binding.status.text = status
        }
    }

    inner class DroupHolder(val binding: DroupAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as StateItem
            binding.name.text = item.gst_state
            binding.clik.setOnClickListener {
                clickListener?.invoke(item)

            }

        }
    }

    inner class DefaultQuotaHolder(val binding: DefaultQuotaAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as String
            binding.quota.text = item
            binding.clickLay.setOnClickListener {
                clickListener?.invoke(item)
                selected = position
                updateAdapter()
            }
            if (selected == position) {
                binding.clickLay.setBackgrounColor(Color.parseColor("#F86202"))
                binding.clickLay.setStrockColor(Color.parseColor("#F86202"))
                binding.quota.setTextColor2(R.color.white)
            } else {
                binding.clickLay.setBackgrounColor(Color.parseColor("#00FFFFFF"))
                binding.clickLay.setStrockColor(Color.parseColor("#CCD2E3"))
                binding.quota.setTextColor(Color.parseColor("#CC1D3667"))
            }
        }
    }

    inner class OfferHolder(val binding: OfferAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as OfferItem

            loadImage(binding.image, item.home_banner)
            binding.image.setOnClickListener {
                clickListener?.invoke(item)
            }
        }
    }

    inner class PendingShopHolder(val binding: PendingShopAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as ShopItem
            binding.shop.text = item.name_of_shop
            binding.address.text = item.address
            binding.name.text = item.contact_person
            binding.mobile.text = item.mobile
            if (is_sales) {
                binding.addedByLay.visibility = View.VISIBLE
            } else {
                binding.addedByLay.visibility = View.GONE
            }
            binding.addedBy.text = item.addedby
            binding.date.text = indiaDate(item.date_of_activation)
            binding.clients.text = item.no_of_clients
            binding.orders.text = item.no_of_orders
            binding.shopid.text = item.shopid
            binding.earning.text = currency + (item.earning)

            binding.click.setOnClickListener {
                clickListener?.invoke(item)
            }
        }
    }

    inner class ChatHolder(val binding: ChatAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as TicketReplies
            binding.message.text = getHtmlSpanned(item.message).trim()
            binding.date.text = indiaTimeFormat(item.message_dt)
            val userType = item.replytype
            val margin40 = binding.clickLay.resources.getDimension(R.dimen._40sp).toInt()
            val margin20 = binding.clickLay.resources.getDimension(R.dimen._20sp).toInt()
            if (userType == "USER") {
                binding.clickLay.setBackgrounColor(Color.parseColor("#F0EEEF"))
                binding.mainLayout.gravity = Gravity.END
                binding.clickLay.setMargins(left = margin40, right = margin20)
                binding.clickLay.setRadius(margin20, 0, margin20, margin20)
            } else {
                binding.clickLay.setBackgrounColor(Color.parseColor("#FBF3DA"))
                binding.mainLayout.gravity = Gravity.START
                binding.clickLay.setMargins(left = margin20, right = margin40)
                binding.clickLay.setRadius(0, margin20, margin20, margin20)
            }
            if (item.attachment.isEmpty()) {
                binding.attachmentLay.visibility = View.GONE
            } else {
                binding.attachmentLay.visibility = View.VISIBLE
            }

            binding.attachmentLay.setOnClickListener {
                clickListener?.invoke(item)
            }

        }
    }

    inner class TicketHolder(val binding: TicketAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as TicketItem
            binding.type.text = item.issue
            binding.subject.text = "Subject: " + item.subject
            binding.ticketId.text = item.ticket_id
            binding.date.text = indiaDate(item.create_dt)

            val status_ = item.status
            if (status_.equals("2")) {
                binding.status.text = "Closed"
                binding.status.setTextColor2("#F82002")
            } else {
                binding.status.text = "Active"
                binding.status.setTextColor2("#34A853")
            }
            binding.viewDetails.setOnClickListener {
                clickListener?.invoke(item)
            }

        }
    }

    inner class ServiceItemHolder(val binding: ServiceItemAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as ServicesItem2
            binding.name.text = item.name

            binding.price.text = formatPrice(item.md_commission)
            binding.comi.text = formatPrice(item.ret_commission)
            binding.customerPrice.text = formatPrice(item.customer_price)
            binding.distComi.text = formatPrice(item.dist_commission)

//            binding.price.text = getPriceFormat(item.md_commission)
//            binding.comi.text = getPriceFormat(item.ret_commission)
//            binding.customerPrice.text = getPriceFormat(item.customer_price)
//            binding.distComi.text = getPriceFormat(item.dist_commission)
        }
    }

    inner class ServiceItemFinanceHolder(val binding: ServiceItemAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as ServicesItem2
            binding.name.text = item.name
            binding.price.visibility = View.VISIBLE
            binding.llPrice.visibility = View.VISIBLE
            binding.customerPrice.text = formatPrice(item.customer_price)
            binding.price.text = formatPrice(item.gsk_commission)
            binding.comi.text = formatPrice(item.shop_commission)
        }
    }

    inner class ServiceItem5FinanceHolder(val binding: ServiceItemAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as ServicesItem2
            binding.name.textAlignment = View.TEXT_ALIGNMENT_TEXT_START
            binding.name.text = item.name
            binding.price.visibility = View.GONE
            binding.view1.visibility = View.GONE
            binding.llPrice.visibility = View.GONE
            binding.view2.visibility = View.VISIBLE
            binding.price.text = getPriceFormat(item.mrp)
            binding.comi.visibility = View.GONE
            binding.comi.text = getPriceFormat(item.gsk_commission)
        }
    }


    inner class Service3Holder(val binding: Service3AdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindHolder(position: Int) {
            val item = arrayList[position] as ServicesItem2
            binding.name.text = item.name
            binding.txtCommission.text = formatPrice(item.md_commission)
            binding.txtPrice.text = formatPrice(item.customer_price)
            binding.txtShopCommission.text = formatPrice(item.ret_commission)
            binding.txtDistCommission.text = formatPrice(item.dist_commission)
            binding.clickLay.setOnClickListener {
            }
        }
    }

    inner class Service2Holder(val binding: Service2AdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as ServicesItem
            binding.name.text = item.head
            binding.line.visibility = View.GONE
            binding.recyclerView.visibility = View.GONE
            binding.headerLay.visibility = View.GONE
            binding.indicator.rotation = 180f
            binding.recyclerView.adapter = null
            binding.clickLay.setOnClickListener {
                if (selected > -1) {
                    notifyItemChanged(selected)
                }
                if (selected == position) {
                    selected = -1
                } else {
                    selected = position
                    binding.indicator.rotation = 0f
                    binding.line.visibility = View.VISIBLE
                    binding.headerLay.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.VISIBLE

                    val serviceItemAdapter = HomeAdapter(model, 13)
                    binding.recyclerView.adapter = serviceItemAdapter
                    serviceItemAdapter.updateAdapter(item.data as ArrayList<Any>)
                }
            }

        }
    }


    inner class Service4Holder(val binding: Service2AdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as ServicesItem
            binding.name.text = item.head
            binding.line.visibility = View.VISIBLE
            binding.txtPrice.visibility = View.VISIBLE
            binding.txtCost.visibility = View.VISIBLE
            binding.indicator.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            binding.headerLay.visibility = View.VISIBLE
            binding.indicator.rotation = 180f
            binding.recyclerView.adapter = null
            val serviceItemAdapter = HomeAdapter(model, 25)
            binding.recyclerView.adapter = serviceItemAdapter
            serviceItemAdapter.updateAdapter(item.data as ArrayList<Any>)
            binding.clickLay.setOnClickListener {
                if (selected > -1) {
                    notifyItemChanged(selected)
                }
                if (selected == position) {
                    selected = -1
                } else {
                    selected = position
                    binding.indicator.rotation = 0f
                    binding.line.visibility = View.VISIBLE
                    binding.headerLay.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.VISIBLE


                }
            }

        }
    }

    inner class Service5Holder(val binding: Service2AdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as ServicesItem
            binding.name.text = item.head
            binding.line.visibility = View.VISIBLE
            binding.txtCost.visibility = View.GONE
            binding.txtServices.visibility = View.GONE
            binding.txtPrice.visibility = View.GONE
            binding.txtCommission.visibility = View.GONE
            binding.ivRupee.visibility = View.GONE
            binding.indicator.visibility = View.GONE
            binding.recyclerView.visibility = View.VISIBLE
            binding.headerLay.visibility = View.GONE
            binding.indicator.rotation = 180f
            binding.recyclerView.adapter = null
            val serviceItemAdapter = HomeAdapter(model, 27)
            binding.recyclerView.adapter = serviceItemAdapter
            serviceItemAdapter.updateAdapter(item.data as ArrayList<Any>)
            binding.clickLay.setOnClickListener {
                if (selected > -1) {
                    notifyItemChanged(selected)
                }
                if (selected == position) {
                    selected = -1
                } else {
                    selected = position
                    binding.indicator.rotation = 0f
                    binding.line.visibility = View.VISIBLE
                    binding.headerLay.visibility = View.VISIBLE
                    binding.recyclerView.visibility = View.VISIBLE


                }
            }

        }
    }

    inner class Service1Holder(val binding: Service1AdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as ServicesItem2
            binding.name.text = item.name
            binding.customerPrice.text = getPriceFormat(item.customer_price)
            binding.shopComi.text = getPriceFormat(item.md_commission)
            binding.yourComi.text = getPriceFormat(item.md_commission)
            binding.distComi.text = getPriceFormat(item.dist_commission)
            binding.retailerComi.text = getPriceFormat(item.ret_commission)
            binding.basePrice.text = getPriceFormat(item.mrp)
            binding.shopYou.text = getPriceFormat(item.shop_commission + item.gsk_commission)
            binding.detailClick1.visibility = View.GONE
            binding.detailClick2.visibility = View.GONE
            binding.moreLay.visibility = View.GONE
            if (item.commission_type == "PERCENT") {
                binding.editPrice.visibility = View.VISIBLE
            } else {
                binding.editPrice.visibility = View.INVISIBLE
            }
            fun click() {
                if (selected > -1) {
                    notifyItemChanged(selected)
                }
                if (selected == position) {
                    selected = -1
                } else {
                    selected = position
                    binding.moreLay.visibility = View.VISIBLE
                    binding.detailClick1.visibility = View.VISIBLE
                    binding.detailClick2.visibility = View.GONE
                }
            }
            binding.detailClick1.setOnClickListener {
                click()
            }
            binding.detailClick2.setOnClickListener {
                click()
            }
            if (item.isedit) {
                binding.editPrice.visibility = View.VISIBLE
            } else {
                binding.editPrice.visibility = View.GONE
            }
            binding.editPrice.setOnClickListener {
                if (item.isedit) {
                    clickListener?.invoke(item)
                } else {
                    showToastShort("")
                }
            }
        }
    }

    inner class NotiHolder(val binding: NotiAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as PushItem
            binding.message.text = getHtmlSpanned(item.message).trim()
            binding.status.text = item.type_status
            binding.date.text = Utils.timeToAgo(item.create_dt)
            if (item.status == 1) {
                binding.clickLay.setBackgroundColor(Color.parseColor("#FFFFFF"))
            } else {
                binding.clickLay.setBackgroundColor(Color.parseColor("#F7F7F9"))
            }

            binding.clickLay.setOnClickListener {
                clickListener?.invoke(item)
                if (item.status == 0) {
                    (model as NotiModel).pushNotificationRead(item.id)
                    item.status = 1
                    notifyItemChanged(position)
                }
            }


        }
    }

    inner class ReportEarnHolder(val binding: ReportEarnAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as ReportEarnItem
            binding.shopId.text = "Distributor ID:  ${item.distributor_id}"
            binding.shop.text = "Distributor Name :  ${item.distributor_name}"
            binding.orderId.text = "#" + item.order_num
            binding.text.text = "Customer ID : " + item.customerid + " | " + indiaDate(item.date)
            binding.orderAmt.text = currency + (item.order_amount)
            binding.orderDate.text = (item.date)
            binding.amtNoGst.text = currency + (item.order_amt_without_gst)
            binding.shopComi.text = currency + (item.shopcommission)
            binding.proComi.text = currency + (item.distributor_commission)
            binding.earn.text = currency + (item.earning)
            val model = (model as ReportEarningModel)
            if (model.type == 1) {
                binding.earnType.text = "Your Earning :"
            } else {
                binding.earnType.text = "Your Future Earning :"
            }
        }
    }

    inner class ReportReferralHolder(val binding: ReportReferralAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as Data
            binding.orderDate.text = (item.date)
            binding.referralName.text = (item.referral_name)
            binding.referralId.text = (item.referral_id)
            binding.referralType.text = (item.referral_type)
            binding.referralName.text = (item.referral_name)
            binding.earn.text = currency + (item.earning)
        }
    }

    inner class AppDownLoadHolder(val binding: DownloadAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as ReportDownlaodItem
            binding.shop.text = item.shopname
            binding.name.text = item.customername
            binding.distributorName.text = item.distrubutor_name
            binding.retailerName.text = item.retailer_name
            binding.date.text = indiaDate(item.date_of_activation)
            binding.customerId.text = item.customerid
            binding.customerNumber.text = item.customermobile
            binding.orders.text = item.no_of_orders
            binding.earning.text = currency + (item.earning)
        }
    }



    inner class ReportOrderHolder(val binding: ReportOrderAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as ReportOrderItem
            binding.orderId.text = "ID: #" + item.order_num
            binding.status.text = "● " + item.order_status
            binding.shopid.text = item.shopid
            binding.orderName.text = item.order_name
            binding.amount.text = currency + item.subtotal
            binding.gst.text = currency + item.gst
            binding.totalAmount.text = item.debitamount
            binding.customerId.text = item.customerid
            binding.name.text = item.distributor_name
            binding.date.text = indiaDate(item.order_date)
            binding.earning.text = currency + item.distributor_commission
            binding.txtRetailerCommission.text = currency + item.retailer_commission
            binding.txtYourCommission.text = currency + item.earning
            if (item.order_status == "Completed") {
                binding.status.setTextColor2("#34A853")
            } else {
                binding.status.setTextColor2("#B31D3667")
            }
        }
    }

    inner class ShopReportHolder(val binding: ShopReportAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as ShopItem
            binding.shop.text = item.name_of_shop
            binding.address.text = item.address
            binding.name.text = item.contact_person
            binding.distName.text = item.distributor_name
            binding.mobile.text = item.mobile
            if (is_sales && viewType == 6) {
                binding.addedByLay.visibility = View.VISIBLE
            } else {
                binding.addedByLay.visibility = View.GONE
            }
            binding.addedBy.text = item.addedby
            binding.date.text = indiaDate(item.date_of_activation)
            binding.clients.text = item.no_of_clients
            binding.shopid.text = item.shopid
            binding.orders.text = item.no_of_orders
            binding.earning.text = currency + (item.earning)
            val status = item.status
            if (status.equals("1")) {
                binding.status.text = "● " + "Active"
                binding.status.setTextColor2("#34A853")
            } else {
                binding.status.text = "● " + "Deactivate"
                binding.status.setTextColor2("#B31D3667")
            }
            binding.viewCertificate.setOnClickListener {
                item.countCustomer=false
                clickListener?.invoke(item)
            }

            binding.clients.setOnClickListener {
                item.countCustomer=true
                clickListener?.invoke(item)
            }

        }
    }

    inner class ShopHolder(val binding: ShopAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as ShopItem
            binding.shop.text = item.name_of_shop
            binding.address.text = item.address
            binding.name.text = item.contact_person
            binding.mobile.text = item.mobile
            binding.addedBy.text = item.addedby
            if (is_sales) {
                binding.addedByLay.visibility = View.VISIBLE
            } else {
                binding.addedByLay.visibility = View.GONE
            }
            binding.date.text = indiaDate(item.date_of_activation)
            binding.clients.text = item.no_of_clients
            binding.orders.text = item.no_of_orders
            binding.shopid.text = item.shopid
            binding.earning.text = currency + (item.earning)
            if (viewType == 3) {
                binding.deactivate.visibility = View.GONE
                binding.viewCertificate.visibility = View.VISIBLE
                val status = item.status
                if (status.equals("1")) {
                    binding.status.text = "● " + "Active"
                    binding.status.setTextColor2("#34A853")
                } else {
                    binding.status.text = "● " + "Deactivate"
                    binding.status.setTextColor2("#B31D3667")
                }
                binding.viewCertificate.setOnClickListener {
                    clickListener?.invoke(item)
                }
            } else if (viewType == 4) {
                binding.status.visibility = View.GONE
                binding.deactivate.visibility = View.VISIBLE
                binding.viewCertificate.visibility = View.GONE
                if (item.isrequest == "1") {
                    binding.deactivate.text = "Request Pending"
                    binding.deactivate.isEnabled = false
                } else {
                    binding.deactivate.text = "Deactivate"
                    binding.deactivate.isEnabled = true
                }
            }

            binding.deactivate.setOnClickListener {
                clickListener?.invoke(item)
            }
        }
    }

    inner class DistributorHolder(val binding: DistributorReportAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as ShopItem
            binding.shop.text = item.name_of_shop
            binding.address.text = item.address
            binding.name.text = item.contact_person
            binding.retailers.text = item.retailer_count
            binding.email.text = item.email
            binding.txtViewCertificate.visibility=View.GONE
            binding.ivArrowLeft.visibility=View.GONE
            binding.mobile.text = item.mobile
            if (is_sales && viewType == 6) {
                binding.addedByLay.visibility = View.VISIBLE
            } else {
                binding.addedByLay.visibility = View.GONE
            }
            binding.addedBy.text = item.addedby
            binding.date.text = indiaDate(item.date_of_activation)
            binding.clients.text = item.no_of_clients
            binding.shopid.text = item.shopid
            binding.orders.text = item.no_of_orders
            binding.earning.text = currency + (item.earning)
            val status = item.status
            if (status.equals("1")) {
                binding.status.text = "● " + "Active"
                binding.status.setTextColor2("#34A853")
            } else {
                binding.status.text = "● " + "Deactivate"
                binding.status.setTextColor2("#B31D3667")
            }

            if (item.training_status) {
                binding.trainingStatus.text = "Yes"
                binding.infoIcon.visibility = View.GONE
                binding.updateTraining.visibility = View.GONE
            } else {
                binding.trainingStatus.text = "No"
                if (item.training_remark.isNotEmpty()){
                    binding.infoIcon.visibility = View.VISIBLE
                }


                binding.updateTraining.visibility = View.VISIBLE
            }

            binding.infoIcon.setOnClickListener {
                item.isInfoClick = true
                item.isUpdateTrainingClick = false
                item.countCustomer = false
                clickListener?.invoke(item)
            }

            binding.updateTraining.setOnClickListener {
                item.isInfoClick = false
                item.isUpdateTrainingClick = true
                item.countCustomer = false
                clickListener?.invoke(item)
            }

            binding.viewCertificate.setOnClickListener {
                item.countCustomer = false
                item.isInfoClick = false
                item.isUpdateTrainingClick = false
                clickListener?.invoke(item)
            }

            binding.clients.setOnClickListener {
                item.countCustomer = true
                item.isInfoClick = false
                item.isUpdateTrainingClick = false
                clickListener?.invoke(item)
            }

        }
    }

    inner class BottomHolder(val binding: BottomAdapterDistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as? BottomMenu ?: return
            binding.name.text = item.name
            if (selected == item.id) {
                binding.image.setImageApp(item.icon + "2")
                binding.name.setTextColor(Color.parseColor("#1D3667"))
            } else {
                binding.image.setImageApp(item.icon)
                binding.name.setTextColor(Color.parseColor("#801D3667"))
            }
            binding.click.setOnClickListener {
                selected = item.id
                updateAdapter()
                clickListener?.invoke(item)
            }
        }
    }

    inner class SliderDotHolder(val binding: SliderDotAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {

            if (selected == position) {
                binding.image.setImageResource(R.drawable.orange_dot)
            } else {
                binding.image.setImageResource(R.drawable.inactive_dot)

            }

        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filter(statusList: List<String>?) {
        val filteredList = ArrayList<Any>()
        if (statusList.isNullOrEmpty()) {
            filteredList.addAll(originalList)
        } else {
            if (viewType == 28) {
                for (item in originalList) {
                    if (item is ReferListData) {
                        if (statusList.any { status ->
                                item.statusname.lowercase().contains(status.lowercase(), ignoreCase = true)
                            }) {
                            filteredList.add(item)
                        }
                    }
                }
            }

        }
        arrayList = filteredList
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun searchFilter(query: String?) {
        val filteredList = ArrayList<Any>()
        Log.d(TAG, "searchFilter: $query")
        if (query.isNullOrEmpty()) {
            filteredList.addAll(originalList)
        } else {
            if (viewType == 6) {
                for (item in originalList) {
                    if (item is ShopItem) {
                        if (item.contact_person.lowercase().contains(query, ignoreCase = true)) {
                            filteredList.add(item)
                        }
                    }
                }
            }
            else if (viewType == 7) {
                for (item in originalList) {
                    if (item is ReportDownlaodItem) {
                        if (item.customername.lowercase().contains(query, ignoreCase = true)) {
                            filteredList.add(item)
                        }
                    }
                }
            }

            else if (viewType == 8) {
                for (item in originalList) {
                    if (item is ReportEarnItem) {
                        if (item.distributor_name.lowercase().contains(query, ignoreCase = true)) {
                            filteredList.add(item)
                        }
                    }
                }
            }
            else if (viewType == 5) {
                for (item in originalList) {
                    if (item is ReportOrderItem) {
                        if (item.fullname.lowercase().contains(query, ignoreCase = true)) {
                            filteredList.add(item)
                        }
                    }
                }
            }
            if (viewType == 28) {
                for (item in originalList) {
                    if (item is ShopItem) {
                        if (item.contact_person.lowercase().contains(query, ignoreCase = true)) {
                            filteredList.add(item)
                        }
                    }
                }
            }

            else if (viewType == 29) {
                for (item in originalList) {
                    if (item is Data) {
                        if (item.referral_name.lowercase().contains(query, ignoreCase = true)) {
                            filteredList.add(item)
                        }
                    }
                }
            }

        }
        arrayList = filteredList
        notifyDataSetChanged()
    }

}