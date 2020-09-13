package me.wizos.loread.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.impl.LoadingPopupView;
import com.socks.library.KLog;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.activity.login.LoginInoReaderActivity;
import me.wizos.loread.activity.login.LoginTinyRSSActivity;
import me.wizos.loread.bean.Token;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.User;
import me.wizos.loread.network.api.FeedlyApi;
import me.wizos.loread.network.api.InoReaderApi;
import me.wizos.loread.network.api.OAuthApi;
import me.wizos.loread.network.callback.CallbackX;
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
    }

    public void loginInoReader(View view){
        Intent intent = new Intent(this, LoginInoReaderActivity.class);
        startActivityForResult(intent, App.ActivityResult_LoginPageToProvider);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    public void oauthInoReader(View view) {
        Intent intent = new Intent(this, WebActivity.class);
        intent.setData(Uri.parse(new InoReaderApi().getOAuthUrl()));
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    public void oauthFeedly(View view) {
        Intent intent = new Intent(this, WebActivity.class);
        intent.setData(Uri.parse(new FeedlyApi().getOAuthUrl()));
        startActivity(intent);
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }
    public void loginTinyRSS(View view) {
        Intent intent = new Intent(this, LoginTinyRSSActivity.class);
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
                KLog.e("授权为：" + token);
                dialog.setTitle(getString(R.string.fetch_user_info));
                api.setAuthorization(token.getAuth());
                api.fetchUserInfo(new CallbackX<User,String>() {
                    @Override
                    public void onSuccess(User user) {
                        KLog.e("用户资料：" + user + token.getAuth());
                        user.setToken(token);

                        App.i().getKeyValue().putString(Contract.UID, user.getId());
                        App.i().setApi(api);
                        CoreDB.i().userDao().insert(user);
                        dialog.dismiss();
                        KLog.e(token);
                        App.i().restartApp();
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
        KLog.e("获取到数据：" + url);
        if (TextUtils.isEmpty(url)) {
            ToastUtils.show(getString(R.string.auth_failure_please_try_again));
            return;
        }
        Uri uri = Uri.parse(url);
        String host = uri.getHost().toLowerCase();
        String code = uri.getQueryParameter("code");
        if (TextUtils.isEmpty(host)) {
            ToastUtils.show(getString(R.string.auth_failure_please_try_again));
            return;
        }

        if (TextUtils.isEmpty(code)) {
            ToastUtils.show("无法获取code");
            return;
        }

        if (host.contains("feedlyauth")) {
            getAccessToken(code, new FeedlyApi());
        } else if (host.contains(Contract.PROVIDER_INOREADER.toLowerCase())) {
            getAccessToken(code, new InoReaderApi());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        KLog.e("---------" + resultCode + requestCode);
        if (resultCode == App.ActivityResult_LoginPageToProvider) {
            App.i().getUser();
            App.i().restartApp();
        }
    }

    public Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        return mColorfulBuilder;
    }
}
