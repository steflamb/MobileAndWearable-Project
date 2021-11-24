package com.example.pathsocial;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.io.Serializable;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import androidx.room.TypeConverters;

import com.example.pathsocial.Converters;

@Entity
public class Trail {

    public Trail()
    {}


    @PrimaryKey
    @NonNull
    public String pathid;

    @ColumnInfo(name = "trail_name")
    public String firstName;

    @ColumnInfo(name = "description")
    public String description;

    @ColumnInfo(name = "duration")
    public String duration;

    @ColumnInfo(name = "datetime")
    public String datetime;

    @ColumnInfo(name = "starting_location_lat")
    public double startingPointLat;

    @ColumnInfo(name = "starting_location_longi")
    public double startingPointLongit;

    @TypeConverters({Converters.class})
    public List<Point> trailData;

    @ColumnInfo(name = "steps")
    public Integer stepsNubers;

    @ColumnInfo(name = "calories")
    public Integer calories;

    @ColumnInfo(name = "distance")
    public Double distance;

    @ColumnInfo(name = "med_speed")
    public Double medianSpeed;

    @ColumnInfo(name = "weather")
    public String weather;

    @ColumnInfo(name = "personal")
    public Boolean personal;

    public void setPathid(@NonNull String pathid) {
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

    public void setTrailData(List<Point> trailData) {
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

    public List<Point> getTrailData() {
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

    @NonNull
    public String getPathid() {
        return pathid;
    }

    public String getWeather() {
        return weather;
    }


    public Trail(String pathid, String firstName, String description, String duration, String datetime, double startingPointLat, double startingPointLongit, List<Point> trailData, Integer stepsNubers, Integer calories, Double distance, Double medianSpeed, String weather)
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
        this.personal=personal;

    }

}