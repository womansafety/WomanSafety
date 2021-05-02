package com.ws.womansafety.user.SendNotificationPack;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAKFHx5LE:APA91bFcCVfm6DSCFQvWiOU1Ch0xmX3ubYPtQlYyF3TVtEm9lDgzLuLUaabPN2_G8JQ-95fmZb-keRttpDOvyCQVy8pmoP4mqB4Qe1bBFQ11hC8sXMdFMsE1ty8QQmRcLT7-OzilG79n" // Your server key refer to video for finding your server key
            }
    )

    @POST("fcm/send")
    Call<MyResponse> sendNotifcation(@Body NotificationSender body);
}

