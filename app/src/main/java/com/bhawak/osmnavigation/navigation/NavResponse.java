package com.bhawak.osmnavigation.navigation;

import com.bhawak.osmnavigation.navigation.OSMPath;
import com.bhawak.osmnavigation.navigation.OSMPathWrapper;

import com.graphhopper.util.Instruction;
import java.util.List;

public class NavResponse {
        private String encoded_polyline;
        private OSMPathWrapper path;
        private double distance;
        private long timeInMs;
        private List<com.graphhopper.util.Instruction> instructionList = null;

        public String getEncoded_polyline () {
        return encoded_polyline;
    }

        public void setEncoded_polyline (String encoded_polyline){
        this.encoded_polyline = encoded_polyline;
    }

        public OSMPathWrapper getPath () {
        return path;
    }

        public void setPath (OSMPathWrapper path){
        this.path = path;
    }

        public double getDistance () {
        return distance;
    }

        public void setDistance ( double distance){
        this.distance = distance;
    }

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

    public NavResponse(String encoded_polyline, double distance, long timeInMs, List<
        Instruction > instructionList){
        this.encoded_polyline = encoded_polyline;
        this.distance = distance;
        this.timeInMs = timeInMs;
        this.instructionList = instructionList;
    }

    public NavResponse(String encoded_polyline, OSMPathWrapper path, double distance,
        long timeInMs, List<Instruction > instructionList){
        this.encoded_polyline = encoded_polyline;
        this.path = path;
        this.distance = distance;
        this.timeInMs = timeInMs;
        this.instructionList = instructionList;
    }

    public NavResponse() {
        super();
        // TODO Auto-generated constructor stub
    }

        @Override
        public String toString () {
        return "NavigationResponse [encoded_polyline=" + encoded_polyline + ", distance=" + distance + ", timeInMs="
                + timeInMs + ", instructionList=" + instructionList + "]";
    }
    }
