package com.gsk.distributor.model

import com.android.masterdistributormdl.gskDistributor.model.User
import com.android.masterdistributormdl.model.territory.Territory
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ErrorAlert(val status: Int, val message: String)
data class OfferResult(
    val `data`: ArrayList<OfferItem>, val message: String, val status: Int
) : Serializable


data class QuotaCreateResult(
    val `data`: CreateData, val message: String, val status: Int
)

data class CertificateUrl(
    val status: Int,
    val message: String,
    val uri: String?
) : Serializable

data class BusinessUrl(
    val status: Int,
    val message: String,
    val dataurl: String?
) : Serializable

data class BusinessProposalData(
    val status: Int,
    val message: String,
    val proposal_url: String,
    val demo_url: String,
    val description: String,
) : Serializable

data class UploadVideo(
    val videofile: String,
    val uid: String,
): Serializable

data class CreateData(
    val amount: String,
    val environment: String,
    val mid: String,
    val orderid: String,
    val ref_id: String, val
    callbackUrl: String,
    val token: String
) : Serializable

data class AccountDetailsResult(
    val status: Int, val message: String,val account_details:AccountDetailsData
) : Serializable

data class TrainingStatusResult(
    val status: Int, val message: String,val data:TrainingStatusData
) : Serializable

data class TrainingStatusData(
    val day_1: String,
    val day_2: String,
    val day_3: String,
    val day_4: String,
    val day_5: String,
    val day_6: String,
    val day_7: String
) : Serializable


data class AccountDetailsData(
    val account_number: String,val account_name:String,val ifsc_code:String
) : Serializable

data class AppUpdateResult(
    val status: Int, val message: String, val app_version: Int, val app_link: String
) : Serializable

data class OfferItem(
    val id: String,
    val home_banner: String,
    val banner: String,
    val title: String,
    val url: String,
    val description: String
) : Serializable

data class LoginResult(
    val status: Int, val message: String, val email: String
) : Serializable

data class ApiResponse(
    val status: Int, val message: String,
) : Serializable



data class ApiResponseAadhar(
    val status: Int, val message: String,val ref_id: String,
) : Serializable

data class ApiResponse1(
    val status: Int, val message: String,val branch_id:Int
) : Serializable


data class AddShopResult(
    val status: Int, val shopid: String, val message: String
) : Serializable

data class VerifyResult(
    val status: Int, val message: String, val data: VerifyData
) : Serializable

data class VerifyData(
    val userid: String,
    val session_id: String,
) : Serializable

data class DashResult(
    val status: Int, val message: String, val data: DashCount
) : Serializable

data class Item(
    val id: String,
    val name: String,
) : Serializable

data class DashCount(
    val Earning: Double,
    val total_earning: Double,
    val Downloads: Int,
    val Shops: Int,
    val gsk_cnt: Int,
    val lead_count: Int,
    val isWithdraw: Boolean,
    val isAddSales: Boolean,
    val Orders: Int,
    val agent_cnt: Int? = 0,
    val agent_count: Int? = 0
) : Serializable

data class UserResult(
    val status: Int, val message: String, val data: User
) : Serializable

data class PincodeResult(
    val message: String,
    val status: Int,
    val territory: ArrayList<com.android.masterdistributormdl.gskDistributor.model.Territory>
): Serializable


data class PANData(var pan_no: String, var dob: String) : Serializable
data class BottomMenu(
    val id: Int, val name: String, val icon: String
) : Serializable


data class TicketsResult(
    val `data`: ArrayList<TicketItem>, val message: String, val status: Int
) : Serializable

data class TicketItem(
    val id: String,
    val create_dt: String,
    val issue: String,
    val status: String,
    val subject: String,
    val message: String,
    val ticket_id: String,
    val attachment: String
) : Serializable

data class TicketTypeResult(
    val `data`: ArrayList<String>, val message: String, val status: Int
) : Serializable

data class TicketReplies(
    val message: String,
    val message_dt: String,
    val reply_id: String,
    val attachment: String,
    val replytype: String,
) : Serializable

data class TicketResult(
    val ticketdata: TicketItem,
    val ticketdetail: ArrayList<TicketReplies>,
    val message: String,
    val status: Int
) : Serializable

data class ShopsResult(
    val `data`: List<ShopItem>, val message: String, val status: Int
) : Serializable

data class ShopItem(
    val id: String,
    val address: String,
    val contact_person: String,
    val retailer_count: String,
    val email: String,
    val distributor_name: String,
    val date_of_activation: String,
    val earning: String, val addedby: String,
    val mobile: String,
    val shopid: String,
    val name_of_shop: String,
    val no_of_clients: String,
    val no_of_orders: String,
    val status: String,
    val isrequest: String,
    var countCustomer: Boolean,
    var training_status: Boolean,
    var training_remark: String,
    var isInfoClick: Boolean = false,
    var isUpdateTrainingClick: Boolean = false,
) : Serializable

