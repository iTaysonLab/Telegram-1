package org.telegram.ui.Components.Animations.Configurator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ComposeShader;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.graphics.ColorUtils;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.BottomSheet;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.AnimationProperties;
import org.telegram.ui.Components.Animations.Background.ChatGradientView;
import org.telegram.ui.Components.EditTextBoldCursor;
import org.telegram.ui.Components.LayoutHelper;

public class ChatBgColorPrefView extends FrameLayout {
    private TextView textView;
    private TextView valueTextView;
    private boolean needDivider;
    private int height = 50;
    private int currentTextColor = Color.WHITE;
    private int currentColor = -1;
    private GradientDrawable gd = new GradientDrawable();
    private ValueAnimator va;

    public ChatBgColorPrefView(Context context) {
        super(context);

        textView = new TextView(context);
        textView.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
        textView.setLines(1);
        textView.setMaxLines(1);
        textView.setSingleLine(true);
        textView.setGravity((LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.CENTER_VERTICAL);
        textView.setEllipsize(TextUtils.TruncateAt.END);
        addView(textView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, (LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT) | Gravity.TOP, LocaleController.isRTL ? 70 : 21, 0, LocaleController.isRTL ? 21 : 70, 0));

        int padding = AndroidUtilities.dp(8f);

        valueTextView = new TextView(context);
        valueTextView.setTextColor(Color.WHITE);
        valueTextView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 13);
        valueTextView.setGravity(LocaleController.isRTL ? Gravity.RIGHT : Gravity.LEFT);
        valueTextView.setLines(1);
        valueTextView.setMaxLines(1);
        valueTextView.setSingleLine(true);
        valueTextView.setTypeface(Typeface.MONOSPACE);
        valueTextView.setPadding(padding, padding, padding, padding);
        valueTextView.setEllipsize(TextUtils.TruncateAt.END);
        addView(valueTextView, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, (LocaleController.isRTL ? Gravity.LEFT : Gravity.RIGHT) | Gravity.CENTER_VERTICAL, 22, 0, 22, 0));

        gd.setCornerRadius(AndroidUtilities.dp(8f));

        setClipChildren(false);
    }

    public void setBgIndex(int index, boolean needDivider) {
        this.needDivider = needDivider;
        setWillNotDraw(!needDivider);

        textView.setText("Color "+(index + 1));

        int currentColor = ChatBackgroundPreferences.getMotionColorOffset(index);
        this.currentColor = currentColor;
        gd.setColor(currentColor);
        valueTextView.setBackground(gd);
        valueTextView.setText(String.format("#%06X", (0xFFFFFF & currentColor)));

        if (ColorUtils.calculateLuminance(currentColor) > 0.7f) {
            currentTextColor = Color.BLACK;
        } else {
            currentTextColor = Color.WHITE;
        }

        valueTextView.setTextColor(currentTextColor);
    }

    public void updateBackground(int newColor) {
        updateBackground(newColor, true);
    }

    public void updateBackground(int newColor, boolean animate) {
        valueTextView.setText(String.format("#%06X", (0xFFFFFF & newColor)));

        int newTextColor;
        if (ColorUtils.calculateLuminance(newColor) > 0.7f) {
            newTextColor = Color.BLACK;
        } else {
            newTextColor = Color.WHITE;
        }

        if (va != null) {
            va.cancel();
        }

        if (!animate) {
            currentColor = newColor;
            currentTextColor = newTextColor;
            gd.setColor(currentColor);
            valueTextView.setTextColor(currentTextColor);
            return;
        }

        va = ValueAnimator.ofFloat(0.0f, 1.0f);
        va.addUpdateListener(animation -> {
            float value = (float) animation.getAnimatedValue();
            gd.setColor(ColorUtils.blendARGB(currentColor, newColor, value));
            valueTextView.setTextColor(ColorUtils.blendARGB(currentTextColor, newTextColor, value));
        });

        va.setDuration(500L);
        va.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationCancel(Animator animation) {
                currentColor = newColor;
                currentTextColor = newTextColor;
                va = null;
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                currentColor = newColor;
                currentTextColor = newTextColor;
                va = null;
            }
        });

        va.start();
    }

    public void callPicker(int currentIndex, ChatGradientView gradientView) {
        if (ChatBackgroundPreferences.isWallpaperColorFetchEnabled() || gradientView == null) return;
        new EditorAlert(getContext(), gradientView, currentIndex, currentColor).show();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(height) + (needDivider ? 1 : 0), MeasureSpec.EXACTLY));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (needDivider) {
            canvas.drawLine(LocaleController.isRTL ? 0 : AndroidUtilities.dp(20), getMeasuredHeight() - 1, getMeasuredWidth() - (LocaleController.isRTL ? AndroidUtilities.dp(20) : 0), getMeasuredHeight() - 1, Theme.dividerPaint);
        }
    }

    //

    public class EditorAlert extends BottomSheet {
        private ChatBgColorPrefView.EditorAlert.ColorPicker colorPicker;
        private FrameLayout bottomSaveLayout;

        private AnimatorSet colorChangeAnimation;
        private boolean startedColorChange;
        private boolean ignoreTextChange;

        private ChatGradientView gradientView;
        private int gvIndex;
        private int gvPreviousColor;

        private class ColorPicker extends FrameLayout {

            private LinearLayout linearLayout;

            private final int paramValueSliderWidth = AndroidUtilities.dp(20);

            private Paint colorWheelPaint;
            private Paint valueSliderPaint;
            private Paint circlePaint;
            private Drawable circleDrawable;

            private Bitmap colorWheelBitmap;

            private EditTextBoldCursor[] colorEditText = new EditTextBoldCursor[4];

            private int colorWheelRadius;

            private float[] colorHSV = new float[] { 0.0f, 0.0f, 1.0f };
            private float alpha = 1.0f;

            private float[] hsvTemp = new float[3];
            private LinearGradient colorGradient;
            private LinearGradient alphaGradient;

            private boolean circlePressed;
            private boolean colorPressed;
            private boolean alphaPressed;

            private DecelerateInterpolator decelerateInterpolator = new DecelerateInterpolator();

            public ColorPicker(Context context) {
                super(context);
                setWillNotDraw(false);

                circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
                circleDrawable = context.getResources().getDrawable(R.drawable.knob_shadow).mutate();

                colorWheelPaint = new Paint();
                colorWheelPaint.setAntiAlias(true);
                colorWheelPaint.setDither(true);

                valueSliderPaint = new Paint();
                valueSliderPaint.setAntiAlias(true);
                valueSliderPaint.setDither(true);

                linearLayout = new LinearLayout(context);
                linearLayout.setOrientation(LinearLayout.HORIZONTAL);
                addView(linearLayout, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL | Gravity.TOP));
                for (int a = 0; a < 3; a++) {
                    colorEditText[a] = new EditTextBoldCursor(context);
                    colorEditText[a].setInputType(InputType.TYPE_CLASS_NUMBER);
                    colorEditText[a].setTextColor(0xff212121);
                    colorEditText[a].setCursorColor(0xff212121);
                    colorEditText[a].setCursorSize(AndroidUtilities.dp(20));
                    colorEditText[a].setCursorWidth(1.5f);
                    colorEditText[a].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
                    colorEditText[a].setBackgroundDrawable(Theme.createEditTextDrawable(context, true));
                    colorEditText[a].setMaxLines(1);
                    colorEditText[a].setTag(a);
                    colorEditText[a].setGravity(Gravity.CENTER);
                    if (a == 0) {
                        colorEditText[a].setHint("red");
                    } else if (a == 1) {
                        colorEditText[a].setHint("green");
                    } else if (a == 2) {
                        colorEditText[a].setHint("blue");
                    }
                    colorEditText[a].setImeOptions((a == 2 ? EditorInfo.IME_ACTION_DONE : EditorInfo.IME_ACTION_NEXT) | EditorInfo.IME_FLAG_NO_EXTRACT_UI);
                    InputFilter[] inputFilters = new InputFilter[1];
                    inputFilters[0] = new InputFilter.LengthFilter(3);
                    colorEditText[a].setFilters(inputFilters);
                    final int num = a;
                    linearLayout.addView(colorEditText[a], LayoutHelper.createLinear(55, 36, 0, 0, a != 2 ? 16 : 0, 0));
                    colorEditText[a].addTextChangedListener(new TextWatcher() {
                        @Override
                        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                        }

                        @Override
                        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {

                        }

                        @Override
                        public void afterTextChanged(Editable editable) {
                            if (ignoreTextChange) {
                                return;
                            }
                            ignoreTextChange = true;
                            int color = Utilities.parseInt(editable.toString());
                            if (color < 0) {
                                color = 0;
                                colorEditText[num].setText("" + color);
                                colorEditText[num].setSelection(colorEditText[num].length());
                            } else if (color > 255) {
                                color = 255;
                                colorEditText[num].setText("" + color);
                                colorEditText[num].setSelection(colorEditText[num].length());
                            }
                            int currentColor = getColor();
                            if (num == 2) {
                                currentColor = (currentColor & 0xffffff00) | (color & 0xff);
                            } else if (num == 1) {
                                currentColor = (currentColor & 0xffff00ff) | ((color & 0xff) << 8);
                            } else if (num == 0) {
                                currentColor = (currentColor & 0xff00ffff) | ((color & 0xff) << 16);
                            }

                            setColor(currentColor);
                            setGV(currentColor);

                            ignoreTextChange = false;
                        }
                    });
                    colorEditText[a].setOnEditorActionListener((textView, i, keyEvent) -> {
                        if (i == EditorInfo.IME_ACTION_DONE) {
                            AndroidUtilities.hideKeyboard(textView);
                            return true;
                        }
                        return false;
                    });
                }
            }

            @Override
            protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
                int widthSize = MeasureSpec.getSize(widthMeasureSpec);
                int heightSize = MeasureSpec.getSize(heightMeasureSpec);
                int size = Math.min(widthSize, heightSize);
                measureChild(linearLayout, widthMeasureSpec, heightMeasureSpec);
                setMeasuredDimension(size, size);
            }

            @Override
            protected void onDraw(Canvas canvas) {
                int centerX = getWidth() / 2 - paramValueSliderWidth * 2;
                int centerY = getHeight() / 2 - AndroidUtilities.dp(8);

                canvas.drawBitmap(colorWheelBitmap, centerX - colorWheelRadius, centerY - colorWheelRadius, null);

                float hueAngle = (float) Math.toRadians(colorHSV[0]);
                int colorPointX = (int) (-Math.cos(hueAngle) * colorHSV[1] * colorWheelRadius) + centerX;
                int colorPointY = (int) (-Math.sin(hueAngle) * colorHSV[1] * colorWheelRadius) + centerY;

                float pointerRadius = 0.075f * colorWheelRadius;

                hsvTemp[0] = colorHSV[0];
                hsvTemp[1] = colorHSV[1];
                hsvTemp[2] = 1.0f;

                drawPointerArrow(canvas, colorPointX, colorPointY, Color.HSVToColor(hsvTemp));

                int x = centerX + colorWheelRadius + paramValueSliderWidth;
                int y = centerY - colorWheelRadius;
                int width = AndroidUtilities.dp(9);
                int height = colorWheelRadius * 2;
                if (colorGradient == null) {
                    colorGradient = new LinearGradient(x, y, x + width, y + height, new int[]{Color.BLACK, Color.HSVToColor(hsvTemp)}, null, Shader.TileMode.CLAMP);
                }
                valueSliderPaint.setShader(colorGradient);
                canvas.drawRect(x, y, x + width, y + height, valueSliderPaint);
                drawPointerArrow(canvas, x + width / 2, (int) (y + colorHSV[2] * height), Color.HSVToColor(colorHSV));

                x += paramValueSliderWidth * 2;
                if (alphaGradient == null) {
                    int color = Color.HSVToColor(hsvTemp);
                    alphaGradient = new LinearGradient(x, y, x + width, y + height, new int[]{color, color & 0x00ffffff}, null, Shader.TileMode.CLAMP);
                }
                valueSliderPaint.setShader(alphaGradient);
                canvas.drawRect(x, y, x + width, y + height, valueSliderPaint);
                drawPointerArrow(canvas, x + width / 2, (int) (y + (1.0f - alpha) * height), (Color.HSVToColor(colorHSV) & 0x00ffffff) | ((int) (255 * alpha) << 24));
            }

            private void drawPointerArrow(Canvas canvas, int x, int y, int color) {
                int side = AndroidUtilities.dp(13);
                circleDrawable.setBounds(x - side, y - side, x + side, y + side);
                circleDrawable.draw(canvas);

                circlePaint.setColor(0xffffffff);
                canvas.drawCircle(x, y, AndroidUtilities.dp(11), circlePaint);
                circlePaint.setColor(color);
                canvas.drawCircle(x, y, AndroidUtilities.dp(9), circlePaint);
            }

            @Override
            protected void onSizeChanged(int width, int height, int oldw, int oldh) {
                colorWheelRadius = Math.max(1, width / 2 - paramValueSliderWidth * 2 - AndroidUtilities.dp(20));
                colorWheelBitmap = createColorWheelBitmap(colorWheelRadius * 2, colorWheelRadius * 2);
                //linearLayout.setTranslationY(colorWheelRadius * 2 + AndroidUtilities.dp(20));
                colorGradient = null;
                alphaGradient = null;
            }

            private Bitmap createColorWheelBitmap(int width, int height) {
                Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

                int colorCount = 12;
                int colorAngleStep = 360 / 12;
                int[] colors = new int[colorCount + 1];
                float[] hsv = new float[]{0.0f, 1.0f, 1.0f};
                for (int i = 0; i < colors.length; i++) {
                    hsv[0] = (i * colorAngleStep + 180) % 360;
                    colors[i] = Color.HSVToColor(hsv);
                }
                colors[colorCount] = colors[0];

                SweepGradient sweepGradient = new SweepGradient(width / 2, height / 2, colors, null);
                RadialGradient radialGradient = new RadialGradient(width / 2, height / 2, colorWheelRadius, 0xffffffff, 0x00ffffff, Shader.TileMode.CLAMP);
                ComposeShader composeShader = new ComposeShader(sweepGradient, radialGradient, PorterDuff.Mode.SRC_OVER);

                colorWheelPaint.setShader(composeShader);

                Canvas canvas = new Canvas(bitmap);
                canvas.drawCircle(width / 2, height / 2, colorWheelRadius, colorWheelPaint);

                return bitmap;
            }

            private void startColorChange(boolean start) {
                if (startedColorChange == start) {
                    return;
                }
                if (colorChangeAnimation != null) {
                    colorChangeAnimation.cancel();
                }
                startedColorChange = start;
                colorChangeAnimation = new AnimatorSet();
                colorChangeAnimation.playTogether(
                        ObjectAnimator.ofInt(backDrawable, AnimationProperties.COLOR_DRAWABLE_ALPHA, start ? 0 : 51),
                        ObjectAnimator.ofFloat(containerView, View.ALPHA, start ? 0.2f : 1.0f));
                colorChangeAnimation.setDuration(150);
                colorChangeAnimation.setInterpolator(decelerateInterpolator);
                colorChangeAnimation.start();
            }

            @Override
            public boolean onTouchEvent(MotionEvent event) {
                int action = event.getAction();
                switch (action) {
                    case MotionEvent.ACTION_DOWN:
                    case MotionEvent.ACTION_MOVE:

                        int x = (int) event.getX();
                        int y = (int) event.getY();
                        int centerX = getWidth() / 2 - paramValueSliderWidth * 2;
                        int centerY = getHeight() / 2 - AndroidUtilities.dp(8);
                        int cx = x - centerX;
                        int cy = y - centerY;
                        double d = Math.sqrt(cx * cx + cy * cy);

                        if (circlePressed || !alphaPressed && !colorPressed && d <= colorWheelRadius) {
                            if (d > colorWheelRadius) {
                                d = colorWheelRadius;
                            }
                            circlePressed = true;
                            colorHSV[0] = (float) (Math.toDegrees(Math.atan2(cy, cx)) + 180.0f);
                            colorHSV[1] = Math.max(0.0f, Math.min(1.0f, (float) (d / colorWheelRadius)));
                            colorGradient = null;
                            alphaGradient = null;
                        }
                        if (colorPressed || !circlePressed && !alphaPressed && x >= centerX + colorWheelRadius + paramValueSliderWidth && x <= centerX + colorWheelRadius + paramValueSliderWidth * 2 && y >= centerY - colorWheelRadius && y <= centerY + colorWheelRadius) {
                            float value = (y - (centerY - colorWheelRadius)) / (colorWheelRadius * 2.0f);
                            if (value < 0.0f) {
                                value = 0.0f;
                            } else if (value > 1.0f) {
                                value = 1.0f;
                            }
                            colorHSV[2] = value;
                            colorPressed = true;
                        }
                        if (alphaPressed || !circlePressed && !colorPressed && x >= centerX + colorWheelRadius + paramValueSliderWidth * 3 && x <= centerX + colorWheelRadius + paramValueSliderWidth * 4 && y >= centerY - colorWheelRadius && y <= centerY + colorWheelRadius) {
                            alpha = 1.0f - (y - (centerY - colorWheelRadius)) / (colorWheelRadius * 2.0f);
                            if (alpha < 0.0f) {
                                alpha = 0.0f;
                            } else if (alpha > 1.0f) {
                                alpha = 1.0f;
                            }
                            alphaPressed = true;
                        }
                        if (alphaPressed || colorPressed || circlePressed) {
                            startColorChange(true);
                            int color = getColor();
                            color = 0xff000000 | color;
                            setGV(color);
                            int red = Color.red(color);
                            int green = Color.green(color);
                            int blue = Color.blue(color);
                            int a = Color.alpha(color);
                            if (!ignoreTextChange) {
                                ignoreTextChange = true;
                                colorEditText[0].setText("" + red);
                                colorEditText[1].setText("" + green);
                                colorEditText[2].setText("" + blue);
                                for (int b = 0; b < 3; b++) {
                                    colorEditText[b].setSelection(colorEditText[b].length());
                                }
                                ignoreTextChange = false;
                            }
                            invalidate();
                        }

                        return true;
                    case MotionEvent.ACTION_UP:
                        alphaPressed = false;
                        colorPressed = false;
                        circlePressed = false;
                        startColorChange(false);
                        break;
                }
                return super.onTouchEvent(event);
            }

            public void setColor(int color) {
                int red = Color.red(color);
                int green = Color.green(color);
                int blue = Color.blue(color);
                int a = Color.alpha(color);
                if (!ignoreTextChange) {
                    ignoreTextChange = true;
                    colorEditText[0].setText("" + red);
                    colorEditText[1].setText("" + green);
                    colorEditText[2].setText("" + blue);
                    for (int b = 0; b < 3; b++) {
                        colorEditText[b].setSelection(colorEditText[b].length());
                    }
                    ignoreTextChange = false;
                }
                alphaGradient = null;
                colorGradient = null;
                alpha = a / 255.0f;
                Color.colorToHSV(color, colorHSV);
                invalidate();
            }

            public int getColor() {
                return (Color.HSVToColor(colorHSV) & 0x00ffffff) | ((int) (alpha * 255) << 24);
            }
        }

        public EditorAlert(final Context context, final ChatGradientView gradientView, final int currentIndex, final int currentColor) {
            super(context, true);

            this.gradientView = gradientView;
            this.gvPreviousColor = currentColor;
            this.gvIndex = currentIndex;

            Drawable shadowDrawable = context.getResources().getDrawable(R.drawable.sheet_shadow_round).mutate();

            containerView = new FrameLayout(getContext()) {
                @Override
                public boolean hasOverlappingRendering() {
                    return false;
                }

                @Override
                public void setTranslationY(float translationY) {
                    super.setTranslationY(translationY);
                    onContainerTranslationYChanged(translationY);
                }
            };
            containerView.setBackgroundDrawable(shadowDrawable);
            containerView.setPadding(backgroundPaddingLeft, AndroidUtilities.dp(12f), backgroundPaddingLeft, 0);

            colorPicker = new ChatBgColorPrefView.EditorAlert.ColorPicker(context);
            colorPicker.setColor(currentColor);
            containerView.addView(colorPicker, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER_HORIZONTAL));

            bottomSaveLayout = new FrameLayout(context);
            bottomSaveLayout.setBackgroundColor(0xffffffff);
            containerView.addView(bottomSaveLayout, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 48, Gravity.LEFT | Gravity.BOTTOM));

            TextView closeButton = new TextView(context);
            closeButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            closeButton.setTextColor(0xff19a7e8);
            closeButton.setGravity(Gravity.CENTER);
            closeButton.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.ACTION_BAR_AUDIO_SELECTOR_COLOR, 0));
            closeButton.setPadding(AndroidUtilities.dp(18), 0, AndroidUtilities.dp(18), 0);
            closeButton.setText("CLOSE");
            closeButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            bottomSaveLayout.addView(closeButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
            closeButton.setOnClickListener(v -> dismiss());

            TextView saveButton = new TextView(context);
            saveButton.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 14);
            saveButton.setTextColor(0xff19a7e8);
            saveButton.setGravity(Gravity.CENTER);
            saveButton.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.ACTION_BAR_AUDIO_SELECTOR_COLOR, 0));
            saveButton.setPadding(AndroidUtilities.dp(18), 0, AndroidUtilities.dp(18), 0);
            saveButton.setText("SAVE");
            saveButton.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
            bottomSaveLayout.addView(saveButton, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.RIGHT));
            saveButton.setOnClickListener(v -> {
                gvPreviousColor = gradientView.getColorByIndex(gvIndex);
                ChatBackgroundPreferences.setMotionColorOffset(gvIndex, gvPreviousColor);
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetNewWallpapper);
                setOnDismissListener(null);
                dismiss();
            });
        }

        private void setGV(int newColor) {
            updateBackground(newColor, false);
            gradientView.changeColorByIndex(gvIndex, newColor);
        }

        private void restoreGV() {
            updateBackground(gvPreviousColor, false);
            gradientView.changeColorByIndex(gvIndex, gvPreviousColor);
        }

        @Override
        public void dismiss() {
            restoreGV();
            super.dismiss();
        }

        @Override
        protected boolean canDismissWithSwipe() {
            return false;
        }
    }
}
