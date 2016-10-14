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
     * Method to read the value as defined in environment. If value is not valid integer, then it returns the default
     * value as specified.
     *
     * @param paramName
     * @param defaultValue
     * @return parameter value
     */
    public static int getEnvParameter(final String paramName, int defaultValue) {
        String val = System.getenv(paramName);
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
     * Method to read environment value. If not defined then it returns the default value as specified.
     * 
     * @param value
     * @param defaultValue
     * @return
     */
    public static String getEnvParameter(final String paramName, String defaultValue) {
        String val = System.getenv(paramName);
        if (!checkNotNull(val)) {
            val = defaultValue;
        }
        return val;
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
     * @return {@link JsonObject}
     */
    public static JsonObject jsonObjectResponse(InputStream is) {
        return Json.createReader(is).readObject();

    }

    /**
     * Method to convert a stream into Json array
     * 
     * @param is
     *            input stream
     * @return {@link JsonArray}
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
     * Method to split and trim space 
     * 
     * @param value
     * @param delimeter
     *           
     * @return String Array
     */
    public static String [] splitAndTrimSpace(String value, String delimeter) {
            return value.split("[\\s]*"+delimeter+"[\\s]*");
    }

}
