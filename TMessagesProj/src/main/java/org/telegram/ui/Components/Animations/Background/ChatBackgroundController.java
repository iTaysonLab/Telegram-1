package org.telegram.ui.Components.Animations.Background;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.palette.graphics.Palette;

import org.telegram.ui.Components.Animations.Configurator.ChatBackgroundPreferences;

import java.util.Random;

public class ChatBackgroundController {
    public static boolean isMotionBackgroundEnabled(Drawable wallpaper) {
        return ChatBackgroundPreferences.isMotionBackgroundEnabled();
    }

    public static void updateColors(ChatGradientView chatGradientView, Drawable wallpaper, boolean enableParallax) {
        updateColors(chatGradientView, wallpaper, enableParallax, true, null);
    }

    public static void updateColors(ChatGradientView chatGradientView, Drawable wallpaper, boolean enableParallax, boolean animate) {
        updateColors(chatGradientView, wallpaper, enableParallax, animate, null);
    }

    public static int[] getDefaultColors() {
        return new int[] {
                ChatBackgroundPreferences.getMotionColorOffset(0),
                ChatBackgroundPreferences.getMotionColorOffset(1),
                ChatBackgroundPreferences.getMotionColorOffset(2),
                ChatBackgroundPreferences.getMotionColorOffset(3)
        };
    }

    public static void updateColors(ChatGradientView chatGradientView, Drawable wallpaper, boolean enableParallax, boolean animate, Result listener) {
        chatGradientView.setMotionModeEnabled(enableParallax);
        if (ChatBackgroundPreferences.isWallpaperColorFetchEnabled() && wallpaper instanceof BitmapDrawable) {
            Bitmap bitmap = ((BitmapDrawable) wallpaper).getBitmap();

            if (bitmap.isRecycled()) {
                chatGradientView.resetColor(animate);
                if (listener != null) listener.onReceive(getDefaultColors());
                return;
            }

            Palette.from(bitmap).clearFilters().addFilter((rgb, hsl) -> {
                boolean black = hsl[2] <= 0.04f;
                boolean nearLine = (hsl[0] > 10f && hsl[0] < 37f) && (hsl[1] <= 0.82f);
                return !black && !nearLine;
            }).generate(palette -> {
                if (palette != null) {
                    int vibrant = palette.getVibrantColor(-1);
                    if (vibrant == -1) {
                        vibrant = palette.getLightVibrantColor(-1);
                        if (vibrant == -1) {
                            vibrant = palette.getDarkVibrantColor(Color.BLACK);
                        }
                    }

                    int dominant = palette.getDominantColor(Color.BLACK);

                    int muted = palette.getLightMutedColor(-1);
                    if (muted == -1) {
                        muted = palette.getMutedColor(-1);
                        if (muted == -1) {
                            muted = palette.getDarkMutedColor(Color.BLACK);
                        }
                    }

                    int lastColor = palette.getLightVibrantColor(-1);
                    if (lastColor == -1) {
                        lastColor = palette.getMutedColor(-1);
                        if (lastColor == -1) {
                            lastColor = palette.getDominantColor(Color.BLACK);
                        }
                    }

                    int[] newColors = new int[]{
                            vibrant,
                            dominant,
                            muted,
                            lastColor
                    };

                    if (ChatBackgroundPreferences.isWallpaperColorFetchRandomEnabled()) shuffleArray(newColors);

                    if (listener != null) listener.onReceive(newColors);
                    chatGradientView.changeColor(animate, newColors);
                } else {
                    if (listener != null) listener.onReceive(getDefaultColors());
                    chatGradientView.resetColor(animate);
                }
            });
        } else {
            // Return to default
            if (listener != null) listener.onReceive(getDefaultColors());
            chatGradientView.resetColor(animate);
        }
    }

    private static void shuffleArray(int[] array) {
        int index;
        Random random = new Random();
        for (int i = array.length - 1; i > 0; i--) {
            index = random.nextInt(i + 1);
            if (index != i) {
                array[index] ^= array[i];
                array[i] ^= array[index];
                array[index] ^= array[i];
            }
        }
    }

    public interface Result {
        void onReceive(int[] colors);
    }
}
