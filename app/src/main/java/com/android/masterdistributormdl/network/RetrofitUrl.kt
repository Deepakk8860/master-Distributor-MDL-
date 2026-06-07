package com.android.masterdistributormdl.network

import com.android.masterdistributormdl.gskDistributor.model.User
import com.android.masterdistributormdl.gskDistributor.model.consultant.ConsultantResult
import com.android.masterdistributormdl.gskDistributor.utils.URL_PATH
import com.android.masterdistributormdl.gskDistributor.utils.URL_PATH_START
import com.android.masterdistributormdl.model.ActivityDetailsResult
import com.android.masterdistributormdl.model.AddLeadResult
import com.android.masterdistributormdl.model.ApiResponse
import com.android.masterdistributormdl.model.FollowUpListCount
import com.android.masterdistributormdl.model.LeadStageResult
import com.android.masterdistributormdl.model.LeadSummaryStats
import com.android.masterdistributormdl.model.UserResult
import com.android.masterdistributormdl.model.auth.LoginResult
import com.android.masterdistributormdl.model.doc.DocResult
import com.android.masterdistributormdl.model.lead.ClientListResult
import com.android.masterdistributormdl.model.leadStatusList.LeadStatusFilterListResult
import com.android.masterdistributormdl.model.leadstatus.LeadStatusDetailsResult
import com.android.masterdistributormdl.model.preferedMessage.PreferedMessageListResult
import com.android.masterdistributormdl.model.profile.ProfileResult
import com.android.masterdistributormdl.model.territory.PincodeResult
import com.google.android.gms.common.api.Api
import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.Multipart
import retrofit2.http.Part


interface RetrofitUrl {
    //send otp with email
   /* @POST("gskfranchise/applogin/validateUser")
    suspend fun sendOtpToMail(
        @Body param: JsonObject
    ): Response<ApiResponse>*/

    @POST("appapi/applogin/validateMdUser")
    suspend fun sendOtpToMail(
        @Body param: JsonObject
    ): Response<ApiResponse>


    //get lead stage
    @POST("$URL_PATH_START/masterdistributor/leadstatuslist")
    suspend fun getLeadStage(
        @Body param: JsonObject
    ): Response<LeadStageResult>

    //get plan by city
    @POST("$URL_PATH_START/masterdistributor/get_pincode_by_plan")
    suspend fun getPlanCity(
        @Body param: JsonObject
    ): Response<com.gsk.distributor.model.PincodeResult>



    //create payment link
    @POST("$URL_PATH_START/masterdistributor/createPaymentLink")
    suspend fun createPaymentLink(
        @Body param: JsonObject
    ): Response<ApiResponse>

    //create distributor
    @POST("$URL_PATH_START/masterdistributor/create_distributor")
    suspend fun createDistributor(
        @Body param: JsonObject
    ): Response<ApiResponse>

    //get lead status data
    @POST("$URL_PATH_START/masterdistributor/leaddetails")
    suspend fun getLeadStatusDetails(
        @Body param: JsonObject
    ): Response<LeadStatusDetailsResult>




    //update lead details
    @POST("$URL_PATH_START/masterdistributor/add_activity_by_type")
    suspend fun updateLeads(
        @Body param: JsonObject
    ): Response<ApiResponse>


    //update lead details
    @POST("$URL_PATH_START/masterdistributor/update_lead_activity_log")
    suspend fun updateActivity(
        @Body param: JsonObject
    ): Response<ApiResponse>

    //get activity details
    @POST("$URL_PATH_START/masterdistributor/get_lead_activity_details")
    suspend fun getActivityDetails(
        @Body param: JsonObject
    ): Response<ActivityDetailsResult>


    //get client list
    @POST("$URL_PATH_START/masterdistributor/leadlistfilter")
    suspend fun getClientListFilter(
        @Body param: JsonObject
    ): Response<LeadStatusFilterListResult>

    @POST("$URL_PATH_START/masterdistributor/leadlist")
    suspend fun getClientList(
        @Body param: JsonObject
    ): Response<ClientListResult>


    //upload doc
    @POST("$URL_PATH_START/masterdistributor/uploadShareDocument")
    suspend fun uploadDoc(
        @Body param: JsonObject
    ): Response<ApiResponse>

