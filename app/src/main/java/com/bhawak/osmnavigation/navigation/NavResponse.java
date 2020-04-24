package com.bhawak.osmnavigation.navigation;

import androidx.annotation.Nullable;

import com.bhawak.osmnavigation.navigation.OSMPath;
import com.bhawak.osmnavigation.navigation.OSMPathWrapper;

import com.graphhopper.util.Instruction;
import java.util.List;

public class NavResponse {
        private String encoded_polyline;
//        private OSMPathWrapper path;
        private Double routeWeight = 0.0;
        private double distanceInMs;
        private long timeInMs;
        private List<com.graphhopper.util.Instruction> instructionList = null;

    public String getEncoded_polyline() {
        return encoded_polyline;
    }

    public void setEncoded_polyline(String encoded_polyline) {
        this.encoded_polyline = encoded_polyline;
    }

    public double getDistanceInMs() {
        return distanceInMs;
    }

    public void setDistanceInMs(double distanceInMs) {
        this.distanceInMs = distanceInMs;
    }

    public Double getRouteWeight() {
        return routeWeight;
    }

    public void setRouteWeight(Double routeWeight) {
            if (routeWeight.isNaN()) {
                this.routeWeight = 0.0;
            } else {
                this.routeWeight = routeWeight;
            }
    }

//        public OSMPathWrapper getPath () {
//        return path;
//    }
//
//        public void setPath (OSMPathWrapper path){
//        this.path = path;
//    }
//
//        public double getDistance () {
//        return distance;
//    }
//
//        public void setDistance ( double distance){
//        this.distance = distance;
//    }

        public long getTimeInMs () {
        return timeInMs;
    }

        public void setTimeInMs ( long timeInMs){
        this.timeInMs = timeInMs;
    }

        public List<Instruction> getInstructionList () {
        return instructionList;
    }

        public void setInstructionList (List < Instruction > instructionList) {
        this.instructionList = instructionList;
    }

    public NavResponse(String encoded_polyline, double distanceInMeters, long timeInMs, List<
        Instruction > instructionList){
        this.encoded_polyline = encoded_polyline;
        this.distanceInMs = distanceInMeters;
        this.timeInMs = timeInMs;
        this.instructionList = instructionList;
    }

//    public NavResponse(String encoded_polyline, double distanceInMeters,
//        long timeInMs, List<Instruction > instructionList){
//        this.encoded_polyline = encoded_polyline;
////        this.path = path;
//        this.distanceInMeters = distanceInMeters;
//        this.timeInMs = timeInMs;
//        this.instructionList = instructionList;
//    }

    public NavResponse() {
        super();
        // TODO Auto-generated constructor stub
    }

        @Override
        public String toString () {
        return "NavigationResponse [encoded_polyline=" + encoded_polyline + ", distance=" + distanceInMs + ", timeInMs="
                + timeInMs + ", instructionList=" + instructionList + "]";
    }
    }
