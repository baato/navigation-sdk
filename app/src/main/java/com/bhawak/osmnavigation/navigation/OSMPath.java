package com.bhawak.osmnavigation.navigation;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.graphhopper.util.details.PathDetail;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class OSMPath {
    @SerializedName("errors")
    @Expose
    private List<Object> errors = null;
    @SerializedName("description")
    @Expose
    private List<Object> description = null;
    @SerializedName("distance")
    @Expose
    private Double distance;
    @SerializedName("ascend")
    @Expose
    private Integer ascend;
    @SerializedName("descend")
    @Expose
    private Integer descend;
    @SerializedName("routeWeight")
    @Expose
    private Double routeWeight;
    @SerializedName("time")
    @Expose
    private Integer time;
    @SerializedName("debugInfo")
    @Expose
    private String debugInfo;
    @SerializedName("instructions")
    @Expose
    private List<OSMInstruction> instructions = null;
    @SerializedName("numChanges")
    @Expose
    private Integer numChanges;
    @SerializedName("legs")
    @Expose
    private List<Object> legs = null;
    @SerializedName("pointsOrder")
    @Expose
    private List<Object> pointsOrder = null;
    @SerializedName("pathDetails")
    @Expose
    private final Map<String, List<PathDetail>> pathDetails = null;
    @SerializedName("fare")
    @Expose
    private Object fare;
    @SerializedName("impossible")
    @Expose
    private Boolean impossible;
    @SerializedName("waypoints")
    @Expose
    private Waypoints waypoints;
    @SerializedName("points")
    @Expose
    private Points points;

    public List<Object> getErrors() {
        return errors;
    }

    public void setErrors(List<Object> errors) {
        this.errors = errors;
    }

    public List<Object> getDescription() {
        return description;
    }

    public void setDescription(List<Object> description) {
        this.description = description;
    }

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Integer getAscend() {
        return ascend;
    }

    public void setAscend(Integer ascend) {
        this.ascend = ascend;
    }

    public Integer getDescend() {
        return descend;
    }

    public void setDescend(Integer descend) {
        this.descend = descend;
    }

    public Double getRouteWeight() {
        return routeWeight;
    }

    public void setRouteWeight(Double routeWeight) {
        this.routeWeight = routeWeight;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public String getDebugInfo() {
        return debugInfo;
    }

    public void setDebugInfo(String debugInfo) {
        this.debugInfo = debugInfo;
    }

    public List<OSMInstruction> getInstructions() {
        return instructions;
    }

    public void setInstructions(List<OSMInstruction> instructions) {
        this.instructions = instructions;
    }

    public Integer getNumChanges() {
        return numChanges;
    }

    public void setNumChanges(Integer numChanges) {
        this.numChanges = numChanges;
    }

    public List<Object> getLegs() {
        return legs;
    }

    public void setLegs(List<Object> legs) {
        this.legs = legs;
    }

    public List<Object> getPointsOrder() {
        return pointsOrder;
    }

    public void setPointsOrder(List<Object> pointsOrder) {
        this.pointsOrder = pointsOrder;
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

    public Object getFare() {
        return fare;
    }

    public void setFare(Object fare) {
        this.fare = fare;
    }

    public Boolean getImpossible() {
        return impossible;
    }

    public void setImpossible(Boolean impossible) {
        this.impossible = impossible;
    }

    public Waypoints getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(Waypoints waypoints) {
        this.waypoints = waypoints;
    }

    public Points getPoints() {
        return points;
    }

    public void setPoints(Points points) {
        this.points = points;
    }

}

class Annotation {
    @SerializedName("empty")
    @Expose
    private Boolean empty;
    @SerializedName("importance")
    @Expose
    private Integer importance;
    @SerializedName("message")
    @Expose
    private String message;

    public Boolean getEmpty() {
        return empty;
    }

    public void setEmpty(Boolean empty) {
        this.empty = empty;
    }

    public Integer getImportance() {
        return importance;
    }

    public void setImportance(Integer importance) {
        this.importance = importance;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}

class ExtraInfoJSON {

    @SerializedName("heading")
    @Expose
    private Double heading;
    @SerializedName("last_heading")
    @Expose
    private Double lastHeading;

    public Double getHeading() {
        return heading;
    }

    public void setHeading(Double heading) {
        this.heading = heading;
    }

    public Double getLastHeading() {
        return lastHeading;
    }

    public void setLastHeading(Double lastHeading) {
        this.lastHeading = lastHeading;
    }

}
class OSMInstruction {

    @SerializedName("points")
    @Expose
    private Points points;
    @SerializedName("annotation")
    @Expose
    private Annotation annotation;
    @SerializedName("sign")
    @Expose
    private Integer sign;
    @SerializedName("name")
    @Expose
    private String name;
    @SerializedName("distance")
    @Expose
    private Integer distance;
    @SerializedName("time")
    @Expose
    private Integer time;
    @SerializedName("length")
    @Expose
    private Integer length;
    @SerializedName("extraInfoJSON")
    @Expose
    private ExtraInfoJSON extraInfoJSON;

    public Points getPoints() {
        return points;
    }

    public void setPoints(Points points) {
        this.points = points;
    }

    public Annotation getAnnotation() {
        return annotation;
    }

    public void setAnnotation(Annotation annotation) {
        this.annotation = annotation;
    }

    public Integer getSign() {
        return sign;
    }

    public void setSign(Integer sign) {
        this.sign = sign;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getDistance() {
        return distance;
    }

    public void setDistance(Integer distance) {
        this.distance = distance;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public ExtraInfoJSON getExtraInfoJSON() {
        return extraInfoJSON;
    }

    public void setExtraInfoJSON(ExtraInfoJSON extraInfoJSON) {
        this.extraInfoJSON = extraInfoJSON;
    }

}

class Points {

    @SerializedName("size")
    @Expose
    private Integer size;
    @SerializedName("empty")
    @Expose
    private Boolean empty;
    @SerializedName("immutable")
    @Expose
    private Boolean immutable;
    @SerializedName("dimension")
    @Expose
    private Integer dimension;
    @SerializedName("3D")
    @Expose
    private Boolean _3D;

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Boolean getEmpty() {
        return empty;
    }

    public void setEmpty(Boolean empty) {
        this.empty = empty;
    }

    public Boolean getImmutable() {
        return immutable;
    }

    public void setImmutable(Boolean immutable) {
        this.immutable = immutable;
    }

    public Integer getDimension() {
        return dimension;
    }

    public void setDimension(Integer dimension) {
        this.dimension = dimension;
    }

    public Boolean get3D() {
        return _3D;
    }

    public void set3D(Boolean _3D) {
        this._3D = _3D;
    }

}
class Waypoints {
    @SerializedName("size")
    @Expose
    private Integer size;
    @SerializedName("empty")
    @Expose
    private Boolean empty;
    @SerializedName("immutable")
    @Expose
    private Boolean immutable;
    @SerializedName("dimension")
    @Expose
    private Integer dimension;
    @SerializedName("3D")
    @Expose
    private Boolean _3D;

    public Integer getSize() {
        return size;
    }

    public void setSize(Integer size) {
        this.size = size;
    }

    public Boolean getEmpty() {
        return empty;
    }

    public void setEmpty(Boolean empty) {
        this.empty = empty;
    }

    public Boolean getImmutable() {
        return immutable;
    }

    public void setImmutable(Boolean immutable) {
        this.immutable = immutable;
    }

    public Integer getDimension() {
        return dimension;
    }

    public void setDimension(Integer dimension) {
        this.dimension = dimension;
    }

    public Boolean get3D() {
        return _3D;
    }

    public void set3D(Boolean _3D) {
        this._3D = _3D;
    }
}
