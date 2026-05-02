package com.android.masterdistributormdl.gskDistributor.network

import com.android.masterdistributormdl.gskDistributor.model.IfscResult
import com.android.masterdistributormdl.gskDistributor.model.NotificationSettingsModel
import com.android.masterdistributormdl.gskDistributor.model.consultant.ConsultantResult
import com.google.gson.JsonObject
import com.gsk.distributor.model.*
import com.android.masterdistributormdl.gskDistributor.model.financialServices.FinancialResult
import com.android.masterdistributormdl.gskDistributor.model.referral.ReferralEarnResult

import com.android.masterdistributormdl.gskDistributor.utils.URL_PATH
import com.android.masterdistributormdl.gskDistributor.utils.URL_PATH_START
import okhttp3.MultipartBody
import okhttp3.RequestBody

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.*


interface RetrofitUrl {
    @POST("gskfranchise/applogin/verifygskuser")
    suspend fun getLogin(
        @Body param: JsonObject
    ): Response<LoginResult>

    @POST("$URL_PATH_START/$URL_PATH/saveContacts")
    suspend fun getSaveContact(
        @Body param: JsonObject
    ): Response<ApiResponse>

    @POST("$URL_PATH_START/$URL_PATH/fcm_token")
    suspend fun saveFcmToken(
        @Body param: JsonObject
    ): Response<ApiResponse>


    ///referral earn adapter
    @POST("$URL_PATH_START/$URL_PATH/referral_earning")
    suspend fun referralEarn(
        @Body param: JsonObject
    ): Response<ReferralEarnResult>

    @POST("$URL_PATH_START/$URL_PATH/referral_earning_potential")
    suspend fun referralEarnPotential(
        @Body param: JsonObject
    ): Response<ReferralEarnResult>

    @POST("gskfranchise/applogin/verifygskotp")
    suspend fun getVerify(
        @Body param: JsonObject
    ): Response<VerifyResult>

    //refer link
    @POST("services/app_refer_link/{uid}/{type}")
    suspend fun getReferrerUrl(@Path("uid") parentId: String,@Path("type") type: String): Response<String>

    @POST("$URL_PATH_START/$URL_PATH/userprofile")
    suspend fun getUserProfile(
        @Body param: JsonObject
    ): Response<UserResult>

    @POST("$URL_PATH_START/$URL_PATH/verify_mobile")
    suspend fun verifyMobile(
        @Body param: JsonObject
    ): Response<com.android.masterdistributormdl.model.ApiResponse>


    //old
   /* @POST("$URL_PATH_START/$URL_PATH/uploadprofileimg")
    suspend fun uploadprofileimg(
        @Body param: JsonObject
    ): Response<ApiResponse>*/

    //new
     @POST("appapi/uploadprofileimg")
    suspend fun uploadprofileimg(
        @Body param: JsonObject
    ): Response<ApiResponse>

    //get account details
    //old
//    @POST("$URL_PATH_START/$URL_PATH/getAccountDetails")
//    suspend fun getAccountDetails(
//        @Body param: JsonObject
//    ): Response<AccountDetailsResult>

    //new
    @POST("appapi/getAccountDetails")
    suspend fun getAccountDetails(
        @Body param: JsonObject
    ): Response<AccountDetailsResult>


//    @POST("$URL_PATH_START/$URL_PATH/getPinLocation")
//    suspend fun getPinLocation(
//        @Body param: JsonObject
//    ): Response<JsonObject>

    @POST("appapi/getPinLocation")
    suspend fun getPinLocation(
        @Body param: JsonObject
    ): Response<JsonObject>

    //aadhar otp
    @POST("$URL_PATH_START/$URL_PATH/verify_aadhaar")
    suspend fun aadharOTPVerify(
        @Body param: JsonObject
    ): Response<ApiResponseAadhar>

    @Multipart
    // Ensure this annotation is present
    @POST("$URL_PATH_START/$URL_PATH/uploadvideokyc")
    suspend fun getUploadVideo(
//        @Body param: UploadVideo
        @Part videofile: MultipartBody.Part,
        // Prepare UID
        @Part("uid") uid: RequestBody,
    ): Response<ApiResponse>

    @POST("$URL_PATH_START/$URL_PATH/uploadkycvideo")
    suspend fun getUploadVideoKyc(
        @Body param: JsonObject
    ): Response<ApiResponse>

