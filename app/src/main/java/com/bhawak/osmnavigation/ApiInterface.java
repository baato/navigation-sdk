package com.bhawak.osmnavigation;

import com.bhawak.osmnavigation.navigation.NavResponse;
import com.google.gson.JsonElement;
import com.graphhopper.GHResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET("/api/v2/directions")
    Call<NavResponse> getRoutes(@Query("points") String[] points, @Query("mode") String mode, @Query("alternatives") Boolean alternatives);

    @GET("/api/v1/ghroutes")
    Call<GHResponse> getGHRoutes(@Query("points") String[] points, @Query("mode") String mode, @Query("alternatives") Boolean alternatives);
}
