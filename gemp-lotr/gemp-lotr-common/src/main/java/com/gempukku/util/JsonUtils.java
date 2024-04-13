package com.gempukku.util;

import com.alibaba.fastjson2.JSON;
import org.apache.commons.io.FilenameUtils;
import org.hjson.JsonValue;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.List;

public final class JsonUtils {

    public static boolean IsValidHjsonFile(File file) {
        String ext = FilenameUtils.getExtension(file.getName());
        return ext.equalsIgnoreCase("json") || ext.equalsIgnoreCase("hjson");
    }

    //Reads both json and hjson files, converting both to json (for compatilibity with other libraries)
    public static String ReadJson(Reader reader) throws IOException {
        return JsonValue.readHjson(reader).toString();
    }

    //Reads the loaded json or hjson file and deserializes as an instance of the provided class
    public static <T> T Convert(Reader reader, Class<T> clazz) throws IOException {
        final String json = ReadJson(reader);
        return JSON.parseObject(json, clazz);
    }

    public static <T> T Convert(String jsonText, Class<T> clazz) throws IOException {
        String json = JsonValue.readHjson(jsonText).toString();
        return JSON.parseObject(json, clazz);
    }

    public static <T> List<T> ConvertArray(Reader reader, Class<T> clazz) throws IOException {
        final String json = ReadJson(reader);
        try {
            var array = JSON.parseArray(json, clazz);
            return array.stream().toList();
        }
        catch(Exception ex)
        {
            return null;
        }

    }
}
