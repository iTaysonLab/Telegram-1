package org.telegram.ui.Components.Animations.Configurator;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Typeface;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.Animations.Background.ChatBackgroundController;
import org.telegram.ui.Components.Animations.Background.ChatGradientView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.SeekBarView;

public class ChatBeiserPrefView extends FrameLayout {
    private SeekBarView topSeekBarView;
    private SeekBarView bottomSeekBarView;
    private BeiserPreviewView previewView;
    private ChatGradientView.Scenario key;

    public void setKey(ChatGradientView.Scenario key) {
        this.key = key;

        float fo = ChatBackgroundPreferences.getInterpolatorFirstOffset(key);
        float so = ChatBackgroundPreferences.getInterpolatorSecondOffset(key);

        topSeekBarView.setProgress(fo);
        bottomSeekBarView.setProgress(so);

        previewView.setFirstControlPointRatio(fo);
        previewView.setSecondControlPointRatio(so);
    }

    public ChatBeiserPrefView(Context context) {
        super(context);

        previewView = new BeiserPreviewView(context);
        addView(previewView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 166, Gravity.CENTER_VERTICAL, 12, 0, 12, 0));

        topSeekBarView = new SeekBarView(context, /* inPercents = */ true) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return super.onTouchEvent(event);
            }
        };
        topSeekBarView.setReportChanges(true);
        topSeekBarView.setInnerColor(Theme.getColor(Theme.key_windowBackgroundGray));
        topSeekBarView.setRotation(180f);
        topSeekBarView.setDelegate(new SeekBarView.SeekBarViewDelegate() {
            @Override
            public void onSeekBarDrag(boolean stop, float progress) {
                previewView.setFirstControlPointRatio(progress);
                if (stop) ChatBackgroundPreferences.setInterpolatorFirstOffset(key, progress);
            }

            @Override
            public void onSeekBarPressed(boolean pressed) {}

            @Override
            public CharSequence getContentDescription() {
                return " ";
            }
        });
        topSeekBarView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        addView(topSeekBarView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 38, Gravity.LEFT | Gravity.TOP, 12, 0, 12, 0));

        bottomSeekBarView = new SeekBarView(context, /* inPercents = */ true) {
            @Override
            public boolean onTouchEvent(MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    getParent().requestDisallowInterceptTouchEvent(true);
                }
                return super.onTouchEvent(event);
            }
        };
        bottomSeekBarView.setReportChanges(true);
        bottomSeekBarView.setInnerColor(Theme.getColor(Theme.key_windowBackgroundGray));
        bottomSeekBarView.setDelegate(new SeekBarView.SeekBarViewDelegate() {
            @Override
            public void onSeekBarDrag(boolean stop, float progress) {
                previewView.setSecondControlPointRatio(progress);
                if (stop) ChatBackgroundPreferences.setInterpolatorSecondOffset(key, progress);
            }

            @Override
            public void onSeekBarPressed(boolean pressed) {}

            @Override
            public CharSequence getContentDescription() {
                return " ";
            }
        });
        bottomSeekBarView.setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        addView(bottomSeekBarView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 38, Gravity.LEFT | Gravity.BOTTOM, 12, 0, 12, 0));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(MeasureSpec.makeMeasureSpec(MeasureSpec.getSize(widthMeasureSpec), MeasureSpec.EXACTLY), MeasureSpec.makeMeasureSpec(AndroidUtilities.dp(38 + 38 + 120), MeasureSpec.EXACTLY));
    }

    private static class BeiserPreviewView extends View {
        private final boolean DRAW_DEBUG = false;

        private final int horizontalPadding = AndroidUtilities.dp(18);
        private final int verticalPadding = AndroidUtilities.dp(4);

        private final Path path = new Path();

        private final Paint paint = new Paint() {
            {
                setColor(Theme.getColor(Theme.key_player_progressBackground));
                setStyle(Style.STROKE);
                setStrokeWidth(8.0f);
                setAntiAlias(true);
            }
        };

        private float firstControlPointRatio = 0f;
        private float secondControlPointRatio = 0f;

        public void setFirstControlPointRatio(float firstControlPointRatio) {
            this.firstControlPointRatio = firstControlPointRatio;
            invalidate();
        }

        public void setSecondControlPointRatio(float secondControlPointRatio) {
            this.secondControlPointRatio = secondControlPointRatio;
            invalidate();
        }

        public BeiserPreviewView(Context context) {
            super(context);
            setWillNotDraw(false);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            final int mw = getMeasuredWidth();
            final int w = getWidth();
            final int h = getHeight();

            final float x1 = horizontalPadding;
            final float y1 = h - verticalPadding;
            final float x4 = w - horizontalPadding;
            final float y4 = verticalPadding;

            final float x2 = (w * secondControlPointRatio) - horizontalPadding + horizontalPadding;
            final float x3 = w * (1f - firstControlPointRatio);

            path.reset();
            path.moveTo(x1, y1);
            path.cubicTo(x2, y1, x3, y4, x4, y4);

            canvas.drawPath(path, paint);

            /*if (DRAW_DEBUG) {
                canvas.drawText("Test%", x2, y1 - (verticalPadding * 3), textPaint);
                canvas.drawCircle(x1, y1, 10, debugPaint);
                canvas.drawCircle(x4, y4, 10, debugPaint);
                canvas.drawCircle(x2, y1, 10, debugPaint2);
                canvas.drawCircle(x3, y4, 10, debugPaint2);
            }*/
        }
    }
}
