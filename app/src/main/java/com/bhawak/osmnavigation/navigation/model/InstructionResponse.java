package com.bhawak.osmnavigation.navigation.model;


import androidx.annotation.Keep;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.graphhopper.util.Helper;
import com.graphhopper.util.Instruction;
import com.graphhopper.util.Translation;

import retrofit2.http.Header;

@Keep
public class InstructionResponse {
    public static final int UNKNOWN = -99;
    public static final int U_TURN_UNKNOWN = -98;
    public static final int U_TURN_LEFT = -8;
    public static final int KEEP_LEFT = -7;
    public static final int LEAVE_ROUNDABOUT = -6;
    public static final int TURN_SHARP_LEFT = -3;
    public static final int TURN_LEFT = -2;
    public static final int TURN_SLIGHT_LEFT = -1;
    public static final int CONTINUE_ON_STREET = 0;
    public static final int TURN_SLIGHT_RIGHT = 1;
    public static final int TURN_RIGHT = 2;
    public static final int TURN_SHARP_RIGHT = 3;
    public static final int FINISH = 4;
    public static final int REACHED_VIA = 5;
    public static final int USE_ROUNDABOUT = 6;
    public static final int IGNORE = -2147483648;
    public static final int KEEP_RIGHT = 7;
    public static final int U_TURN_RIGHT = 8;
    public static final int PT_START_TRIP = 101;
    public static final int PT_TRANSFER = 102;
    public static final int PT_END_TRIP = 103;

    @SerializedName("points")
    @Expose
    private BaatoPoints points;
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
    private Double distance;
    @SerializedName("time")
    @Expose
    private Integer time;
    @SerializedName("extraInfoJSON")
    @Expose
    private ExtraInfoJSON extraInfoJSON;
    @SerializedName("length")
    @Expose
    private Integer length;
    @SerializedName("exitNumber")
    @Expose
    private Integer exitNumber;
    @SerializedName("exited")
    @Expose
    private Boolean exited;
    @SerializedName("turnAngle")
    @Expose
    private String turnAngle;

    public BaatoPoints getPoints() {
        return points;
    }

    public void setPoints(BaatoPoints points) {
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

    public Double getDistance() {
        return distance;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public Integer getTime() {
        return time;
    }

    public void setTime(Integer time) {
        this.time = time;
    }

    public ExtraInfoJSON getExtraInfoJSON() {
        return extraInfoJSON;
    }

    public void setExtraInfoJSON(ExtraInfoJSON extraInfoJSON) {
        this.extraInfoJSON = extraInfoJSON;
    }

    public Integer getLength() {
        return length;
    }

    public void setLength(Integer length) {
        this.length = length;
    }

    public Integer getExitNumber() {
        return exitNumber;
    }

    public void setExitNumber(Integer exitNumber) {
        this.exitNumber = exitNumber;
    }

    public Boolean getExited() {
        return exited;
    }

    public void setExited(Boolean exited) {
        this.exited = exited;
    }

    public Double getTurnAngle() {
        return turnAngle.matches("\\d([\\d.]*\\d)?") ? Double.parseDouble(turnAngle) : 0.00;
    }

    public void setTurnAngle(String turnAngle) {
        this.turnAngle = turnAngle;
    }
    public class BaatoPoints {

        @SerializedName("size")
        @Expose
        private Integer size;
        @SerializedName("intervalString")
        @Expose
        private String intervalString;
        @SerializedName("immutable")
        @Expose
        private Boolean immutable;
        @SerializedName("dimension")
        @Expose
        private Integer dimension;
        @SerializedName("3D")
        @Expose
        private Boolean _3D;
        @SerializedName("empty")
        @Expose
        private Boolean empty;

        public Integer getSize() {
            return size;
        }

        public void setSize(Integer size) {
            this.size = size;
        }

        public String getIntervalString() {
            return intervalString;
        }

        public void setIntervalString(String intervalString) {
            this.intervalString = intervalString;
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

        public Boolean getEmpty() {
            return empty;
        }

        public void setEmpty(Boolean empty) {
            this.empty = empty;
        }

    }
    public String getTurnDescription(Translation tr) {
            String streetName = this.getName();
            int indi = this.getSign();
            String str;
            if (indi == 0) {
                str = Helper.isEmpty(streetName) ? tr.tr("continue", new Object[0]) : tr.tr("continue_onto", new Object[]{streetName});
            } else if (indi == 101) {
                str = tr.tr("pt_start_trip", new Object[]{streetName});
            } else if (indi == 102) {
                str = tr.tr("pt_transfer_to", new Object[]{streetName});
            } else if (indi == 103) {
                str = tr.tr("pt_end_trip", new Object[]{streetName});
            } else if (indi == Instruction.USE_ROUNDABOUT) {
                if (!exited) {
                    str = tr.tr("roundabout_enter");
                } else {
                    str = Helper.isEmpty(streetName) ? tr.tr ("roundabout_exit", getExitNumber()): tr.tr(
                            "roundabout_exit_onto", getExitNumber(), streetName);
                }
            } else {
                String dir = null;
                switch(indi) {
                    case -98:
                        dir = tr.tr("u_turn", new Object[0]);
                        break;
                    case -8:
                        dir = tr.tr("u_turn", new Object[0]);
                        break;
                    case -7:
                        dir = tr.tr("keep_left", new Object[0]);
                        break;
                    case -3:
                        dir = tr.tr("turn_sharp_left", new Object[0]);
                        break;
                    case -2:
                        dir = tr.tr("turn_left", new Object[0]);
                        break;
                    case -1:
                        dir = tr.tr("turn_slight_left", new Object[0]);
                        break;
                    case 1:
                        dir = tr.tr("turn_slight_right", new Object[0]);
                        break;
                    case 2:
                        dir = tr.tr("turn_right", new Object[0]);
                        break;
                    case 3:
                        dir = tr.tr("turn_sharp_right", new Object[0]);
                        break;
                    case 4:
                        dir = tr.tr("arrive", new Object[0]);
                        break;
                    case 7:
                        dir = tr.tr("keep_right", new Object[0]);
                        break;
                    case 8:
                        dir = tr.tr("u_turn", new Object[0]);
                        break;
                }

                if (dir == null) {
                    str = tr.tr("unknown", new Object[]{indi});
                } else {
                    str = Helper.isEmpty(streetName) ? dir : tr.tr("turn_onto", new Object[]{dir, streetName});
                }
            }

            return str;
    }
}
