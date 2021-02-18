package me.wizos.loread.activity;

import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageButton;

import com.afollestad.materialdialogs.MaterialDialog;
import com.elvishew.xlog.XLog;
import com.hjq.toast.ToastUtils;
import com.lzy.okgo.OkGo;
import com.lzy.okgo.callback.FileCallback;
import com.lzy.okgo.model.Response;
import com.lzy.okgo.request.GetRequest;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.config.HeaderRefererConfig;
import me.wizos.loread.config.HostBlockConfig;
import me.wizos.loread.config.header_useragent.UserAgentConfig;
import me.wizos.loread.config.url_rewrite.UrlRewriteConfig;
import me.wizos.loread.db.CorePref;
import me.wizos.loread.network.HttpClientManager;
import me.wizos.loread.utils.TimeUtils;
import me.wizos.loread.view.colorful.Colorful;

/**
 * @author Wizos
 */
public class RuleUpdateActivity extends BaseActivity {
    @BindView(R.id.rule_black_host_edit_button)
    AppCompatImageButton hostBlockUrlButton;
    @BindView(R.id.rule_black_host_cloud_download_button)
    AppCompatImageButton hostBlockSyncButton;
    @BindView(R.id.rule_black_host_url)
    TextView hostBlockUrlView;
    @BindView(R.id.rule_black_host_update)
    TextView hostBlockUpdateView;

    @BindView(R.id.rule_url_rewrite_edit_button)
    AppCompatImageButton urlRewriteUrlButton;
    @BindView(R.id.rule_url_rewrite_cloud_download_button)
    AppCompatImageButton urlRewriteSyncButton;
    @BindView(R.id.rule_url_rewrite_url)
    TextView urlRewriteUrlView;
    @BindView(R.id.rule_url_rewrite_update)
    TextView urlRewriteUpdateView;

    @BindView(R.id.rule_header_referer_edit_button)
    AppCompatImageButton refererUrlButton;
    @BindView(R.id.rule_header_referer_cloud_download_button)
    AppCompatImageButton refererSyncButton;
    @BindView(R.id.rule_header_referer_url)
    TextView refererUrlView;
    @BindView(R.id.rule_header_referer_update)
    TextView refererUpdateView;

    @BindView(R.id.rule_header_useragent_edit_button)
    AppCompatImageButton userAgentUrlButton;
    @BindView(R.id.rule_header_useragent_cloud_download_button)
    AppCompatImageButton userAgentSyncButton;
    @BindView(R.id.rule_header_useragent_url)
    TextView userAgentUrlView;
    @BindView(R.id.rule_header_useragent_update)
    TextView userAgentUpdateView;

