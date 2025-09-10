package com.example.kosplus.func;

import androidx.room.TypeConverter;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class Converters {
    private static final Gson gson = new Gson();

    // -------- List<String> --------
    @TypeConverter
    public static String fromStringList(List<String> list) {
        return list == null ? null : gson.toJson(list);
    }

    @TypeConverter
    public static List<String> toStringList(String data) {
        if (data == null) return null;
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(data, listType);
    }

    // -------- List<Integer> --------
    @TypeConverter
    public static String fromIntegerList(List<Integer> list) {
        return list == null ? null : gson.toJson(list);
    }

    @TypeConverter
    public static List<Integer> toIntegerList(String data) {
        if (data == null) return null;
        Type listType = new TypeToken<List<Integer>>() {}.getType();
        return gson.fromJson(data, listType);
    }
}
