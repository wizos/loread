package me.wizos.loread.activity.viewmodel;

import android.app.Application;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.jetbrains.annotations.NotNull;

import me.wizos.loread.R;
import me.wizos.loread.utils.UriUtils;

public class ProxyViewModel extends AndroidViewModel {
    public ProxyViewModel(@NonNull Application application) {
        super(application);
    }

    protected MutableLiveData<FormState> formStateLD = new MutableLiveData<>();

    public LiveData<FormState> getFormStateLiveData() {
        return formStateLD;
    }

    public void formChanged(String server,String port) {
        FormState formState = new FormState();

        if(!UriUtils.isWebUrl(server)){
            formState.serverHint = R.string.invalid_host_hint;
        }else {
            if(TextUtils.isEmpty(port)){
                formState.portHint = R.string.invalid_port_hint;
            }else {
                int integer = Integer.parseInt(port);
                if(integer < 1 || integer > 65535){
                    formState.portHint = R.string.invalid_port_hint;
                }else {
                    formState.isDataValid = true;
                }
            }
        }

        formStateLD.postValue(formState);
    }


    public static class FormState {
        @Nullable
        public Integer serverHint;
        @Nullable
        public Integer portHint;
        // @Nullable
        // public Integer usernameHint;
        // @Nullable
        // public Integer passwordHint;
        public boolean isDataValid = false;

        @NotNull
        @Override
        public String toString() {
            return "FormState{" +
                    "serverHint=" + serverHint +
                    ", portHint=" + portHint +
                    // ", usernameHint=" + usernameHint +
                    // ", passwordHint=" + passwordHint +
                    ", isDataValid=" + isDataValid +
                    '}';
        }
    }
}
