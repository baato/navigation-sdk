package com.bhawak.osmnavigation.navigation.model;

import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.directions.v5.models.DirectionsWaypoint;

import java.util.List;

public class MyDirectionResponse extends DirectionsResponse {
    @Override
    public String code() {
        return null;
    }

    @Override
    public String message() {
        return null;
    }

    @Override
    public List<DirectionsWaypoint> waypoints() {
        return null;
    }

    @Override
    public List<DirectionsRoute> routes() {
        return null;
    }

    @Override
    public String uuid() {
        return null;
    }

    @Override
    public Builder toBuilder() {
        return null;
    }
}
