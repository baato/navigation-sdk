package com.bhawak.osmnavigation;

import com.bhawak.osmnavigation.navigation.NavResponse;
import com.google.gson.JsonElement;
import com.graphhopper.GHResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface ApiInterface {
    @GET("/api/v1/routes")
    Call<NavResponse> getRoutes(@Query("points") String[] points, @Query("mode") String mode, @Query("alternatives") Boolean alternatives);

    @GET("/api/v2/directions")
    Call<NavAPIResponse> getNavigationRoute(@Query("key") String key, @Query("points[]") String[] points, @Query("mode") String mode, @Query("alternatives") Boolean alternatives, @Query("instructions") Boolean instructions);

    @GET("/api/v1/ghroutes")
    Call<GHResponse> getGHRoutes(@Query("points") String[] points, @Query("mode") String mode, @Query("alternatives") Boolean alternatives);

//    @GET("api/v2/styles/bdfaa22bc8f24d49b838da1b59f705fb.json")
//    Call<StyleResponse> getStyle(@Header("Authorization") String token);
}
