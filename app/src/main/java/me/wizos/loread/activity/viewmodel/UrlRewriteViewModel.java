package me.wizos.loread.activity.viewmodel;

import android.app.Application;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.elvishew.xlog.XLog;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import javax.script.Bindings;
import javax.script.ScriptException;
import javax.script.SimpleBindings;

import me.wizos.loread.R;
import me.wizos.loread.config.url_rewrite.UrlRewriteConfig;
import me.wizos.loread.log.Console;
import me.wizos.loread.utils.HttpCall;
import me.wizos.loread.utils.ScriptUtils;
import me.wizos.loread.utils.UriUtils;

public class UrlRewriteViewModel extends AndroidViewModel {
    public UrlRewriteViewModel(@NonNull Application application) {
        super(application);
    }

    protected MutableLiveData<FormState> formStateLD = new MutableLiveData<>();
    protected MutableLiveData<TestResult> testResultLD = new MutableLiveData<>();
    protected MutableLiveData<List<String>> hostsLD = new MutableLiveData<>();

    public LiveData<FormState> getFormStateLiveData() {
        return formStateLD;
    }
    public LiveData<TestResult> getTestResultLiveData() {
        return testResultLD;
    }
    public LiveData<List<String>> getHostsLiveData() {
        return hostsLD;
    }
    public void urlChanged(boolean isReplaceHost, String testUrl) {
        if (UriUtils.isHttpOrHttpsUrl(testUrl)) {
            if(isReplaceHost){
                ArrayList<String> hosts = new ArrayList<>();
                hosts.add(Uri.parse(testUrl).getHost());
                hostsLD.setValue(hosts);
            }else {
                hostsLD.setValue(UrlRewriteConfig.getReduceSlice(testUrl));
            }
        }
    }

    public void testFormChanged(boolean isReplaceHost, String testUrl, String value) {
        FormState formState = new FormState();

        if (!UriUtils.isHttpOrHttpsUrl(testUrl)) {
            formState.testUrlHint = R.string.invalid_url_hint;
        } else {
            if (isReplaceHost) {
                if(!UriUtils.isWebUrl(value)){
                    formState.valueHint = R.string.invalid_host_hint;
                }else {
                    formState.isDataValid = true;
                }
            } else {
                if(!isValidValue(value)){
                    formState.valueHint = R.string.not_allowed_to_be_empty;
                }else {
                    formState.isDataValid = true;
                }
            }
        }
        formStateLD.postValue(formState);
    }


    public void test(boolean isReplaceHost, String oldUrl, String host, String value) {
        TestResult testResult = new TestResult();
        testResult.host = host;
        testResult.isReplaceHost = isReplaceHost;
        testResult.value = value;
        if(isReplaceHost){
            String newUrl = oldUrl.replaceFirst(host, value);
            testResult.result = 1;
            testResult.msg = getApplication().getString(R.string.any_url_with_host_x_the_host_will_be_rewritten_to_y, host,value,oldUrl,newUrl);
            testResultLD.postValue(testResult);
        }else {
            AsyncTask.SERIAL_EXECUTOR.execute(new Runnable() {
                @Override
                public void run() {
                    Console console = new Console().setListener(new Console.Listener() {
                        @Override
                        public void onLog(Object object) {
                            testResult.result = 0;
                            testResult.msg = (String) object;
                            testResultLD.postValue(testResult);
                        }
                    });
                    Bindings bindings = new SimpleBindings();
                    bindings.put("url", oldUrl);
                    bindings.put("call", HttpCall.i());
                    bindings.put("console", console);
                    try {
                        ScriptUtils.i().exe(value, bindings);
                        String newUrl = (String) bindings.get("url");
                        testResult.result = 1;
                        testResult.msg = getApplication().getString(R.string.any_url_with_host_x_the_url_will_be_rewritten_to_y, host, oldUrl, newUrl);
                        XLog.d("重定向JS：" + newUrl);
                    } catch (ScriptException e) {
                        testResult.result = 2;
                        testResult.msg = getApplication().getString(R.string.script_execution, e.getMessage());
                        XLog.e("脚本执行错误" + e.getMessage() + "," +e.getFileName()  + ","+ e.getColumnNumber()  + "," + e.getLineNumber() );
                        e.printStackTrace();
                    } catch (Exception e){
                        testResult.result = 2;
                        testResult.msg = getApplication().getString(R.string.script_execution, e.getMessage());
                        XLog.e("脚本执行错误2：" + e.getMessage());
                        e.printStackTrace();
                    }
                    testResultLD.postValue(testResult);
                }
            });
        }
    }

    private boolean isValidValue(String value) {
        return !TextUtils.isEmpty(value);
    }

    public void addRule(boolean isReplaceHost, String host, String value) {

    }

    public static class FormState {
        @Nullable
        public Integer testUrlHint;
        @Nullable
        public List<String> keyHint;
        @Nullable
        public Integer valueHint;
        public boolean isDataValid = false;

        @NotNull
        @Override
        public String toString() {
            return "FormState{" +
                    "testUrlHint=" + testUrlHint +
                    ", keyHint=" + keyHint +
                    ", valueHint=" + valueHint +
                    ", isDataValid=" + isDataValid +
                    '}';
        }
    }

    public static class TestResult {
        public int result = 0; // 0为处理中，1为成功，2为失败
        @Nullable
        public String msg;
        public String host;
        public String value;
        public boolean isReplaceHost = true;

        @NotNull
        @Override
        public String toString() {
            return "TestResult{" +
                    "result=" + result +
                    ", msg='" + msg + '\'' +
                    ", host='" + host + '\'' +
                    ", value='" + value + '\'' +
                    ", isReplaceHost=" + isReplaceHost +
                    '}';
        }
    }


}
