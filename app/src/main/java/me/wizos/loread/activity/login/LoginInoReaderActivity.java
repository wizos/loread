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

import me.wizos.loread.R;
import me.wizos.loread.activity.BaseActivity;
import me.wizos.loread.activity.ProviderActivity;
import me.wizos.loread.activity.viewmodel.InoReaderUserViewModel;
import me.wizos.loread.network.api.InoReaderApi;
import me.wizos.loread.view.colorful.Colorful;

public class LoginInoReaderActivity extends BaseActivity {
    private InoReaderUserViewModel loginViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_inoreader);
        Toolbar mToolbar = findViewById(R.id.inoreader_toolbar);
        setSupportActionBar(mToolbar);
        // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        getSupportActionBar().setHomeButtonEnabled(true);
        // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        loginViewModel = new ViewModelProvider(this).get(InoReaderUserViewModel.class);

        final EditText hostEditText = findViewById(R.id.inoreader_host_edittext);
        final EditText usernameEditText = findViewById(R.id.inoreader_username_edittext);
        final EditText passwordEditText = findViewById(R.id.inoreader_password_edittext);
        final Button loginButton = findViewById(R.id.inoreader_login_button);
        final ProgressBar loadingProgressBar = findViewById(R.id.inoreader_loading);


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
                        hostEditText.getText().toString(),
                        usernameEditText.getText().toString(),
                        passwordEditText.getText().toString()
                );
            }
        };
        hostEditText.addTextChangedListener(afterTextChangedListener);
        usernameEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        /**
         * 需要注意的是 setOnEditorActionListener这个方法，并不是在我们点击EditText的时候触发，
         * 也不是在我们对EditText进行编辑时触发，而是在我们编辑完之后点击软键盘上的各种键才会触发。
         * 因为通过布局文件中的imeOptions可以控制软件盘右下角的按钮显示为不同按钮。所以和EditorInfo搭配起来可以实现各种软键盘的功能。
         */
        passwordEditText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    loadingProgressBar.setVisibility(View.VISIBLE);
                    loginViewModel.login(
                            hostEditText.getText().toString(),
                            usernameEditText.getText().toString(),
                            passwordEditText.getText().toString()
                    );
                }
                return false;
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                loginButton.setEnabled(false);
                loginViewModel.login(
                        hostEditText.getText().toString(),
                        usernameEditText.getText().toString(),
                        passwordEditText.getText().toString()
                );
                InoReaderApi.tempBaseUrl = hostEditText.getText().toString();
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
                    hostEditText.setError(getString(loginFormState.getHostHint()));
                }
                if (loginFormState.getUsernameHint() != null) {
                    usernameEditText.setError(getString(loginFormState.getUsernameHint()));
                }
                if (loginFormState.getPasswordHint() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordHint()));
                }
            }
        });

        loginViewModel.getLoginResultLiveData().observe(this, new Observer<LoginResult>() {
            @Override
            public void onChanged(@Nullable LoginResult loginResult) {
                if (loginResult == null) {
                    return;
                }
                loginButton.setEnabled(true);
                loadingProgressBar.setVisibility(View.GONE);
                String tips = getString(R.string.welcome);
                if (loginResult.isSuccess()) {
                    ToastUtils.show(tips);
                    setResult(ProviderActivity.LOGIN_CODE);
                    finish();
                    overridePendingTransition(R.anim.fade_in, R.anim.out_from_bottom);
                } else {
                    ToastUtils.show(loginResult.getData());
                }
            }
        });
    }
    public Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        return mColorfulBuilder;
    }
}
