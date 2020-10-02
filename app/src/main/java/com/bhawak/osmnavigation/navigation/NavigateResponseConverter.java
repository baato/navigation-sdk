/*
 *  Licensed to GraphHopper GmbH under one or more contributor
 *  license agreements. See the NOTICE file distributed with this work for
 *  additional information regarding copyright ownership.
 *
 *  GraphHopper GmbH licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except in
 *  compliance with the License. You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.bhawak.osmnavigation.navigation;

import android.annotation.SuppressLint;
import android.content.res.AssetManager;
import android.os.Build;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.bhawak.osmnavigation.DecodeLine;
import com.bhawak.osmnavigation.NavigationResponse;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.graphhopper.GHResponse;
import com.graphhopper.ResponsePath;
import com.graphhopper.util.Helper;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.PointList;
import com.graphhopper.util.RoundaboutInstruction;
import com.graphhopper.util.Translation;
//import com.graphhopper.util.TranslationMap;
//import com.baato.baatolibrary.models.NavResponse;


import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.UUID;

import timber.log.Timber;

import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;



public class NavigateResponseConverter {
    private static class MapObj{
        private int start;
        private int end;
        private String polyline;
        private PointList pointList;

        private MapObj(int start, int end, String polyline, PointList pointList) {
            this.start = start;
            this.end = end;
            this.polyline = polyline;
            this.pointList = pointList;
        }

        public int getStart() {
            return start;
        }

        public void setStart(int start) {
            this.start = start;
        }

        public int getEnd() {
            return end;
        }

        public void setEnd(int end) {
            this.end = end;
        }

        public String getPolyline() {
            return polyline;
        }

        public void setPolyline(String polyline) {
            this.polyline = polyline;
        }

        public PointList getPointList() {
            return pointList;
        }

        public void setPointList(PointList pointList) {
            this.pointList = pointList;
        }
    }

    private static final int VOICE_INSTRUCTION_MERGE_TRESHHOLD = 100;
    private static NavigationResponse ghResponse = new NavigationResponse();
    private static List<List<Double>> allCord = new ArrayList<>();
    private static boolean pointadded = false;

//    private static TranslationMap trMap = new TranslationMap().doImport();
    private static BaatoTranslationMap trMap;
    private static BaatoTranslationMap mtrMap;
    private static String uuid = UUID.randomUUID().toString().replaceAll("-", "");
    private static String mode = "driving";
    private static Locale locale = new Locale("en_US");
    private enum Profile {
        CAR,
        BIKE,
        WALK
    }

    /**
     * Converts a GHResponse into a json that follows the Mapbox API specification
     */
    public static ObjectNode convertFromGHResponse(NavigationResponse ghResponsee, String type) {
        ghResponse = ghResponsee;
        mtrMap = new NavigateResponseConverterTranslationMap(locale.getLanguage()).doImport();
        trMap = new BaatoTranslationMap().doImport("locale/");
        return navConverter(type);
    }
    public static ObjectNode convertFromGHResponse(NavigationResponse ghResponsee, String type, Locale localevar) {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        ghResponse = ghResponsee;
        locale = localevar;
        mtrMap = new NavigateResponseConverterTranslationMap(locale.getLanguage()).doImport();
        trMap = new BaatoTranslationMap().doImport("locale/");
        return  navConverter(type);
//        if (ghResponse.hasErrors())
//            throw new IllegalStateException("If the response has errors, you should use the method NavigateResponseConverter#convertFromGHResponseError");

//        PointList waypoints = ghResponse.getPath().getWaypoints();

    }
    private static ObjectNode navConverter(String type) {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        PointList routePoints = new PointList(10,false);


        final ArrayNode routesJson = json.putArray("routes");
        switch (type){
            case "car":
                mode = "driving";
                break;
            case "foot":
                mode = "walking";
                break;
            case "hike":
                mode = "walking";
                break;
            case "bike":
                mode = "cycling";
                break;
            default:
                mode = "driving";
        }

//        List<PathWrapper> paths = ghResponse.getAll();
        List<List<Double>>  waypointsg = DecodeLine.decodePolyline(ghResponse.getEncoded_polyline(), false);
//        ResponsePath responsePath = ghResponsee;
        allCord = waypointsg;
        ObjectNode pathJson = routesJson.addObject();
        for (int i = 0; i < waypointsg.size(); i++) {routePoints.add(waypointsg.get(i).get(0), waypointsg.get(i).get(1));}
//        putRouteInformation(pathJson,0, Locale.ENGLISH, new DistanceConfig(DistanceUtils.Unit.METRIC, trMap, mtrMap, Locale.ENGLISH), routePoints);
        putRouteInformation(pathJson,0, locale, new DistanceConfig(DistanceUtils.Unit.METRIC, trMap, mtrMap, locale), routePoints);
        final ArrayNode waypointsJson = json.putArray("waypoints");
        getWaypoints(waypointsJson, waypointsg, 0);
        getWaypoints(waypointsJson, waypointsg, ghResponse.getInstructionList().size()-1);
        json.put("code", "Ok");
        // TODO: Maybe we need a different format... uuid: "cji4ja4f8004o6xrsta8w4p4h"
        json.put("uuid", uuid);
        return json;
    }
    private static void getWaypoints(ArrayNode waypointsJson, List<List<Double>> wayPoints, int index){

        ObjectNode waypointJson = waypointsJson.addObject();
        waypointJson.put("name",getName(index));
        if (index > 0) {
            putLocation(wayPoints.get(wayPoints.size()-1).get(0), wayPoints.get(wayPoints.size()-1).get(1), waypointJson);
        } else {
            putLocation(wayPoints.get(index).get(0), wayPoints.get(index).get(1), waypointJson);
        }
//        wayPoints.get(0), wayPoints.get(wayPoints.size()-1)
    }

    private static String getName(int index){
//        Log.d("Index", String.valueOf(index));
        if(ghResponse.getInstructionList().get(index).getExtraInfoJSON().getLandmark() != null)
            return ghResponse.getInstructionList().get(index).getExtraInfoJSON().getLandmark();
        else
         return ghResponse.getInstructionList().get(index).getName();
    }

    private static void putRouteInformation(ObjectNode pathJson, int routeNr, Locale locale, DistanceConfig distanceConfig, PointList polyline) {
        ArrayList<InstructionResponse> instructions = (ArrayList<InstructionResponse>) ghResponse.getInstructionList();
//        pathJson.put("geometry",ghResponse.getEncoded_polyline());
        pathJson.put("geometry", WebHelper.encodePolyline(polyline,false, 1e6));
//        pathJson.put("geometry", polyline);
        ArrayNode legsJson = pathJson.putArray("legs");
        ObjectNode legJson = legsJson.addObject();
        ArrayNode steps = legJson.putArray("steps");

        long time = 0;
        double distance = 0;
        boolean isFirstInstructionOfLeg = true;

        for (int i = 0; i < instructions.size(); i++) {
            ObjectNode instructionJson = steps.addObject();

            putInstruction(instructions, i, locale, instructionJson, isFirstInstructionOfLeg, distanceConfig);
            InstructionResponse instruction = instructions.get(i);
            time += instruction.getTime();
            distance += instruction.getDistance();
//            Log.wtf("ins", String.valueOf(instruction.getPoints()));
            isFirstInstructionOfLeg = false;
            if (instruction.getSign() == InstructionResponse.REACHED_VIA || instruction.getSign() == InstructionResponse.FINISH) {
                putLegInformation(legJson, routeNr, time, distance);
                isFirstInstructionOfLeg = true;
                time = 0;
                distance = 0;

                if (instruction.getSign() == InstructionResponse.REACHED_VIA) {
                    // Create new leg and steps after a via points
                    legJson = legsJson.addObject();
                    steps = legJson.putArray("steps");
                }
            }
        }
        ObjectNode routeObject =
                pathJson.put("weight_name", "routability");

        double weight = ghResponse.getRouteWeight();
        pathJson.put("weight", Helper.round(weight, 1));
        pathJson.put("duration", convertToSeconds(ghResponse.getTimeInMs()));
        pathJson.put("distance", Helper.round(ghResponse.getDistanceInMeters(), 1));
        pathJson.put("routeOptions",getRouteOptions("pk.xxx"));
        pathJson.put("voiceLocale", locale.toLanguageTag());
    }
    private static ObjectNode getRouteOptions(String accessKey){
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        json.put("baseUrl", "https://api.baato.io");
        json.put("user", "mapbox");
        json.put("profile", mode);
        ArrayNode coordinates = json.putArray("coordinates");
        getCord(coordinates);
//        json.put("coordinates",getCord(coordinates));
//        json.put("language", String.valueOf(Locale.ENGLISH));
        json.put("language", String.valueOf(locale));
        json.put("bearings", ";");
        json.put("continueStraight", true);
        json.put("roundaboutExits", true);
        json.put("geometries", "polyline6");
        json.put("overview", "full");
        json.put("steps", true);
        json.put("annotations", "");
        json.put("voiceInstructions", true);
        json.put("bannerInstructions", true);
        json.put("voiceUnits", String.valueOf(DistanceUtils.Unit.METRIC));
        json.put("accessToken",accessKey);
        json.put("requestUuid",uuid);
        return  json;
//        json.put("coordinates", path.)

    }
    private static void getCord(ArrayNode coordinates){
        double a1 = allCord.get(0).get(1);
        double a2 = allCord.get(0).get(0);
        ArrayNode cord1 =coordinates.addArray();
        cord1.add(a1);
        cord1.add(a2);
        ArrayNode cord2 = coordinates.addArray();
        double a3 = allCord.get(allCord.size()-1).get(1);
        double a4 = allCord.get(allCord.size()-1).get(0);
        cord2.add(a3);
        cord2.add(a4);
//        return "[["+ a1 + "," + a2 +"],[" + a3 + ","+ a4 + "]]";
    }

    private static void putLegInformation(ObjectNode legJson, int i, long time, double distance) {

        // TODO: Improve path descriptions, so that every path has a description, not just alternative routes
        String summary;
//        if (!path.getDescription().isEmpty())
//            summary = TextUtils.join(",", path.getDescription());
//        else
        summary = "Baato Route " + i;
        legJson.put("summary", summary);

        // TODO there is no weight per instruction, let's use time
        legJson.put("weight", convertToSeconds(time));
        legJson.put("duration", convertToSeconds(time));
        legJson.put("distance", Helper.round(distance, 1));
    }

    private static void putInstruction(ArrayList<InstructionResponse> instructions, int index, Locale locale, ObjectNode instructionJson, boolean isFirstInstructionOfLeg, DistanceConfig distanceConfig) {
        InstructionResponse instruction = instructions.get(index);
        ArrayNode intersections = instructionJson.putArray("intersections");
//        getIntersections(intersections, instruction);
//        ObjectNode intersection = intersections.addObject();
        getIntersections(intersections, instruction);
        MapObj mapObj = computeInterval(instruction);
        Log.d("Interval:", "["+String.valueOf(mapObj.start)+","+String.valueOf(mapObj.end)+"]");
//
//        intersection.putArray("entry");
//        intersection.putArray("bearings");

        //Make pointList mutable
        PointList pointList = mapObj.getPointList().clone(false);

        if (index + 2 < instructions.size()) {
            // Add the first point of the next instruction
            MapObj mapObj1 = computeInterval(instructions.get(index+1));
            pointList.add(mapObj1.pointList.getLat(0), mapObj1.pointList.getLon(0));
        } else if (pointList.size() == 1) {
            // Duplicate the last point in the arrive instruction, if the size is 1
            pointList.add(pointList.getLat(0), pointList.getLon(0), pointList.getEle(0));
        }


        instructionJson.put("driving_side", "left");

        // Does not include elevation
        instructionJson.put("geometry", WebHelper.encodePolyline(pointList, false, 1e6));
//        instructionJson.put("geometry", pointList);

        // TODO: how about other modes?
        instructionJson.put("mode", mode);

        putManeuver(instruction, instructionJson, locale, isFirstInstructionOfLeg);

        // TODO distance = weight, is weight even important?
        double distance = Helper.round(instruction.getDistance(), 1);
        instructionJson.put("weight", distance);
        instructionJson.put("duration", convertToSeconds(instruction.getTime()));
        if (instruction.getExtraInfoJSON().getLandmark() != null)
            instructionJson.put("name", instruction.getExtraInfoJSON().getLandmark());
        else
            instructionJson.put("name", instruction.getName());
        instructionJson.put("distance", distance);
        ArrayNode voiceInstructions = instructionJson.putArray("voiceInstructions");
        ArrayNode bannerInstructions = instructionJson.putArray("bannerInstructions");
        if (!isLastInstruction(instruction)) {
//            putVoiceInstructions(instructions, distance, index, Locale.ENGLISH,trMap,mtrMap,voiceInstructions, distanceConfig);
//            putBannerInstructions(instructions, distance, index, Locale.ENGLISH,trMap,bannerInstructions);
            putVoiceInstructions(instructions, distance, index, locale,trMap,mtrMap,voiceInstructions, distanceConfig);
            putBannerInstructions(instructions, distance, index, locale,trMap,bannerInstructions);
        }
    }
    private static boolean isFirstInstruction(InstructionResponse instruction){
        return getPreviousInstruction(instruction) == null;
    }
    private static boolean isLastInstruction(InstructionResponse instruction){
        return getNextInstruction(instruction) == null;
    }
    private static int getInstructionIndex(InstructionResponse instruction){
        int index = -1;
        for(int i = 0; i< ghResponse.getInstructionList().size(); i++){
            if (String.valueOf(ghResponse.getInstructionList().get(i)).equals(instruction.toString()))
                index = i;
        }
        return index;
    }
    private synchronized static void getIntersections (ArrayNode instersections, InstructionResponse instruction) {
        MapObj mapObj = computeInterval(instruction);
        int rangeStart = mapObj.getStart();
        int rangeEnd = mapObj.getEnd()-1;
        if(hasIntersections(instruction)){
            for (int index = rangeStart; index<rangeEnd;index++){
                ObjectNode intersection = instersections.addObject();
                List<Double> cord1 = allCord.get(index);
                List<Double> cord2 = allCord.get(index+1);
                getSingleIntersection(intersection,cord1, cord2);
            }
        } else {
            ObjectNode intersection = instersections.addObject();
            createDummyIntersection(intersection,instruction);
        }
    }
    private static double returnIntersections(InstructionResponse instruction){
        ObjectNode jsonObj = JsonNodeFactory.instance.objectNode();
//        List<StepIntersection> stepIntersections = new ArrayList<>();
        ArrayNode intersection = jsonObj.putArray("intersections");
        ObjectNode intersections = intersection.addObject();
        MapObj mapObj = computeInterval(instruction);
        int rangeEnd = mapObj.getEnd()-1;
        if(hasIntersections(instruction)){
            List<Double> cord1 = allCord.get(rangeEnd-1);
            List<Double> cord2 = allCord.get(rangeEnd);
            return  returnBearing(cord1, cord2);
        }
        double bearing = getBearingBefore(instruction);
        if (isLastInstruction(instruction)) {
            return bearing - 180;
//                    - 180 ;// acc. to mapbox doc., substracting 180 gives the direction of driving
        }
        return bearing;

    }
    private static int returnBearing (List<Double> coordinate, List<Double> nextCoordinate){
        return calculateBearing(coordinate.get(0),coordinate.get(1),nextCoordinate.get(0),nextCoordinate.get(1));
    }
    private static void getSingleIntersection (ObjectNode intersection, List<Double> coordinate, List<Double> nextCoordinate) {
//        ObjectNode obj = intersections.addObject();
        intersection.put("out",0);
        ArrayNode entry = intersection.putArray("entry");
        entry.add(true);
        ArrayNode bearings = intersection.putArray("bearings");
        bearings.add(calculateBearing(coordinate.get(0),coordinate.get(1),nextCoordinate.get(0),nextCoordinate.get(1)));
        putLocation(coordinate.get(0),coordinate.get(1),intersection);
    }
    private static void createDummyIntersection (ObjectNode intersection, InstructionResponse instruction) {
        double bearing = getBearingBefore(instruction);


        if (isLastInstruction(instruction)) {
            bearing = bearing - 180 ;// acc. to mapbox doc., substracting 180 gives the direction of driving
        }
//        ObjectNode obj = intersection.addObject();
        intersection.put("out",0);
        ArrayNode entry = intersection.putArray("entry");
        entry.add(true);
        ArrayNode bearings = intersection.putArray("bearings");
        bearings.add((int)Math.round(bearing));
        MapObj mapObj = computeInterval(instruction);
//        if (isLastInstruction(instruction))
//            putLocation(mapObj.pointList.getLatitude(instruction.getPoints().getSize()),mapObj.pointList.getLongitude(instruction.getPoints().getSize()),intersection);
//        else
        putLocation(mapObj.pointList.getLatitude(instruction.getPoints().getSize()-1),mapObj.pointList.getLongitude(instruction.getPoints().getSize()-1),intersection);
    }

    @Nullable
    private static InstructionResponse getPreviousInstruction(InstructionResponse instruction) {
        int index = getInstructionIndex(instruction);
        return index != 0 ? ghResponse.getInstructionList().get(index - 1) : null;
    }
    @Nullable
    private static InstructionResponse getNextInstruction(InstructionResponse instruction) {
        int index = getInstructionIndex(instruction);
        return index != ghResponse.getInstructionList().size()-1 ? ghResponse.getInstructionList().get(index+1) : null;
    }

    private static synchronized MapObj computeInterval(InstructionResponse instruction){
        int size = instruction.getPoints().getSize();
//        int geometrySize = allCord.size();
//        int index = -1;
        int start = 0;
        for(int i = 0; i< ghResponse.getInstructionList().size(); i++){
            if (String.valueOf(ghResponse.getInstructionList().get(i)).equals(instruction.toString())) {
                break;
            }
            start+=ghResponse.getInstructionList().get(i).getPoints().getSize();
        }
        int end = start + size;
        PointList routePoints = new PointList(10,false);
//        if (end-start == 0) {
//            routePoints.add(allCord.get(0).get(0), allCord.get(0).get(1));
//           if (!pointadded) {
//               allCord.add(allCord.get(0));
//               pointadded = true;
//           }
//        }
        for(int i = 0; i<end-start;i++) {
//            if (allCord.size() == start+i){
//                start = start - 1;
//            }
            double ltr = allCord.get(start+i).get(0);
            double rtr = allCord.get(start+i).get(1);
            routePoints.add(ltr,rtr);
        }
//        Log.d("route:", String.valueOf(routePoints.size()));

//        for (List<Double> geoList: allCord
//             ) {
//            double ltr = geoList.get(start);
//            double rtr = geoList.get(start);
//            routePoints.add(rtr,ltr);
//        }
        String engeometry = WebHelper.encodePolyline(routePoints,false, 1e6);
        return new MapObj(start,end,engeometry, routePoints);
    }

    private static int calculateBearing(double startLat, double startLng, double destLat, double destLng) {
        double startLatRad = toRadians(startLat);
        double startLngRad = toRadians(startLng);
        double destLatRad = toRadians(destLat);
        double destLngRad = toRadians(destLng);

        double y = Math.sin(destLngRad - startLngRad) * Math.cos(destLatRad);
        double x =
                Math.cos(startLatRad) * Math.sin(destLatRad) -
                        Math.sin(startLatRad) *
                                Math.cos(destLatRad) *
                                Math.cos(destLngRad - startLngRad);
        double brng = Math.atan2(y, x);
        brng = toDegrees(brng);

        return (int) Math.round((brng + 360) % 360) | 0;
    }

    private static double getBearingBefore(InstructionResponse instruction){
        MapObj mapObj = computeInterval(instruction);
        double[] geom = new double[4];
        if (isFirstInstruction(instruction))
            return 0;
        if (isLastInstruction(instruction))
            return getLastBearingOfPreviousInstruction(instruction);
        else {
            geom[0]=allCord.get(mapObj.getStart()).get(0);
            geom[1]=allCord.get(mapObj.getStart()).get(1);
            geom[2]=allCord.get(mapObj.getEnd()-1).get(0);
            geom[3]=allCord.get(mapObj.getEnd()-1).get(1);
            if (hasIntersections(instruction)) {
                geom[0] = allCord.get(mapObj.getStart() - 1).get(0);
                geom[1] = allCord.get(mapObj.getStart() - 1).get(1);
            }
            return calculateBearing(geom[0],geom[1],geom[2],geom[3]);

//            int index = getInstructionIndex(instruction);
//            return calculateBearing(allCord.get(index).get(0), allCord.get(index).get(1), allCord.get(index+1).get(0), allCord.get(index+1).get(1));
        }
    }
    //    @RequiresApi(api = Build.VERSION_CODES.N)
    private static double getBearingAfter(InstructionResponse instruction) {
//        int index = getInstructionIndex(instruction);
        MapObj mapObj = computeInterval(instruction);
        double[] geom = new double[4];
        geom[0] = allCord.get(mapObj.getStart()).get(0);
        geom[1] = allCord.get(mapObj.getStart()).get(1);
        geom[2] = allCord.get(mapObj.getEnd()-1).get(0);
        geom[3] = allCord.get(mapObj.getEnd()-1).get(1);
        if (hasIntersections(instruction)) {
            geom[0] = allCord.get(mapObj.getStart() + 1).get(0);
            geom[1] = allCord.get(mapObj.getStart() + 1).get(1);
        }


//        int index = ObjectInputStream.range(0, ghResponse.getInstructionList().size()).filter(i -> instruction.equals(ghResponse.getInstructionList().get(i))).findFirst().orElse(-1);
//        int index = ghResponse.getInstructionList().lastIndexOf(instruction);

        return calculateBearing(geom[0],geom[1],geom[2],geom[3]);
    }

    @SuppressLint("NewApi")
    private static double getLastBearingOfPreviousInstruction(InstructionResponse instruction){
        InstructionResponse previousInstruction = getPreviousInstruction(instruction);

        if (hasIntersections(previousInstruction)) {
            returnIntersections(previousInstruction);
        }
        return 0;

    }
    private static boolean hasIntersections(InstructionResponse instruction){
        MapObj mapObj = computeInterval(instruction);
        return (mapObj.getEnd() - mapObj.getStart() -1)>0;
    }
    private static void putVoiceInstructions(ArrayList<InstructionResponse> instructions, double distance, int index, Locale locale, BaatoTranslationMap translationMap, BaatoTranslationMap navigateResponseConverterTranslationMap, ArrayNode voiceInstructions, DistanceConfig distanceConfig) {
        /*
            A VoiceInstruction Object looks like this
            {
                distanceAlongGeometry: 40.9,
                announcement: "Exit the traffic circle",
                ssmlAnnouncement: "<speak><amazon:effect name="drc"><prosody rate="1.08">Exit the traffic circle</prosody></amazon:effect></speak>",
            }
        */

        InstructionResponse nextInstruction = instructions.get(index + 1);
        String turnDescription = nextInstruction.getTurnDescription(translationMap.getWithFallBack(locale));
        String thenVoiceInstruction = getThenVoiceInstructionpart(instructions, index, locale, translationMap, navigateResponseConverterTranslationMap);
        List<VoiceInstructionConfig.VoiceInstructionValue> voiceValues = distanceConfig.getVoiceInstructionsForDistance(distance, turnDescription, thenVoiceInstruction);

        for (VoiceInstructionConfig.VoiceInstructionValue voiceValue : voiceValues) {
            String turnDesc = voiceValue.turnDescription;
            if (nextInstruction.getSign() == 4){
                turnDesc = voiceValue.turnDescription.replace("unknown instruction sign '4'", "you will arrive at your destination.");
            }
//            Log.wtf("turn desc", turnDesc);
            putSingleVoiceInstruction(voiceValue.spokenDistance, turnDesc, voiceInstructions);
        }

        // Speak 80m instructions 80 before the turn
        // Note: distanceAlongGeometry: "how far from the upcoming maneuver the voice instruction should begin"
        double distanceAlongGeometry = Helper.round(Math.min(distance, 70), 1);
        thenVoiceInstruction = thenVoiceInstruction.replace("unknown instruction sign '4'","you will arrive at your destination.");
        if (nextInstruction.getSign() == 4) {
            thenVoiceInstruction = "you will arrive";
        }
        String description = turnDescription + thenVoiceInstruction;
        // Special case for the arrive instruction
        if (index + 2 == instructions.size()) {
            distanceAlongGeometry = Helper.round(Math.min(distance, 20), 1);
            if (locale.getLanguage().equals("ne"))
               description = "तपाईं आफ्नो गन्तव्यमा पुग्नुभयो";
            else
              description = "You have arrived at your destination";
        }
        String value = getTranslatedDistance((int) distanceAlongGeometry);
//        Log.wtf("turn desc then", description);
//        description = description.replace("unknown instruction sign '6'", "Continue on " + instructions.get(index).getName());
//        description = description.replace("then unknown instruction sign '4'", "you will arrive your destination.");
        putSingleVoiceInstruction(distanceAlongGeometry, description, voiceInstructions);
    }
    public static String getTranslatedDistance(int distance){
        String unit = "meters";
        int convertedDistance = 0;
        if (distance / 1000 >= 1) {
            unit = "kilometres";
            convertedDistance = distance / 1000;
        } else {
            convertedDistance = distance;
            unit = "metres";
        }
        return "In " + convertedDistance +" " + unit + ", ";
    }

    private static void putSingleVoiceInstruction(double distanceAlongGeometry, String turnDescription, ArrayNode voiceInstructions) {
//        Log.wtf("::", turnDescription);
        ObjectNode voiceInstruction = voiceInstructions.addObject();
        voiceInstruction.put("distanceAlongGeometry", distanceAlongGeometry);
        turnDescription = turnDescription.replace("then unknown instruction sign '4'", " ");
        //TODO: ideally, we would even generate instructions including the instructions after the next like turn left **then** turn right
        voiceInstruction.put("announcement", turnDescription);
        voiceInstruction.put("ssmlAnnouncement", "<speak><amazon:effect name=\"drc\"><prosody rate=\"1.08\">" + turnDescription + "</prosody></amazon:effect></speak>");
    }

    /**
     * For close turns, it is important to announce the next turn in the earlier instruction.
     * e.g.: instruction i+1= turn right, instruction i+2=turn left, with instruction i+1 distance < VOICE_INSTRUCTION_MERGE_TRESHHOLD
     * The voice instruction should be like "turn right, then turn left"
     * <p>
     * For instruction i+1 distance > VOICE_INSTRUCTION_MERGE_TRESHHOLD an empty String will be returned
     */
    private static String getThenVoiceInstructionpart(ArrayList<InstructionResponse> instructions, int index, Locale locale, BaatoTranslationMap translationMap, BaatoTranslationMap navigateResponseConverterTranslationMap) {
        if (instructions.size() > index + 2) {
            InstructionResponse firstInstruction = instructions.get(index + 1);
            if (firstInstruction.getDistance() < VOICE_INSTRUCTION_MERGE_TRESHHOLD) {
                InstructionResponse secondInstruction = instructions.get(index + 2);
                if (secondInstruction.getSign() != InstructionResponse.REACHED_VIA)
                    return ", " + navigateResponseConverterTranslationMap.getWithFallBack(locale).tr("then") + " " + secondInstruction.getTurnDescription(translationMap.getWithFallBack(locale));
            }
        }

        return "";
    }

    /**
     * Banner instructions are the turn instructions that are shown to the user in the top bar.
     * <p>
     * Between two instructions we can show multiple banner instructions, you can control when they pop up using distanceAlongGeometry.
     */
    private static void putBannerInstructions(ArrayList<InstructionResponse> instructions, double distance, int index, Locale locale, BaatoTranslationMap translationMap, ArrayNode bannerInstructions) {
        /*
        A BannerInstruction looks like this
        distanceAlongGeometry: 107,
        primary: {
            text: "Lichtensteinstraße",
            components: [
            {
                text: "Lichtensteinstraße",
                type: "text",
            }
            ],
            type: "turn",
            modifier: "right",
        },
        secondary: null,
         */

        ObjectNode bannerInstruction = bannerInstructions.addObject();

        //Show from the beginning
        bannerInstruction.put("distanceAlongGeometry", distance);

        ObjectNode primary = bannerInstruction.putObject("primary");
        putSingleBannerInstruction(instructions.get(index + 1), locale, translationMap, primary);

        bannerInstruction.putNull("secondary");

        if (instructions.size() > index + 2 && instructions.get(index + 2).getSign() != InstructionResponse.REACHED_VIA && instructions.get(index+2).getDistance() < 60) {
            // Sub shows the instruction after the current one
            ObjectNode sub = bannerInstruction.putObject("sub");

            putSingleBannerInstruction(instructions.get(index + 2), locale, translationMap, sub);
        }
    }

    private static void putSingleBannerInstruction(InstructionResponse instruction, Locale locale, BaatoTranslationMap translationMap, ObjectNode singleBannerInstruction) {
        String bannerInstructionName = instruction.getName();
        if (bannerInstructionName == null || bannerInstructionName.isEmpty()) {
            // Fix for final instruction and for instructions without name
            bannerInstructionName = instruction.getTurnDescription(translationMap.getWithFallBack(locale));

            // Uppercase first letter
            // TODO: should we do this for all cases? Then we might change the spelling of street names though
            bannerInstructionName = Helper.firstBig(bannerInstructionName);
        }
        String bannerInstruction = bannerInstructionName;
        if (getTurnType(instruction, false).equals("arrive")) {
            bannerInstruction = "Destination";
        } else {
            singleBannerInstruction.put("text", bannerInstruction);
        }

        ArrayNode components = singleBannerInstruction.putArray("components");
        ObjectNode component = components.addObject();
        component.put("text", bannerInstruction);
        component.put("type", "text");

        singleBannerInstruction.put("type", getTurnType(instruction, false));
        String modifier = getModifier(instruction);
        if (modifier != null)
            singleBannerInstruction.put("modifier", modifier);

        if (instruction.getSign() == InstructionResponse.USE_ROUNDABOUT) {
                double turnAngle = instruction.getTurnAngle();
                if (Double.isNaN(turnAngle)) {
                    singleBannerInstruction.putNull("degrees");
                } else {
                    double degree = (Math.abs(turnAngle) * 180) / Math.PI;
                    singleBannerInstruction.put("degrees", degree);
                }
            }
    }

    @SuppressLint("NewApi")
    private static void putManeuver(InstructionResponse instruction, ObjectNode instructionJson, Locale locale, boolean isFirstInstructionOfLeg) {
        ObjectNode maneuver = instructionJson.putObject("maneuver");
//        maneuver.put("bearing_after", 0);
//        maneuver.put("bearing_before", 0);
        maneuver.put("bearing_after", getBearingAfter(instruction));
        maneuver.put("bearing_before", getBearingBefore(instruction));
//        int index = getInstructionIndex(instruction);
        MapObj mapObj = computeInterval(instruction);
        int rangeStart = mapObj.getStart();
        putLocation(allCord.get(rangeStart).get(0), allCord.get(rangeStart).get(1), maneuver);
//        PointList points = instruction.getPoints();
//        putLocation(allCord.get(index).get(0), allCord.get(index).get(1), maneuver);
//        putLocation(points.getLat(0), points.getLon(0), maneuver);

        String modifier = getModifier(instruction);
        if (modifier != null)
            maneuver.put("modifier", modifier);

        maneuver.put("type", getTurnType(instruction, isFirstInstructionOfLeg));
        // exit number
        if (instruction.getSign() ==  InstructionResponse.USE_ROUNDABOUT || instruction.getSign() ==  InstructionResponse.LEAVE_ROUNDABOUT  )
            maneuver.put("exit", instruction.getExitNumber());
//        final BaatoTranslationMap navigateResponseConverterTranslationMap = new NavigateResponseConverterTranslationMap(locale.getLanguage()).doImport();
//        maneuver.put("instruction", instruction.getTurnDescription(navigateResponseConverterTranslationMap.get("en_US")));
        maneuver.put("instruction", instruction.getTurnDescription(trMap.get(locale.getLanguage())));
    }

    /**
     * Relevant maneuver types are:
     * depart (firs instruction)
     * turn (regular turns)
     * roundabout (enter roundabout, maneuver contains also the exit number)
     * arrive (last instruction and waypoints)
     * <p>
     * You can find all maneuver types at: https://www.mapbox.com/api-documentation/#maneuver-types
     */
    private static String getTurnType(InstructionResponse instruction, boolean isFirstInstructionOfLeg) {
        if (isFirstInstructionOfLeg) {
            return "depart";
        } else {
            switch (instruction.getSign()) {
                case InstructionResponse.FINISH:
//                    return "arrive";
                case InstructionResponse.REACHED_VIA:
                    return "arrive";
                case InstructionResponse.USE_ROUNDABOUT:
                    return "roundabout";
                default:
                    return "turn";
            }
        }
    }

    /**
     * No modifier values for arrive and depart
     * <p>
     * Find modifier values here: https://www.mapbox.com/api-documentation/#stepmaneuver-object
     */
    private static String getModifier(InstructionResponse instruction) {
        switch (instruction.getSign()) {
            case InstructionResponse.CONTINUE_ON_STREET:
                return "straight";
            case InstructionResponse.U_TURN_LEFT:
            case InstructionResponse.U_TURN_RIGHT:
            case InstructionResponse.U_TURN_UNKNOWN:
                return "uturn";
            case InstructionResponse.KEEP_LEFT:
            case InstructionResponse.TURN_SLIGHT_LEFT:
                return "slight left";
            case InstructionResponse.TURN_LEFT:
                return "left";
            case InstructionResponse.TURN_SHARP_LEFT:
                return "sharp left";
            case InstructionResponse.KEEP_RIGHT:
            case InstructionResponse.TURN_SLIGHT_RIGHT:
                return "slight right";
            case InstructionResponse.TURN_RIGHT:
                return "right";
            case InstructionResponse.TURN_SHARP_RIGHT:
                return "sharp right";
            case InstructionResponse.USE_ROUNDABOUT:
                // TODO: This might be an issue in left-handed traffic, because there it schould be left
                return "left";
            default:
                return null;
        }
    }

    /**
     * Puts a location array in GeoJson format into the node
     */
    private static ObjectNode putLocation(double lat, double lon, ObjectNode node) {
        ArrayNode location = node.putArray("location");
        // GeoJson lon,lat
        location.add(Helper.round6(lon));
        location.add(Helper.round6(lat));
        return node;
    }

    /**
     * Mapbox uses seconds instead of milliSeconds
     */
    private static double convertToSeconds(double milliSeconds) {
        return Helper.round(milliSeconds / 1000, 1);
    }

    public static ObjectNode convertFromGHResponseError(GHResponse ghResponse) {
        ObjectNode json = JsonNodeFactory.instance.objectNode();
        // TODO we could make this more fine grained
        json.put("code", "InvalidInput");
        json.put("message", ghResponse.getErrors().get(0).getMessage());
        return json;
    }
}
