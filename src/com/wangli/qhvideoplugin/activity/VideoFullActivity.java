
package com.wangli.qhvideoplugin.activity;

import com.qihoo.qplayer.QihooMediaPlayer;
import com.qihoo.qplayer.QihooMediaPlayer.OnBufferingUpdateListener;
import com.qihoo.qplayer.QihooMediaPlayer.OnPositionChangeListener;
import com.qihoo.qplayer.QihooMediaPlayer.OnPreparedListener;
import com.qihoo.qplayer.view.QihooVideoView;
import com.wangli.qhvideoplugin.R;
import com.wangli.qhvideoplugin.utils.CommonUtil;
import com.wangli.qhvideoplugin.view.VerticalSeekBar;
import com.wangli.qhvideoplugin.view.VerticalSeekBar.OnSeekBarChangeListener;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

public class VideoFullActivity extends BaseActivity {

    private QihooVideoView videoView;
    private RelativeLayout rlController;
    private ImageButton ibPlay;
    private TextView tvCurrentProgress;
    private TextView tvAllProgress;
    private SeekBar sbProgress;
    private ProgressBar pbBuffer;

    private int position;
    private String website;
    private String url;
    private String title;

    private final int controllerBottomMarginDp = 18;
    private final float alphaController = 0.3f;
    private ImageButton ibBrightness;
    private ImageButton ibVolume;
    private VerticalSeekBar vsbBrightness;
    private VerticalSeekBar vsbVolume;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getData();

        initView();
    }

    private void getData() {
        Intent intent = getIntent();
        position = intent.getIntExtra("position", 0);
        website = intent.getStringExtra("website");
        url = intent.getStringExtra("url");
        title = intent.getStringExtra("title");
    }

    @SuppressWarnings("deprecation")
    private void initView() {

        FrameLayout decorView = (FrameLayout) getWindow().getDecorView();

        videoView = new QihooVideoView(this);
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
        vsbBrightness = (VerticalSeekBar) findViewById(R.id.vsb_brightness_full);
        vsbVolume = (VerticalSeekBar) findViewById(R.id.vsb_volume_full);
        ibBrightness.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                vsbBrightness.setVisibility(View.VISIBLE);
                int brightness = getScreenBrightness();
                vsbBrightness.setProgress((int) ((brightness / 255f) * vsbBrightness.getMax()));
            }
        });

        ibVolume.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                vsbVolume.setVisibility(View.VISIBLE);
                vsbVolume
                        .setProgress((int) ((float) (getCurrentVolume() / getMaxVolume()) * vsbVolume
                                .getMax()));
            }
        });

        vsbBrightness.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(VerticalSeekBar vBar, int progress, boolean fromUser) {
                Log.e("brightness", "xxxprogress..."+progress);
                saveScreenBrightness((int) ((float) (progress / vBar.getMax()) * 255));                
            }

            @Override
            public void onStartTrackingTouch(VerticalSeekBar vBar) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onStopTrackingTouch(VerticalSeekBar vBar) {
                // TODO Auto-generated method stub
                
            }
        });
        vsbVolume.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(VerticalSeekBar vBar, int progress, boolean fromUser) {
                Log.e("volume", "xxxvolume..."+progress);
                setVolume((int) ((float) (progress / vBar.getMax()) * getMaxVolume()));                
            }

            @Override
            public void onStartTrackingTouch(VerticalSeekBar vBar) {
                // TODO Auto-generated method stub
                
            }

            @Override
            public void onStopTrackingTouch(VerticalSeekBar vBar) {
                // TODO Auto-generated method stub
                
            }
        });

        pbBuffer = new ProgressBar(this);

        findViewById(R.id.rl_controller_bottom).getBackground().setAlpha(
                (int) (255 * alphaController));

        FrameLayout.LayoutParams centerParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        centerParams.gravity = Gravity.CENTER;
        decorView.addView(pbBuffer, centerParams);

        pbBuffer.setVisibility(View.VISIBLE);
        rlController.setVisibility(View.GONE);

        videoView.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(QihooMediaPlayer player) {
                tvAllProgress.setText(CommonUtil.getTime(player.getDuration()));
                tvCurrentProgress.setText(CommonUtil.getTime(player.getCurrentPosition()));
                videoView.seekTo(position);
                pbBuffer.setVisibility(View.GONE);
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
                // 缓冲
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
                if (rlController.isShown()) {
                    rlController.setVisibility(View.GONE);
                } else {
                    rlController.setVisibility(View.VISIBLE);
                }
            }
        });

        ibPlay.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (videoView.isPlaying()) {
                    videoView.pause();
                } else {
                    videoView.start();
                }
            }
        });

        Display display = getWindowManager().getDefaultDisplay();

        int width = display.getWidth();
        int height = display.getHeight();

        videoView.initVideoWidAndHeight(width, height);
        videoView.setDataSource(website, url);

    }

    private int getScreenBrightness() {
        int screenBrightness = 255;
        try {
            screenBrightness = Settings.System.getInt(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS);
        } catch (Exception localException) {

        }
        return screenBrightness;
    }

    private void saveScreenBrightness(int paramInt) {
        try {
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS,
                    paramInt);
        } catch (Exception localException) {
            localException.printStackTrace();
        }
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

    // @Override
    // protected void onDestroy() {
    // super.onDestroy();
    // videoView.release();
    // videoView=null;
    // }
}
