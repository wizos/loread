package me.wizos.loread.activity;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.lzy.okgo.callback.StringCallback;
import com.lzy.okgo.model.Response;
import com.socks.library.KLog;

import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.data.WithDB;
import me.wizos.loread.db.Feed;
import me.wizos.loread.db.Tag;
import me.wizos.loread.net.Api;
import me.wizos.loread.net.DataApi;
import me.wizos.loread.utils.ToastUtil;

/**
 * @author Wizos on 2018/3/31.
 */

public class FeedConfigDialog {
    String selectedFeedDisplayMode = Api.DISPLAY_RSS;
    Tag selectedFeedGroup;
    EditText feedNameEdit;

    private StringCallback stringCallback;

    public void setOnUnsubscribeFeedListener(StringCallback stringCallback) {
        this.stringCallback = stringCallback;
    }

    public void showConfigFeedDialog(@NonNull final Context context, final Feed feed) {
        if (feed == null) {
            return;
        }
        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .title("配置该源")
                .customView(R.layout.config_feed_view, true)
                .positiveText("确认")
                .negativeText("取消")
                .neutralText("退订")
                .neutralColor(Color.RED)
                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                        unsubscribeFeed2(feed);
                    }
                })
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                        KLog.e("显示模式：" + selectedFeedDisplayMode);

                        Feed feedx = feed;
                        renameFeed(feedNameEdit.getText().toString(), feedx);
                        if (!selectedFeedDisplayMode.equals(Api.DISPLAY_RSS)) {
                            feed.setDisplayMode(selectedFeedDisplayMode);
                        } else {
                            feed.setDisplayMode(null);
                        }

                        if (selectedFeedGroup != null && selectedFeedGroup.getId() != null && !feed.getCategoryid().equals(selectedFeedGroup.getId())) {
                            // TODO: 2018/3/31  改变feed的分组
                            KLog.e("改变feed的分组");
                        }
                        feedx.update();
                        feedx.saveConfig();
                        dialog.dismiss();
                    }
                }).build();

        dialog.show();

        feedNameEdit = (EditText) dialog.findViewById(R.id.feed_name_edit);
        feedNameEdit.setText(feed.getTitle());


        TextView feedOpenModeSelect = (TextView) dialog.findViewById(R.id.feed_open_mode_select);

        if (TextUtils.isEmpty(feed.getDisplayMode())) {
            selectedFeedDisplayMode = Api.DISPLAY_RSS;
        } else {
            selectedFeedDisplayMode = feed.getDisplayMode();
        }
        feedOpenModeSelect.setText(selectedFeedDisplayMode);
        feedOpenModeSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDefaultDisplayModePopupMenu(context, view);
            }
        });


//        TextView feedTagSelect = (TextView) dialog.findViewById(R.id.feed_tag_select);
//        feedTagSelect.setText(feed.getCategorylabel());
//        feedTagSelect.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showClassPopupMenu( context, view );
//            }
//        });
    }


    public void showDefaultDisplayModePopupMenu(final Context context, final View view) {
        KLog.e("onClickedArticleListOrder图标被点击");
        PopupMenu popupMenu = new PopupMenu(context, view);
        MenuInflater menuInflater = popupMenu.getMenuInflater();
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.display_mode_rss:
                        selectedFeedDisplayMode = Api.DISPLAY_RSS;
                        break;
                    case R.id.display_mode_readability:
                        selectedFeedDisplayMode = Api.DISPLAY_READABILITY;
                        break;
                    case R.id.display_mode_link:
                        selectedFeedDisplayMode = Api.DISPLAY_LINK;
                        break;
                    default:
                        selectedFeedDisplayMode = Api.DISPLAY_RSS;
                        break;
                }
                KLog.e("选择：" + selectedFeedDisplayMode);
                ((TextView) view).setText(menuItem.getTitle());
                return false;
            }
        });
        // 加载布局文件到菜单中去
        menuInflater.inflate(R.menu.menu_default_open_mode, popupMenu.getMenu());
        popupMenu.show();
    }


    public void renameFeed(final String renamedTitle, final Feed feedx) {
        KLog.e("=====" + renamedTitle + feedx.getId());
        if (renamedTitle.equals("") || feedx.getTitle().equals(renamedTitle)) {
            return;
        }
        DataApi.i().renameFeed(feedx.getId(), renamedTitle, new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                if (!response.body().equals("OK")) {
                    this.onError(response);
                    return;
                }
                Feed feed = feedx;
                feed.setTitle(renamedTitle);
                feed.update();
//                WithDB.i().updateFeed(feed);
                // 由于改了 feed 的名字，而每个 article 自带的 feed 名字也得改过来。
                WithDB.i().updateArtsFeedTitle(feed);
//                KLog.e("改了名字" + renamedTitle );
            }

            @Override
            public void onError(Response<String> response) {
                ToastUtil.showLong(App.i().getString(R.string.toast_rename_fail));
            }
        });
    }

    private void unsubscribeFeed2(final Feed feed) {
        if (stringCallback == null) {
            return;
        }
        DataApi.i().unsubscribeFeed(feed.getId(), stringCallback);
    }


    public void showClassPopupMenu(final Context context, final View view) {
        KLog.e("onClickedArticleListOrder图标被点击");
        PopupMenu popupMenu = new PopupMenu(context, view);
        Menu menuList = popupMenu.getMenu();
        final List<Tag> tagsList = WithDB.i().getTags();
        for (int i = 0, size = tagsList.size(); i < size; i++) {
            // GroupId, ItemId, Order/位置，标题
            menuList.add(0, i, i, tagsList.get(i).getTitle());
        }

        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                selectedFeedGroup = tagsList.get(menuItem.getItemId());
                ((TextView) view).setText(menuItem.getTitle());
                KLog.e("选择：" + menuItem.getTitle() + menuItem.getItemId() + "   " + menuItem.getGroupId() + "   " + menuItem.getOrder());
                return false;
            }
        });
        popupMenu.show();
    }

    private void unsubscribeFeed(final Feed feed) {
        if (stringCallback == null) {
            return;
        }
        DataApi.i().unsubscribeFeed(feed.getId(), stringCallback);

        DataApi.i().unsubscribeFeed(feed.getId(), new StringCallback() {
            @Override
            public void onSuccess(Response<String> response) {
                if (!response.body().equals("OK")) {
                    this.onError(response);
                    return;
                }
                WithDB.i().delFeed(feed);
                // 返回 mainActivity 页面，并且跳到下一个 tag/feed
//              KLog.e("移除" + itemView.groupPos + "  " + itemView.childPos );
//                tagListAdapter.removeChild(itemView.groupPos, itemView.childPos);
//                tagListAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Response<String> response) {
                ToastUtil.showLong(App.i().getString(R.string.toast_unsubscribe_fail));
            }
        });
    }

}
