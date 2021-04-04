package org.telegram.ui.Components.Animations.Configurator;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.telegram.messenger.AndroidUtilities;
import org.telegram.messenger.ApplicationLoader;
import org.telegram.messenger.BuildConfig;
import org.telegram.messenger.FileLog;
import org.telegram.messenger.LocaleController;
import org.telegram.messenger.NotificationCenter;
import org.telegram.messenger.R;
import org.telegram.messenger.Utilities;
import org.telegram.ui.ActionBar.ActionBar;
import org.telegram.ui.ActionBar.ActionBarMenu;
import org.telegram.ui.ActionBar.ActionBarMenuItem;
import org.telegram.ui.ActionBar.ActionBarPopupWindow;
import org.telegram.ui.ActionBar.AlertDialog;
import org.telegram.ui.ActionBar.BaseFragment;
import org.telegram.ui.ActionBar.Theme;
import org.telegram.ui.ActionBar.ThemeDescription;
import org.telegram.ui.Cells.HeaderCell;
import org.telegram.ui.Cells.NotificationsCheckCell;
import org.telegram.ui.Cells.ShadowSectionCell;
import org.telegram.ui.Cells.TextCell;
import org.telegram.ui.Cells.TextCheckCell;
import org.telegram.ui.Cells.TextDetailSettingsCell;
import org.telegram.ui.Cells.TextInfoPrivacyCell;
import org.telegram.ui.Cells.TextSettingsCell;
import org.telegram.ui.Components.Animations.Background.ChatBackgroundController;
import org.telegram.ui.Components.Animations.Background.ChatBackgroundPreviewActivity;
import org.telegram.ui.Components.Animations.Background.ChatGradientView;
import org.telegram.ui.Components.LayoutHelper;
import org.telegram.ui.Components.RecyclerListView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

public class ChatBgConfiguratorActivity extends BaseFragment {
    private final int[] AVAILABLE_DURATIONS = new int[]{
            200, 300, 400, 500, 600, 700, 800, 900, 1000, 1500, 2000, 3000
    };

    private ListAdapter listAdapter;
    private RecyclerListView listView;

    private ChatBgColorPrefView[] cgvs = new ChatBgColorPrefView[4];
    private ActionBarPopupWindow currentPopup = null;

    private int abm_export = 10;
    private int abm_import = 11;
    private int abm_reset = 12;

    private int rowCount;

    private int previewSectionRow;
    private int previewViewRow;
    private int previewOpenFsRow;
    private int previewSection2Row;

    private int optionsSectionRow;
    private int optionsEnabledRow;
    private int optionsAutoColorRow;
    private int optionsAutoColorRandomizeRow;
    private int optionsPathRandomizeRow;
    private int optionsSection2Row;

    private int colorsSectionRow;
    private int colorOneRow;
    private int colorTwoRow;
    private int colorThreeRow;
    private int colorFourRow;
    private int colorsSection2Row;

    private int sendMessageRow;
    private int sendMessageDurationRow;
    private int sendMessageActualRow;
    private int sendMessage2Row;

    private int openChatRow;
    private int openChatDurationRow;
    private int openChatActualRow;
    private int openChat2Row;

    private int jumpToMessageRow;
    private int jumpToMessageDurationRow;
    private int jumpToMessageActualRow;
    private int jumpToMessage2Row;

    private ChatGradientView gradientRef;

    @Override
    public boolean onFragmentCreate() {
        super.onFragmentCreate();

        rowCount = 0;

        previewSectionRow = rowCount++;
        previewViewRow = rowCount++;
        previewOpenFsRow = rowCount++;
        previewSection2Row = rowCount++;

        optionsSectionRow = rowCount++;
        optionsEnabledRow = rowCount++;
        optionsAutoColorRow = rowCount++;
        optionsAutoColorRandomizeRow = rowCount++;
        optionsPathRandomizeRow = rowCount++;
        optionsSection2Row = rowCount++;

        colorsSectionRow = rowCount++;
        colorOneRow = rowCount++;
        colorTwoRow = rowCount++;
        colorThreeRow = rowCount++;
        colorFourRow = rowCount++;
        colorsSection2Row = rowCount++;

        sendMessageRow = rowCount++;
        sendMessageDurationRow = rowCount++;
        sendMessageActualRow = rowCount++;
        sendMessage2Row = rowCount++;

        openChatRow = rowCount++;
        openChatDurationRow = rowCount++;
        openChatActualRow = rowCount++;
        openChat2Row = rowCount++;

        jumpToMessageRow = rowCount++;
        jumpToMessageDurationRow = rowCount++;
        jumpToMessageActualRow = rowCount++;
        jumpToMessage2Row = rowCount++;

        return true;
    }