    @POST("$URL_PATH_START/$URL_PATH/video_kyc_text")
    suspend fun videoKycText(
        @Body param: JsonObject
    ): Response<VideoKyc>

    @POST("$URL_PATH_START/$URL_PATH/shopquota")
    suspend fun getShopQuota(
        @Body param: JsonObject
    ): Response<ShopQuotaResult>

    @POST("$URL_PATH_START/$URL_PATH/initretailerToken")
    suspend fun initShopToken(
        @Body param: JsonObject
    ): Response<QuotaCreateResult>


    @POST("$URL_PATH_START/$URL_PATH/generateshopCerticate")
    suspend fun generateshopCerticate(
        @Body param: JsonObject
    ): Response<JsonObject>

    @POST("$URL_PATH_START/$URL_PATH/generateshopCerticate")
    suspend fun generateShopCertificateDist(
        @Body param: JsonObject
    ): Response<CertificateUrl>

    @POST("$URL_PATH_START/$URL_PATH/generateMFCerticate")
    suspend fun generateMFCerticate(
        @Body param: JsonObject
    ): Response<CertificateUrl>

    @POST("$URL_PATH_START/$URL_PATH/business_proposal")
    suspend fun generateBusinessProposal(
        @Body param: JsonObject
    ): Response<BusinessProposalData>

    @POST("$URL_PATH_START/$URL_PATH/retailerdetail")
    suspend fun getShops(
        @Body param: JsonObject
    ): Response<ShopsResult>

    @POST("$URL_PATH_START/$URL_PATH/distributordetail")
    suspend fun getDistributor(
        @Body param: JsonObject
    ): Response<ShopsResult>

    @POST("$URL_PATH_START/$URL_PATH/{end}")
    suspend fun deactiverequestshop(
        @Path("end") end: String,
        @Body param: JsonObject
    ): Response<ShopsResult>


    @POST("$URL_PATH_START/$URL_PATH/getblockstatus")
    suspend fun getBlockStatus(
        @Body param: JsonObject
    ): Response<ApiResponse1>

    @POST("$URL_PATH_START/$URL_PATH/unblock_crm_user")
    suspend fun getUnblockStatus(
        @Body param: JsonObject
    ): Response<ApiResponse>

    @POST("$URL_PATH_START/$URL_PATH/filtershopdetail")
    suspend fun getShopsBySearch(
        @Body param: JsonObject
    ): Response<ShopsResult>

    @POST("$URL_PATH_START/$URL_PATH/deactivateshop")
    suspend fun deleteShop(
        @Body param: JsonObject
    ): Response<ApiResponse>

    @POST("$URL_PATH_START/$URL_PATH/addshopuser")
    suspend fun addShop(
        @Body param: JsonObject
    ): Response<AddShopResult>

    @POST("$URL_PATH_START/$URL_PATH/create_distributor")
    suspend fun addDistributor(
        @Body param: JsonObject
    ): Response<ApiResponse>

    @POST("$URL_PATH_START/$URL_PATH/add_sales_agent")
    suspend fun addSalesAgent(
        @Body param: JsonObject
    ): Response<ApiResponse>


    @POST("$URL_PATH_START/$URL_PATH/refergsk")
    suspend fun refer(
        @Body param: JsonObject
    ): Response<ApiResponse>

    @POST("$URL_PATH_START/$URL_PATH/dashboardcount")
    suspend fun dashCount(
        @Body param: JsonObject
    ): Response<DashResult>

    @POST("$URL_PATH_START/$URL_PATH/services")
    suspend fun getServices(
        @Body param: JsonObject
    ): Response<ServicesResult>

    @POST("$URL_PATH_START/$URL_PATH/productservice")
    suspend fun updatePrice(
        @Body param: JsonObject
    ): Response<ApiResponse>

    //get finance List
    @POST("$URL_PATH_START/$URL_PATH/financial_service_commission")
    suspend fun getFinanceList(
        @Body param: JsonObject
    ): Response<FinancialResult>

    @POST("$URL_PATH_START/$URL_PATH/orderdetail")
    suspend fun getReportOrders(
        @Body param: JsonObject
    ): Response<ReportOrderResult>

    @POST("$URL_PATH_START/$URL_PATH/agent_detail")
    suspend fun salesAgents(
        @Body param: JsonObject
    ): Response<SalesAgentResult>

