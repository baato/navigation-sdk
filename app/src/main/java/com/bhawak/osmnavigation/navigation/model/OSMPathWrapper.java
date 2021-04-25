package com.bhawak.osmnavigation.navigation.model;
import android.util.ArrayMap;
import android.util.Log;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import com.graphhopper.Trip;
import com.graphhopper.Trip.Leg;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.InstructionList;
import com.graphhopper.util.PointList;
import com.graphhopper.util.details.PathDetail;
import com.graphhopper.util.shapes.BBox;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class OSMPathWrapper {
        private final List<Throwable> errors = new ArrayList(4);
        private List<String> description;
        private double distance;
        private double ascend;
        private double descend;
        private double routeWeight;
        private long time;
        private String debugInfo = "";
        @SerializedName("instructions")
        @Expose
        private ArrayList<Instruction> instructions;
        private PointList waypointList;
        private PointList pointList;
        private int numChanges;
        private List<Trip.Leg> legs;
        private List<Integer> pointsOrder;
        private Map<String, List<PathDetail>> pathDetails;
        private BigDecimal fare;
        private boolean impossible;

        public OSMPathWrapper() {
            this.waypointList = PointList.EMPTY;
            this.pointList = PointList.EMPTY;
            this.legs = new ArrayList(5);
            this.pointsOrder = new ArrayList(5);
            this.pathDetails = new HashMap();
            this.impossible = false;
        }

        public List<String> getDescription() {
            return this.description == null ? Collections.emptyList() : this.description;
        }

        public OSMPathWrapper setDescription(List<String> names) {
            this.description = names;
            return this;
        }

        public OSMPathWrapper addDebugInfo(String debugInfo) {
            if (debugInfo == null) {
                throw new IllegalStateException("Debug information has to be none null");
            } else {
                if (!this.debugInfo.isEmpty()) {
                    this.debugInfo = this.debugInfo + ";";
                }

                this.debugInfo = this.debugInfo + debugInfo;
                return this;
            }
        }

        public String getDebugInfo() {
            return this.debugInfo;
        }

        public OSMPathWrapper setPointsOrder(List<Integer> list) {
            this.pointsOrder.clear();
            this.pointsOrder.addAll(list);
            return this;
        }

        public List<Integer> getPointsOrder() {
            return this.pointsOrder;
        }

        public PointList getPoints() {
            this.check("getPoints");
            return this.pointList;
        }

        public OSMPathWrapper setPoints(PointList points) {
            if (this.pointList != PointList.EMPTY) {
                throw new IllegalStateException("Cannot call setPoint twice");
            } else {
                this.pointList = points;
                return this;
            }
        }

        public PointList getWaypoints() {
            this.check("getWaypoints");
            return this.waypointList;
        }

        public void setWaypoints(PointList wpList) {
            if (this.waypointList != PointList.EMPTY) {
                throw new IllegalStateException("Cannot call setWaypoints twice");
            } else {
                this.waypointList = wpList;
            }
        }

        public double getDistance() {
            this.check("getDistance");
            return this.distance;
        }

        public OSMPathWrapper setDistance(double distance) {
            this.distance = distance;
            return this;
        }

        public double getAscend() {
            return this.ascend;
        }

        public OSMPathWrapper setAscend(double ascend) {
            if (ascend >= 0.0D && !Double.isNaN(ascend)) {
                this.ascend = ascend;
                return this;
            } else {
                throw new IllegalStateException("ascend has to be positive but was " + ascend);
            }
        }

        public double getDescend() {
            return this.descend;
        }

        public OSMPathWrapper setDescend(double descend) {
            if (descend >= 0.0D && !Double.isNaN(descend)) {
                this.descend = descend;
                return this;
            } else {
                throw new IllegalStateException("descend has to be positive but was " + descend);
            }
        }

        public long getTime() {
            this.check("getTimes");
            return this.time;
        }

        public OSMPathWrapper setTime(long timeInMillis) {
            this.time = timeInMillis;
            return this;
        }

        public double getRouteWeight() {
            this.check("getRouteWeight");
            return this.routeWeight;
        }

        public OSMPathWrapper setRouteWeight(double weight) {
            this.routeWeight = weight;
            return this;
        }

        public BBox calcBBox2D() {
            this.check("calcRouteBBox");
            BBox bounds = BBox.createInverse(false);

            for(int i = 0; i < this.pointList.getSize(); ++i) {
                bounds.update(this.pointList.getLatitude(i), this.pointList.getLongitude(i));
            }

            return bounds;
        }

        public String toString() {
            String str = "nodes:" + this.pointList.getSize() + "; " + this.pointList.toString();
            if (this.instructions != null && !this.instructions.isEmpty()) {
                str = str + ", " + this.instructions.toString();
            }

            if (this.hasErrors()) {
                str = str + ", " + this.errors.toString();
            }

            return str;
        }

        public ArrayList<Instruction> getInstructions() {
            this.check("getInstructions");
            if (this.instructions == null) {
                throw new IllegalArgumentException("To access instructions you need to enable creation before routing");
            } else {
                return this.instructions;
            }
        }

        public void setInstructions(ArrayList<Instruction> instructions) {
//            Log.d("Instrustion Setter", instructions.toString());
            this.instructions = instructions;
        }

        public void addPathDetails(Map<String, List<PathDetail>> details) {
            if (!this.pathDetails.isEmpty() && !details.isEmpty() && this.pathDetails.size() != details.size()) {
                throw new IllegalStateException("Details have to be the same size");
            } else {
                Iterator var2 = details.entrySet().iterator();

                while(var2.hasNext()) {
                    Map.Entry<String, List<PathDetail>> detailEntry = (Map.Entry)var2.next();
                    if (this.pathDetails.containsKey(detailEntry.getKey())) {
                        List<PathDetail> pd = (List)this.pathDetails.get(detailEntry.getKey());
                        merge(pd, (List)detailEntry.getValue());
                    } else {
                        this.pathDetails.put(detailEntry.getKey(), detailEntry.getValue());
                    }
                }

            }
        }

        public static void merge(List<PathDetail> pathDetails, List<PathDetail> otherDetails) {
            if (!pathDetails.isEmpty() && !otherDetails.isEmpty()) {
                PathDetail lastDetail = (PathDetail)pathDetails.get(pathDetails.size() - 1);
                boolean extend = lastDetail.getValue() != null ? lastDetail.getValue().equals(((PathDetail)otherDetails.get(0)).getValue()) : ((PathDetail)otherDetails.get(0)).getValue() != null;
                if (extend) {
                    lastDetail.setLast(((PathDetail)otherDetails.get(0)).getLast());
                    otherDetails.remove(0);
                }
            }

            pathDetails.addAll(otherDetails);
        }

        public Map<String, List<PathDetail>> getPathDetails() {
            return this.pathDetails;
        }

        private void check(String method) {
            if (this.hasErrors()) {
                throw new RuntimeException("You cannot call " + method + " if response contains errors. Check this with ghResponse.hasErrors(). Errors are: " + this.getErrors());
            }
        }

        public boolean hasErrors() {
            return !this.errors.isEmpty();
        }

        public List<Throwable> getErrors() {
            return this.errors;
        }

        public OSMPathWrapper addError(Throwable error) {
            this.errors.add(error);
            return this;
        }

        public OSMPathWrapper addErrors(List<Throwable> errors) {
            this.errors.addAll(errors);
            return this;
        }

        public void setNumChanges(int numChanges) {
            this.numChanges = numChanges;
        }

        public int getNumChanges() {
            return this.numChanges;
        }

        public List<Trip.Leg> getLegs() {
            return this.legs;
        }

        public void setFare(BigDecimal fare) {
            this.fare = fare;
        }

        public BigDecimal getFare() {
            return this.fare;
        }

        public boolean isImpossible() {
            return this.impossible;
        }

        public void setImpossible(boolean impossible) {
            this.impossible = impossible;
        }
}

