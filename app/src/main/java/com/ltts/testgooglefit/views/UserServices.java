package com.ltts.testgooglefit.views;

import com.ltts.testgooglefit.utils.Constants;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface UserServices {

    @POST("/users/me/dataset:aggregate?key=" + Constants.API_KEY)
    Call<ResponseBody> getBloodGlucoseData(@Body String jsonData);
}
