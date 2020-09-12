package me.wizos.loread.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.AudioFocusRequest;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import androidx.annotation.Nullable;

import com.hjq.toast.ToastUtils;
import com.socks.library.KLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Locale;

import me.wizos.loread.App;
import me.wizos.loread.config.TestConfig;
import me.wizos.loread.db.Article;
import me.wizos.loread.utils.ArticleUtil;

import static android.media.AudioAttributes.USAGE_MEDIA;
import static android.media.AudioManager.AUDIOFOCUS_GAIN;

/**
 * implements TextToSpeech.OnInitListener , TextToSpeech.OnUtteranceCompletedListener
 * http://mp.weixin.qq.com/s?__biz=MzA3NTYzODYzMg==&mid=2653577446&idx=2&sn=940cfe45f8da91277d1046d90368d440&scene=4#wechat_redirect
 */

public class AudioService extends Service {
    private static String TAG = "TTSService";
    private TextToSpeech textToSpeech;
    private MediaPlayer player;
    private String title = "";
    private int articleNo;
    private String utteranceId;
    private boolean isQueue;
    private boolean isSpeark = false;

    @Override
    public void onCreate() {
        super.onCreate();
        KLog.e(TAG, "onCreate");

        createTextToSpeech();
        //这里只执行一次，用于准备播放器
        createMediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        KLog.e(TAG, "onStartCommand");
        if (intent != null) {
            articleNo = intent.getIntExtra("articleNo", 0);
            isQueue = intent.getBooleanExtra("isQueue",false);
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //当执行完了onCreate后，就会执行onBind把操作歌曲的方法返回
        KLog.e(TAG, "onBind");
        return new AudioControlBinder();
    }


//    @SuppressLint("SdCardPath")
    public void speak(){
        Article article = App.i().articlesAdapter.getItem(articleNo);
//        Article article = CoreDB.i().articleDao().getById(App.i().getUser().getId(),App.i().articlesAdapter.getArticleId(articleNo));
        KLog.e("准备播放" + article.getId() + " , " + utteranceId + " , " + textToSpeech.isSpeaking());
        if ( textToSpeech.isSpeaking() && article.getId().equalsIgnoreCase(utteranceId) ){
            return;
        }
        utteranceId = article.getId();

        String content = ArticleUtil.getContentForSpeak(article);

        if(TestConfig.i().isTtsFile()){
            File file = new File(App.i().getExternalCacheDir() + "/" + utteranceId + ".wav");
            textToSpeech.synthesizeToFile(content,null,file,utteranceId);
            //textToSpeech.synthesizeToFile(content,null,new File("/mnt/sdcard/speak.wav"),"test");
            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                //这个是开始的时候。是先发声之后才会走这里
                @Override
                public void onStart(String utteranceId) {
                    KLog.e("textToSpeech UtteranceProgressListener", "开始: " + file.getAbsolutePath() + "  " + file.exists());
                    if(file.exists()){
                        playMusic(file);
                    }
                }
                //这个是播报完毕的时候 每一次播报完毕都会走
                @Override
                public void onDone(String utteranceId) {
                    KLog.e("textToSpeech UtteranceProgressListener", "播放完毕  " + file.exists());
                    if(file.exists()){
//                        file.delete();
                    }
                    //playMusic(file.getAbsolutePath());
                }
                //错误
                @Override
                public void onError(String utteranceId) {
                    KLog.e("textToSpeech UtteranceProgressListener", "错误");
                }
            });
        }else {
            // textToSpeech.speak(content,TextToSpeech.QUEUE_ADD,null, article.getId());
            textToSpeech.speak(content,TextToSpeech.QUEUE_FLUSH,null, article.getId());
            textToSpeech.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                //这个是开始的时候。是先发声之后才会走这里
                @Override
                public void onStart(String utteranceId) {
                    KLog.i("textToSpeech  onStart");
                }
                //这个是播报完毕的时候 每一次播报完毕都会走
                @Override
                public void onDone(String utteranceId) {
                    if(isQueue){
                        articleNo++;
                        speak();
                    }

                    KLog.e("播放完毕" + isQueue + articleNo );
                }
                //错误
                @Override
                public void onError(String utteranceId) {
                    KLog.i("textToSpeech  onError: " + utteranceId);
                }
            });
        }
    }


    public void playMusic(final String playUrl) {
        KLog.i(TAG,"播放音乐");
        try {
            if (player == null) {
                player = createMediaPlayer();
            } else {
                player.reset();
                player.stop();
            }
            player.setDataSource(playUrl);
            //异步准备
            player.prepareAsync();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            KLog.e("设置播放地址失败A");
        } catch (IOException e) {
            e.printStackTrace();
            KLog.e("设置播放地址失败B");
        }
    }
    public void playMusic(File file) {
        KLog.i(TAG,"播放音乐");
        try {
            if (player == null) {
                player = createMediaPlayer();
            } else {
                player.reset();
                player.stop();
            }
            FileInputStream fis = new FileInputStream(file);
            player.setDataSource(fis.getFD());
            //异步准备
            player.prepareAsync();
        } catch (IllegalStateException e) {
            e.printStackTrace();
            KLog.e("设置播放地址失败A");
        } catch (IOException e) {
            e.printStackTrace();
            KLog.e("设置播放地址失败B");
        }
    }

