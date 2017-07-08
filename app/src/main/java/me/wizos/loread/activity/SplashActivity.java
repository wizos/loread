package me.wizos.loread.activity;

import android.os.Bundle;

import me.wizos.loread.R;
import me.wizos.loread.data.WithSet;
import me.wizos.loread.net.API;
import me.wizos.loread.view.colorful.Colorful;

public class SplashActivity extends BaseActivity {
    protected static final String TAG = "SplashActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        API.INOREADER_ATUH = WithSet.i().getAuth();
        if(API.INOREADER_ATUH.equals("")){
            goTo(LoginActivity.TAG);
        }else {
            goTo(MainActivity.TAG);
        }
        notifyDataChanged();
    }


    @Override
    protected void notifyDataChanged(){
        this.finish();
    }


    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        return mColorfulBuilder;
    }
}