    @Override
    public View createView(Context context) {
        actionBar.setBackButtonImage(R.drawable.ic_ab_back);
        actionBar.setTitle("Chat Background");

        if (AndroidUtilities.isTablet()) {
            actionBar.setOccupyStatusBar(false);
        }

        ActionBarMenu menu = actionBar.createMenu();
        ActionBarMenuItem headerItem = menu.addItem(0, R.drawable.ic_ab_other);
        headerItem.setContentDescription(LocaleController.getString("AccDescrMoreOptions", R.string.AccDescrMoreOptions));
        headerItem.addSubItem(abm_export, "Share Parameters");
        headerItem.addSubItem(abm_import, "Import Parameters");
        headerItem.addSubItem(abm_reset, "Restore to Default");

        actionBar.setAllowOverlayTitle(true);
        actionBar.setActionBarMenuOnItemClick(new ActionBar.ActionBarMenuOnItemClick() {
            @Override
            public void onItemClick(int id) {
                if (id == -1) {
                    finishFragment();
                } else if (id == abm_export) {
                    callExportConfig();
                } else if (id == abm_import) {
                    callImportConfig();
                } else if (id == abm_reset) {
                    ChatBackgroundPreferences.resetPrefs();
                    listAdapter.notifyDataSetChanged();
                    NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetNewWallpapper);
                }
            }
        });

        listAdapter = new ListAdapter(context);