//    @Override
//    public void onUtteranceCompleted(String utteranceId){
//    }

    //该方法包含关于歌曲的操作
    public class AudioControlBinder extends Binder {
        public void setPlayStatusListener(PlayStatusListener playStatusListener) {
            AudioService.this.playStatusListener = playStatusListener;
        }

        public AudioService getService() {
            return AudioService.this;
        }

        //播放或暂停歌曲
        public void play() {
            speak();
            //player.start();
            KLog.i("服务", "播放音乐");
        }

        public void pause() {
            if(textToSpeech !=null){
                textToSpeech.stop();
                textToSpeech.shutdown();
            }
            KLog.i("服务", "暂停音乐");
        }

//        public boolean isPrepared() {
//            return prepared;
//        }
//
//        public int getBufferedPercent() {
//            return bufferedPercent;
//        }
//
        //判断是否处于播放状态
        public boolean isPlaying() {
            return textToSpeech.isSpeaking();
        }
//
//        //返回歌曲的长度，单位为毫秒
//        public int getDuration() {
//            return player.getDuration();
//        }
//
//        //返回歌曲目前的进度，单位为毫秒
//        public int getCurrentPosition() {
//            return player.getCurrentPosition();
//        }
//
//        //设置歌曲播放的进度，单位为毫秒
//        public void seekTo(int mesc) {
//            player.seekTo(mesc);
//        }
//
//        public void setSpeed(float speed) {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (player.isPlaying()) {
//                    player.setPlaybackParams(player.getPlaybackParams().setSpeed(speed));
//                } else {
//                    player.setPlaybackParams(player.getPlaybackParams().setSpeed(speed));
//                    player.pause(); // 会自动播放，所以要暂停？
//                }
//            }
//        }
//
//        public String getSpeed() {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                return player.getPlaybackParams().getSpeed() + "";
//            }
//            return getString(R.string.music_speed);
//        }

        public String getTitle() {
            return title;
        }
    }


    private PlayStatusListener playStatusListener;
    public interface PlayStatusListener {
        void onPlay();
        void onPause(); // 例如在被其他音乐播放器抢占了焦点
        void onEnd();
        void onError(String cause);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
    }

    private int bufferedPercent = 0;


    public void createTextToSpeech(){
        if (textToSpeech == null){
            textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
                @Override
                public void onInit(int status) {
                    KLog.e(TAG, "onInit   "  + status);
                    if (status == TextToSpeech.SUCCESS) {
                        //初始化tts引擎
                        int result = textToSpeech.setLanguage(Locale.CHINA);
                        KLog.i("初始化" + result );
                        //设置参数
                        // ttsParam();
                        // TextToSpeech.LANG_MISSING_DATA：表示语言的数据丢失
                        // TextToSpeech.LANG_NOT_SUPPORTED：不支持
                        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                            ToastUtils.show( "语音包丢失或语音不支持");
                        }
                        speak();
                    }
                }
            });
        }
    }


    public MediaPlayer createMediaPlayer() {
        requestAudioFocus();
        player = new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        //添加准备好的监听
        player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mediaPlayer) {
                KLog.e("准备好了，开始播放");
                //mErrorCount = 0;//清空原来的错误
                //如果准备好了，就会进行这个方法
                mediaPlayer.start();
                if (playStatusListener != null) {
                    playStatusListener.onPlay();
                }
            }
        });
        player.setOnBufferingUpdateListener(new MediaPlayer.OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(MediaPlayer arg0, int percent) {
                bufferedPercent = percent;
                /* 打印缓冲的百分比, 如果缓冲 */
                KLog.i("缓冲了的百分比 : " + percent + " %");
            }
        });

        player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (playStatusListener != null) {
                    playStatusListener.onEnd();
                }
            }
        });

        player.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            /**
             *
             * @param mp
             * @param what 发生的错误类型
             * @param extra 特定于错误的额外代码。通常依赖于实现。
             * @return 如果方法处理了错误，则为True。如果没有处理错误，则为false。返回false，或者根本没有OnErrorListener，将导致调用OnCompletionListener。
             */
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                String whatStr = "", extraStr = "";
                boolean error = false;
                switch (extra) {
                    case MediaPlayer.MEDIA_ERROR_IO:
                        extraStr = "文件流错误";
                        error = true;
                        break;
                    case MediaPlayer.MEDIA_ERROR_MALFORMED:
                        extraStr = "格式不正确";
                        error = true;
                        break;
                    case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
                        extraStr = " 此文件不支持";
                        error = true;
                        break;
                    case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
                        extraStr = "请求超时";
                        error = true;
                        break;
                    default:
                        extraStr = " extra=(" + extra + ")";
                        break;
                }
                switch (what) {
                    case MediaPlayer.MEDIA_ERROR_UNKNOWN:
                        error = true;
                        whatStr = "未知(waht=" + what + ")";
                        break;
                    case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
                        error = true;
                        whatStr = "服务器已关闭";
                        break;
                    default:
                        whatStr = "(waht:" + what + ")";
                }

                if (playStatusListener != null && error) {
                    playStatusListener.onError(whatStr + ", " + extraStr );
                }
                KLog.e("onError播放出现错误,waht:" + what + ",extra:" + extra + ", 原因为：" + whatStr + "=" + extraStr);
                // 如果方法处理了错误，则为True。如果没有处理，则为false。返回false，或者根本没有OnErrorListener，将导致调用OnCompletionListener。
                return true;
            }
        });
        return player;
    }


    /**
     * 好像只能处理一次。
     * 结果发现 如果另外一个播放器播放获取了焦点了，那么一直就是对方的，除非对方释放了，除非你再次强求也许才会回调 focusChangeListenre，所以
     * 测试歌曲播放的时候打开qq音乐，然后开始播放 会被qq音乐获取焦点了，然后再在本软件播放然后再用qq音乐打开 无效了，因此 看来 要反复的操作，经不起折腾了，所以视频的我还是直接检测是否在播放播放就关闭了。
     *
     */
    //@RequiresApi(api = Build.VERSION_CODES.O)
    private void requestAudioFocus() {
        // 音频管理者，用于处理各app之间的音频冲突
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        assert audioManager != null;
        if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            AudioFocusRequest audioFocusRequest = new AudioFocusRequest.Builder(AUDIOFOCUS_GAIN)
                    .setOnAudioFocusChangeListener(afChangeListener)
                    .setAudioAttributes(new AudioAttributes.Builder().setUsage(USAGE_MEDIA).setContentType(AudioAttributes.CONTENT_TYPE_MUSIC).build())
                    .build();
            audioManager.requestAudioFocus(audioFocusRequest);
        }else {
            audioManager.requestAudioFocus(
                    // 音频焦点改变监听器
                    afChangeListener,
                    // Use the music stream.
                    AudioManager.STREAM_MUSIC,
                    // Request permanent focus.
                    AUDIOFOCUS_GAIN);
        }
    }

