package me.wizos.loread.activity;

import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;

import androidx.recyclerview.widget.LinearLayoutManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListAdapter;
import com.afollestad.materialdialogs.simplelist.MaterialSimpleListItem;
import com.elvishew.xlog.XLog;
import com.hjq.toast.ToastUtils;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.LoadingPopupView;

import org.jetbrains.annotations.NotNull;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.BuildConfig;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.activity.login.LoginActivity;
import me.wizos.loread.bean.Token;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.CorePref;
import me.wizos.loread.db.User;
import me.wizos.loread.network.SyncWorker;
import me.wizos.loread.network.api.FeedlyApi;
import me.wizos.loread.network.api.InoReaderApi;
import me.wizos.loread.network.api.OAuthApi;
import me.wizos.loread.network.callback.CallbackX;
import me.wizos.loread.utils.StringUtils;
import me.wizos.loread.view.colorful.Colorful;

public class ProviderActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(Build.VERSION.SDK_INT < 23){
            setContentView(R.layout.activity_provider_low_version);
        }else {
            setContentView(R.layout.activity_provider);
        }

        // setContentView(R.layout.activity_provider);

        View selectLoginAccountView = findViewById(R.id.select_login_account);
        if(CoreDB.i().userDao().size() > 0){
            selectLoginAccountView.setVisibility(View.VISIBLE);
        }else {
            selectLoginAccountView.setVisibility(View.GONE);
        }
    }

    public void selectLoginAccount(View view){
        final List<User> users = CoreDB.i().userDao().loadAll();
        XLog.i("点击切换账号：" + users );
        // 弹窗的适配器
        MaterialSimpleListAdapter adapter = new MaterialSimpleListAdapter(new MaterialSimpleListAdapter.Callback() {
            @Override
            public void onMaterialListItemSelected(MaterialDialog dialog, int index, MaterialSimpleListItem item) {
                dialog.dismiss();
                CorePref.i().globalPref().putString(Contract.UID, users.get(index).getId());
                App.i().restartApp();
            }
        });
        int iconRefs = R.drawable.ic_rename;
        for (User user : users) {
            switch (user.getSource()) {
                case Contract.PROVIDER_FEEDLY:
                    iconRefs = R.drawable.logo_feedly;
                    break;
                case Contract.PROVIDER_INOREADER:
                    iconRefs = R.drawable.logo_inoreader;
                    break;
                case Contract.PROVIDER_TINYRSS:
                    iconRefs = R.drawable.logo_tinytinyrss;
                    break;
                case Contract.PROVIDER_FEVER_TINYRSS:
                case Contract.PROVIDER_FEVER:
                    iconRefs = R.drawable.logo_fever;
                    break;
            }
            adapter.add(new MaterialSimpleListItem.Builder(this)
                    .content( user.getUserName())
                    .icon(iconRefs)
                    .backgroundColor(Color.TRANSPARENT)
                    .build());
        }
        new MaterialDialog.Builder(this)
                .title(R.string.switch_account)
                .adapter(adapter, new LinearLayoutManager(this))
                .show();
    }

    public void oauthInoReader(View view) {
        new MaterialDialog.Builder(this)
                .title(R.string.inoreader_site_url)
                .content(R.string.need_to_access_inoreader_url_normally_hint)
                .inputType(InputType.TYPE_CLASS_TEXT)
                .inputRange(1, 48)
                .input(null, InoReaderApi.OFFICIAL_BASE_URL, new MaterialDialog.InputCallback() {
                    @Override
                    public void onInput(@NotNull MaterialDialog dialog, CharSequence input) {
                        try {
                            URL url = new URL(input.toString());
                            String inoReaderUrl = input.toString();
                            XLog.e("输入的url" + inoReaderUrl);
                            if(StringUtils.isEmpty(inoReaderUrl)){
                                ToastUtils.show(R.string.invalid_url_hint);
                                return;
                            }
                            Intent intent = new Intent(ProviderActivity.this, WebActivity.class);
                            intent.setData(Uri.parse(InoReaderApi.getOAuthUrl(inoReaderUrl)));
                            InoReaderApi.tempBaseUrl = inoReaderUrl;
                            startActivity(intent);
                            overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
                        } catch (MalformedURLException e) {
                            ToastUtils.show(R.string.invalid_url_hint);
                            e.printStackTrace();
                        }
                    }
                })
                .positiveText(R.string.confirm)
                .show();
    }
    public void oauthFeedly(View view) {
        Intent intent = new Intent(this, WebActivity.class);
        intent.setData(Uri.parse(new FeedlyApi().getOAuthUrl()));
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public void loginInoReader(View view){
        // Intent intent = new Intent(this, LoginInoReaderActivity.class);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setAction(Contract.PROVIDER_INOREADER);
        startActivityForResult(intent, App.ActivityResult_LoginPageToProvider);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    public void loginTinyRSS(View view) {
        // Intent intent = new Intent(this, LoginTinyRSSActivity.class);
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setAction(Contract.PROVIDER_TINYRSS);
        startActivityForResult(intent, App.ActivityResult_LoginPageToProvider);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

    public void loginFever(View view) {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setAction(Contract.PROVIDER_FEVER);
        startActivityForResult(intent, App.ActivityResult_LoginPageToProvider);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    // TODO: 2019/2/16  跳转到自建RSS页（导入OPML，热门RSS）
    // public void selectLocalRSS(View view) {
    // }


    private void getAccessToken(final String code, OAuthApi api) {
        final LoadingPopupView dialog = new XPopup.Builder(this)
                .dismissOnTouchOutside(false)
                .asLoading(getString(R.string.authing));
        dialog.show();

        api.getAccessToken(code, new CallbackX<Token,String>() {
            @Override
            public void onSuccess(Token token) {
                XLog.i("token：" + token);
                dialog.setTitle(getString(R.string.fetch_user_info));
                api.setAuthorization(token.getAuth());
                api.fetchUserInfo(new CallbackX<User,String>() {
                    @Override
                    public void onSuccess(User user) {
                        XLog.i("用户：" + user + token.getAuth());
                        user.setToken(token);
                        if(api instanceof InoReaderApi){
                            user.setHost( ((InoReaderApi)api).getTempBaseUrl() );
                        }

                        CorePref.i().globalPref().putString(Contract.UID, user.getId());
                        App.i().setApi(api);
                        CoreDB.i().userDao().insert(user);
                        dialog.dismiss();
                        XLog.e(token);
                        App.i().restartApp();
                        LiveEventBus.get(SyncWorker.SYNC_TASK_STATUS).post(true);
                    }

                    @Override
                    public void onFailure(String error) {
                        ToastUtils.show(getString(R.string.login_failure_please_try_again) + error);
                        dialog.dismiss();
                    }
                });
            }

            @Override
            public void onFailure(String error) {
            }
        });
    }

    @Override
    protected void onNewIntent(Intent paramIntent) {
        super.onNewIntent(paramIntent);
        String url = paramIntent.getDataString();
        XLog.i("获取到数据：" + url + " , " + paramIntent );
        if (TextUtils.isEmpty(url)) {
            return;
        }
        Uri uri = Uri.parse(url);
        String schema = uri.getScheme();
        String host = uri.getHost();
        String code = uri.getQueryParameter("code");

        XLog.i("获取：" + schema + " , " + host + " , " + code);
        if (StringUtils.isEmpty(schema) || TextUtils.isEmpty(host) || TextUtils.isEmpty(code)) {
            ToastUtils.show(getString(R.string.auth_failure_please_try_again));
            return;
        }

        if (FeedlyApi.REDIRECT_URI.contains(host)) {
            getAccessToken(code, new FeedlyApi());
        } else if (Contract.SCHEMA_LOREAD.contains(schema)) {
            getAccessToken(code, new InoReaderApi(InoReaderApi.tempBaseUrl));
        }
    }


    public void clickAuthor(View view){
        if(!BuildConfig.DEBUG){
           return;
        }
        Intent intent = new Intent(this, LabActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);

    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == App.ActivityResult_LoginPageToProvider) {
            App.i().restartApp();
        }
    }

    public Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        return mColorfulBuilder;
    }
}
