package me.wizos.loread.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.elvishew.xlog.XLog;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hjq.toast.ToastUtils;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.kyleduo.switchbutton.SwitchButton;
import com.umeng.analytics.MobclickAgent;

import org.jetbrains.annotations.NotNull;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.activity.viewmodel.ProxyViewModel;
import me.wizos.loread.db.CorePref;
import me.wizos.loread.network.HttpClientManager;
import me.wizos.loread.network.proxy.ProxyNodeSocks5;
import me.wizos.loread.network.proxy.ProxyWebkit;
import me.wizos.loread.view.colorful.Colorful;

/**
 * 内置的 webView 页面，用来相应 a，iframe 的跳转内容
 * @author Wizos
 */
public class ProxyActivity extends BaseActivity {
    private ProxyViewModel proxyViewModel;
    SwitchButton enableButton;
    EditText serverEditText;
    EditText portEditText;
    EditText usernameEditText;
    EditText passwordEditText;
    Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_proxy);
        setSupportActionBar(findViewById(R.id.proxy_toolbar));
        // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        getSupportActionBar().setHomeButtonEnabled(true);
        // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        super.onCreate(savedInstanceState);// 由于使用了自动换主题，所以要放在这里

        if (savedInstanceState != null) {
            onRecoveryInstanceState(savedInstanceState);
        }

        enableButton = findViewById(R.id.proxy_enable_sb);
        serverEditText = findViewById(R.id.proxy_server_edittext);
        portEditText = findViewById(R.id.proxy_port_edittext);
        usernameEditText = findViewById(R.id.proxy_username_edittext);
        passwordEditText = findViewById(R.id.proxy_password_edittext);
        saveButton = findViewById(R.id.proxy_save_button);
        proxyViewModel = new ViewModelProvider(this).get(ProxyViewModel.class);

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                proxyViewModel.formChanged(
                        serverEditText.getText() != null ? serverEditText.getText().toString() : null,
                        portEditText.getText() != null ? portEditText.getText().toString() : null
                );
            }
        };
        serverEditText.addTextChangedListener(afterTextChangedListener);
        portEditText.addTextChangedListener(afterTextChangedListener);
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);


        enableButton.setChecked(CorePref.i().globalPref().getBoolean(Contract.ENABLE_PROXY, false));
        enableButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                MobclickAgent.onEvent(ProxyActivity.this, "enable_proxy", String.valueOf(isChecked));
                if(isChecked){
                    String json = CorePref.i().globalPref().getString(Contract.SOCKS5_PROXY,"");
                    if(!TextUtils.isEmpty(json)){
                        App.i().proxyNodeSocks5 = new Gson().fromJson(json, ProxyNodeSocks5.class);
                        CorePref.i().globalPref().putBoolean(Contract.ENABLE_PROXY,true);
                        LiveEventBus.get(Contract.ENABLE_PROXY, Boolean.class).post(true);
                        boolean success = ProxyWebkit.setSocksProxy(getApplicationContext(),  App.i().proxyNodeSocks5.getServer(), App.i().proxyNodeSocks5.getPort());;
                        XLog.d("设置全局代理是否成功：" + success);
                    }else {
                        buttonView.setChecked(false);
                        App.i().proxyNodeSocks5 = null;
                        ToastUtils.show(R.string.plz_set_and_save_proxy_first);
                        CorePref.i().globalPref().putBoolean(Contract.ENABLE_PROXY,false);
                        LiveEventBus.get(Contract.ENABLE_PROXY, Boolean.class).post(false);
                        ProxyWebkit.resetSocksProxy(getApplicationContext());
                        XLog.i("检查网络代理：" + " false");
                    }
                }else {
                    App.i().proxyNodeSocks5 = null;
                    CorePref.i().globalPref().putBoolean(Contract.ENABLE_PROXY,false);
                    LiveEventBus.get(Contract.ENABLE_PROXY, Boolean.class).post(false);
                    ProxyWebkit.resetSocksProxy(getApplicationContext());
                    XLog.i("检查网络代理：" + " true");
                }
                App.i().resetApi();
                HttpClientManager.i().init();
            }
        });

        String json = CorePref.i().globalPref().getString(Contract.SOCKS5_PROXY,"");
        if(!TextUtils.isEmpty(json)){
            ProxyNodeSocks5 proxyNode = new Gson().fromJson(json, ProxyNodeSocks5.class);
            serverEditText.setText(proxyNode.getServer());
            portEditText.setText(String.valueOf(proxyNode.getPort()));
            usernameEditText.setText(proxyNode.getUsername());
            passwordEditText.setText(proxyNode.getPassword());
        }

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ProxyNodeSocks5 proxyNodeSocks5 = new ProxyNodeSocks5(
                        serverEditText.getText() != null ? serverEditText.getText().toString() : null,
                        Integer.parseInt(portEditText.getText() != null ? portEditText.getText().toString() : "0"),
                        usernameEditText.getText() != null ? usernameEditText.getText().toString() : null,
                        passwordEditText.getText() != null ? passwordEditText.getText().toString() : null);
                String json = new GsonBuilder().disableHtmlEscaping().create().toJson(proxyNodeSocks5);
                CorePref.i().globalPref().putString(Contract.SOCKS5_PROXY, json);
                ToastUtils.show(R.string.saved);
            }
        });

        proxyViewModel.getFormStateLiveData().observe(this, new Observer<ProxyViewModel.FormState>() {
            @Override
            public void onChanged(@Nullable ProxyViewModel.FormState formState) {
                if (formState == null) {
                    return;
                }
                saveButton.setEnabled(formState.isDataValid);
                if(formState.isDataValid){
                    saveButton.setBackgroundColor(getResources().getColor(R.color.bluePrimary));
                }else {
                    saveButton.setBackgroundColor(getResources().getColor(R.color.main_day_info));
                    if (formState.serverHint != null) {
                        serverEditText.setError(getString(formState.serverHint));
                    }else if (formState.portHint != null) {
                        portEditText.setError(getString(formState.portHint));
                    }
                }
            }
        });
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //监听左上角的返回箭头
            case android.R.id.home:
                exit();
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
                .backgroundColor(R.id.proxy_root, R.attr.root_view_bg)
                .backgroundColor(R.id.proxy_toolbar, R.attr.topbar_bg)
                .textColor(R.id.proxy_server_edittext,R.attr.list_item_title_color)
                .textColor(R.id.proxy_port_edittext,R.attr.list_item_title_color)
                .textColor(R.id.proxy_username_edittext,R.attr.list_item_title_color)
                .textColor(R.id.proxy_password_edittext,R.attr.list_item_title_color)
                .textColor(R.id.proxy_enable_title, R.attr.list_item_title_color);
        return mColorfulBuilder;
    }
}
