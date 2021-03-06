//package com.bhawak.osmnavigation.navigation;
//import android.annotation.SuppressLint;
//import android.os.Build;
//import androidx.annotation.Nullable;
//import androidx.annotation.RequiresApi;
//import android.text.TextUtils;
//import android.util.Log;
//
//import com.bhawak.osmnavigation.DecodeLine;
//import com.bhawak.osmnavigation.NavigationResponse;
//import com.fasterxml.jackson.databind.node.ArrayNode;
//import com.fasterxml.jackson.databind.node.JsonNodeFactory;
//import com.fasterxml.jackson.databind.node.ObjectNode;
//import com.graphhopper.GHResponse;
//import com.graphhopper.ResponsePath;
//import com.graphhopper.util.Helper;
////import com.graphhopper.util.InstructionResponse;
//import com.graphhopper.util.PointList;
//import com.graphhopper.util.RoundaboutInstruction;
//import com.graphhopper.util.TranslationMap;
//
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//import java.util.UUID;
//
//import static java.lang.Math.toDegrees;
//import static java.lang.Math.toRadians;
//
//public class SimpleConverter {
//    private static class MapObj{
//        private int start;
//        private int end;
//        private String polyline;
//        private PointList pointList;
//
//        private MapObj(int start, int end, String polyline, PointList pointList) {
//            this.start = start;
//            this.end = end;
//            this.polyline = polyline;
//            this.pointList = pointList;
//        }
//
//        public int getStart() {
//            return start;
//        }
//
//        public void setStart(int start) {
//            this.start = start;
//        }
//
//        public int getEnd() {
//            return end;
//        }
//
//        public void setEnd(int end) {
//            this.end = end;
//        }
//
//        public String getPolyline() {
//            return polyline;
//        }
//
//        public void setPolyline(String polyline) {
//            this.polyline = polyline;
//        }
//
//        public PointList getPointList() {
//            return pointList;
//        }
//
//        public void setPointList(PointList pointList) {
//            this.pointList = pointList;
//        }
//    }
//        private static final int VOICE_INSTRUCTION_MERGE_TRESHHOLD = 100;
//    private static NavigationResponse ghResponse = new NavigationResponse();
//    private static List<List<Double>> allCord = new ArrayList<>();
//    private static boolean pointadded = false;
//    private static final TranslationMap trMap = new TranslationMap().doImport();
//    private static  final  TranslationMap mtrMap = new NavigateResponseConverterTranslationMap().doImport();
//    private static String uuid = UUID.randomUUID().toString().replaceAll("-", "");
//    private static String mode = "driving";
//    private enum Profile {
//        CAR,
//        BIKE,
//        WALK
//    }
//
//        /**
//         * Converts a GHResponse into a json that follows the Mapbox API specification
//         */
//        public static ObjectNode convertFromGHResponse(NavigationResponse ghResponses, String type) {
//        ObjectNode json = JsonNodeFactory.instance.objectNode();
//        ghResponse = ghResponses;
//            PointList routePoints = new PointList(10,false);
//
//
//            final ArrayNode routesJson = json.putArray("routes");
//            switch (type){
//                case "car":
//                    mode = "driving";
//                    break;
//                case "foot":
//                    mode = "walking";
//                    break;
//                case "hike":
//                    mode = "walking";
//                    break;
//                case "bike":
//                    mode = "cycling";
//                    break;
//                default:
//                    mode = "driving";
//            }
//
//            List<List<Double>>  waypointsg = DecodeLine.decodePolyline(ghResponse.getEncoded_polyline(), false);
//
//            allCord = waypointsg;
//            ObjectNode pathJson = routesJson.addObject();
//            for (int i = 0; i < waypointsg.size(); i++) {routePoints.add(waypointsg.get(i).get(0), waypointsg.get(i).get(1));}
//            putRouteInformation(pathJson,0, Locale.ENGLISH, new DistanceConfig(DistanceUtils.Unit.METRIC, trMap, mtrMap, Locale.ENGLISH), routePoints);
//            final ArrayNode waypointsJson = json.putArray("waypoints");
//            getWaypoints(waypointsJson, waypointsg, 0);
//            getWaypoints(waypointsJson, waypointsg, ghResponse.getInstructionList().size()-1);
//            json.put("code", "Ok");
//            // TODO: Maybe we need a different format... uuid: "cji4ja4f8004o6xrsta8w4p4h"
//            json.put("uuid", uuid);
//            return json;
//    }
//    private static void getWaypoints(ArrayNode waypointsJson, List<List<Double>> wayPoints, int index){
//
//        ObjectNode waypointJson = waypointsJson.addObject();
//        waypointJson.put("name",getName(index));
//        if (index > 0) {
//            putLocation(wayPoints.get(wayPoints.size()-1).get(0), wayPoints.get(wayPoints.size()-1).get(1), waypointJson);
//        } else {
//            putLocation(wayPoints.get(index).get(0), wayPoints.get(index).get(1), waypointJson);
//        }
//    }
//    private static String getName(int index){
////        Log.d("Index", String.valueOf(index));
//        return ghResponse.getInstructionList().get(index).getName();
//    }
//
//        private static void putRouteInformation(ObjectNode pathJson, int routeNr, Locale locale, DistanceConfig distanceConfig, PointList polyline) {
//            ArrayList<InstructionResponse> instructions = (ArrayList<InstructionResponse>) ghResponse.getInstructionList();
//
//        pathJson.put("geometry", WebHelper.encodePolyline(polyline, false, 1e6));
//        ArrayNode legsJson = pathJson.putArray("legs");
//
//        ObjectNode legJson = legsJson.addObject();
//        ArrayNode steps = legJson.putArray("steps");
//
//        long time = 0;
//        double distance = 0;
//        boolean isFirstInstructionOfLeg = true;
//
//        for (int i = 0; i < instructions.size(); i++) {
//            ObjectNode instructionJson = steps.addObject();
//            putInstruction(instructions, i, locale, instructionJson, isFirstInstructionOfLeg, distanceConfig);
//            InstructionResponse instruction = instructions.get(i);
//            time += instruction.getTime();
//            distance += instruction.getDistance();
//            isFirstInstructionOfLeg = false;
//            if (instruction.getSign() == InstructionResponse.REACHED_VIA || instruction.getSign() == InstructionResponse.FINISH) {
//                putLegInformation(legJson, routeNr, time, distance);
//                isFirstInstructionOfLeg = true;
//                time = 0;
//                distance = 0;
//
//                if (instruction.getSign() == InstructionResponse.REACHED_VIA) {
//                    // Create new leg and steps after a via points
//                    legJson = legsJson.addObject();
//                    steps = legJson.putArray("steps");
//                }
//            }
//        }
//
//            double weight = ghResponse.getRouteWeight();
//            pathJson.put("weight", Helper.round(weight, 1));
//            pathJson.put("duration", convertToSeconds(ghResponse.getTimeInMs()));
//            pathJson.put("distance", Helper.round(ghResponse.getDistanceInMeters(), 1));
//            pathJson.put("voiceLocale", "en-US");
//    }
//
//        private static void putLegInformation(ObjectNode legJson, int i, long time, double distance) {
//        // TODO: Improve path descriptions, so that every path has a description, not just alternative routes
//        String summary;
//            summary = "GraphHopper Route " + i;
//        legJson.put("summary", summary);
//
//        // TODO there is no weight per instruction, let's use time
//        legJson.put("weight", convertToSeconds(time));
//        legJson.put("duration", convertToSeconds(time));
//        legJson.put("distance", Helper.round(distance, 1));
//    }
//    private static MapObj computeInterval(InstructionResponse instruction){
//        int size = instruction.getPoints().getSize();
////        int geometrySize = allCord.size();
////        int index = -1;
//        int start = 0;
//        for(int i = 0; i< ghResponse.getInstructionList().size(); i++){
//            if (String.valueOf(ghResponse.getInstructionList().get(i)).equals(instruction.toString())) {
//                break;
//            }
//            start+=ghResponse.getInstructionList().get(i).getPoints().getSize();
//        }
//        int end = start + size;
//        PointList routePoints = new PointList(10,false);
////        if (end-start == 0) {
////            routePoints.add(allCord.get(0).get(0), allCord.get(0).get(1));
////           if (!pointadded) {
////               allCord.add(allCord.get(0));
////               pointadded = true;
////           }
////        }
//        for(int i = 0; i<end-start;i++) {
////            if (allCord.size() == start+i){
////                start = start - 1;
////            }
//            double ltr = allCord.get(start+i).get(0);
//            double rtr = allCord.get(start+i).get(1);
//            routePoints.add(ltr,rtr);
//        }
////        Log.d("route:", String.valueOf(routePoints.size()));
//
////        for (List<Double> geoList: allCord
////             ) {
////            double ltr = geoList.get(start);
////            double rtr = geoList.get(start);
////            routePoints.add(rtr,ltr);
////        }
//        String engeometry = WebHelper.encodePolyline(routePoints,false, 1e6);
//        return new MapObj(start,end,engeometry, routePoints);
//    }
//
//        private static ObjectNode putInstruction(ArrayList<InstructionResponse> instructions, int index, Locale locale, ObjectNode instructionJson, boolean isFirstInstructionOfLeg, DistanceConfig distanceConfig) {
//        InstructionResponse instruction = instructions.get(index);
//        ArrayNode intersections = instructionJson.putArray("intersections");
//        ObjectNode intersection = intersections.addObject();
//        intersection.putArray("entry");
//        intersection.putArray("bearings");
//            MapObj mapObj = computeInterval(instruction);
//        //Make pointList mutable
//        PointList pointList = mapObj.pointList;
//
//        if (index + 2 < instructions.size()) {
//            // Add the first point of the next instruction
////            PointList nextPoints = instructions.get(index + 1).getPoints();
//            MapObj mapObj1 = computeInterval(instructions.get(index + 1));
//            pointList.add(mapObj1.pointList.getLat(0), mapObj1.pointList.getLon(0));
//        } else if (pointList.size() == 1) {
//            // Duplicate the last point in the arrive instruction, if the size is 1
//            pointList.add(pointList.getLat(0), pointList.getLon(0), pointList.getEle(0));
//        }
//
//        putLocation(pointList.getLat(0), pointList.getLon(0), intersection);
//
//        instructionJson.put("driving_side", "right");
//
//        // Does not include elevation
//        instructionJson.put("geometry", WebHelper.encodePolyline(pointList, false, 1e6));
//
//        // TODO: how about other modes?
//        instructionJson.put("mode", "driving");
//
//        putManeuver(instruction, instructionJson, locale, trMap, isFirstInstructionOfLeg);
//
//        // TODO distance = weight, is weight even important?
//        double distance = Helper.round(instruction.getDistance(), 1);
//        instructionJson.put("weight", distance);
//        instructionJson.put("duration", convertToSeconds(instruction.getTime()));
//        instructionJson.put("name", instruction.getName());
//        instructionJson.put("distance", distance);
//
//        ArrayNode voiceInstructions = instructionJson.putArray("voiceInstructions");
//        ArrayNode bannerInstructions = instructionJson.putArray("bannerInstructions");
//
//        // Voice and banner instructions are empty for the last element
//        if (index + 1 < instructions.size()) {
//            putVoiceInstructions(instructions, distance, index, locale, trMap, mtrMap, voiceInstructions, distanceConfig);
//            putBannerInstructions(instructions, distance, index, locale, trMap, bannerInstructions);
//        }
//
//        return instructionJson;
//    }
//
//        private static void putVoiceInstructions(ArrayList<InstructionResponse> instructions, double distance, int index, Locale locale, TranslationMap translationMap, TranslationMap navigateResponseConverterTranslationMap, ArrayNode voiceInstructions, DistanceConfig distanceConfig) {
//        /*
//            A VoiceInstruction Object looks like this
//            {
//                distanceAlongGeometry: 40.9,
//                announcement: "Exit the traffic circle",
//                ssmlAnnouncement: "<speak><amazon:effect name="drc"><prosody rate="1.08">Exit the traffic circle</prosody></amazon:effect></speak>",
//            }
//        */
//        InstructionResponse nextInstruction = instructions.get(index + 1);
//        String turnDescription = nextInstruction.getTurnDescription(translationMap.getWithFallBack(locale));
//
//        String thenVoiceInstruction = getThenVoiceInstructionpart(instructions, index, locale, translationMap, navigateResponseConverterTranslationMap);
//
//        List<VoiceInstructionConfig.VoiceInstructionValue> voiceValues = distanceConfig.getVoiceInstructionsForDistance(distance, turnDescription, thenVoiceInstruction);
//
//        for (VoiceInstructionConfig.VoiceInstructionValue voiceValue : voiceValues) {
//            putSingleVoiceInstruction(voiceValue.spokenDistance, voiceValue.turnDescription, voiceInstructions);
//        }
//
//        // Speak 80m instructions 80 before the turn
//        // Note: distanceAlongGeometry: "how far from the upcoming maneuver the voice instruction should begin"
//        double distanceAlongGeometry = Helper.round(Math.min(distance, 80), 1);
//
//        // Special case for the arrive instruction
//        if (index + 2 == instructions.size())
//            distanceAlongGeometry = Helper.round(Math.min(distance, 25), 1);
//
//        putSingleVoiceInstruction(distanceAlongGeometry, turnDescription + thenVoiceInstruction, voiceInstructions);
//    }
//
//        private static void putSingleVoiceInstruction(double distanceAlongGeometry, String turnDescription, ArrayNode voiceInstructions) {
//        ObjectNode voiceInstruction = voiceInstructions.addObject();
//        voiceInstruction.put("distanceAlongGeometry", distanceAlongGeometry);
//        //TODO: ideally, we would even generate instructions including the instructions after the next like turn left **then** turn right
//        voiceInstruction.put("announcement", turnDescription);
//        voiceInstruction.put("ssmlAnnouncement", "<speak><amazon:effect name=\"drc\"><prosody rate=\"1.08\">" + turnDescription + "</prosody></amazon:effect></speak>");
//    }
//
//        /**
//         * For close turns, it is important to announce the next turn in the earlier instruction.
//         * e.g.: instruction i+1= turn right, instruction i+2=turn left, with instruction i+1 distance < VOICE_INSTRUCTION_MERGE_TRESHHOLD
//         * The voice instruction should be like "turn right, then turn left"
//         * <p>
//         * For instruction i+1 distance > VOICE_INSTRUCTION_MERGE_TRESHHOLD an empty String will be returned
//         */
//        private static String getThenVoiceInstructionpart(ArrayList<InstructionResponse> instructions, int index, Locale locale, TranslationMap translationMap, TranslationMap navigateResponseConverterTranslationMap) {
//        if (instructions.size() > index + 2) {
//            InstructionResponse firstInstruction = instructions.get(index + 1);
//            if (firstInstruction.getDistance() < VOICE_INSTRUCTION_MERGE_TRESHHOLD) {
//                InstructionResponse secondInstruction = instructions.get(index + 2);
//                if (secondInstruction.getSign() != InstructionResponse.REACHED_VIA)
//                    return ", " + navigateResponseConverterTranslationMap.getWithFallBack(locale).tr("then") + " " + secondInstruction.getTurnDescription(translationMap.getWithFallBack(locale));
//            }
//        }
//
//        return "";
//    }
//
//        /**
//         * Banner instructions are the turn instructions that are shown to the user in the top bar.
//         * <p>
//         * Between two instructions we can show multiple banner instructions, you can control when they pop up using distanceAlongGeometry.
//         */
//        private static void putBannerInstructions(ArrayList<InstructionResponse> instructions, double distance, int index, Locale locale, TranslationMap translationMap, ArrayNode bannerInstructions) {
//        /*
//        A BannerInstruction looks like this
//        distanceAlongGeometry: 107,
//        primary: {
//            text: "Lichtensteinstraße",
//            components: [
//            {
//                text: "Lichtensteinstraße",
//                type: "text",
//            }
//            ],
//            type: "turn",
//            modifier: "right",
//        },
//        secondary: null,
//         */
//
//        ObjectNode bannerInstruction = bannerInstructions.addObject();
//
//        //Show from the beginning
//        bannerInstruction.put("distanceAlongGeometry", distance);
//
//        ObjectNode primary = bannerInstruction.putObject("primary");
//        putSingleBannerInstruction(instructions.get(index + 1), locale, translationMap, primary);
//
//        bannerInstruction.putNull("secondary");
//
//        if (instructions.size() > index + 2 && instructions.get(index + 2).getSign() != InstructionResponse.REACHED_VIA) {
//            // Sub shows the instruction after the current one
//            ObjectNode sub = bannerInstruction.putObject("sub");
//            putSingleBannerInstruction(instructions.get(index + 2), locale, translationMap, sub);
//        }
//    }
//
//        private static void putSingleBannerInstruction(InstructionResponse instruction, Locale locale, TranslationMap translationMap, ObjectNode singleBannerInstruction) {
//        String bannerInstructionName = instruction.getName();
//        if (bannerInstructionName == null || bannerInstructionName.isEmpty()) {
//            // Fix for final instruction and for instructions without name
//            bannerInstructionName = instruction.getTurnDescription(translationMap.getWithFallBack(locale));
//
//            // Uppercase first letter
//            // TODO: should we do this for all cases? Then we might change the spelling of street names though
//            bannerInstructionName = Helper.firstBig(bannerInstructionName);
//        }
//
//        singleBannerInstruction.put("text", bannerInstructionName);
//
//        ArrayNode components = singleBannerInstruction.putArray("components");
//        ObjectNode component = components.addObject();
//        component.put("text", bannerInstructionName);
//        component.put("type", "text");
//
//        singleBannerInstruction.put("type", getTurnType(instruction, false));
//        String modifier = getModifier(instruction);
//        if (modifier != null)
//            singleBannerInstruction.put("modifier", modifier);
//
//        if (instruction.getSign() == InstructionResponse.USE_ROUNDABOUT) {
////            if (instruction instanceof RoundaboutInstruction) {
//                double turnAngle = instruction.getTurnAngle();
//                if (Double.isNaN(turnAngle)) {
//                    singleBannerInstruction.putNull("degrees");
//                } else {
//                    double degree = (Math.abs(turnAngle) * 180) / Math.PI;
//                    singleBannerInstruction.put("degrees", degree);
//                }
////            }
//        }
//    }
//
//        private static void putManeuver(InstructionResponse instruction, ObjectNode instructionJson, Locale locale, TranslationMap translationMap, boolean isFirstInstructionOfLeg) {
//        ObjectNode maneuver = instructionJson.putObject("maneuver");
//        maneuver.put("bearing_after", 0);
//        maneuver.put("bearing_before", 0);
//
////        PointList points = instruction.getPoints();
//        putLocation(0, 0, maneuver);
//
//        String modifier = getModifier(instruction);
//        if (modifier != null)
//            maneuver.put("modifier", modifier);
//
//        maneuver.put("type", getTurnType(instruction, isFirstInstructionOfLeg));
//        // exit number
//        if (instruction.getSign() ==  InstructionResponse.USE_ROUNDABOUT || instruction.getSign() ==  InstructionResponse.LEAVE_ROUNDABOUT  )
//            maneuver.put("exit", instruction.getExitNumber());
//
//        maneuver.put("instruction", instruction.getTurnDescription(translationMap.getWithFallBack(locale)));
//
//    }
//
//        /**
//         * Relevant maneuver types are:
//         * depart (firs instruction)
//         * turn (regular turns)
//         * roundabout (enter roundabout, maneuver contains also the exit number)
//         * arrive (last instruction and waypoints)
//         * <p>
//         * You can find all maneuver types at: https://www.mapbox.com/api-documentation/#maneuver-types
//         */
//        private static String getTurnType(InstructionResponse instruction, boolean isFirstInstructionOfLeg) {
//        if (isFirstInstructionOfLeg) {
//            return "depart";
//        } else {
//            switch (instruction.getSign()) {
//                case InstructionResponse.FINISH:
//                case InstructionResponse.REACHED_VIA:
//                    return "arrive";
//                case InstructionResponse.USE_ROUNDABOUT:
//                    return "roundabout";
//                default:
//                    return "turn";
//            }
//        }
//    }
//
//        /**
//         * No modifier values for arrive and depart
//         * <p>
//         * Find modifier values here: https://www.mapbox.com/api-documentation/#stepmaneuver-object
//         */
//        private static String getModifier(InstructionResponse instruction) {
//        switch (instruction.getSign()) {
//            case InstructionResponse.CONTINUE_ON_STREET:
//                return "straight";
//            case InstructionResponse.U_TURN_LEFT:
//            case InstructionResponse.U_TURN_RIGHT:
//            case InstructionResponse.U_TURN_UNKNOWN:
//                return "uturn";
//            case InstructionResponse.KEEP_LEFT:
//            case InstructionResponse.TURN_SLIGHT_LEFT:
//                return "slight left";
//            case InstructionResponse.TURN_LEFT:
//                return "left";
//            case InstructionResponse.TURN_SHARP_LEFT:
//                return "sharp left";
//            case InstructionResponse.KEEP_RIGHT:
//            case InstructionResponse.TURN_SLIGHT_RIGHT:
//                return "slight right";
//            case InstructionResponse.TURN_RIGHT:
//                return "right";
//            case InstructionResponse.TURN_SHARP_RIGHT:
//                return "sharp right";
//            case InstructionResponse.USE_ROUNDABOUT:
//                // TODO: This might be an issue in left-handed traffic, because there it schould be left
//                return "right";
//            default:
//                return null;
//        }
//    }
//
//        /**
//         * Puts a location array in GeoJson format into the node
//         */
//        private static ObjectNode putLocation(double lat, double lon, ObjectNode node) {
//        ArrayNode location = node.putArray("location");
//        // GeoJson lon,lat
//        location.add(Helper.round6(lon));
//        location.add(Helper.round6(lat));
//        return node;
//    }
//
//        /**
//         * Mapbox uses seconds instead of milliSeconds
//         */
//        private static double convertToSeconds(double milliSeconds) {
//        return Helper.round(milliSeconds / 1000, 1);
//    }
//
//        public static ObjectNode convertFromGHResponseError(GHResponse ghResponse) {
//        ObjectNode json = JsonNodeFactory.instance.objectNode();
//        // TODO we could make this more fine grained
//        json.put("code", "InvalidInput");
//        json.put("message", ghResponse.getErrors().get(0).getMessage());
//        return json;
//    }
//    }
