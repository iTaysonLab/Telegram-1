package org.telegram.ui.Components.Animations.Background;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;

import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.Animations.Configurator.ChatBackgroundPreferences;
import org.telegram.ui.Components.CubicBezierInterpolator;
import org.telegram.ui.Components.WallpaperParallaxEffect;

import java.util.Arrays;
import java.util.Random;

public class ChatGradientView extends View {
    public enum Scenario {
        OpenChat("openChat", 2000L, 1.0f, 0.16f),
        JumpToMessage("jumpToMsg", 1000L, 1.0f, 0.33f),
        SendMessage("sendMsg", 1000L, 1.0f, 0.33f);

        public String prefKey;
        public long defaultDuration;
        public float defaultFirstInterpolation;
        public float defaultSecondInterpolation;

        Scenario(String prefKey, long defaultDuration, float defaultFirstInterpolation, float defaultSecondInterpolation) {
            this.prefKey = prefKey;
            this.defaultDuration = defaultDuration;
            this.defaultFirstInterpolation = defaultFirstInterpolation;
            this.defaultSecondInterpolation = defaultSecondInterpolation;
        }
    }

    // Constants
    private final static int DIMMING_COLOR = Color.BLACK;
    private final static float DIMMING_PERCENT = 0.25f;
    private final static float BOUND_BOX = 0.25f;
    private final static float PATHFINDING_MINIMUM_DISTANCE = 0.35f;
    private final static float POINT_RADIUS = 1f;
    private final static long ANIMATION_DURATION = 1250L;
    private final static boolean PATHFINDING_MODE = true;

    public final static int[] DEFAULT_COLORS = {
            Color.parseColor("#87A384"),
            Color.parseColor("#FEF5CA"),
            Color.parseColor("#F8E48B"),
            Color.parseColor("#436E57")
    };

    private final static PointF[] DEFAULT_POS = {
            new PointF(0.9f, 0.1f),
            new PointF(0.3f, 0.3f),
            new PointF(0.7f, 0.7f),
            new PointF(0.2f, 0.9f),
    };

    // Drawables
    private final GradientDrawable dimDrawable;
    private final LayerDrawable background;
    private final GradientDrawable[] gradients;
    private final GradientDrawable[] staticGradients;
    private final Random random;

    // Colors
    private final int[] currentColors = Arrays.copyOf(DEFAULT_COLORS, DEFAULT_COLORS.length);
    private PointF[] currentValues = Arrays.copyOf(DEFAULT_POS, DEFAULT_POS.length);
    private PointF[] currentValuesTemp = new PointF[DEFAULT_POS.length];
    private PointF[] _newValues = new PointF[]{};

    // Animators
    private ValueAnimator changeColorAnimator = null;
    private ValueAnimator changePosAnimator = null;

    // Non-Random mode data
    private int pathIndex = 0;

    // Parallax
    private WallpaperParallaxEffect parallaxEffect;
    private float translationX;
    private float translationY;
    private float parallaxScale = 1.0f;
    private boolean paused = true;

    {
        random = new Random();

        dimDrawable = new GradientDrawable();
        dimDrawable.setColor(ColorUtils.setAlphaComponent(DIMMING_COLOR, (int) (255 * DIMMING_PERCENT)));

        gradients = createGradients();
        staticGradients = createGradients();

        Drawable[] drw = new Drawable[staticGradients.length + gradients.length + 1];
        drw[0] = new ColorDrawable(Theme.getColor(Theme.key_windowBackgroundGray));

        int idx = 1;

        for (GradientDrawable staticGradient : staticGradients) {
            drw[idx] = staticGradient;
            idx++;
        }

        for (GradientDrawable gradient : gradients) {
            drw[idx] = gradient;
            idx++;
        }

        background = new LayerDrawable(drw);
        setBackground(background);
    }

    //

    /*@Override
    protected void onDraw(Canvas canvas) {
        int actionBarHeight = ActionBar.getCurrentActionBarHeight() + (Build.VERSION.SDK_INT >= 21 ? AndroidUtilities.statusBarHeight : 0);
        int viewHeight = getRootView().getMeasuredHeight() - actionBarHeight;
        float scaleX = (float) getMeasuredWidth() / (float) background.getIntrinsicWidth();
        float scaleY = (float) (viewHeight) / (float) background.getIntrinsicHeight();
        float scale = Math.max(scaleX, scaleY);
        int width = (int) Math.ceil(background.getIntrinsicWidth() * scale * parallaxScale);
        int height = (int) Math.ceil(background.getIntrinsicHeight() * scale * parallaxScale);
        int x = (getMeasuredWidth() - width) / 2 + (int) translationX;
        int y = (viewHeight - height) / 2 + actionBarHeight + (int) translationY;
        canvas.save();
        canvas.clipRect(0, actionBarHeight, width, getMeasuredHeight());
        background.setBounds(x, y, x + width, y + height);
        background.draw(canvas);
        canvas.restore();
    }*/

