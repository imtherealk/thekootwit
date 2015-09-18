package com.realk.thekootwit.service;

import com.realk.thekootwit.model.CursoredUsers;
import com.twitter.sdk.android.core.models.User;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by my on 2015-09-05.
 */
public interface ListService {
    @GET("/1.1/lists/members.json")
    void members(@Query("slug") String slug, @Query("owner_id") int ownerId, Callback<CursoredUsers> callback);

}
