package me.wizos.loread.activity;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import me.wizos.loread.dao.WithSet;
import me.wizos.loread.net.API;
import me.wizos.loread.R;

public class SplashActivity extends BaseActivity {

    protected Context context;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
//        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);
        API.INOREADER_ATUH = WithSet.getInstance().getAuth();
        if(API.INOREADER_ATUH.equals("")){
            goTo(LoginActivity.TAG);
        }else {
            goTo(MainActivity.TAG);
        }
        notifyDataChanged();
    }
    protected static final String TAG = "SplashActivity";

    @Override
    public String getTAG(){
        return TAG;
    }

    @Override
    protected void notifyDataChanged(){
        this.finish();
    }
    @Override
    protected Context getActivity(){
        return SplashActivity.this;
    }
    @Override
    public void onClick(View v) {
    }
}
