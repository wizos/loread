package me.wizos.loread.activity.login;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.hjq.toast.ToastUtils;
import com.umeng.analytics.MobclickAgent;

import me.wizos.loread.App;
import me.wizos.loread.Contract;
import me.wizos.loread.R;
import me.wizos.loread.activity.BaseActivity;
import me.wizos.loread.activity.ProviderActivity;
import me.wizos.loread.activity.viewmodel.FeverUserViewModel;
import me.wizos.loread.activity.viewmodel.LoginViewModel;
import me.wizos.loread.activity.viewmodel.TinyRSSUserViewModel;
import me.wizos.loread.view.colorful.Colorful;

public class LoginActivity extends BaseActivity {
    private LoginViewModel loginViewModel;
    private String provider;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle bundle;
        if (savedInstanceState != null) {
            bundle = savedInstanceState;
        } else {
            bundle = getIntent().getExtras();
        }
        setContentView(R.layout.activity_login);
        Toolbar mToolbar = findViewById(R.id.login_toolbar);
        setSupportActionBar(mToolbar);
        // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        getSupportActionBar().setHomeButtonEnabled(true);
        // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);


        final EditText baseUrlEditText = findViewById(R.id.login_baseurl_edittext);
        final EditText accountEditText = findViewById(R.id.login_account_edittext);
        final EditText passwordEditText = findViewById(R.id.login_password_edittext);
        final Button loginButton = findViewById(R.id.login_login_button);
        final ProgressBar loadingProgressBar = findViewById(R.id.login_loading);

        // final LinearLayout linearLayout = findViewById(R.id.login_use_tiny_fever_layout);
        // final SwitchButton switchButton = findViewById(R.id.login_use_tiny_fever_sb);
        // switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
        //     @Override
        //     public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        //     }
        // });

        if(bundle == null){
            finish();
            return;
        }
        provider = bundle.getString(Contract.PROVIDER);
        if(Contract.PROVIDER_TINYRSS.equals(provider)){
            loginViewModel = new ViewModelProvider(this).get(TinyRSSUserViewModel.class);
            TextView noteView = findViewById(R.id.login_suggestion);
            noteView.setVisibility(View.VISIBLE);
        }else if(Contract.PROVIDER_FEVER.equals(provider)){
            loginViewModel = new ViewModelProvider(this).get(FeverUserViewModel.class);
        }else {
            // loginViewModel = new ViewModelProvider(this).get(FeverUserViewModel.class);
            finish();
            return;
        }

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.loginFormChanged(
                        baseUrlEditText.getText().toString(),
                        accountEditText.getText().toString(),
                        passwordEditText.getText().toString()
                );
            }
        };
        baseUrlEditText.addTextChangedListener(afterTextChangedListener);
        accountEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        /*
         * 需要注意的是 setOnEditorActionListener这个方法，并不是在我们点击EditText的时候触发，
         * 也不是在我们对EditText进行编辑时触发，而是在我们编辑完之后点击软键盘上的各种键才会触发。
         * 因为通过布局文件中的imeOptions可以控制软件盘右下角的按钮显示为不同按钮。所以和EditorInfo搭配起来可以实现各种软键盘的功能。
         */
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loginButton.setEnabled(false);
                    baseUrlEditText.setEnabled(false);
                    accountEditText.setEnabled(false);
                    passwordEditText.setEnabled(false);
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    loginViewModel.login(
                            baseUrlEditText.getText().toString(),
                            accountEditText.getText().toString(),
                            passwordEditText.getText().toString()
                    );
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginButton.setEnabled(false);
                baseUrlEditText.setEnabled(false);
                accountEditText.setEnabled(false);
                passwordEditText.setEnabled(false);
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginViewModel.login(
                        baseUrlEditText.getText().toString(),
                        accountEditText.getText().toString(),
                        passwordEditText.getText().toString()
                );
            }
        });

        loginViewModel.getLoginFormLiveData().observe(this, new Observer<LoginFormState>() {
            @Override
            public void onChanged(@Nullable LoginFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());

                if (loginFormState.getHostHint() != null) {
                    baseUrlEditText.setError(getString(loginFormState.getHostHint()));
                }
                if (loginFormState.getUsernameHint() != null) {
                    accountEditText.setError(getString(loginFormState.getUsernameHint()));
                }
                if (loginFormState.getPasswordHint() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordHint()));
                }
            }
        });

        loginViewModel.getLoginResultLiveData().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                loginButton.setEnabled(true);
                baseUrlEditText.setEnabled(true);
                accountEditText.setEnabled(true);
                passwordEditText.setEnabled(true);
                loadingProgressBar.setVisibility(View.GONE);
                if (loginResult != null && loginResult.isSuccess()) {
                    //当用户使用第三方账号（如新浪微博）登录时，可以这样统计：
                    MobclickAgent.onProfileSignIn(App.i().getUser().getSource(), App.i().getUser().getUserId());
                    String tips = getString(R.string.welcome);
                    ToastUtils.show(tips);
                    setResult(ProviderActivity.LOGIN_CODE);
                    finish();
                    overridePendingTransition(R.anim.fade_in, R.anim.out_from_bottom);
                } else {
                    ToastUtils.show(getString(R.string.login_failed_with_reason, loginResult.getData()));
                }
            }
        });
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(Contract.PROVIDER, provider);
        super.onSaveInstanceState(outState);
    }
    public Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        return mColorfulBuilder;
    }
}
