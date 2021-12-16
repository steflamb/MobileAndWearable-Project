package com.example.pathsocial;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.mapbox.geojson.Point;

import java.util.List;

public class TrailForDb {

    public TrailForDb()
    {}
    public String pathid;
    public String usrId;
    public String firstName;
    public String description;
    public String duration;
    public String datetime;
    public double startingPointLat;
    public double startingPointLongit;
    public Integer stepsNubers;
    public Integer calories;
    public Double distance;
    public Double medianSpeed;
    public String weather;
    public String trailData;
    public Integer reviewPositive;
    public Integer reviewNegative;

    public void setUsrId(String usrId){this.usrId= usrId; }

    public void setPathid(String pathid) {
        this.pathid = pathid;
    }

    public void setCalories(Integer calories) {
        this.calories = calories;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDistance(Double distance) {
        this.distance = distance;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setMedianSpeed(Double medianSpeed) {
        this.medianSpeed = medianSpeed;
    }

    public void setStartingPointLat(double startingPointLat) {
        this.startingPointLat = startingPointLat;
    }

    public void setStartingPointLongit(double startingPointLongit) {
        this.startingPointLongit = startingPointLongit;
    }

    public void setStepsNubers(Integer stepsNubers) {
        this.stepsNubers = stepsNubers;
    }

    public void setTrailData(String trailData) {
        this.trailData = trailData;
    }

    public void setWeather(String weather) {
        this.weather = weather;
    }

    public Double getDistance() {
        return distance;
    }

    public Double getMedianSpeed() {
        return medianSpeed;
    }

    public double getStartingPointLat() {
        return startingPointLat;
    }

    public double getStartingPointLongit() {
        return startingPointLongit;
    }

    public Integer getCalories() {
        return calories;
    }

    public Integer getStepsNubers() {
        return stepsNubers;
    }

    public String getTrailData() {
        return trailData;
    }

    public String getDatetime() {
        return datetime;
    }

    public String getDescription() {
        return description;
    }

    public String getDuration() {
        return duration;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getPathid() {
        return pathid;
    }

    public String getWeather() {
        return weather;
    }

    public String getUsrId() {
        return usrId;
    }

    public Integer getReviewNegative() {
        return reviewNegative;
    }

    public Integer getReviewPositive()
    {
        return reviewPositive;
    }

    public void setReviewPositive(Integer reviewPositive)
    {
        this.reviewPositive=reviewPositive;
    }

    public void setReviewNegative(Integer reviewNegative)
    {
        this.reviewNegative=reviewNegative;
    }

    public TrailForDb(String pathid, String firstName, String description, String duration, String datetime, double startingPointLat, double startingPointLongit, String trailData, Integer stepsNubers, Integer calories, Double distance, Double medianSpeed, String weather, String usrId,Integer reviewPositive,Integer reviewNegative)
    {
        this.pathid=pathid;
        this.firstName=firstName;
        this.description=description;
        this.duration=duration;
        this.datetime=datetime;
        this.startingPointLat=startingPointLat;
        this.startingPointLongit=startingPointLongit;
        this.trailData=trailData;
        this.stepsNubers=stepsNubers;
        this.calories=calories;
        this.distance=distance;
        this.medianSpeed=medianSpeed;
        this.weather=weather;
        this.usrId=usrId;
        this.reviewPositive=reviewPositive;
        this.reviewNegative=reviewNegative;

    }

}