package com.polyu.cmms.model;

import java.util.Date;

/**
 * WorksFor模型类，对应works_for表
 * 表示员工参与活动的关联关系
 */
public class WorksFor {
    private Integer worksForId;
    private Integer staffId;
    private Integer activityId;
    private String activityResponsibility;
    private Date assignedDatetime;
    private String activeFlag;

    // 构造函数
    public WorksFor() {
    }

    public WorksFor(Integer staffId, Integer activityId, String activityResponsibility) {
        this.staffId = staffId;
        this.activityId = activityId;
        this.activityResponsibility = activityResponsibility;
        this.assignedDatetime = new Date();
        this.activeFlag = "Y";
    }

    // Getter和Setter方法
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