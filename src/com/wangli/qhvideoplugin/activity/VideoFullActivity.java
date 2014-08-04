
package com.wangli.qhvideoplugin.activity;

import com.qihoo.qplayer.QihooMediaPlayer;
import com.qihoo.qplayer.QihooMediaPlayer.OnBufferingUpdateListener;
import com.qihoo.qplayer.QihooMediaPlayer.OnCompletionListener;
import com.qihoo.qplayer.QihooMediaPlayer.OnErrorListener;
import com.qihoo.qplayer.QihooMediaPlayer.OnPositionChangeListener;
import com.qihoo.qplayer.QihooMediaPlayer.OnPreparedListener;
import com.qihoo.qplayer.QihooMediaPlayer.OnSeekCompleteListener;
import com.qihoo.qplayer.view.QihooVideoView;
import com.wangli.qhvideoplugin.R;
import com.wangli.qhvideoplugin.utils.CommonUtil;
import com.wangli.qhvideoplugin.view.VerticalSeekBar;
import com.wangli.qhvideoplugin.view.VerticalSeekBar.OnSeekBarChangeListener;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class VideoFullActivity extends BaseActivity {

    private boolean isBrightnessAuto = false;

    private QihooVideoView videoView;
    private RelativeLayout rlController;
    private ImageButton ibPlay;
    private TextView tvCurrentProgress;
    private TextView tvAllProgress;
    private SeekBar sbProgress;
    private ProgressBar pbBuffer;

    private int position;
    private String url;
    private String title;

    private final int controllerBottomMarginDp = 18;
    private final float alphaController = 0.3f;
    private ImageButton ibBrightness;
    private ImageButton ibVolume;
    private VerticalSeekBar vsbBrightness;
    private VerticalSeekBar vsbVolume;

    private FrameLayout decorView;

    private static final int MSG_HIDE_CONTROLLER = 0x00000001;

    private boolean adjustVolume = false;

    private static class ControllerHandler extends Handler {

        private WeakReference<VideoFullActivity> reference;

        public ControllerHandler(WeakReference<VideoFullActivity> reference) {
            this.reference = reference;
        }

        @Override
        public void handleMessage(Message msg) {
            VideoFullActivity activity = reference.get();
            switch (msg.what) {
                case MSG_HIDE_CONTROLLER:
                    if (activity != null) {
                        activity.hideBrightness();
                        activity.hideVolume();
                        activity.hideController();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void hideController() {
        rlControllerTop.setVisibility(View.GONE);
        rlControllerBottom.setVisibility(View.GONE);
        rlControllerTop.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.anim_top_out));
        rlControllerBottom.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.anim_bottom_out));
    }

    public void showController() {
        rlControllerTop.setVisibility(View.VISIBLE);
        rlControllerBottom.setVisibility(View.VISIBLE);
        rlControllerTop.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.anim_top_in));
        rlControllerBottom.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.anim_bottom_in));
    }

    public void hideBrightness() {
        if (vsbBrightness.isShown()) {
            vsbBrightness.setVisibility(View.GONE);
            ibBrightness.setBackgroundResource(R.drawable.ib_brightness_full_video);
        }
    }

    public void hideVolume() {
        if (vsbVolume.isShown()) {
            vsbVolume.setVisibility(View.GONE);
            initIbVolumeBack();
        }
    }

    private ControllerHandler handler = new ControllerHandler(new WeakReference<VideoFullActivity>(
            this));

    private RelativeLayout rlControllerTop;

    private RelativeLayout rlControllerBottom;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getData();

        initView();
    }

    private void getData() {
        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);
        url = intent.getStringExtra("url");
        title = intent.getStringExtra("title");
    }

    @SuppressWarnings("deprecation")
    private void initView() {

        initScreenBrightness();

        decorView = (FrameLayout) getWindow().getDecorView();
        videoView = new QihooVideoView(this);
        videoView.setKeepScreenOn(true);
        FrameLayout.LayoutParams videoParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        videoParams.gravity = Gravity.CENTER;
        decorView.addView(videoView, videoParams);

        rlController = (RelativeLayout) LayoutInflater.from(this).inflate(R.layout.video_full,
                decorView, false);
        FrameLayout.LayoutParams controllerParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        controllerParams.bottomMargin = CommonUtil.dip2px(this, controllerBottomMarginDp);
        decorView.addView(rlController, controllerParams);

        rlControllerTop = (RelativeLayout) findViewById(R.id.rl_controller_top);
        rlControllerBottom = (RelativeLayout) findViewById(R.id.rl_controller_bottom);

        ImageButton ibReturn = (ImageButton) findViewById(R.id.ib_return);
        TextView tvTitle = (TextView) findViewById(R.id.tv_title);
        tvTitle.setText(title);

        ibReturn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ibPlay = (ImageButton) findViewById(R.id.ib_play_full);
        tvCurrentProgress = (TextView) findViewById(R.id.tv_current_progress_full);
        tvAllProgress = (TextView) findViewById(R.id.tv_total_progress_full);
        sbProgress = (SeekBar) findViewById(R.id.sb_progress_full);
        ibBrightness = (ImageButton) findViewById(R.id.ib_brightness_full);
        ibVolume = (ImageButton) findViewById(R.id.ib_volume_full);

        initIbVolumeBack();

        vsbBrightness = (VerticalSeekBar) findViewById(R.id.vsb_brightness_full);
        vsbVolume = (VerticalSeekBar) findViewById(R.id.vsb_volume_full);

        vsbBrightness.setMax(100);
        vsbVolume.setMax(100);

        ibBrightness.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vsbBrightness.isShown()) {
                    vsbBrightness.setVisibility(View.GONE);
                    ibBrightness.setBackgroundResource(R.drawable.ib_brightness_full_video);
                } else {
                    vsbBrightness.setVisibility(View.VISIBLE);
                    int brightness = getScreenBrightness();
                    vsbBrightness.setProgress((int) ((brightness * 1.00f / 255) * vsbBrightness
                            .getMax()));
                    hideVolume();
                    ibBrightness.setBackgroundResource(R.drawable.ib_brightness_selected_video);
                }

            }
        });

        ibVolume.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (vsbVolume.isShown()) {
                    vsbVolume.setVisibility(View.GONE);
                    if (getCurrentVolume() == 0) {
                        ibVolume.setBackgroundResource(R.drawable.ib_volume_silence_video);
                    } else {
                        ibVolume.setBackgroundResource(R.drawable.ib_volume_default_video);
                    }
                } else {
                    vsbVolume.setVisibility(View.VISIBLE);
                    if (getCurrentVolume() == 0) {
                        ibVolume.setBackgroundResource(R.drawable.ib_volume_silence_selected_video);
                    } else {
                        ibVolume.setBackgroundResource(R.drawable.ib_volume_selected_video);
                    }
                    vsbVolume
                            .setProgress((int) ((getCurrentVolume() * 1.00f / getMaxVolume()) * vsbVolume
                                    .getMax()));
                    hideBrightness();
                }
            }
        });

        vsbBrightness.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(VerticalSeekBar vBar, int progress, boolean fromUser) {
                if (fromUser) {
                    saveScreenBrightness((int) ((progress * 1.00f / vBar.getMax()) * 255));
                }
            }

            @Override
            public void onStartTrackingTouch(VerticalSeekBar vBar) {

            }

            @Override
            public void onStopTrackingTouch(VerticalSeekBar vBar) {

            }
        });
        vsbVolume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(VerticalSeekBar vBar, int progress, boolean fromUser) {
                if (fromUser || adjustVolume) {
                    setVolume((int) ((progress * 1.00f / vBar.getMax() * getMaxVolume())));
                    adjustVolume = false;
                }
                if (vsbVolume.isShown()) {
                    if (progress == 0) {
                        ibVolume.setBackgroundResource(R.drawable.ib_volume_silence_selected_video);
                    } else {
                        ibVolume.setBackgroundResource(R.drawable.ib_volume_selected_video);
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(VerticalSeekBar vBar) {

            }

            @Override
            public void onStopTrackingTouch(VerticalSeekBar vBar) {

            }
        });

        pbBuffer = new ProgressBar(this);
        pbBuffer.setIndeterminateDrawable(getResources().getDrawable(R.drawable.loading_video_progress));

        findViewById(R.id.rl_video_bottom).getBackground().setAlpha(
                (int) (255 * alphaController));
        rlControllerTop.getBackground().setAlpha((int) (255 * alphaController));

        FrameLayout.LayoutParams centerParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        centerParams.gravity = Gravity.CENTER;
        decorView.addView(pbBuffer, centerParams);

        pbBuffer.setVisibility(View.VISIBLE);

        videoView.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(QihooMediaPlayer player) {
                tvAllProgress.setText(CommonUtil.getTime(player.getDuration()));
                tvCurrentProgress.setText(CommonUtil.getTime(player.getCurrentPosition()));
                videoView.start();
                videoView.seekTo(position);
            }
        });

        videoView.setOnPositionChangeListener(new OnPositionChangeListener() {
            @Override
            public void onPlayPositionChanged(QihooMediaPlayer player, int position) {
                int progress = (int) (((float) position / (float) player.getDuration()) * 100);
                sbProgress.setProgress(progress);
                tvCurrentProgress.setText(CommonUtil.getTime(position));
            }
        });

        videoView.setOnBufferListener(new OnBufferingUpdateListener() {
            @Override
            public void onBufferingUpdate(QihooMediaPlayer arg0, int arg1) {
                if (arg1 >= 0 && arg1 < 100) {
                    if (!pbBuffer.isShown()) {
                        pbBuffer.setVisibility(View.VISIBLE);
                    }
                } else if (arg1 == 100) {
                    if (pbBuffer.isShown()) {
                        pbBuffer.setVisibility(View.GONE);
                    }
                }
            }
        });

        videoView.setOnSeekCompleteListener(new OnSeekCompleteListener() {

            @Override
            public void onSeekComplete(QihooMediaPlayer player) {
                tvCurrentProgress.setText(CommonUtil.getTime(player.getCurrentPosition()));
                if (pbBuffer.isShown()) {
                    pbBuffer.setVisibility(View.GONE);
                }
                ibPlay.setBackgroundResource(R.drawable.ib_pause_full_video);
                player.start();
            }
        });

        videoView.setOnCompletetionListener(new OnCompletionListener() {

            @Override
            public void onCompletion(QihooMediaPlayer player) {
                tvCurrentProgress.setText(CommonUtil.getTime(player.getDuration()));
                finish();
            }
        });

        videoView.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(QihooMediaPlayer arg0, int arg1, int arg2) {
                Toast.makeText(VideoFullActivity.this, R.string.can_not_play, Toast.LENGTH_LONG)
                        .show();
                release();
                return false;
            }
        });

        sbProgress.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    videoView.seekTo((int) ((progress / 100f) * videoView
                            .getDuration()));
                }
            }
        });

        videoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (rlControllerTop.isShown()) {
                    hideBrightness();
                    hideVolume();
                    hideController();
                } else {
                    showController();
                }
            }
        });

        ibPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView.isPlaying()) {
                    ibPlay.setBackgroundResource(R.drawable.ib_play_full_video);
                    videoView.pause();
                } else {
                    ibPlay.setBackgroundResource(R.drawable.ib_pause_full_video);
                    videoView.start();
                }
            }
        });

        Display display = getWindowManager().getDefaultDisplay();

        int width = display.getWidth();
        int height = display.getHeight();

        videoView.initVideoWidAndHeight(width, height);
        videoView.setDataSource(url);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void initIbVolumeBack() {
        if (getCurrentVolume() == 0) {
            ibVolume.setBackgroundResource(R.drawable.ib_volume_silence_video);
        } else {
            ibVolume.setBackgroundResource(R.drawable.ib_volume_default_video);
        }
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        switch (ev.getAction()) {
            case MotionEvent.ACTION_UP:
                handler.removeMessages(MSG_HIDE_CONTROLLER);
                handler.sendEmptyMessageDelayed(MSG_HIDE_CONTROLLER, 5000);
                break;

            default:
                break;
        }
        return super.dispatchTouchEvent(ev);
    }

    private void initScreenBrightness() {
        if (getScreenMode() == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
            setScreenMode(Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            isBrightnessAuto = true;
        } else {
            isBrightnessAuto = false;
        }
    }

    private void restoreScreenBrightness() {
        if (isBrightnessAuto) {
            setScreenMode(Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
            isBrightnessAuto = true;
        }
    }

    private int getScreenBrightness() {
        int screenBrightness = 255;
        try {
            screenBrightness = Settings.System.getInt(this.getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception localException) {

        }
        return screenBrightness;
    }

    private void setScreenMode(int paramInt) {
        try {

            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE,
                    paramInt);

        } catch (Exception localException) {
            localException.printStackTrace();
        }
    }

    private int getScreenMode() {
        int screenMode = 0;
        try {
            screenMode = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE);
        } catch (Exception localException) {

        }
        return screenMode;
    }

    private void saveScreenBrightness(int brightness) {
        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.screenBrightness = brightness / 255f;
        getWindow().setAttributes(params);

        Uri uri = android.provider.Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS);
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, brightness);
        getContentResolver().notifyChange(uri, null);
    }

    private int getCurrentVolume() {
        // 音量控制,初始化定义
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // 当前音量
        int currentVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        return currentVolume;
    }

    private int getMaxVolume() {
        // 音量控制,初始化定义
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        // 最大音量
        int maxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        return maxVolume;
    }

    private void setVolume(int volume) {
        // 音量控制,初始化定义
        AudioManager mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(rlController!=null){
            handler.removeMessages(MSG_HIDE_CONTROLLER);
            handler.sendEmptyMessageDelayed(MSG_HIDE_CONTROLLER, 5000);
            rlControllerTop.setVisibility(View.VISIBLE);
            rlControllerBottom.setVisibility(View.VISIBLE);
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        if(videoView!=null){
            ibPlay.setBackgroundResource(R.drawable.ib_play_full_video);
            videoView.pause();
        }
    }
    
    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                adjustVolume = true;
                vsbVolume.setProgress(vsbVolume.getProgress() - 10);
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                adjustVolume = true;
                vsbVolume.setProgress(vsbVolume.getProgress() + 10);
                break;
            case KeyEvent.KEYCODE_BACK:
                super.onKeyDown(keyCode, event);
                break;
            default:
                break;
        }
        return true;
    }

    private void release() {
        restoreScreenBrightness();
        if (videoView != null) {
            videoView.stop();
            videoView.release();
            decorView.removeView(videoView);
            videoView = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        release();
    }
}