    public void onPause() {
        if (parallaxEffect != null) {
            parallaxEffect.setEnabled(false);
        }
        paused = true;
    }

    public void onResume() {
        if (parallaxEffect != null) {
            parallaxEffect.setEnabled(true);
        }
        paused = false;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        notifyHeightChanged();
    }

    public void notifyHeightChanged() {
        if (parallaxEffect != null) {
            parallaxScale = parallaxEffect.getScale(getMeasuredWidth(), getMeasuredHeight());
        }
    }

    public void setMotionModeEnabled(boolean enabled) {
        if (enabled) {
            if (parallaxEffect == null) {
                parallaxEffect = new WallpaperParallaxEffect(getContext());
                parallaxEffect.setCallback((offsetX, offsetY) -> {
                    Log.d("CBC", "parallaxEffect = "+translationX+" / "+translationY);
                    translationX = offsetX;
                    translationY = offsetY;
                    invalidate();
                });
                if (getMeasuredWidth() != 0 && getMeasuredHeight() != 0) {
                    parallaxScale = parallaxEffect.getScale(getMeasuredWidth(), getMeasuredHeight());
                }
            }
            if (!paused) {
                parallaxEffect.setEnabled(true);
            }
        } else if (parallaxEffect != null) {
            parallaxEffect.setEnabled(false);
            parallaxEffect = null;
            parallaxScale = 1.0f;
            translationX = 0;
            translationY = 0;
        }
    }

    //

    public ChatGradientView(Context context) {
        this(context, null);
    }

