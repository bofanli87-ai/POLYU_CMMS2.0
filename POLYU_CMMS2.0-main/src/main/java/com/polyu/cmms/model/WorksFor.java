package com.polyu.cmms.model;

import java.util.Date;

/**
 * WorksFor model，relate to works_for table
 * Indicates the relationship between employee participation and activities
 */
public class WorksFor {
    private Integer worksForId;
    private Integer staffId;
    private Integer activityId;
    private String activityResponsibility;
    private Date assignedDatetime;
    private String activeFlag;

    // constructor
    public WorksFor() {
    }

    public WorksFor(Integer staffId, Integer activityId, String activityResponsibility) {
        this.staffId = staffId;
        this.activityId = activityId;
        this.activityResponsibility = activityResponsibility;
        this.assignedDatetime = new Date();
        this.activeFlag = "Y";
    }

    // Getter and Setter methods
    public Integer getWorksForId() {
        return worksForId;
    }

    public void setWorksForId(Integer worksForId) {
        this.worksForId = worksForId;
    }

    public Integer getStaffId() {
        return staffId;
    }

    public void setStaffId(Integer staffId) {
        this.staffId = staffId;
    }

    public Integer getActivityId() {
        return activityId;
    }

    public void setActivityId(Integer activityId) {
        this.activityId = activityId;
    }

    public String getActivityResponsibility() {
        return activityResponsibility;
    }

    public void setActivityResponsibility(String activityResponsibility) {
        this.activityResponsibility = activityResponsibility;
    }

    public Date getAssignedDatetime() {
        return assignedDatetime;
    }

    public void setAssignedDatetime(Date assignedDatetime) {
        this.assignedDatetime = assignedDatetime;
    }

    public String getActiveFlag() {
        return activeFlag;
    }

    public void setActiveFlag(String activeFlag) {
        this.activeFlag = activeFlag;
    }

    @Override
    public String toString() {
        return "WorksFor{" +
                "worksForId=" + worksForId +
                ", staffId=" + staffId +
                ", activityId=" + activityId +
                ", activityResponsibility='" + activityResponsibility + '\'' +
                ", assignedDatetime=" + assignedDatetime +
                ", activeFlag='" + activeFlag + '\'' +
                '}';
    }
}