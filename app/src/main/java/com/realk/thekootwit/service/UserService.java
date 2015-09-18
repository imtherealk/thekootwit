package com.realk.thekootwit.service;

import com.twitter.sdk.android.core.models.User;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Retrofit twitter service
 */
public interface UserService {
    @GET("/1.1/users/search.json")
    void search(@Query("q") String query, @Query("page") int page, Callback<List<User>> callback);
}
