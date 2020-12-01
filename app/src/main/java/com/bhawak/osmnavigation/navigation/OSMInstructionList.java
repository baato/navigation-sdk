//package com.bhawak.osmnavigation.navigation;
//
//import com.graphhopper.util.DistanceCalc;
//import com.graphhopper.util.Helper;
//import com.graphhopper.util.Instruction;
//import com.graphhopper.util.PointList;
//import com.graphhopper.util.Translation;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class OSMInstructionList extends ArrayList<Instruction> {
//
//    private List<Instruction> instructions = new ArrayList<>();
//    private Translation tr = null;
//
//    public OSMInstructionList(Translation tr) {
//        this(10, tr);
//    }
//
//    public OSMInstructionList(int cap, Translation tr) {
//        instructions = new ArrayList<>(cap);
//        this.tr = tr;
//    }
//
//    @Override
//    public int size() {
//        return instructions.size();
//    }
//
//    @Override
//    public Instruction get(int index) {
//        return instructions.get(index);
//    }
//
//    @Override
//    public Instruction set(int index, Instruction element) {
//        return instructions.set(index, element);
//    }
//
//    @Override
//    public void add(int index, Instruction element) {
//        instructions.add(index, element);
//    }
//
//    @Override
//    public Instruction remove(int index) {
//        return instructions.remove(index);
//    }
//
//    /**
//     * This method is useful for navigation devices to find the next instruction for the specified
//     * coordinate (e.g. the current position).
//     * <p>
//     *
//     * @param maxDistance the maximum acceptable distance to the instruction (in meter)
//     * @return the next Instruction or null if too far away.
//     */
//    public Instruction find(double lat, double lon, double maxDistance) {
//        // handle special cases
//        if (size() == 0) {
//            return null;
//        }
//        PointList points = get(0).getPoints();
//        double prevLat = points.getLatitude(0);
//        double prevLon = points.getLongitude(0);
//        DistanceCalc distCalc = Helper.DIST_EARTH;
//        double foundMinDistance = distCalc.calcNormalizedDist(lat, lon, prevLat, prevLon);
//        int foundInstruction = 0;
//
//        // Search the closest edge to the query point
//        if (size() > 1) {
//            for (int instructionIndex = 0; instructionIndex < size(); instructionIndex++) {
//                points = get(instructionIndex).getPoints();
//                for (int pointIndex = 0; pointIndex < points.size(); pointIndex++) {
//                    double currLat = points.getLatitude(pointIndex);
//                    double currLon = points.getLongitude(pointIndex);
//
//                    if (!(instructionIndex == 0 && pointIndex == 0)) {
//                        // calculate the distance from the point to the edge
//                        double distance;
//                        int index = instructionIndex;
//                        if (distCalc.validEdgeDistance(lat, lon, currLat, currLon, prevLat, prevLon)) {
//                            distance = distCalc.calcNormalizedEdgeDistance(lat, lon, currLat, currLon, prevLat, prevLon);
//                            if (pointIndex > 0)
//                                index++;
//                        } else {
//                            distance = distCalc.calcNormalizedDist(lat, lon, currLat, currLon);
//                            if (pointIndex > 0)
//                                index++;
//                        }
//
//                        if (distance < foundMinDistance) {
//                            foundMinDistance = distance;
//                            foundInstruction = index;
//                        }
//                    }
//                    prevLat = currLat;
//                    prevLon = currLon;
//                }
//            }
//        }
//
//        if (distCalc.calcDenormalizedDist(foundMinDistance) > maxDistance)
//            return null;
//
//        // special case finish condition
//        if (foundInstruction == size())
//            foundInstruction--;
//
//        return get(foundInstruction);
//    }
//
//    public Translation getTr() {
//        return tr;
//    }
//}
