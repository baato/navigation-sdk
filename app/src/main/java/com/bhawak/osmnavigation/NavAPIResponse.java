package com.bhawak.osmnavigation;

import java.util.List;

public class NavAPIResponse {
    private String timestamp;
    private Integer status;
    private String message;
    private List<NavigationResponse> data;

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<NavigationResponse> getData() {
        return data;
    }

    public void setData(List<NavigationResponse> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "DirectionsAPIResponse{" +
                "timestamp='" + timestamp + '\'' +
                ", status=" + status +
                ", message='" + message + '\'' +
                ", data=" + data +
                '}';
    }

}
