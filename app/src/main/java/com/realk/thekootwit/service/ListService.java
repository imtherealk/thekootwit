package com.realk.thekootwit.service;

import com.realk.thekootwit.model.CursoredUsers;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.User;

import java.util.List;

import retrofit.Callback;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by my on 2015-09-05.
 */
public interface ListService {
    @GET("/1.1/lists/members.json")
    void members(@Query("slug") String slug, @Query("owner_id") long ownerId, Callback<CursoredUsers> callback);

    @GET("/1.1/lists/members.json")
    void members(@Query("slug") String slug, @Query("owner_id") long ownerId, @Query("count") long count,
                 @Query("cursor") long cursor, Callback<CursoredUsers> callback);

    @POST("/1.1/lists/create.json?mode=private&description=The%20koo%20twit%20%EA%B8%B0%EB%B3%B8%20list")
    void create(@Query("name") String name, Callback<Object> callback);

    @POST("/1.1/lists/members/create.json")
    void addMember(@Query("slug") String slug, @Query("owner_id") long ownerId,
                   @Query("user_id") long userId, Callback<Object> callback);

    @POST("/1.1/lists/members/destroy.json")
    void removeMember(@Query("slug") String slug, @Query("owner_id") long ownerId,
                       @Query("user_id") long userId, Callback<Object> callback);

    @GET("/1.1/lists/statuses.json?count=20")
    void statuses(@Query("slug") String slug, @Query("owner_id") long ownerId, Callback<List<Tweet>> callback);

    @GET("/1.1/lists/statuses.json?count=20")
    void statuses(@Query("slug") String slug, @Query("owner_id") long ownerId, @Query("max_id") long maxId, Callback<List<Tweet>> callback);
}