    @POST("$URL_PATH_START/$URL_PATH/customerdetail")
    suspend fun getReportDownloads(
        @Body param: JsonObject
    ): Response<ReportDownLoadResult>


    //refer list
    @POST("$URL_PATH_START/$URL_PATH/referuser_list")
    suspend fun getReferList(
        @Body param: JsonObject
    ): Response<ReferListResult>


    //video list
    @POST("$URL_PATH_START/$URL_PATH/webinar")
    suspend fun getTrainingVideoList(
        @Body param: JsonObject
    ): Response<TrainingVideoListResult>


    //get notification settings list
    @POST("$URL_PATH_START/$URL_PATH/notification_settings")
    suspend fun getNotificationSettings(
        @Body param: JsonObject
    ): Response<NotificationSettingsModel>

    //update notification settings list
    @POST("$URL_PATH_START/$URL_PATH/update_notification")
    suspend fun updateNotificationSettings(
        @Body param: JsonObject
    ): Response<ApiResponse>

    //promote user list
    @POST("$URL_PATH_START/$URL_PATH/promote_user_list")
    suspend fun getPromoteUserList(
        @Body param: JsonObject
    ): Response<ReferListResult>

    @POST("$URL_PATH_START/$URL_PATH/{end}")
    suspend fun getReportEarn(
        @Path("end") end: String, @Body param: JsonObject
    ): Response<ReportEarnResult>


    //get promotional images
    @POST("$URL_PATH_START/$URL_PATH/promotional_image")
    suspend fun getPromotionalImages(
        @Body param: JsonObject
    ): Response<PromotionalImages>

    //get promotional video
    @POST("$URL_PATH_START/$URL_PATH/promotional_video")
    suspend fun getPromotionalVideo(
        @Body param: JsonObject
    ): Response<PromotionalImages>


    //old
   /* @POST("gskfranchise/ticketsystem/tickettype")
    suspend fun getTicketType(
        @Body param: JsonObject
    ): Response<TicketTypeResult>*/


    //new
     @POST("appapi/ticketsystem/tickettype")
    suspend fun getTicketType(
        @Body param: JsonObject
    ): Response<TicketTypeResult>

    //old
  /*  @POST("gskfranchise/ticketsystem/createticket")
    suspend fun createTicket(
        @Body param: JsonObject
    ): Response<ApiResponse>*/


    //new
    @POST("appapi/ticketsystem/createticket")
    suspend fun createTicket(
        @Body param: JsonObject
    ): Response<ApiResponse>


    //old
  /*  @POST("gskfranchise/ticketsystem/replyticket")
    suspend fun sendChat(
        @Body param: JsonObject
    ): Response<ApiResponse>*/

    //new
    @POST("appapi/ticketsystem/replyticket")
    suspend fun sendChat(
        @Body param: JsonObject
    ): Response<ApiResponse>

    //get consultant list
    @POST("$URL_PATH_START/masterdistributor/consultant_details")
    suspend fun getConsultantDetails(
        @Body param: JsonObject
    ): Response<ConsultantResult>


    //old
   /* @POST("gskfranchise/ticketsystem/ticketlist")
    suspend fun getTickets(
        @Body param: JsonObject
    ): Response<TicketsResult>*/

    //new
    @POST("appapi/ticketsystem/ticketlist")
    suspend fun getTickets(
        @Body param: JsonObject
    ): Response<TicketsResult>

    //old
   /* @POST("gskfranchise/ticketsystem/ticketdetail")
    suspend fun getTicketInfo(
        @Body param: JsonObject
    ): Response<TicketResult> */


    //new
    @POST("appapi/ticketsystem/ticketdetail")
    suspend fun getTicketInfo(
        @Body param: JsonObject
    ): Response<TicketResult>

    @POST("$URL_PATH_START/$URL_PATH/offer_enquiry")
    suspend fun offer_enquiry(
        @Body param: JsonObject
    ): Response<ApiResponse>

    @POST("$URL_PATH_START/$URL_PATH/getTrainingStatus")
    suspend fun getTrainingStatus(
        @Body param: JsonObject
    ): Response<TrainingStatusResult>

    @POST("$URL_PATH_START/$URL_PATH/offerimage")
    suspend fun offerimage(
        @Body param: JsonObject
    ): Response<OfferResult>

    /*@POST("gskfranchise/applogin/getAdroidAppUpdate")
    suspend fun gstAppUpdate(
        @Body param: JsonObject
    ): Response<AppUpdateResult>*/