        fragmentView = new FrameLayout(context);
        fragmentView.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundGray));
        FrameLayout frameLayout = (FrameLayout) fragmentView;

        listView = new RecyclerListView(context);
        listView.setVerticalScrollBarEnabled(false);
        listView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));
        frameLayout.addView(listView, LayoutHelper.createFrame(LayoutHelper.MATCH_PARENT, LayoutHelper.MATCH_PARENT, Gravity.TOP | Gravity.LEFT));
        listView.setAdapter(listAdapter);

        listView.setOnItemClickListener((view, position, x, y) -> {
            if (position == previewOpenFsRow) {
                presentFragment(new ChatBackgroundPreviewActivity());
            } else if (position == optionsEnabledRow) {
                boolean value = !ChatBackgroundPreferences.isMotionBackgroundEnabled();
                ChatBackgroundPreferences.setMotionBackgroundEnabled(value);
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetNewWallpapper);
                ((TextCheckCell) view).setChecked(value);
            } else if (position == optionsAutoColorRow) {
                boolean value = !ChatBackgroundPreferences.isWallpaperColorFetchEnabled();
                ChatBackgroundPreferences.setWallpaperColorFetchEnabled(value);
                if (gradientRef != null)
                    ChatBackgroundController.updateColors(gradientRef, Theme.getCachedWallpaper(), false, true, this::updatePrefColors);
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetNewWallpapper);
                ((TextCheckCell) view).setChecked(value);
            } else if (position == optionsAutoColorRandomizeRow) {
                boolean value = !ChatBackgroundPreferences.isWallpaperColorFetchRandomEnabled();
                ChatBackgroundPreferences.setWallpaperColorFetchRandomEnabled(value);
                if (gradientRef != null)
                    ChatBackgroundController.updateColors(gradientRef, Theme.getCachedWallpaper(), false, true, this::updatePrefColors);
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetNewWallpapper);
                ((TextCheckCell) view).setChecked(value);
            } else if (position == optionsPathRandomizeRow) {
                boolean value = !ChatBackgroundPreferences.isWallpaperPathRandomizeEnabled();
                ChatBackgroundPreferences.setWallpaperPathRandomizeEnabled(value);
                ((TextCheckCell) view).setChecked(value);
            } else if (position == colorOneRow) {
                ((ChatBgColorPrefView) view).callPicker(0, gradientRef);
            } else if (position == colorTwoRow) {
                ((ChatBgColorPrefView) view).callPicker(1, gradientRef);
            } else if (position == colorThreeRow) {
                ((ChatBgColorPrefView) view).callPicker(2, gradientRef);
            } else if (position == colorFourRow) {
                ((ChatBgColorPrefView) view).callPicker(3, gradientRef);
            } else if (position == sendMessageDurationRow) {
                callDurationPopup(ChatGradientView.Scenario.SendMessage, (TextSettingsCell) view);
            } else if (position == openChatDurationRow) {
                callDurationPopup(ChatGradientView.Scenario.OpenChat, (TextSettingsCell) view);
            } else if (position == jumpToMessageDurationRow) {
                callDurationPopup(ChatGradientView.Scenario.JumpToMessage, (TextSettingsCell) view);
            }
        });

        return fragmentView;
    }

    private void callExportConfig() {
        AlertDialog progressDialog = new AlertDialog(getParentActivity(), 3);
        progressDialog.setCanCacnel(false);
        progressDialog.show();
        Utilities.globalQueue.postRunnable(() -> {
            try {
                File sdCard = ApplicationLoader.applicationContext.getExternalFilesDir(null);
                File dir = new File(sdCard.getAbsolutePath() + "/logs");

                File zipFile = new File(dir, "contest_config.txt");
                if (zipFile.exists()) {
                    zipFile.delete();
                }

                BufferedWriter out = null;
                try {
                    FileOutputStream dest = new FileOutputStream(zipFile);
                    out = new BufferedWriter(new OutputStreamWriter(dest));
                    ChatBackgroundPreferences.Saver.writePrefsToFile(out);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    if (out != null) {
                        out.close();
                    }
                }

                AndroidUtilities.runOnUIThread(() -> {
                    try {
                        progressDialog.dismiss();
                    } catch (Exception ignore) {

                    }

                    Uri uri;
                    if (Build.VERSION.SDK_INT >= 24) {
                        uri = FileProvider.getUriForFile(getParentActivity(), BuildConfig.APPLICATION_ID + ".provider", zipFile);
                    } else {
                        uri = Uri.fromFile(zipFile);
                    }

                    Intent i = new Intent(Intent.ACTION_SEND);
                    if (Build.VERSION.SDK_INT >= 24) {
                        i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    }

                    i.setType("text/plain");
                    i.putExtra(Intent.EXTRA_SUBJECT, "Settings export from " + LocaleController.getInstance().formatterStats.format(System.currentTimeMillis()));
                    i.putExtra(Intent.EXTRA_STREAM, uri);
                    if (getParentActivity() != null) {
                        try {
                            Intent chooserIntent = Intent.createChooser(i, "Select email application.");

                            List<ResolveInfo> resInfoList = getParentActivity().getPackageManager().queryIntentActivities(chooserIntent, PackageManager.MATCH_DEFAULT_ONLY);

                            for (ResolveInfo resolveInfo : resInfoList) {
                                String packageName = resolveInfo.activityInfo.packageName;
                                getParentActivity().grantUriPermission(packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
                            }

                            getParentActivity().startActivityForResult(chooserIntent, 500);
                        } catch (Exception e) {
                            FileLog.e(e);
                        }
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onActivityResultFragment(int requestCode, int resultCode, Intent data) {
        if (requestCode == 15 && resultCode == Activity.RESULT_OK && data.getData() != null) {
            try (InputStream inputStream = getParentActivity().getContentResolver().openInputStream(data.getData()); BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                ChatBackgroundPreferences.Saver.stringToPrefs(reader);
                listAdapter.notifyDataSetChanged();
                NotificationCenter.getGlobalInstance().postNotificationName(NotificationCenter.didSetNewWallpapper);
            } catch (Exception e) {
                Toast.makeText(getParentActivity(), "Error occurred - the file might be malformed!", Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }

    private void callImportConfig() {
        Intent filePickerIntent = new Intent(Intent.ACTION_GET_CONTENT);
        filePickerIntent.setType("text/plain");
        startActivityForResult(filePickerIntent, 15);
    }

    public void callDurationPopup(ChatGradientView.Scenario key, TextSettingsCell anchor) {
        if (currentPopup != null) {
            currentPopup.dismiss();
        }

        ArrayList<ChatBgPopupHelper.PopupItem> items = new ArrayList<>();

        for (int dur : AVAILABLE_DURATIONS) {
            items.add(new ChatBgPopupHelper.PopupItem(dur + "ms", 0, (view) -> {
                ChatBackgroundPreferences.setInterpolatorDuration(key, dur);
                anchor.setTextAndValue("Duration", dur + "ms", true);
                currentPopup.dismiss();
                currentPopup = null;
            }));
        }

        currentPopup = ChatBgPopupHelper.createPopupWindow((FrameLayout) fragmentView, anchor, getParentActivity(), items);
    }

    public void updatePrefColors(int[] colors) {
        for (int i = 0; i < colors.length; i++) {
            if (cgvs[i] != null) cgvs[i].updateBackground(colors[i]);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (listAdapter != null) {
            listAdapter.notifyDataSetChanged();
        }
    }

    private class ListAdapter extends RecyclerListView.SelectionAdapter {

        private Context mContext;

        ListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return rowCount;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            switch (holder.getItemViewType()) {
                case 0: {
                    holder.itemView.setBackgroundDrawable(Theme.getThemedDrawable(mContext, R.drawable.greydivider, Theme.key_windowBackgroundGrayShadow));
                    break;
                }
                case 1: {
                    TextSettingsCell textCell = (TextSettingsCell) holder.itemView;
                    textCell.setCanDisable(false);
                    textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlackText));

                    if (position == previewOpenFsRow) {
                        textCell.setTextColor(Theme.getColor(Theme.key_windowBackgroundWhiteBlueText));
                        textCell.setText("Open Full Screen", false);
                    } else if (position == sendMessageDurationRow) {
                        textCell.setTextAndValue("Duration", ChatBackgroundPreferences.getInterpolatorDuration(ChatGradientView.Scenario.SendMessage) + "ms", true);
                    } else if (position == openChatDurationRow) {
                        textCell.setTextAndValue("Duration", ChatBackgroundPreferences.getInterpolatorDuration(ChatGradientView.Scenario.OpenChat) + "ms", true);
                    } else if (position == jumpToMessageDurationRow) {
                        textCell.setTextAndValue("Duration", ChatBackgroundPreferences.getInterpolatorDuration(ChatGradientView.Scenario.JumpToMessage) + "ms", true);
                    }

                    break;
                }
                case 2: {
                    HeaderCell cell = ((HeaderCell) holder.itemView);

                    cell.setPadding(0, 0, 0, position == previewSectionRow ? AndroidUtilities.dp(15f) : 0);

                    if (position == previewSectionRow) {
                        cell.setText("Background Preview");
                    } else if (position == optionsSectionRow) {
                        cell.setText("Options");
                    } else if (position == colorsSectionRow) {
                        cell.setText("Colors");
                    } else if (position == sendMessageRow) {
                        cell.setText("Send Message");
                    } else if (position == openChatRow) {
                        cell.setText("Open Chat");
                    } else if (position == jumpToMessageRow) {
                        cell.setText("Jump to Message");
                    }

                    break;
                }
                case 3: {
                    TextCheckCell checkCell = (TextCheckCell) holder.itemView;

                    if (position == optionsAutoColorRow) {
                        checkCell.setTextAndCheck("Auto-color based on wallpaper", ChatBackgroundPreferences.isWallpaperColorFetchEnabled(), true);
                    } else if (position == optionsAutoColorRandomizeRow) {
                        checkCell.setTextAndCheck("[AC only] Randomize color positions", ChatBackgroundPreferences.isWallpaperColorFetchRandomEnabled(), true);
                    }  else if (position == optionsPathRandomizeRow) {
                        checkCell.setTextAndCheck("Randomize point positions", ChatBackgroundPreferences.isWallpaperPathRandomizeEnabled(), false);
                    } else if (position == optionsEnabledRow) {
                        checkCell.setTextAndCheck("Enabled", ChatBackgroundPreferences.isMotionBackgroundEnabled(), true);
                    }

                    break;
                }
                case 4: {
                    TextDetailSettingsCell settingsCell = (TextDetailSettingsCell) holder.itemView;
                    settingsCell.setMultilineDetail(true);
                    //settingsCell.setTextAndValue();
                    break;
                }
                case 5: {
                    TextCell cell = (TextCell) holder.itemView;
                    break;
                }
                case 9: {
                    ChatBgColorPrefView cell = (ChatBgColorPrefView) holder.itemView;
                    if (position == colorOneRow) {
                        cell.setBgIndex(0, true);
                    } else if (position == colorTwoRow) {
                        cell.setBgIndex(1, true);
                    } else if (position == colorThreeRow) {
                        cell.setBgIndex(2, true);
                    } else if (position == colorFourRow) {
                        cell.setBgIndex(3, false);
                    }
                    break;
                }
                case 6: {
                    gradientRef = (ChatGradientView) holder.itemView;
                    ChatBackgroundController.updateColors(gradientRef, Theme.getCachedWallpaper(), false, true, ChatBgConfiguratorActivity.this::updatePrefColors);
                    break;
                }
                case 10: {
                    ChatBeiserPrefView view = (ChatBeiserPrefView) holder.itemView;

                    if (position == sendMessageActualRow) {
                        view.setKey(ChatGradientView.Scenario.SendMessage);
                    } else if (position == jumpToMessageActualRow) {
                        view.setKey(ChatGradientView.Scenario.JumpToMessage);
                    } else if (position == openChatActualRow) {
                        view.setKey(ChatGradientView.Scenario.OpenChat);
                    }
                }
            }
        }

        @Override
        public void onViewAttachedToWindow(RecyclerView.ViewHolder holder) {
            int viewType = holder.getItemViewType();
            int position = holder.getAdapterPosition();

            if (viewType == 3) {
                TextCheckCell checkCell = (TextCheckCell) holder.itemView;

                if (position == optionsAutoColorRow) {
                    checkCell.setChecked(ChatBackgroundPreferences.isWallpaperColorFetchEnabled());
                } else if (position == optionsEnabledRow) {
                    checkCell.setChecked(ChatBackgroundPreferences.isMotionBackgroundEnabled());
                } else if (position == optionsAutoColorRandomizeRow) {
                    checkCell.setChecked(ChatBackgroundPreferences.isWallpaperColorFetchRandomEnabled());
                } else if (position == optionsPathRandomizeRow) {
                    checkCell.setChecked(ChatBackgroundPreferences.isWallpaperPathRandomizeEnabled());
                }
            } else if (viewType == 6) {
                gradientRef = (ChatGradientView) holder.itemView;
            } else if (viewType == 9) {
                ChatBgColorPrefView cell = (ChatBgColorPrefView) holder.itemView;
                if (position == colorOneRow) {
                    //cell.setBgIndex(0, true);
                    cgvs[0] = cell;
                } else if (position == colorTwoRow) {
                    //cell.setBgIndex(1, true);
                    cgvs[1] = cell;
                } else if (position == colorThreeRow) {
                    //cell.setBgIndex(2, true);
                    cgvs[2] = cell;
                } else if (position == colorFourRow) {
                    //cell.setBgIndex(3, false);
                    cgvs[3] = cell;
                }
            }
        }

        @Override
        public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
            int viewType = holder.getItemViewType();
            int position = holder.getAdapterPosition();

            if (viewType == 6) {
                gradientRef = null;
            } else if (viewType == 9) {
                if (position == colorOneRow) {
                    cgvs[0] = null;
                } else if (position == colorTwoRow) {
                    cgvs[1] = null;
                } else if (position == colorThreeRow) {
                    cgvs[2] = null;
                } else if (position == colorFourRow) {
                    cgvs[3] = null;
                }
            }
        }

        public boolean isRowEnabled(int position) {
            return position == sendMessageDurationRow || position == openChatDurationRow || position == jumpToMessageDurationRow || position == previewOpenFsRow || position == optionsAutoColorRow || position == optionsAutoColorRandomizeRow || position == optionsPathRandomizeRow || position == optionsEnabledRow || position == colorOneRow || position == colorTwoRow || position == colorThreeRow || position == colorFourRow;
        }

        @Override
        public boolean isEnabled(RecyclerView.ViewHolder holder) {
            return isRowEnabled(holder.getAdapterPosition());
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = null;
            switch (viewType) {
                case 0:
                    view = new ShadowSectionCell(mContext);
                    break;
                case 1:
                    view = new TextSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 2:
                    view = new HeaderCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 3:
                    view = new TextCheckCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 4:
                    view = new TextDetailSettingsCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 5:
                    view = new TextCell(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 6:
                    view = new ChatGradientView(mContext);
                    break;
                case 8:
                    view = new TextInfoPrivacyCell(mContext);
                    break;
                case 9:
                    view = new ChatBgColorPrefView(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
                case 10:
                    view = new ChatBeiserPrefView(mContext);
                    view.setBackgroundColor(Theme.getColor(Theme.key_windowBackgroundWhite));
                    break;
            }
            if (viewType == 6) {
                view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, AndroidUtilities.dp(200f)));
            } else {
                view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.WRAP_CONTENT));
            }
            return new RecyclerListView.Holder(view);
        }

        @Override
        public int getItemViewType(int position) {
            if (position == previewSectionRow || position == optionsSectionRow || position == colorsSectionRow || position == jumpToMessageRow || position == openChatRow || position == sendMessageRow) {
                return 2;
            } else if (position == previewSection2Row || position == optionsSection2Row || position == colorsSection2Row || position == jumpToMessage2Row || position == openChat2Row || position == sendMessage2Row) {
                return 0;
            } else if (position == previewOpenFsRow || position == jumpToMessageDurationRow || position == openChatDurationRow || position == sendMessageDurationRow) {
                return 1;
            } else if (position == previewViewRow) {
                return 6;
            } else if (position == optionsAutoColorRow || position == optionsAutoColorRandomizeRow || position == optionsPathRandomizeRow || position == optionsEnabledRow) {
                return 3;
            } else if (position == colorOneRow || position == colorTwoRow || position == colorThreeRow || position == colorFourRow) {
                return 9;
            } else if (position == jumpToMessageActualRow || position == openChatActualRow || position == sendMessageActualRow) {
                return 10;
            } else {
                return 1;
            }
        }
    }

    @Override
    public ArrayList<ThemeDescription> getThemeDescriptions() {
        ArrayList<ThemeDescription> themeDescriptions = new ArrayList<>();

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_CELLBACKGROUNDCOLOR, new Class[]{TextSettingsCell.class, TextCheckCell.class, HeaderCell.class, NotificationsCheckCell.class}, null, null, null, Theme.key_windowBackgroundWhite));
        themeDescriptions.add(new ThemeDescription(fragmentView, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_windowBackgroundGray));

        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_BACKGROUND, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_LISTGLOWCOLOR, null, null, null, null, Theme.key_actionBarDefault));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_ITEMSCOLOR, null, null, null, null, Theme.key_actionBarDefaultIcon));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_TITLECOLOR, null, null, null, null, Theme.key_actionBarDefaultTitle));
        themeDescriptions.add(new ThemeDescription(actionBar, ThemeDescription.FLAG_AB_SELECTORCOLOR, null, null, null, null, Theme.key_actionBarDefaultSelector));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{NotificationsCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_SELECTOR, null, null, null, null, Theme.key_listSelector));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{View.class}, Theme.dividerPaint, null, null, Theme.key_divider));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{ShadowSectionCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextSettingsCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteValueText));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{HeaderCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlueHeader));

        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteBlackText));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"valueTextView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText2));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrack));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextCheckCell.class}, new String[]{"checkBox"}, null, null, null, Theme.key_switchTrackChecked));

        themeDescriptions.add(new ThemeDescription(listView, ThemeDescription.FLAG_BACKGROUNDFILTER, new Class[]{TextInfoPrivacyCell.class}, null, null, null, Theme.key_windowBackgroundGrayShadow));
        themeDescriptions.add(new ThemeDescription(listView, 0, new Class[]{TextInfoPrivacyCell.class}, new String[]{"textView"}, null, null, null, Theme.key_windowBackgroundWhiteGrayText4));

        return themeDescriptions;
    }
}