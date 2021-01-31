package me.wizos.loread.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hjq.toast.ToastUtils;
import com.reginald.editspinner.EditSpinner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import me.wizos.loread.R;
import me.wizos.loread.activity.viewmodel.UrlRewriteViewModel;
import me.wizos.loread.config.url_rewrite.UrlRewriteConfig;
import me.wizos.loread.utils.StringUtils;
import me.wizos.loread.view.colorful.Colorful;

/**
 * 内置的 webView 页面，用来相应 a，iframe 的跳转内容
 * @author Wizos
 */
public class UrlRewriteActivity extends BaseActivity {
    private UrlRewriteViewModel urlRewriteViewModel;
    EditSpinner rewriteMethodEditSpinner;
    EditText testUrlEditText;
    EditSpinner keyEditSpinner;
    EditText valueEditText;
    Button testRuleButton;
    Button saveRuleButton;
    TextView consoleTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_url_rewrite);
        setSupportActionBar(findViewById(R.id.url_rewrite_toolbar));
        // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        getSupportActionBar().setHomeButtonEnabled(true);
        // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);

        if (savedInstanceState != null) {
            onRecoveryInstanceState(savedInstanceState);
        }

        rewriteMethodEditSpinner = findViewById(R.id.rewrite_method_editspinner);
        rewriteMethodEditSpinner.setEditable(false);

        testUrlEditText = findViewById(R.id.url_for_testing_edittext);

        keyEditSpinner = findViewById(R.id.url_rewrite_key_editspinner);
        keyEditSpinner.setEditable(false);

        valueEditText = findViewById(R.id.url_rewrite_value_edittext);

        testRuleButton = findViewById(R.id.test_url_rewrite_rule_button);
        saveRuleButton = findViewById(R.id.save_url_rewrite_rule_button);

        consoleTextView = findViewById(R.id.console);

        final String[] methodArray = this.getResources().getStringArray(R.array.url_rewrite_method_title);
        List<String> items = new ArrayList<>(Arrays.asList(methodArray));
        ListAdapter adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, items);
        rewriteMethodEditSpinner.setAdapter(adapter);
        rewriteMethodEditSpinner.selectItem(0);
        rewriteMethodEditSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                valueEditText.setText("");
            }
        });


        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                urlRewriteViewModel.testFormChanged(
                        rewriteMethodEditSpinner.getSelected() == 0,
                        testUrlEditText.getText().toString(),
                        valueEditText.getText().toString()
                );
                saveRuleButton.setVisibility(View.GONE);
            }
        };
        rewriteMethodEditSpinner.addTextChangedListener(afterTextChangedListener);
        testUrlEditText.addTextChangedListener(afterTextChangedListener);
        testUrlEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                urlRewriteViewModel.urlChanged(rewriteMethodEditSpinner.getSelected() == 0,testUrlEditText.getText().toString());
            }
        });
        keyEditSpinner.addTextChangedListener(afterTextChangedListener);
        valueEditText.addTextChangedListener(afterTextChangedListener);

        testRuleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rewriteMethodEditSpinner.setEnabled(false);
                rewriteMethodEditSpinner.setClickable(false);
                testUrlEditText.setEnabled(false);
                keyEditSpinner.setEnabled(false);
                keyEditSpinner.setClickable(false);
                valueEditText.setEnabled(false);
                testRuleButton.setEnabled(false);
                saveRuleButton.setVisibility(View.GONE);
                consoleTextView.setText("");
                urlRewriteViewModel.test(
                        rewriteMethodEditSpinner.getSelected() == 0,
                        testUrlEditText.getText().toString(),
                        keyEditSpinner.getText().toString(),
                        valueEditText.getText().toString()
                );
            }
        });
        saveRuleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(testRuleResult != null){
                    String oldValue = UrlRewriteConfig.i().getValue(testRuleResult.host);
                    if(StringUtils.isEmpty(oldValue)){
                        if(UrlRewriteConfig.i().addRule(testRuleResult.isReplaceHost, testRuleResult.host, testRuleResult.value)){
                            ToastUtils.show(R.string.success);
                        }else {
                            ToastUtils.show(R.string.failure);
                        }
                    }else {
                        new MaterialDialog.Builder(UrlRewriteActivity.this)
                                .title(getString(R.string.rule_already_exists))
                                .positiveText(R.string.override)
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        if(UrlRewriteConfig.i().addRule(testRuleResult.isReplaceHost, testRuleResult.host, testRuleResult.value)){
                                            ToastUtils.show(R.string.success);
                                        }else {
                                            ToastUtils.show(R.string.failure);
                                        }
                                        dialog.dismiss();
                                    }
                                })
                                .negativeText(R.string.cancel)
                                .show();
                    }
                }
            }
        });

        urlRewriteViewModel = new ViewModelProvider(this).get(UrlRewriteViewModel.class);
        urlRewriteViewModel.getHostsLiveData().observe(this, new Observer<List<String>>() {
            @Override
            public void onChanged(List<String> strings) {
                ListAdapter adapter = new ArrayAdapter<String>(UrlRewriteActivity.this, android.R.layout.simple_spinner_dropdown_item, strings);
                keyEditSpinner.setAdapter(adapter);
                keyEditSpinner.selectItem(0);
            }
        });
        urlRewriteViewModel.getFormStateLiveData().observe(this, new Observer<UrlRewriteViewModel.FormState>() {
            @Override
            public void onChanged(@Nullable UrlRewriteViewModel.FormState formState) {
                if (formState == null) {
                    return;
                }
                testRuleButton.setEnabled(formState.isDataValid);
                if(formState.isDataValid){
                    testRuleButton.setBackgroundColor(getResources().getColor(R.color.bluePrimary));
                }else {
                    if (formState.testUrlHint != null) {
                        testUrlEditText.setError(getString(formState.testUrlHint));
                    }
                    if (formState.valueHint != null) {
                        valueEditText.setError(getString(formState.valueHint));
                    }
                    testRuleButton.setBackgroundColor(getResources().getColor(R.color.main_day_info));
                }

            }
        });
        urlRewriteViewModel.getTestResultLiveData().observe(this, new Observer<UrlRewriteViewModel.TestResult>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onChanged(UrlRewriteViewModel.TestResult testResult) {
                if (testResult.result != 0) {
                    rewriteMethodEditSpinner.setEnabled(true);
                    rewriteMethodEditSpinner.setClickable(true);
                    testUrlEditText.setEnabled(true);
                    keyEditSpinner.setEnabled(true);
                    keyEditSpinner.setClickable(true);
                    valueEditText.setEnabled(true);
                    testRuleButton.setEnabled(true);
                    if(testResult.result == 1){
                        saveRuleButton.setVisibility(View.VISIBLE);
                        testRuleResult = testResult;
                    }else {
                        saveRuleButton.setVisibility(View.GONE);
                        testRuleResult = null;
                    }
                }else {
                    saveRuleButton.setVisibility(View.GONE);
                }
                if(StringUtils.isEmpty(consoleTextView.getText())){
                    consoleTextView.setText(testResult.msg);
                }else {
                    consoleTextView.setText(consoleTextView.getText() + "\n" + testResult.msg);
                }
            }
        });
    }

    UrlRewriteViewModel.TestResult testRuleResult;

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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void onRecoveryInstanceState(@NonNull Bundle outState) {
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_url_rewrite, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            //监听左上角的返回箭头
            case android.R.id.home:
                exit();
                break;
            case R.id.rewrite_menu_example_1:
                rewriteMethodEditSpinner.selectItem(0);
                // isReplaceHost = true;
                testUrlEditText.setText("https://www.baidu.com");
                valueEditText.setText("google.com");
                break;
            case R.id.rewrite_menu_example_2:
                rewriteMethodEditSpinner.selectItem(1);
                // isReplaceHost = false;
                testUrlEditText.setText("https://pic1.zhimg.com/3198e34d61b236a1f824440794a64ca2_xl.jpg");
                valueEditText.setText("var m = null; if( (m = url.match(/^(https?:\\/\\/.+\\.zhimg\\.com\\/)(?:\\d+\\/)?([\\w\\-]+_)(\\w+)(\\.(jpg|jpeg|gif|png|bmp|webp))(?:\\?.+)?$/i)) ){if(m[3]!='r') {var newUrl = url.replace(/^.*?$/i, m[1] + m[2] + 'r' + m[4]); if(call.valid(newUrl)){ url=newUrl;}}}");
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
                .backgroundColor(R.id.url_rewrite_root, R.attr.root_view_bg)
                .backgroundColor(R.id.url_rewrite_toolbar, R.attr.topbar_bg);
        return mColorfulBuilder;
    }
}