    //upload share in file format
    @Multipart
    @POST("$URL_PATH_START/masterdistributor/uploadShareDocumentFile")
    suspend fun uploadDocInFile(
        @Part document_file: MultipartBody.Part,
        @Part document_name: MultipartBody.Part,
        @Part uid: MultipartBody.Part
    ): Response<ApiResponse>

    //doc list
    @POST("$URL_PATH_START/masterdistributor/getShareDocumentList")
    suspend fun docList(
        @Body param: JsonObject
    ): Response<DocResult>

    //get plan list
    @POST("$URL_PATH_START/masterdistributor/planlist")
    suspend fun getPlanList(
        @Body param: JsonObject
    ): Response<LeadStageResult>

    //UPDATE training status
    @POST("$URL_PATH_START/masterdistributor/updatetraining")
    suspend fun updateTraining(
        @Body param: JsonObject
    ): Response<ApiResponse>

    //get prefered message
    @POST("$URL_PATH_START/masterdistributor/lead_share_messages")
    suspend fun getPreferedMessage(
        @Body param: JsonObject
    ): Response<PreferedMessageListResult>


    //get follow up list
    @POST("$URL_PATH_START/masterdistributor/get_user_leads_by_type")
    suspend fun getFollowUpList(
        @Body param: JsonObject
    ): Response<ClientListResult>

    //get follow up list count
    @POST("$URL_PATH_START/masterdistributor/get_lead_summary_count")
    suspend fun getFollowUpListCount(
        @Body param: JsonObject
    ): Response<FollowUpListCount>


    //get lead summary
    @POST("$URL_PATH_START/masterdistributor/get_lead_summary_stats")
    suspend fun getLeadSummary(
        @Body param: JsonObject
    ): Response<LeadSummaryStats>


    //get pincode location
   /* @POST("gskfranchise/masterdistributor/getPinLocation")
    suspend fun getPinLocation(
        @Body param: JsonObject
    ): Response<JsonObject>*/

    @POST("appapi/getPinLocation")
    suspend fun getPinLocation(
        @Body param: JsonObject
    ): Response<JsonObject>

    //add lead
    @POST("$URL_PATH_START/masterdistributor/leadcreate")
    suspend fun addLead(
        @Body param: JsonObject
    ): Response<AddLeadResult>


    //verify otp
    /*@POST("gskfranchise/applogin/verifyLoginUserOTP")
    suspend fun verifyOtp(
        @Body param: JsonObject
    ): Response<LoginResult>*/

    @POST("appapi/applogin/verifyMdUserOTP")
    suspend fun verifyOtp(
        @Body param: JsonObject
    ): Response<LoginResult>

    //verify by google login
  /*  @POST("gskfranchise/applogin/userGoogleLogin")
    suspend fun verifyByGoogleLogin(
        @Body param: JsonObject
    ): Response<LoginResult>*/

    @POST("appapi/applogin/userGoogleLogin")
    suspend fun verifyByGoogleLogin(
        @Body param: JsonObject
    ): Response<LoginResult>

    @POST("$URL_PATH_START/masterdistributor/profile")
    suspend fun getUserProfile(
        @Body param: JsonObject
    ): Response<ProfileResult>

    @POST("$URL_PATH_START/masterdistributor/updateProfile")
    suspend fun editProfile(
        @Body param: JsonObject
    ): Response<ApiResponse>

    //edit profile photo
    @POST("$URL_PATH_START/masterdistributor/updateProfileimg")
    suspend fun editProfilePhoto(
        @Body param: JsonObject
    ): Response<ApiResponse>

    @POST("$URL_PATH_START/masterdistributor/add_agent")
    suspend fun addAgent(
        @Body param: JsonObject
    ): Response<ApiResponse>

    @POST("$URL_PATH_START/masterdistributor/edit_agent")
    suspend fun editAgent(
        @Body param: JsonObject
    ): Response<ApiResponse>

    @POST("$URL_PATH_START/masterdistributor/agent_list")
    suspend fun agentList(
        @Body param: JsonObject
    ): Response<com.gsk.distributor.model.AgentListResult>
}