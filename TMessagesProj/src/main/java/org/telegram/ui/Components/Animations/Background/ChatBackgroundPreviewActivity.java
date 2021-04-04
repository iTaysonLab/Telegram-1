package org.telegram.ui.Components.Animations.Background;

import android.content.Context;
import android.graphics.Canvas;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.util.Log;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.R;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.Components.Animations.Configurator.ChatBgConfiguratorActivity;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.ScrollSlidingTextTabStrip;

public class ChatBackgroundPreviewActivity extends BaseFragment {
    private ScrollSlidingTextTabStrip scrollSlidingTextTabStrip;
    private ChatGradientView gradientView;
    private ChatGradientView.Scenario currentPreviewScenario = ChatGradientView.Scenario.SendMessage;

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle(LocaleController.getString("NetworkUsage", R.string.NetworkUsage));
        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }
        actionBar.setExtraHeight(AndroidUtilities.dp(44));
        actionBar.setAllowOverlayTitle(false);
        actionBar.setClipContent(true);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                }
            }
        });

        scrollSlidingTextTabStrip = new ScrollSlidingTextTabStrip(context);
        scrollSlidingTextTabStrip.setUseSameWidth(false);
        actionBar.addView(scrollSlidingTextTabStrip, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 44, Gravity.LEFT | Gravity.BOTTOM));
        scrollSlidingTextTabStrip.setDelegate(new ScrollSlidingTextTabStrip.ScrollSlidingTabStripDelegate() {
            @Override
            public void onPageSelected(int id, boolean forward) {
                if (id == 0) {
                    currentPreviewScenario = ChatGradientView.Scenario.SendMessage;
                } else if (id == 1) {
                    currentPreviewScenario = ChatGradientView.Scenario.OpenChat;
                } else if (id == 2) {
                    currentPreviewScenario = ChatGradientView.Scenario.JumpToMessage;
                }
            }

            @Override
            public void onPageScrolled(float progress) {
            }
        });

        scrollSlidingTextTabStrip.addTextTab(0, "Send Message");
        scrollSlidingTextTabStrip.addTextTab(1, "Open Chat");
        scrollSlidingTextTabStrip.addTextTab(2, "Jump to Message");
        scrollSlidingTextTabStrip.setVisibility(View.VISIBLE);
        actionBar.setExtraHeight(AndroidUtilities.dp(44));
        scrollSlidingTextTabStrip.finishAddingTabs();

        FrameLayout frameLayout;
        fragmentView = frameLayout = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));

        gradientView = new ChatGradientView(context);
        frameLayout.addView(gradientView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.WRAP_CONTENT, Gravity.TOP, 0, 0, 0, 51));
        ChatBackgroundController.updateColors(gradientView, Theme.getCachedWallpaper(), false, false);

        FrameLayout bottomOverlayChat = new FrameLayout(context);
        bottomOverlayChat.setBackgroundDrawable(Theme.createSelectorDrawable(Theme.getColor(Theme.key_actionBarDefaultSelector), 3));
        bottomOverlayChat.setWillNotDraw(false);
        bottomOverlayChat.setPadding(0, AndroidUtilities.dp(3), 0, 0);
        frameLayout.addView(bottomOverlayChat, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, 51, Gravity.BOTTOM));
        bottomOverlayChat.setOnClickListener(view -> gradientView.animatePosition(currentPreviewScenario));

        TextView bottomOverlayChatText = new TextView(context);
        bottomOverlayChatText.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 15);
        bottomOverlayChatText.setTypeface(AndroidUtilities.getTypeface("fonts/rmedium.ttf"));
        bottomOverlayChatText.setTextColor(Theme.getColor(Theme.key_chat_fieldOverlayText));
        bottomOverlayChatText.setText("ANIMATE");
        bottomOverlayChat.addView(bottomOverlayChatText, LayoutHelper.createFrame(LayoutHelper.WRAP_CONTENT, LayoutHelper.WRAP_CONTENT, Gravity.CENTER));

        return fragmentView;
    }
}
