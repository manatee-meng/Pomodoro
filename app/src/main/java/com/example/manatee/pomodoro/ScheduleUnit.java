package com.example.manatee.pomodoro;

import cn.bmob.v3.BmobObject;

/**
 * Created by manatee on 2016/4/11.
 *
 */
public class ScheduleUnit extends BmobObject {
    private Long unitId;
    private Long startTime;
    private Long endTime;
    private String message;
    private Long overflow;
    private Integer isfinish;

    public Long getUnitId() {
        return this.unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public Long getStartTime() {
        return this.startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return this.endTime;
    }

    public void setEndTime(Long EndTime) {
        this.endTime = EndTime;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Long getOverflow() {
        return this.overflow;
    }

    public void setOverflow(Long overflow) {
        this.overflow = overflow;
    }

    public Integer getIsfinish() {
        return this.isfinish;
    }

    public void setIsfinish(Integer isfinish) {
        this.isfinish = isfinish;
    }
}