//    private void abandonAudioFocus() {
//        audioManager.abandonAudioFocus(null);
//    }

    private boolean lastAudioFocusIsLossTransient = false;
    AudioManager.OnAudioFocusChangeListener afChangeListener = new AudioManager.OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            /*
             * focusChange主要有以下四种参数：
             * AUDIOFOCUS_AGIN:你已经完全获得了音频焦点
             * AUDIOFOCUS_LOSS:你会长时间的失去焦点，所以不要指望在短时间内能获得。请结束自己的相关音频工作并做好收尾工作。比如另外一个音乐播放器开始播放音乐了（前提是这个另外的音乐播放器他也实现了音频焦点的控制，baidu音乐，天天静听很遗憾的就没有实现，所以他们两个是可以跟别的播放器同时播放的）
             * AUDIOFOCUS_LOSS_TRANSIENT:你会短暂的失去音频焦点，你可以暂停音乐，但不要释放资源，因为你一会就可以夺回焦点并继续使用
             * AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:你的焦点会短暂失去，但是你可以与新的使用者共同使用音频焦点
             */
            KLog.e("焦点转移：" + focusChange);
            switch (focusChange) {
                case AUDIOFOCUS_GAIN:
                    // Resume playback
                    if (player != null && !player.isPlaying() && lastAudioFocusIsLossTransient) {
                        player.start();
                        playStatusListener.onPlay();
                        lastAudioFocusIsLossTransient = false;
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                    // audioManager.abandonAudioFocus(afChangeListener);
                    // Stop playback
                    if (player != null && player.isPlaying()) {
                        player.pause();
                        playStatusListener.onPause();
                    }
                    break;
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                    // Pause playback
                    if (player != null && player.isPlaying()) {
                        player.pause();
                        playStatusListener.onPause();
                        lastAudioFocusIsLossTransient = true;
                    }
                    break;
            }

        }
    };

}
