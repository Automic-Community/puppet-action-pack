package com.automic.puppet.util;

import java.io.InputStream;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

import com.automic.puppet.constants.Constants;

/**
 * Common Utility class contains basic function(s) required by Puppet actions.
 * 
 */
public class CommonUtil {

    private CommonUtil() {
    }

    /**
     * Method to format error message in the format "ERROR | message"
     *
     * @param message
     * @return formatted message
     */
    public static String formatErrorMessage(final String message) {
        final StringBuilder sb = new StringBuilder();
        sb.append("ERROR").append(" | ").append(message);
        return sb.toString();
    }

    /**
     *
     * Method to parse string containing numeric integer value. If string is not valid, then it returns the default
     * value as specified.
     *
     * @param value
     * @param defaultValue
     * @return numeric value
     */
    public static int parseEnvIntValue(final String value, int defaultValue) {
        String val = System.getenv(value);
        int i;
        if (checkNotNull(val)) {
            try {
                i = Integer.parseInt(val);
            } catch (final NumberFormatException nfe) {
                i = defaultValue;
            }
        } else {
            i = defaultValue;
        }
        return i;
    }

    /**
     * Method to parse string value. If string is not valid, then it returns the default value as specified.
     * 
     * @param value
     * @param defaultValue
     * @return
     */
    public static String parseEnvStringValue(final String value, String defaultValue) {
        String val = System.getenv(value);
        if (!checkNotNull(val)) {
            val = defaultValue;
        }
        return val;
    }

    /**
     *
     * Method to parse String containing numeric integer value. If string is not valid, then it returns the default
     * value as specified.
     *
     * @param value
     * @param defaultValue
     * @return numeric value
     */
    public static int parseStringValue(final String value, int defaultValue) {
        int i = defaultValue;
        if (checkNotEmpty(value)) {
            try {
                i = Integer.parseInt(value);
            } catch (final NumberFormatException nfe) {
                i = defaultValue;
            }
        }
        return i;
    }

    /**
     * Method to check if a String is not empty
     *
     * @param field
     * @return true if String is not empty else false
     */
    public static boolean checkNotEmpty(String field) {
        return field != null && !field.isEmpty();
    }

    /**
     * Method to check if an Object is null
     *
     * @param field
     * @return true or false
     */
    public static boolean checkNotNull(Object field) {
        return field != null;
    }

    /**
     * Method to convert a stream into Json object
     * 
     * @param is
     *            input stream
     * @return JSONObject
     */
    public static JsonObject jsonObjectResponse(InputStream is) {
        return Json.createReader(is).readObject();

    }

    /**
     * Method to convert a stream into Json array
     * 
     * @param is
     *            input stream
     * @return JSONObject
     */
    public static JsonArray jsonArrayResponse(InputStream is) {
        return Json.createReader(is).readArray();

    }

    /**
     * Method to convert YES/NO values to boolean true or false
     * 
     * @param value
     * @return true if YES, 1
     */
    public static boolean convert2Bool(String value) {
        boolean ret = false;
        if (checkNotEmpty(value)) {
            ret = Constants.YES.equalsIgnoreCase(value) || Constants.TRUE.equalsIgnoreCase(value)
                    || Constants.ONE.equalsIgnoreCase(value);
        }
        return ret;
    }

    /**
     * Check if the class exist in the given node group
     * 
     * @param jsonArray
     * @param className
     * @param nodeGroup
     * @return
     */
    public static boolean checkClassExist(JsonObject jsonobj, String className, String nodeGroup) {
        boolean classExist = false;
        if (jsonobj != null) {
            JsonObject classobj = jsonobj.getJsonObject("classes");
            if (classobj.containsKey(className)) {
                classExist = true;
            }

        }
        return classExist;
    }
}
