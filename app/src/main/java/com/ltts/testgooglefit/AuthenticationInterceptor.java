package com.ltts.testgooglefit;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;


public class AuthenticationInterceptor implements Interceptor {

    private String authToken;

    public AuthenticationInterceptor(String token) {
        this.authToken = token;
    }

    @Override
    public Response intercept(Chain chain) throws IOException {
        Request original = chain.request();

        Request.Builder builder = original.newBuilder()
                .header("Authorization", "Bearer " + authToken)
                .header("Accept", "application/json")
                .header("Content-Type", "application/json");

        Request request = builder.build();
        return chain.proceed(request);
    }
}