    @POST("appapi/get_app_update")
    suspend fun gstAppUpdate(
        @Body param: JsonObject
    ): Response<AppUpdateResult>

    @Streaming
    @GET
    fun downloadCertificate(@Url url: String): Call<ResponseBody>
    /*    @FormUrlEncoded
        @POST("GST/Api/GstUserDetail")
        suspend fun gstUserDetail(
            @Field("gstin") gstin: String
        ): Response<GstnResult>*/


    //old
    /*@POST("$URL_PATH_START/$URL_PATH/pushnotificationread")
    suspend fun pushNotificationRead(
        @Body param: JsonObject
    ): Response<ApiResponse>*/

    //new
    @POST("appapi/pushnotificationread")
    suspend fun pushNotificationRead(
        @Body param: JsonObject
    ): Response<ApiResponse>


    //old
   /* @POST("$URL_PATH_START/$URL_PATH/pushnotification")
    suspend fun pushnotification(
        @Body param: JsonObject
    ): Response<PushNotiResult> */

    //new
    @POST("appapi/pushnotification")
    suspend fun pushnotification(
        @Body param: JsonObject
    ): Response<PushNotiResult>

    @POST("$URL_PATH_START/masterdistributor/updatetraining")
    suspend fun updateTraining(
        @Body param: JsonObject
    ): Response<ApiResponse>

    @POST("$URL_PATH_START/$URL_PATH/paymentShopUpdate")
    suspend fun paymentShopUpdate(
        @Body param: JsonObject
    ): Response<ApiResponse>
    // Onboarding

    @POST("$URL_PATH_START/$URL_PATH/update_basic")
    suspend fun update_basic(
        @Body param: JsonObject
    ): Response<ApiResponse>

    @POST("$URL_PATH_START/$URL_PATH/update_aadhaar")
    suspend fun update_aadhaar(
        @Body param: JsonObject
    ): Response<ApiResponse>

    @POST("$URL_PATH_START/$URL_PATH/agreementText")
    suspend fun agreementText(
        @Body param: JsonObject
    ): Response<JsonObject>

    @POST("$URL_PATH_START/$URL_PATH/upload_signature")
    suspend fun upload_signature(
        @Body param: JsonObject
    ): Response<ApiResponse>

    @POST("$URL_PATH_START/$URL_PATH/update_pan")
    suspend fun update_pan(
        @Body param: JsonObject
    ): Response<ApiResponse>

    @POST("$URL_PATH_START/$URL_PATH/update_address")
    suspend fun update_address(
        @Body param: JsonObject
    ): Response<ApiResponse>

    //old
   /* @POST("$URL_PATH_START/$URL_PATH/statewisecode")
    suspend fun statewisecode(
        @Body param: JsonObject
    ): Response<StateResult>*/

    //new
    @POST("appapi/statewisecode")
    suspend fun statewisecode(
        @Body param: JsonObject
    ): Response<StateResult>

    @POST("$URL_PATH_START/$URL_PATH/addbankdetail")
    suspend fun addBankDetails(
        @Body param: JsonObject
    ): Response<ApiResponse>

    //get ifsc bank data
//    @POST("gskfranchise/applogin/verifyIFSC")
//    suspend fun getBankIfscData(
//        @Body param: JsonObject
//    ): Response<IfscResult>

    @POST("appapi/verifyIFSC")
    suspend fun getBankIfscData(
        @Body param: JsonObject
    ): Response<IfscResult>

    //old
   /* @POST("$URL_PATH_START/$URL_PATH/settlementreport")
    suspend fun settlementreport(
        @Body param: JsonObject
    ): Response<SettlementResult> */


    //new
    @POST("appapi/settlementreport")
    suspend fun settlementreport(
        @Body param: JsonObject
    ): Response<SettlementResult>

    @POST("$URL_PATH_START/$URL_PATH/withdrawcommission")
    suspend fun withdrawCommission(
        @Body param: JsonObject
    ): Response<ApiResponse>


    //old
    /*@POST("$URL_PATH_START/$URL_PATH/payoutcharge")
    suspend fun payoutcharge(
        @Body param: JsonObject
    ): Response<ApiResponse>*/


    //new
    @POST("appapi/payoutcharge")
    suspend fun payoutcharge(
        @Body param: JsonObject
    ): Response<ApiResponse>
}