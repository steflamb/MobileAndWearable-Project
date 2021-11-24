package com.example.pathsocial;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mapbox.geojson.Point;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Converters {
    @TypeConverter
    public static List<com.mapbox.geojson.Point> fromString(String value) {
        Type listType = new TypeToken<List<Point>>() {}.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromArrayList(List<com.mapbox.geojson.Point> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);
        return json;
    }
}