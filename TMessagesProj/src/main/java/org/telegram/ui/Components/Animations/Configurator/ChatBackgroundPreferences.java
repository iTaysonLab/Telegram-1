package org.telegram.ui.Components.Animations.Configurator;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.SharedPreferences;

import org.telegram.messenger.ApplicationLoader;
import org.telegram.ui.Components.Animations.Background.ChatGradientView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.Map;

@SuppressLint("ApplySharedPref")
public class ChatBackgroundPreferences {
    private static final SharedPreferences prefs = ApplicationLoader.applicationContext.getSharedPreferences("contest_data", Activity.MODE_PRIVATE);

    public static boolean isMotionBackgroundEnabled() {
        return prefs.getBoolean("motion_wall", true);
    }

    public static void setMotionBackgroundEnabled(boolean value) {
        prefs.edit().putBoolean("motion_wall", value).commit();
    }

    public static boolean isWallpaperColorFetchEnabled() {
        return prefs.getBoolean("motion_wall_autocolor", false);
    }

    public static void setWallpaperColorFetchEnabled(boolean value) {
        prefs.edit().putBoolean("motion_wall_autocolor", value).commit();
    }

    public static boolean isWallpaperColorFetchRandomEnabled() {
        return prefs.getBoolean("motion_wall_autocolor_rnd", false);
    }

    public static void setWallpaperColorFetchRandomEnabled(boolean value) {
        prefs.edit().putBoolean("motion_wall_autocolor_rnd", value).commit();
    }

    public static boolean isWallpaperPathRandomizeEnabled() {
        return prefs.getBoolean("motion_wall_path_randomize", false);
    }

    public static void setWallpaperPathRandomizeEnabled(boolean value) {
        prefs.edit().putBoolean("motion_wall_path_randomize", value).commit();
    }

    public static int getMotionColorOffset(int offset) {
        return prefs.getInt("motion_wall_color_idx" + offset, ChatGradientView.DEFAULT_COLORS[offset]);
    }

    public static void setMotionColorOffset(int offset, int color) {
        prefs.edit().putInt("motion_wall_color_idx" + offset, color).commit();
    }

    public static float getInterpolatorFirstOffset(ChatGradientView.Scenario type) {
        return prefs.getFloat("motion_wall_ip_offset_"+type.prefKey+"_first", type.defaultFirstInterpolation);
    }

    public static void setInterpolatorFirstOffset(ChatGradientView.Scenario type, float offset) {
        prefs.edit().putFloat("motion_wall_ip_offset_"+type.prefKey+"_first", offset).commit();
    }

    public static float getInterpolatorSecondOffset(ChatGradientView.Scenario type) {
        return prefs.getFloat("motion_wall_ip_offset_"+type.prefKey+"_second", type.defaultSecondInterpolation);
    }

    public static void setInterpolatorSecondOffset(ChatGradientView.Scenario type, float offset) {
        prefs.edit().putFloat("motion_wall_ip_offset_"+type.prefKey+"_second", offset).commit();
    }

    public static long getInterpolatorDuration(ChatGradientView.Scenario type) {
        return prefs.getLong("motion_wall_ip_offset_"+type.prefKey+"_duration", type.defaultDuration);
    }

    public static void setInterpolatorDuration(ChatGradientView.Scenario type, long duration) {
        prefs.edit().putLong("motion_wall_ip_offset_"+type.prefKey+"_duration", duration).commit();
    }

    public static void resetPrefs() {
        prefs.edit().clear().commit();
    }

    public static class Saver {
        private final static String TYPE_INT = "I", TYPE_BOOL = "Z", TYPE_FLOAT = "F", TYPE_LONG = "L", TYPE_UNKNOWN = "0";

        public static void writePrefsToFile(BufferedWriter bw) throws IOException {
            Map<String, ?> allEntries = prefs.getAll();

            int idx = 0;
            int lastIdx = allEntries.size() - 1;

            for (Map.Entry<String, ?> entry : allEntries.entrySet()) {
                // NAME:TYPE:VALUE

                Object value = entry.getValue();

                String currentType;
                if (value instanceof Integer) {
                    currentType = TYPE_INT;
                } else if (value instanceof Float) {
                    currentType = TYPE_FLOAT;
                } else if (value instanceof Long) {
                    currentType = TYPE_LONG;
                } else if (value instanceof Boolean) {
                    currentType = TYPE_BOOL;
                } else {
                    currentType = TYPE_UNKNOWN;
                }

                bw.write(entry.getKey() + ":" + currentType + ":" + value.toString());
                if (idx != lastIdx) bw.newLine();
                idx++;
            }
        }

        public static void stringToPrefs(BufferedReader br) throws IOException {
            SharedPreferences.Editor prefEditor = prefs.edit();

            String line;
            while ((line = br.readLine()) != null) {
                String[] parsedData = line.split(":");

                String prefKey = parsedData[0];
                String prefValue = parsedData[2];

                switch (parsedData[1]) {
                    case TYPE_INT:
                        prefEditor.putInt(prefKey, Integer.parseInt(prefValue));
                        break;
                    case TYPE_BOOL:
                        prefEditor.putBoolean(prefKey, Boolean.parseBoolean(prefValue));
                        break;
                    case TYPE_LONG:
                        prefEditor.putLong(prefKey, Long.parseLong(prefValue));
                        break;
                    case TYPE_FLOAT:
                        prefEditor.putFloat(prefKey, Float.parseFloat(prefValue));
                        break;
                }
            }

            prefEditor.commit();
        }
    }
}
