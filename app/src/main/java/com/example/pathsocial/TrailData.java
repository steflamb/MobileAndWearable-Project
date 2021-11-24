package com.example.pathsocial;

import com.mapbox.geojson.Point;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TrailData
{

    public List<Point> routeCoordinates = new ArrayList<>();
    public LocalDateTime dateTimeCreated;
    public Duration durationTrail;

    public TrailData(List<Point> routeCoordinates,LocalDateTime dateTimeCreated,Duration durationTrail) {
        this.routeCoordinates = routeCoordinates;
        this.dateTimeCreated = dateTimeCreated;
        this.durationTrail=durationTrail;
    }
}