    @BindView(R.id.rule_readable_edit_button)
    AppCompatImageButton readabilityUrlButton;
    @BindView(R.id.rule_readable_cloud_download_button)
    AppCompatImageButton readabilitySyncButton;
    @BindView(R.id.rule_readable_url)
    TextView readabilityUrlView;
    @BindView(R.id.rule_readable_update)
    TextView readabilityUpdateView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_rule_update);
        ButterKnife.bind(this);

        setSupportActionBar(findViewById(R.id.rule_manager_toolbar));
        // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        Objects.requireNonNull(getSupportActionBar()).setHomeButtonEnabled(true);
        // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        super.onCreate(savedInstanceState);// 由于使用了自动换主题，所以要放在这里

        if (savedInstanceState != null) {
            onRecoveryInstanceState(savedInstanceState);
        }


        hostBlockUrlView.setText(CorePref.i().globalPref().getString(Contract.HOST_BLOCK_URL,""));
        if(CorePref.i().globalPref().getLong(Contract.HOST_BLOCK_UPDATE,0) != 0){
            hostBlockUpdateView.setText(TimeUtils.readability(CorePref.i().globalPref().getLong(Contract.HOST_BLOCK_UPDATE,0)));
        }
        hostBlockUrlButton.setOnClickListener(v -> {
            String preFill = CorePref.i().globalPref().getString(Contract.HOST_BLOCK_URL,null);
            if(TextUtils.isEmpty(preFill)){
                preFill = Contract.SCHEMA_HTTPS;
            }
            new MaterialDialog.Builder(RuleUpdateActivity.this)
                    .title(R.string.edit_sync_url)
                    .inputType(InputType.TYPE_TEXT_VARIATION_URI)
                    .inputRange(11, 120)
                    .input(null, preFill, (dialog, input) -> {
                        XLog.d("保存的网址为：" + input.toString());
                        CorePref.i().globalPref().putString(Contract.HOST_BLOCK_URL, input.toString());
                        hostBlockUrlView.setText(input.toString());
                        hostBlockUpdateView.setText(TimeUtils.readability(System.currentTimeMillis()));
                        hostBlockSyncButton.setEnabled(true);
                    })
                    .positiveText(R.string.confirm)
                    .negativeText(android.R.string.cancel)
                    .show();
        });
        urlRewriteUrlView.setText(CorePref.i().globalPref().getString(Contract.URL_REWRITE_URL,""));
        if(CorePref.i().globalPref().getLong(Contract.URL_REWRITE_UPDATE,0) != 0){
            urlRewriteUrlView.setText(TimeUtils.readability(CorePref.i().globalPref().getLong(Contract.URL_REWRITE_UPDATE,0)));
        }
        urlRewriteUrlButton.setOnClickListener(v -> {
            String preFill = CorePref.i().globalPref().getString(Contract.URL_REWRITE_URL,null);
            if(TextUtils.isEmpty(preFill)){
                preFill = Contract.SCHEMA_HTTPS;
            }
            new MaterialDialog.Builder(RuleUpdateActivity.this)
                    .title(R.string.edit_sync_url)
                    .inputType(InputType.TYPE_TEXT_VARIATION_URI)
                    .inputRange(11, 120)
                    .input(null, preFill, (dialog, input) -> {
                        XLog.d("保存的网址为：" + input.toString());
                        CorePref.i().globalPref().putString(Contract.URL_REWRITE_URL, input.toString());
                        urlRewriteUrlView.setText(input.toString());
                        urlRewriteUpdateView.setText(TimeUtils.readability(System.currentTimeMillis()));
                        urlRewriteSyncButton.setEnabled(true);
                    })
                    .positiveText(R.string.confirm)
                    .negativeText(android.R.string.cancel)
                    .show();
        });
        refererUrlView.setText(CorePref.i().globalPref().getString(Contract.HEADER_REFERER_URL,""));
        refererUrlButton.setOnClickListener(v -> {
            String preFill = CorePref.i().globalPref().getString(Contract.HEADER_REFERER_URL,null);
            if(TextUtils.isEmpty(preFill)){
                preFill = Contract.SCHEMA_HTTPS;
            }
            new MaterialDialog.Builder(RuleUpdateActivity.this)
                    .title(R.string.edit_sync_url)
                    .inputType(InputType.TYPE_TEXT_VARIATION_URI)
                    .inputRange(11, 120)
                    .input(null, preFill, (dialog, input) -> {
                        XLog.d("保存的网址为：" + input.toString());
                        CorePref.i().globalPref().putString(Contract.HEADER_REFERER_URL, input.toString());
                        refererUrlView.setText(input.toString());
                        refererUpdateView.setText(TimeUtils.readability(System.currentTimeMillis()));
                        refererSyncButton.setEnabled(true);
                    })
                    .positiveText(R.string.confirm)
                    .negativeText(android.R.string.cancel)
                    .show();
        });
        userAgentUrlView.setText(CorePref.i().globalPref().getString(Contract.HEADER_USER_AGENT_URL,""));
        userAgentUrlButton.setOnClickListener(v -> {
            String preFill = CorePref.i().globalPref().getString(Contract.HEADER_USER_AGENT_URL,null);
            if(TextUtils.isEmpty(preFill)){
                preFill = Contract.SCHEMA_HTTPS;
            }
            new MaterialDialog.Builder(RuleUpdateActivity.this)
                    .title(R.string.edit_sync_url)
                    .inputType(InputType.TYPE_TEXT_VARIATION_URI)
                    .inputRange(11, 120)
                    .input(null, preFill, (dialog, input) -> {
                        XLog.d("保存的网址为：" + input.toString());
                        CorePref.i().globalPref().putString(Contract.HEADER_USER_AGENT_URL, input.toString());
                        userAgentUrlView.setText(input.toString());
                        userAgentUpdateView.setText(TimeUtils.readability(System.currentTimeMillis()));
                        userAgentSyncButton.setEnabled(true);
                    })
                    .positiveText(R.string.confirm)
                    .negativeText(android.R.string.cancel)
                    .show();
        });
        readabilityUrlView.setText(CorePref.i().globalPref().getString(Contract.READABILITY_URL,""));
        readabilityUrlButton.setOnClickListener(v -> {
            String preFill = CorePref.i().globalPref().getString(Contract.READABILITY_URL,null);
            if(TextUtils.isEmpty(preFill)){
                preFill = Contract.SCHEMA_HTTPS;
            }
            new MaterialDialog.Builder(RuleUpdateActivity.this)
                    .title(R.string.edit_sync_url)
                    .inputType(InputType.TYPE_TEXT_VARIATION_URI)
                    .inputRange(11, 120)
                    .input(null, preFill, (dialog, input) -> {
                        XLog.d("保存的网址为：" + input.toString());
                        CorePref.i().globalPref().putString(Contract.READABILITY_URL, input.toString());
                        readabilityUrlView.setText(input.toString());
                        readabilityUpdateView.setText(TimeUtils.readability(System.currentTimeMillis()));
                        readabilitySyncButton.setEnabled(true);
                    })
                    .positiveText(R.string.confirm)
                    .negativeText(android.R.string.cancel)
                    .show();
        });

        hostBlockSyncButton.setEnabled(!TextUtils.isEmpty(CorePref.i().globalPref().getString(Contract.HOST_BLOCK_URL,null)));
        hostBlockSyncButton.setOnClickListener(v -> {
            download(CorePref.i().globalPref().getString(Contract.HOST_BLOCK_URL,null), HostBlockConfig.FILENAME);
            HostBlockConfig.i().reset();
            CorePref.i().globalPref().putLong(Contract.HOST_BLOCK_UPDATE, System.currentTimeMillis());
        });

        urlRewriteSyncButton.setEnabled(!TextUtils.isEmpty(CorePref.i().globalPref().getString(Contract.URL_REWRITE_URL,null)));
        urlRewriteSyncButton.setOnClickListener(v -> {
            download(CorePref.i().globalPref().getString(Contract.URL_REWRITE_URL,null), UrlRewriteConfig.FILENAME);
            UrlRewriteConfig.i().reset();
            CorePref.i().globalPref().putLong(Contract.URL_REWRITE_UPDATE, System.currentTimeMillis());
        });

        refererSyncButton.setEnabled(!TextUtils.isEmpty(CorePref.i().globalPref().getString(Contract.HEADER_REFERER_URL,null)));
        refererSyncButton.setOnClickListener(v -> {
            download(CorePref.i().globalPref().getString(Contract.HEADER_REFERER_URL,null), HeaderRefererConfig.FILE_NAME);
            HeaderRefererConfig.i().reset();
            CorePref.i().globalPref().putLong(Contract.HEADER_REFERER_UPDATE, System.currentTimeMillis());
        });

        userAgentSyncButton.setEnabled(!TextUtils.isEmpty(CorePref.i().globalPref().getString(Contract.HEADER_USER_AGENT_URL,null)));
        userAgentSyncButton.setOnClickListener(v -> {
            download(CorePref.i().globalPref().getString(Contract.HEADER_USER_AGENT_URL,null), UserAgentConfig.FILE_NAME);
            UserAgentConfig.i().reset();
            CorePref.i().globalPref().putLong(Contract.HEADER_USER_AGENT_UPDATE, System.currentTimeMillis());
        });

        readabilitySyncButton.setEnabled(!TextUtils.isEmpty(CorePref.i().globalPref().getString(Contract.READABILITY_URL,null)));
        readabilitySyncButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtils.show(R.string.temporarily_not_supported);
                // download(CorePref.i().globalPref().getString(Contract.READABILITY_URL,null), ArticleActionConfig.FILENAME);
            }
        });

        findViewById(R.id.rule_readable_title).setVisibility(View.GONE);
        readabilityUrlButton.setVisibility(View.GONE);
        readabilitySyncButton.setVisibility(View.GONE);
    }


    private void download(String url, String fileName){
        if(TextUtils.isEmpty(url)){
            return;
        }
        GetRequest<File> request = OkGo.<File>get(url)
                .tag(url)
                .client(HttpClientManager.i().simpleClient());
        DownFileCallback downFileCallback = new DownFileCallback(App.i().getGlobalConfigPath(), fileName);
        request.execute(downFileCallback);
    }



    private static class DownFileCallback extends FileCallback {
        DownFileCallback(String destFileDir, String destFileName) {
            super(destFileDir, destFileName);
        }
        @Override
        public void onSuccess(Response<File> response) {
            ToastUtils.show(R.string.download_success);
        }

        // 该方法执行在主线程中
        @Override
        public void onError(Response<File> response) {
            XLog.w("下载失败：" + "', '" + response.code() + "  " + response.getException());
            ToastUtils.show(R.string.download_fail);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // 后者为短期内按下的次数
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) {
            exit();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }



    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void onRecoveryInstanceState(@NonNull Bundle outState) {
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_rule_update, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //监听左上角的返回箭头
            case android.R.id.home:
                exit();
                break;
            case R.id.rule_manager_menu_cloud_download:
                download(CorePref.i().globalPref().getString(Contract.HOST_BLOCK_URL,null), HostBlockConfig.FILENAME);
                HostBlockConfig.i().reset();

                download(CorePref.i().globalPref().getString(Contract.URL_REWRITE_URL,null), UrlRewriteConfig.FILENAME);
                UrlRewriteConfig.i().reset();

                download(CorePref.i().globalPref().getString(Contract.HEADER_REFERER_URL,null), HeaderRefererConfig.FILE_NAME);
                HeaderRefererConfig.i().reset();

                download(CorePref.i().globalPref().getString(Contract.HEADER_USER_AGENT_URL,null), UserAgentConfig.FILE_NAME);
                UserAgentConfig.i().reset();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void exit() {
        this.finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        mColorfulBuilder
                .backgroundColor(R.id.rule_manager_root, R.attr.root_view_bg)
                .backgroundColor(R.id.rule_manager_toolbar, R.attr.topbar_bg)

                .textColor(R.id.rule_black_host_title,R.attr.lv_item_title_color)
                .textColor(R.id.rule_black_host_url,R.attr.lv_item_info_color)
                .textColor(R.id.rule_black_host_update,R.attr.lv_item_info_color)

                .textColor(R.id.rule_url_rewrite_title,R.attr.lv_item_title_color)
                .textColor(R.id.rule_url_rewrite_url,R.attr.lv_item_info_color)
                .textColor(R.id.rule_url_rewrite_update,R.attr.lv_item_info_color)

                .textColor(R.id.rule_header_referer_title,R.attr.lv_item_title_color)
                .textColor(R.id.rule_header_referer_url,R.attr.lv_item_info_color)
                .textColor(R.id.rule_header_referer_update,R.attr.lv_item_info_color)

                .textColor(R.id.rule_header_useragent_title,R.attr.lv_item_title_color)
                .textColor(R.id.rule_header_useragent_url,R.attr.lv_item_info_color)
                .textColor(R.id.rule_header_useragent_update,R.attr.lv_item_info_color)

                .textColor(R.id.rule_readable_title,R.attr.lv_item_title_color)
                .textColor(R.id.rule_readable_url,R.attr.lv_item_info_color)
                .textColor(R.id.rule_readable_update,R.attr.lv_item_info_color);
        return mColorfulBuilder;
    }
}
