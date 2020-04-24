package com.bhawak.osmnavigation.navigation;

import android.util.Log;

import com.bhawak.osmnavigation.ApiInterface;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphhopper.util.TranslationMap;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.geojson.Point;
import com.mapbox.services.android.navigation.ui.v5.route.NavigationMapRoute;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public final class NavigationBato {
    private TranslationMap translationMap;
    private static final TranslationMap navigateResponseConverterTranslationMap = new NavigateResponseConverterTranslationMap().doImport();
    private NavigationMapRoute navigationMapRoute;
    private NavigateResponseConverter navigateResponseConverter;
    public NavigateResponseConverter getNavigateResponseConverter() {
        return navigateResponseConverter;
    }
    public ApiInterface getApiInterface() {
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.connectTimeout(100, TimeUnit.SECONDS);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://192.168.1.103:8080")
                .addConverterFactory(GsonConverterFactory.create())
                .client(httpClient.build())
                .build();
        return retrofit.create(ApiInterface.class);
    }

    private static final String TAG = "NavigationBato";

    private void getRoute(Point origin, Point destination) {
        String[] points = new String[2];
        points[0] = origin.latitude() + "," + origin.longitude();
//        points[0] = "27.713042695157757,85.2703857421875";
        points[1] = destination.latitude() + "," + destination.longitude();
        Call<NavResponse> call = getApiInterface().getRoutes(points, "car", false);
        call.enqueue(new Callback<NavResponse>() {
            @Override
            public void onResponse(Call<NavResponse> call, Response<NavResponse> response) {
                if (response.body() == null) {
                    Log.e(TAG, "No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().getInstructionList() != null && response.body().getInstructionList().size() == 0) {
                    Log.e(TAG, "No routes found");
                    return;
                }
                ObjectNode obj = NavigateResponseConverter.convertFromGHResponse(response.body(), "car");
                DirectionsResponse directionsResponse = DirectionsResponse.fromJson(obj.toString());

            }

            @Override
            public void onFailure(Call<NavResponse> call, Throwable t) {
                Log.d(TAG, "onFailure: " + t);
                Log.d(TAG, "Request:" + call.request());
            }
        });

    }
}
