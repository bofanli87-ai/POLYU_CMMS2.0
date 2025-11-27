package com.polyu.cmms.model;

import java.util.Date;

public class Activity {
    private int activityId;
    private String activityType;
    private String title;
    private String description;
    private String status;
    private String priority;
    private Date date;
    private Integer expectedDowntime; // 存储分钟数
    private Date actualCompletionDatetime;
    private int createdByStaffId;
    private Integer weatherId;
    private Integer buildingId;
    private Integer areaId;
    private String hazardLevel;
    private String facilityType;
    private Integer roomId;
    private Integer levelId;
    private Integer squareId;
    private Integer gateId;
    private Integer canteenId;
    private String activeFlag;
    
    // constructor
    public Activity() {}
    
    public Activity(int activityId, String activityType, String title, String description, 
                   String status, Date date, Integer expectedDowntime, int createdByStaffId, 
                   Integer weatherId, Integer buildingId, Integer areaId, String hazardLevel) {
        this.activityId = activityId;
        this.activityType = activityType;
        this.title = title;
        this.description = description;
        this.status = status;
        this.date = date;
        this.expectedDowntime = expectedDowntime;
        this.createdByStaffId = createdByStaffId;
        this.weatherId = weatherId;
        this.buildingId = buildingId;
        this.areaId = areaId;
        this.hazardLevel = hazardLevel;
        // 设置默认值
        this.priority = "medium";
        this.facilityType = "none";
        this.activeFlag = "Y";
    }


    // getter and setter methods
    public int getActivityId() { return activityId; }
    public void setActivityId(int activityId) { this.activityId = activityId; }
    public String getActivityType() { return activityType; }
    public void setActivityType(String activityType) { this.activityType = activityType; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public Date getDate() { return date; }
    public void setDate(Date date) { this.date = date; }
    public Integer getExpectedDowntime() { return expectedDowntime; }
    public void setExpectedDowntime(Integer expectedDowntime) { this.expectedDowntime = expectedDowntime; }
    public Date getActualCompletionDatetime() { return actualCompletionDatetime; }
    public void setActualCompletionDatetime(Date actualCompletionDatetime) { this.actualCompletionDatetime = actualCompletionDatetime; }
    public int getCreatedByStaffId() { return createdByStaffId; }
    public void setCreatedByStaffId(int createdByStaffId) { this.createdByStaffId = createdByStaffId; }
    public Integer getWeatherId() { return weatherId; }
    public void setWeatherId(Integer weatherId) { this.weatherId = weatherId; }
    public Integer getBuildingId() { return buildingId; }
    public void setBuildingId(Integer buildingId) { this.buildingId = buildingId; }
    public Integer getAreaId() { return areaId; }
    public void setAreaId(Integer areaId) { this.areaId = areaId; }
    public String getHazardLevel() { return hazardLevel; }
    public void setHazardLevel(String hazardLevel) { this.hazardLevel = hazardLevel; }
    public String getFacilityType() { return facilityType; }
    public void setFacilityType(String facilityType) { this.facilityType = facilityType; }
    public Integer getRoomId() { return roomId; }
    public void setRoomId(Integer roomId) { this.roomId = roomId; }
    public Integer getLevelId() { return levelId; }
    public void setLevelId(Integer levelId) { this.levelId = levelId; }
    public Integer getSquareId() { return squareId; }
    public void setSquareId(Integer squareId) { this.squareId = squareId; }
    public Integer getGateId() { return gateId; }
    public void setGateId(Integer gateId) { this.gateId = gateId; }
    public Integer getCanteenId() { return canteenId; }
    public void setCanteenId(Integer canteenId) { this.canteenId = canteenId; }
    public String getActiveFlag() { return activeFlag; }
    public void setActiveFlag(String activeFlag) { this.activeFlag = activeFlag; }
}