data class ReportOrderResult(
    val status: Int,
    val message: String,
    val `data`: List<ReportOrderItem>,
) : Serializable

data class ReportOrderItem(
    val branchid: String,
    val earning: String,
    val distributor_name: String,
    val distributor_commission: String,
    val retailer_commission: String,
    val fullname: String,
    val order_date: String,
    val order_status: String,
    val shopid: String,
    val order_num: String,
    val order_name: String,
    val gst: String,
    val subtotal: String,
    val debitamount: String,
    val customerid: String
) : Serializable


data class SalesAgentResult(
    val status: Int,
    val message: String,
    val `data`: List<SalesAgentItem>,
) : Serializable

data class SalesAgentItem(
    val id: String,
    val name: String,
    val email: String,
    val mobile: String,
    val address: String,
    val create_dt: String,
    val nofranchise: String,
) : Serializable

data class ReportDownLoadResult(
    val status: Int,
    val message: String,
    val `data`: List<ReportDownlaodItem>,
) : Serializable

data class ReportDownlaodItem(
    val date_of_activation: String,
    val customername: String,
    val distrubutor_name: String,
    val retailer_name: String,
    val customermobile: String,
    val customerid: String,
    val shopname: String,
    val no_of_orders: String,
    val earning: String,
) : Serializable


data class ReferListResult(
    val status: Int,
    val message: String,
    val `userdata`: List<ReferListData>,
) : Serializable



data class ReferListData(
    val name: String,
    val mobile: String,
    val type: String,
    val status: String,
    val statusname: String,
) : Serializable

data class TrainingVideoListResult(
    val status: Int,
    val message: String,
    val `data`: List<TrainingVideoListData>,
) : Serializable

data class TrainingVideoListData(
    val title: String,
    val url: String,
    val thumbnail: String,
) : Serializable

data class ReportEarnResult(
    val status: Int,
    val message: String,
    val earning: String,
    val `data`: List<ReportEarnItem>,
) : Serializable

data class ReportEarnItem(
    val customerid: String,
    val date: String,
    val earning: String,
    val order_amount: String,
    val order_amt_without_gst: String,
    val prologiccommission: String,
    val shopcommission: String,
    val distributor_commission: String,
    val order_num: String,
    val shopname: String,
    val distributor_name: String,
    val distributor_id: String,
    val shopid: String
) : Serializable

data class ShopQuotaResult(
    val status: Int, val message: String, val `data`: ShopQuota?
) : Serializable

data class ShopQuota(
    val available: Int,
    val registered: Int,
    val unlimited: Boolean,
    val shop_price: Double,
    val gatewayCharge: Double,
) : Serializable

data class StateResult(
    val status: Int, val message: String, val state: ArrayList<StateItem>
) : Serializable

data class StateItem(
    val gst_code: String,
    val gst_state: String,
) : Serializable


/*---------------------------------------------*/
data class ServicesResult(
    val status: Int, val message: String, val `data`: List<ServicesItem>
) : Serializable

data class ServicesItem(
    val head: String, val `data`: List<ServicesItem2>
) : Serializable

data class ServicesItem2(
    val id: String,
    val name: String,
    val mrp: Double,
    val min: Double,
    val max: Double, val isedit: Boolean,
    val max_shop_commission: Double,
    val min_shop_commission: Double,
    val commission_type: String,
    val commission_value: Double,
    val customer_price: Double,
    val shop_commission: Double,
    val dist_commission: Double,
    val ret_commission: Double,
    val md_commission: Double,
    val gsk_commission: Double,
) : Serializable {

}


class PdfImages : ArrayList<Images>(), Serializable

class Images(
    val position: Int, val path: String
) : Serializable


data class PromotionalImages(
    val `data`: ArrayList<PromotionalImagesData>,
    val message: String,
    val status: Int
) : Serializable

data class PromotionalImagesData(
    val title: String,
    val link: String,
    val id: String,
    val sharelink: String,
) : Serializable




data class SettlementResult(
    val `data`: ArrayList<SettlementItem>,
    val message: String,
    val status: Int
) : Serializable

data class VideoKyc(
    val `data`: VideoKycData,
    val message: String,
    val status: Int
) : Serializable

data class VideoKycData(
    val english: String,
    val hindi: String,
) : Serializable


data class SettlementItem(
    val txnid: String,
    val txn_dt: String,
    val txn_type: String, val rrn: String,
    val txn_amount: Double, val txn_charge: Double, val debitamount: Double,
    val txn_status: String,
) : Serializable

data class AgentListResult(
    val status: Int,
    val message: String,
    val data: List<AgentItem>
) : Serializable

data class AgentItem(
    val id: String,
    @SerializedName("fullname")
    val name: String,
    val email: String,
    val mobile: String,
    val pincode: String,
    val address: String,
    val create_dt: String? = null
) : Serializable