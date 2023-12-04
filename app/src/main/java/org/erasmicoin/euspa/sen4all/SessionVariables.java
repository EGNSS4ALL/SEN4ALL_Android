package org.erasmicoin.euspa.sen4all;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Date;


public class SessionVariables {

    private final static String PREFS_NAME = "EUSPACE4ALL_PREFS";

    private final static String CONTEXT = "CONTEXT";

    private final static String CURRENT_LAYER = "CURRENT_LAYER";

    private final static String CURRENT_SATELLITE = "CURRENT_SATELLITE";
    private final static String CURRENT_LAYER_NAME = "CURRENT_LAYER_NAME";

    private final static String FEATURE_BBOX = "FEATURE_BBOX";

    private final static String DATE = "DATE";

    public static void clearSession(Context ctx){
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        editor.apply();
    }
    public static ContextEnum readContext(Context ctx){
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        return ContextEnum.fromInteger(settings.getInt(CONTEXT, -1));
    }
    public static void writeContext(ContextEnum context, Context ctx){
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putInt(CONTEXT, ContextEnum.toInteger(context));

        editor.apply();
    }

    public static void deleteContext(Context ctx){
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.remove(CONTEXT);

        editor.apply();
    }

    public static String readLayer(Context ctx){
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(CURRENT_LAYER, null);
    }
    public static void writeLayer(String layerTag, Context ctx){
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(CURRENT_LAYER, layerTag);

        editor.apply();
    }
    public static void deleteLayer(Context ctx){
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.remove(CURRENT_LAYER);

        editor.apply();
    }


    public static String readSatellite(Context ctx){
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(CURRENT_SATELLITE, "");
    }
    public static void writeSatellite(String satellite, Context ctx){
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(CURRENT_SATELLITE, satellite);

        editor.apply();
    }

    public static String readFeatureBBox(Context ctx){
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(FEATURE_BBOX, "");
    }
    public static void writeFeatureBBox(String bbox, Context ctx){
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(FEATURE_BBOX, bbox);

        editor.apply();
    }

    public static String readLayerName(Context ctx){
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        return settings.getString(CURRENT_LAYER_NAME, "");
    }
    public static void writeLayerName(String layerName, Context ctx){
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putString(CURRENT_LAYER_NAME, layerName);

        editor.apply();
    }

    public static long readDate(Context ctx){
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        return settings.getLong(DATE, new Date().getTime());
    }
    public static void writeDate(Long date, Context ctx){
        SharedPreferences settings = ctx.getSharedPreferences(PREFS_NAME, 0);
        SharedPreferences.Editor editor = settings.edit();

        editor.putLong(DATE, date);

        editor.apply();
    }


}
