package com.bhawak.osmnavigation.navigation.model;

import android.util.Log;

import androidx.annotation.Nullable;

import com.bhawak.osmnavigation.navigation.model.OSMPath;
import com.bhawak.osmnavigation.navigation.model.OSMPathWrapper;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionAnnotation;
import com.graphhopper.util.PointList;
import com.graphhopper.util.RoundaboutInstruction;

import java.util.ArrayList;
import java.util.List;

public class NavResponse {
    private String encodedPolyline;
    private double routeWeight;
    private double distanceInMeters;
    private long timeInMs;
//    @SerializedName("instructionList")
//    @Expose
    private List<Instruction> instructionList = new ArrayList<>();
//    @SerializedName("instructionList")
//    @Expose
//    private List<InstructionResponse> baatoInstruction = new ArrayList<>();

    public String getEncoded_polyline () {
        return encodedPolyline;
    }

    public void setEncoded_polyline (String encoded_polyline){
        this.encodedPolyline = encoded_polyline;
    }

    public double getRouteWeight() {
        return routeWeight;
    }

    public void setRouteWeight(double routeWeight) {
        this.routeWeight = routeWeight;
    }

    public double getDistanceInMeters() {
        return distanceInMeters;
    }

    public void setDistanceInMeters(double distanceInMeters) {
        this.distanceInMeters = distanceInMeters;
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

    public void setInstructionList (List <Instruction> instructionList) {
        this.instructionList = instructionList;
    }

    public NavResponse(String encoded_polyline, double distanceInMeters, long timeInMs, List<
            Instruction> instructionList){
        this.encodedPolyline = encoded_polyline;
        this.distanceInMeters = distanceInMeters;
        this.timeInMs = timeInMs;
        this.instructionList = instructionList;
    }
//    public NavResponse(String encoded_polyline, double distanceInMeters, long timeInMs, List<
//            InstructionResponse> baatoInstruction){
//        this.encodedPolyline = encoded_polyline;
//        this.distanceInMeters = distanceInMeters;
//        this.timeInMs = timeInMs;
//        this.baatoInstruction = baatoInstruction;
//    }
    public NavResponse() {
        super();
        // TODO Auto-generated constructor stub
    }

    @Override
    public String toString () {
        return "NavigationResponse [encoded_polyline=" + encodedPolyline + ", distance=" + distanceInMeters + ", timeInMs="
                + timeInMs + ", instructionList=" + instructionList + "]";
    }

//    public List<Instruction> getInstructionList(){
//        List<Instruction> myInstruction = new ArrayList<>();
//        for (int i = 0; i< baatoInstruction.size(); i++){
//            Instruction instruction = new Instruction(baatoInstruction.get(i).getSign(), baatoInstruction.get(i).getName(), new InstructionAnnotation(baatoInstruction.get(i).getAnnotation().getImportance(),
//                    baatoInstruction.get(i).getAnnotation().getMessage()), new PointList(baatoInstruction.get(i).getLength(), false));
//            myInstruction.add(i, instruction);
//        }
////        for (InstructionResponse irs: baatoInstruction
////             ) {
////            Instruction instruction = new Instruction(irs.getSign(), irs.getName(), new InstructionAnnotation(irs.getAnnotation().getImportance(), irs.getAnnotation().getMessage()), new PointList(irs.getLength(), false));
////                myInstruction.add(instruction);
////        }
//        return myInstruction;
//    }

}