    public ChatGradientView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        //setWillNotDraw(false);
    }

    public void animatePosition(Scenario scenario) {
        if (changePosAnimator != null) return;

        _newValues = calculateNewValues();
        currentValuesTemp = new PointF[gradients.length];

        changePosAnimator = createAnimator(scenario, (value) -> {
            for (int i = 0; i < gradients.length; i++) {
                PointF curData = currentValues[i];
                PointF shouldNew = _newValues[i];

                PointF actualNew = new PointF(
                        interpolateFloats(curData.x, shouldNew.x, value),
                        interpolateFloats(curData.y, shouldNew.y, value)
                );

                currentValuesTemp[i] = actualNew;
                gradients[i].setGradientCenter(actualNew.x, actualNew.y);
            }
        }, () -> {
            changePosAnimator = null;
            currentValues = _newValues;
        }, () -> {
            changePosAnimator = null;
            currentValues = currentValuesTemp;
        }, () -> {
            currentValues = _newValues;
            _newValues = calculateNewValues();
            currentValuesTemp = new PointF[gradients.length];
        });
    }

    public void resetColor(boolean animate) {
        changeColor(animate, ChatBackgroundController.getDefaultColors());
    }

    @ColorInt
    public int getColorByIndex(int index) {
        return currentColors[index];
    }

    public void changeColorByIndex(int index, @ColorInt int color) {
        currentColors[index] = color;
        gradients[index].setColors(new int[]{currentColors[index], Color.TRANSPARENT});
        staticGradients[index].setColors(new int[]{currentColors[index], Color.TRANSPARENT});
    }

    public void changeColor(boolean animate, @ColorInt int... colors) {
        if (colors.length != gradients.length) throw new IllegalStateException("Size mismatch!");

        if (!animate) {
            for (int i = 0; i < gradients.length; i++) {
                currentColors[i] = colors[i];
                gradients[i].setColors(new int[]{currentColors[i], Color.TRANSPARENT});
                staticGradients[i].setColors(new int[]{currentColors[i], Color.TRANSPARENT});
            }
            return;
        }

        if (changeColorAnimator != null) changeColorAnimator.cancel();
        changeColorAnimator = createStaticAnimator(0, (value) -> {
            for (int i = 0; i < gradients.length; i++) {
                currentColors[i] = ColorUtils.blendARGB(currentColors[i], colors[i], value);
                gradients[i].setColors(new int[]{currentColors[i], Color.TRANSPARENT});
                staticGradients[i].setColors(new int[]{currentColors[i], Color.TRANSPARENT});
            }
        }, () -> changeColorAnimator = null, () -> changeColorAnimator = null, () -> {
        });
    }

    public void cancelAnimation() {
        if (changePosAnimator != null) changePosAnimator.cancel();
        changePosAnimator = null;
    }

    private ValueAnimator createAnimator(Scenario scenario, FloatListener onUpdate, Runnable onEnd, Runnable onCancel, Runnable onRepeat) {
        ValueAnimator va = ValueAnimator.ofFloat(0f, 1f);
        va.setDuration(ChatBackgroundPreferences.getInterpolatorDuration(scenario));
        va.setRepeatCount(0);
        va.addUpdateListener(animation -> onUpdate.receive((Float) animation.getAnimatedValue()));
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                onCancel.run();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onEnd.run();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                onRepeat.run();
            }
        });
        va.setInterpolator(new CubicBezierInterpolator(
                ChatBackgroundPreferences.getInterpolatorSecondOffset(scenario), 0f,
                (1f - ChatBackgroundPreferences.getInterpolatorFirstOffset(scenario)), 1f
        ));
        va.start();
        return va;
    }

    private ValueAnimator createStaticAnimator(int repeatCount, FloatListener onUpdate, Runnable onEnd, Runnable onCancel, Runnable onRepeat) {
        ValueAnimator va = ValueAnimator.ofFloat(0f, 1f);
        va.setDuration(ANIMATION_DURATION);
        va.setRepeatCount(repeatCount);
        va.addUpdateListener(animation -> onUpdate.receive((Float) animation.getAnimatedValue()));
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                onCancel.run();
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                onEnd.run();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                onRepeat.run();
            }
        });
        va.start();
        return va;
    }

    private float interpolateFloats(float f1, float f2, float v) {
        return (f2 - f1) * v + f1;
    }

    private void fillDefaultPositions(PointF[] arr, int idx1, int idx2, int idx3, int idx4) {
        arr[0] = DEFAULT_POS[idx1];
        arr[1] = DEFAULT_POS[idx2];
        arr[2] = DEFAULT_POS[idx3];
        arr[3] = DEFAULT_POS[idx4];
    }

    private PointF[] calculateNewValues() {
        PointF[] newValues = new PointF[gradients.length];

        if (ChatBackgroundPreferences.isWallpaperPathRandomizeEnabled()) {
            for (int i = 0; i < gradients.length; i++) {
                newValues[i] = calculateSpecificValue(newValues);
            }
        } else {
            pathIndex++;
            if (pathIndex == 4) pathIndex = 0;

            switch (pathIndex) {
                case 0:
                    fillDefaultPositions(newValues, 0, 1, 2, 3);
                    break;
                case 1:
                    fillDefaultPositions(newValues, 1, 2, 3, 0);
                    break;
                case 2:
                    fillDefaultPositions(newValues, 2, 3, 0, 1);
                    break;
                case 3:
                    fillDefaultPositions(newValues, 3, 0, 1, 2);
                    break;
            }
        }

        return newValues;
    }

    private PointF calculateSpecificValue(PointF[] values) {
        PointF candidate = new PointF(
                Math.max(random.nextFloat(), BOUND_BOX),
                Math.max(random.nextFloat(), BOUND_BOX)
        );

        if (!PATHFINDING_MODE) return candidate;
        if (values[0] == null) return candidate; // array is empty!

        for (PointF it : values) {
            if (it == null) break; // not initiated
            double distance = Math.sqrt(Math.pow((candidate.x - it.x), 2) + Math.pow((candidate.y - it.y), 2));
            if (distance >= PATHFINDING_MINIMUM_DISTANCE) return candidate;
        }

        return calculateSpecificValue(values);
    }

    private GradientDrawable[] createGradients() {
        return new GradientDrawable[]{
                createGradient(currentColors[0], currentValues[0], POINT_RADIUS),
                createGradient(currentColors[1], currentValues[1], POINT_RADIUS),
                createGradient(currentColors[2], currentValues[2], POINT_RADIUS),
                createGradient(currentColors[3], currentValues[3], POINT_RADIUS),
        };
    }

    private GradientDrawable createGradient(@ColorInt int color, PointF center, Float radiusPercent) {
        GradientDrawable gd = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, new int[]{color, Color.TRANSPARENT});
        gd.setGradientType(GradientDrawable.RADIAL_GRADIENT);
        gd.setGradientCenter(center.x, center.y);
        gd.setGradientRadius(percentPixels(radiusPercent));
        return gd;
    }

    private float percentPixels(float percent) {
        return Math.min(getResources().getDisplayMetrics().heightPixels, getResources().getDisplayMetrics().widthPixels) * percent;
    }

    interface FloatListener {
        void receive(float value);
    }
}
