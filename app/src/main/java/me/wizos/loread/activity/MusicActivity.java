package me.wizos.loread.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.BounceInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;

import com.elvishew.xlog.XLog;
import com.freedom.lauzy.playpauseviewlib.PlayPauseView;
import com.hjq.permissions.OnPermissionCallback;
import com.hjq.permissions.Permission;
import com.hjq.permissions.XXPermissions;
import com.hjq.toast.ToastUtils;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.enums.PopupAnimation;
import com.lxj.xpopup.interfaces.OnSelectListener;
import com.noober.background.drawable.DrawableCreator;
import com.socks.library.KLog;
import com.yhao.floatwindow.constant.MoveType;
import com.yhao.floatwindow.constant.Screen;
import com.yhao.floatwindow.view.FloatWindow;

import java.util.List;

import me.wizos.loread.App;
import me.wizos.loread.R;
import me.wizos.loread.db.CoreDB;
import me.wizos.loread.db.User;
import me.wizos.loread.service.MusicService;
import me.wizos.loread.utils.ScreenUtil;
import me.wizos.loread.utils.TimeUtil;
import me.wizos.loread.view.colorful.Colorful;

public class MusicActivity extends BaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);
        initToolbar();

        Intent intent = getIntent();
        String playUrl = intent.getDataString();

        // 补救，获取 playUrl
        if (TextUtils.isEmpty(playUrl)) {
            playUrl = intent.getStringExtra(Intent.EXTRA_TEXT);
        }
        String title = intent.getStringExtra("title");

        KLog.e("获取到链接：" + title + playUrl);

        playConnection = new PlayConnection();
        intent = new Intent(this, MusicService.class);

        if (!TextUtils.isEmpty(playUrl)) {
            intent.setData(Uri.parse(playUrl));
            intent.putExtra("title", title);
            initFloatWindow();
        }
        startService(intent);
        bindService(intent, playConnection, BIND_AUTO_CREATE);
        applyPermissions();
    }


    private void applyPermissions() {
        XXPermissions.with(this)
                //.constantRequest() //可设置被拒绝后继续申请，直到用户授权或者永久拒绝
                .permission(Permission.SYSTEM_ALERT_WINDOW) //支持请求6.0悬浮窗权限8.0请求安装权限
                .request(new OnPermissionCallback() {
                    @Override
                    public void onGranted(List<String> permissions, boolean all) {
                    }

                    @Override
                    public void onDenied(List<String> permissions, boolean never) {
                        for (String id : permissions) {
                            XLog.w("无法获取权限：" + id);
                        }
                        ToastUtils.show(getString(R.string.plz_grant_permission_tips));
                    }
                });
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        maHandler.removeCallbacksAndMessages(null);
        if (playConnection != null) {
            //退出应用后与service解除绑定
            unbindService(playConnection);
        }
    }

    @Override
    protected Colorful.Builder buildColorful(Colorful.Builder mColorfulBuilder) {
        return mColorfulBuilder;
    }

    private boolean isChangeProgress = false;
    protected SeekBar seekBar;
    protected TextView speedView;
    protected TextView currTimeView;
    protected TextView totalTimeView;
    protected PlayPauseView playPauseView;
    protected PlayConnection playConnection;
    protected MusicService.MusicControlBinder musicControl;

    protected static Handler maHandler = new Handler();

    protected Runnable progressTask = new Runnable() {
        @Override
        public void run() {
            int currentPosition = musicControl.getCurrentPosition();
            int duration = musicControl.getDuration();

            if (seekBar != null && !isChangeProgress) {
                seekBar.setProgress(currentPosition);
                seekBar.setSecondaryProgress(musicControl.getBufferedPercent() * duration / 100);
                seekBar.setMax(duration);
            }
            if (currTimeView != null && !isChangeProgress) {
                currTimeView.setText(TimeUtil.getTime(currentPosition));
                totalTimeView.setText(TimeUtil.getTime(duration));
            }
            //KLog.e("进度：" + seekBar + ", " + currTimeView + " , " + duration + " = "  + TimeUtil.getTime(duration));
            maHandler.postDelayed(progressTask, 1000);
        }
    };

    public class PlayConnection implements ServiceConnection {
        //服务启动完成后会进入到这个方法
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //获得service中的MyBinder
            KLog.e("服务连接：onServiceConnected" + musicControl);
            initView(service);
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
        }
    }

    public void initToolbar() {
        Toolbar toolbar = findViewById(R.id.music_toolbar);
        setSupportActionBar(toolbar);
        // 这个小于4.0版本是默认为true，在4.0及其以上是false。该方法的作用：决定左上角的图标是否可以点击(没有向左的小图标)，true 可点
        getSupportActionBar().setHomeButtonEnabled(true);
        // 决定左上角图标的左侧是否有向左的小箭头，true 有小箭头
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setTitle(getString(R.string.music));
        toolbar.setTitle(getString(R.string.music));
        toolbar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MusicActivity.this.finish();
            }
        });
    }

    public void initView(IBinder service) {
        musicControl = (MusicService.MusicControlBinder) service;
        ImageView closeView = findViewById(R.id.music_close);
        TextView titleView = findViewById(R.id.music_title);
        playPauseView = findViewById(R.id.music_play_pause_view);
        currTimeView = findViewById(R.id.currTime);
        totalTimeView = findViewById(R.id.totalTime);
        seekBar = findViewById(R.id.progressBar);
        speedView = findViewById(R.id.music_speed);

        titleView.setText(musicControl.getTitle());

        if (musicControl.isPlaying()) {
            playPauseView.play();
            maHandler.post(progressTask);
        } else {
            playPauseView.pause();
            currTimeView.setText(TimeUtil.getTime(musicControl.getCurrentPosition()));
            totalTimeView.setText(TimeUtil.getTime(musicControl.getDuration()));
            seekBar.setProgress(musicControl.getCurrentPosition());
        }


        musicControl.setPlayStatusListener(new MusicService.PlayStatusListener() {
            @Override
            public void onPlay() {
                playPauseView.play();
                musicControl.setSpeed(App.i().getUser().getAudioSpeed());
                maHandler.postDelayed(progressTask, 1000);
            }

            @Override
            public void onPause() {
                playPauseView.pause();
                maHandler.removeCallbacks(progressTask);
            }

            @Override
            public void onEnd() {
                playPauseView.pause();
                maHandler.removeCallbacks(progressTask);
                MusicActivity.this.finish();
            }

            @Override
            public void onError(String cause) {
                ToastUtils.show("系统出错：" + cause);
                playPauseView.pause();
                maHandler.removeCallbacks(progressTask);
            }
        });
        playPauseView.setPlayPauseListener(new PlayPauseView.PlayPauseListener() {
            @Override
            public void play() {
                musicControl.play();
                maHandler.removeCallbacks(progressTask);
                maHandler.postDelayed(progressTask, 1000);
            }

            @Override
            public void pause() {
                maHandler.removeCallbacks(progressTask);
                musicControl.pause();
            }
        });

        seekBar.setMax(musicControl.getDuration());
        seekBar.setProgress(musicControl.getCurrentPosition());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser && currTimeView != null) {
                    currTimeView.setText(TimeUtil.getTime(progress));
                }
            }

            //开始触摸进度条，停止更新进度条
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isChangeProgress = true;
            }

            //停止触摸进度条
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                isChangeProgress = false;
                musicControl.seekTo(seekBar.getProgress());
            }
        });


        closeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                maHandler.removeCallbacks(progressTask);
                // 关闭悬浮窗
                FloatWindow.destroy();
                // 关闭 serview
                Intent intent2 = new Intent(MusicActivity.this, MusicService.class);
                stopService(intent2);
                // 关闭 activity
                MusicActivity.this.finish();
            }
        });


        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            speedView.setVisibility(View.GONE);
        } else {
            speedView.setText(App.i().getUser().getAudioSpeed() + "");
            speedView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new XPopup.Builder(MusicActivity.this)
                            .isCenterHorizontal(true) //是否与目标水平居中对齐
                            .offsetY(-10)
                            .hasShadowBg(true)
                            .popupAnimation(PopupAnimation.ScaleAlphaFromCenter)
                            .atView(speedView)  // 依附于所点击的View，内部会自动判断在上方或者下方显示
                            .asAttachList(new String[]{"0.8", "1.0", "1.2", "1.5", "2.0"},
                                    null,
                                    new OnSelectListener() {
                                        @Override
                                        public void onSelect(int which, String text) {
                                            musicControl.setSpeed(Float.parseFloat(text));
                                            User user = App.i().getUser();
                                            user.setAudioSpeed(Float.parseFloat(text));
                                            //App.i().getUserBox().put(user);
                                            CoreDB.i().userDao().update(user);
                                            speedView.setText(text);
                                        }
                                    })
                            .show();
                }
            });
        }
    }


    private void initFloatWindow() {
        ImageView imageView = new ImageView(this);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setPadding(ScreenUtil.dp2px(10), ScreenUtil.dp2px(10), ScreenUtil.dp2px(10), ScreenUtil.dp2px(10));
        imageView.setImageDrawable(getDrawable(R.drawable.ic_music));

        //imageView.setBackground(getDrawable(R.drawable.shape_corners));
        Drawable drawable = new DrawableCreator.Builder()
//                .setUnPressedDrawable( getDrawable(R.color.bluePrimary) )
                .setRipple(true, getResources().getColor(R.color.primary))
                .setPressedSolidColor(getResources().getColor(R.color.primary), getResources().getColor(R.color.bluePrimary))
                .setSolidColor(getResources().getColor(R.color.bluePrimary))
                .setCornersRadius(ScreenUtil.dp2px(30))
                .build();
        imageView.setBackground(drawable);
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MusicActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });


        FloatWindow
                .with(getApplicationContext())
                .setView(imageView)
                .setWidth(Screen.width, 0.15f) //设置悬浮控件宽高
                .setHeight(Screen.width, 0.15f)
                .setX(Screen.width, 0.8f) //设置控件初始位置
                .setY(Screen.height, 0.8f)
                .setMoveType(MoveType.slide, 10, 10, 10, 10)
                .setMoveStyle(500, new BounceInterpolator())
                .setFilter(true, MainActivity.class, ArticleActivity.class)
                .setDesktopShow(false)
                .build();
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if( item.getItemId() == android.R.id.home ){
            this.finish();
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
        return super.onOptionsItemSelected(item);
    }

}
