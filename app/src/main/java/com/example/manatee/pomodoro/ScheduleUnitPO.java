package com.example.manatee.pomodoro;

import android.database.Cursor;
import android.util.Log;

import java.util.Calendar;

/**
 * Created by manatee on 2016/4/12.
 */
public class ScheduleUnitPO {
    private Long unitId;
    private Long startTime;
    private Long endTime;
    private String message;
    private Long overflow;
    private Integer isfinish;

    public ScheduleUnitPO(ScheduleUnit unitDTO) {
        assert (unitDTO != null);
        unitId = unitDTO.getUnitId();
        startTime = unitDTO.getStartTime();
        endTime = unitDTO.getEndTime();
        message = unitDTO.getMessage();
        overflow = unitDTO.getOverflow();
        isfinish = unitDTO.getIsfinish();
    }

    public ScheduleUnitPO(Cursor cursor) {
        assert (cursor != null);
        unitId = cursor.getLong(cursor.getColumnIndex("id"));
        startTime = cursor.getLong(cursor.getColumnIndex("start"));
        endTime = cursor.getLong(cursor.getColumnIndex("end"));
        message = cursor.getString(cursor.getColumnIndex("message"));
        overflow = cursor.getLong(cursor.getColumnIndex("overflow"));
        isfinish = cursor.getInt(cursor.getColumnIndex("isfinish"));
    }

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

    public void setStartCalendar(Calendar startTime) {
        this.setStartTime(startTime.getTimeInMillis());
    }

    public Calendar getStartCalendar() {
        Calendar startTime = Calendar.getInstance();
        startTime.setTimeInMillis(this.getStartTime());
        return startTime;
    }

    public void setEndCalendar(Calendar endTime) {
        this.setEndTime(endTime.getTimeInMillis());
    }

    public Calendar getEndCalendar() {
        Calendar endTime = Calendar.getInstance();
        endTime.setTimeInMillis(this.getStartTime());
        return endTime;
    }

    public ScheduleUnit toBmob() {
        ScheduleUnit unitBmob = new ScheduleUnit();
        unitBmob.setUnitId(this.getUnitId());
        unitBmob.setStartTime(this.getStartTime());
        unitBmob.setEndTime(this.getEndTime());
        unitBmob.setMessage(this.getMessage());
        unitBmob.setOverflow(this.getOverflow());
        unitBmob.setIsfinish(this.getIsfinish());
        //unitBmob.setObjectId(Long.toString(this.getId()));
        return unitBmob;
    }

    public boolean isSame(ScheduleUnit unitBmob){
        if (unitBmob.getUnitId() != this.getUnitId())
            return false;
        if (unitBmob.getStartTime() != this.getStartTime() ||
                unitBmob.getEndTime() != this.getEndTime() ||
                unitBmob.getMessage() != this.getMessage() ||
                unitBmob.getOverflow() != this.getOverflow() ||
                unitBmob.getIsfinish() != this.getIsfinish() )
            return false;
        return true;
    }

    public boolean isSame(ScheduleUnitPO unitPO){
        if (!unitPO.getUnitId().equals(this.getUnitId()))
            return false;

        if (!unitPO.getStartTime().equals(this.getStartTime()) ||
                !unitPO.getEndTime().equals(this.getEndTime()) ||
                !unitPO.getMessage().equals(this.getMessage()) ||
                !unitPO.getOverflow().equals(this.getOverflow()) ||
                !unitPO.getIsfinish().equals(this.getIsfinish()) )
            return false;

        return true;
    }
}
