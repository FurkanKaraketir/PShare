package com.furkankrktr.pshare.send_notification_pack

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface APIService {
    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAA24lt174:APA91bGsmV_XdqrzOKzOIjlBUTHF2Y_pcMavedv-AIlLGHyvjto5lcd9z-Fi7nlslYWOmuaR3aaOb5D61EZUh0xlKVSOQ-8Qq1u9EPC1vDg2QAdE4X2Zhf_k3c7KUeYcXyewc_cA7gom"
    )
    @POST("fcm/send")
    fun sendNotifcation(@Body body: NotificationSender?): Call<MyResponse?>?
}