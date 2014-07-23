
package com.wangli.qhvideoplugin.view;

import com.qihoo.qplayer.QihooMediaPlayer;
import com.qihoo.qplayer.QihooMediaPlayer.OnBufferingUpdateListener;
import com.qihoo.qplayer.QihooMediaPlayer.OnPositionChangeListener;
import com.qihoo.qplayer.QihooMediaPlayer.OnPreparedListener;
import com.qihoo.qplayer.view.QihooVideoView;
import com.wangli.qhvideoplugin.R;
import com.wangli.qhvideoplugin.activity.VideoFullActivity;
import com.wangli.qhvideoplugin.utils.CommonUtil;

import android.content.Context;
import android.content.Intent;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class QHVideoController extends FrameLayout {

    private QihooVideoView videoView;
    private RelativeLayout rlController;
    private ImageButton ibPlay;
    private TextView tvCurrentProgress;
    private TextView tvAllProgress;
    private SeekBar sbProgress;
    private ImageButton ibFull;
    private ImageView ivThumb;
    private ProgressBar pbBuffer;
    private ImageView ivPause;

    private final int videoWidthDp = 320;
    private final int controllerHeightDp = 30;
    private final float alphaController = 0.3f;

    private String website = "youku";
    private String url = "http%3A%2F%2Fv.youku.com%2Fv_show%2Fid_XNzMzNjkwMjQ4.html";
    private String title;

    public QHVideoController(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(context);
    }

    public QHVideoController(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(context);
    }

    public QHVideoController(Context context) {
        super(context);
        initView(context);
    }

    private void initView(final Context context) {
        initVideoView(context);
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        FrameLayout.LayoutParams controllerParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, CommonUtil.dip2px(context, controllerHeightDp));
        rlController = (RelativeLayout) layoutInflater.inflate(R.layout.controller_strip_video,
                this, false);
        controllerParams.gravity=Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL;
        rlController.getBackground().setAlpha((int) (255*alphaController));
        addView(rlController, controllerParams);

        ibFull = (ImageButton) findViewById(R.id.ib_full);
        ibPlay = (ImageButton) findViewById(R.id.ib_play);
        tvCurrentProgress = (TextView) findViewById(R.id.tv_current_progress);
        tvAllProgress = (TextView) findViewById(R.id.tv_total_progress);
        sbProgress = (SeekBar) findViewById(R.id.sb_progress);

        pbBuffer = new ProgressBar(context);
        ivThumb = new ImageView(context);
        ivThumb.setBackgroundResource(R.drawable.iv_thumb);
        ivPause = new ImageView(context);
        ivPause.setBackgroundResource(R.drawable.iv_pause);

        FrameLayout.LayoutParams centerParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        centerParams.gravity = Gravity.CENTER;
        addView(ivThumb, centerParams);
        addView(pbBuffer, centerParams);

        addView(ivPause, centerParams);

        pbBuffer.setVisibility(View.GONE);
        rlController.setVisibility(View.GONE);

        ivThumb.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ivThumb.isShown()) {
                    ivPause.setVisibility(View.GONE);
                    
                    if(videoView==null){
                        initVideoView(context);
                    }
                    
                    int width = CommonUtil.dip2px(context, videoWidthDp);
                    
                    videoView.setVisibility(View.VISIBLE);
                    videoView.initVideoWidAndHeight(width, width);
                    videoView.setDataSource(website,
                            url);
                    pbBuffer.setVisibility(View.VISIBLE);
                }
            }
        });

        sbProgress.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

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

        ibFull.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                int position = videoView.getCurrentPosition();
                release();
                Intent intent = new Intent(context, VideoFullActivity.class);
                intent.putExtra("position", position);
                intent.putExtra("url", url);
                intent.putExtra("website", website);
                intent.putExtra("title", title);

                context.startActivity(intent);
            }
        });
    }

    private void initVideoView(Context context) {
        
        FrameLayout.LayoutParams videoParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        videoView = new QihooVideoView(context);
        videoParams.gravity = Gravity.CENTER;
        addView(videoView, videoParams);
        
        videoView.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(QihooMediaPlayer player) {
                tvAllProgress.setText(CommonUtil.getTime(player.getDuration()));
                tvCurrentProgress.setText(CommonUtil.getTime(player.getCurrentPosition()));
                videoView.start();
                pbBuffer.setVisibility(View.GONE);
                ivThumb.setVisibility(View.GONE);
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
    }

    public void pause() {
        if (videoView != null) {

            videoView.pause();
        }
    }

    public void start() {
        if (videoView != null) {

            videoView.start();
        }
    }

    public void stop() {
        if (videoView != null) {

            videoView.stop();
        }
    }

    public void release() {
        if (videoView != null) {
            videoView.stop();
            videoView.release();
            videoView.setVisibility(View.GONE);
            removeView(videoView);
            videoView = null;
        }
        pbBuffer.setVisibility(View.GONE);
        ivThumb.setVisibility(View.VISIBLE);
        rlController.setVisibility(View.GONE);
        ivPause.setVisibility(View.VISIBLE);
    }
}
