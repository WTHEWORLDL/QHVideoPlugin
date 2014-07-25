
package com.wangli.qhvideoplugin.view;

import com.qihoo.qplayer.QihooMediaPlayer;
import com.qihoo.qplayer.QihooMediaPlayer.OnBufferingUpdateListener;
import com.qihoo.qplayer.QihooMediaPlayer.OnCompletionListener;
import com.qihoo.qplayer.QihooMediaPlayer.OnErrorListener;
import com.qihoo.qplayer.QihooMediaPlayer.OnPositionChangeListener;
import com.qihoo.qplayer.QihooMediaPlayer.OnPreparedListener;
import com.qihoo.qplayer.view.QihooVideoView;
import com.wangli.qhvideoplugin.R;
import com.wangli.qhvideoplugin.activity.VideoFullActivity;
import com.wangli.qhvideoplugin.utils.CommonUtil;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
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
import android.widget.Toast;

import java.lang.ref.WeakReference;

public class QHVideoController extends FrameLayout {

    private QihooVideoView videoView;
    private RelativeLayout rlController;
    private SeekBar sbProgress;
    private ImageButton ibFull;
    private ImageView ivThumb;
    private ProgressBar pbBuffer;
    private ImageView ivPause;
    private TextView tvTime;

    private final int videoWidthDp = 320;
    private final int controllerHeightDp = 30;
    private final int ivPauseId = 0x12345678;
    private final int tvTimeMarginTopDp = 5;

    private String website = "youku";
    private String url = "http%3A%2F%2Fv.youku.com%2Fv_show%2Fid_XNzMzNjkwMjQ4.html";
    private String title = "狗血的山姆";

    private RelativeLayout rlPause;

    private static final int MSG_RELEASE = 0x00000001;

    private static class ControllerHandler extends Handler {

        private WeakReference<QHVideoController> reference;

        public ControllerHandler(WeakReference<QHVideoController> reference) {
            this.reference = reference;
        }

        @Override
        public void handleMessage(Message msg) {
            QHVideoController controller = reference.get();
            switch (msg.what) {
                case MSG_RELEASE:
                    if (controller != null) {
                        controller.release();
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private ControllerHandler handler = new ControllerHandler(new WeakReference<QHVideoController>(
            this));

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

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    }

    public void setThumb(Bitmap bitmap) {
        if (ivThumb != null) {
            ivThumb.setImageBitmap(bitmap);
        }
    }

    public void setTime(long time) {
        if (tvTime != null) {
            tvTime.setText(CommonUtil.getTime(time));
        }
    }

    private void initView(final Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);

        FrameLayout.LayoutParams controllerParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, CommonUtil.dip2px(context,
                        controllerHeightDp));
        rlController = (RelativeLayout) layoutInflater.inflate(R.layout.controller_strip_video,
                this, false);
        controllerParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        addView(rlController, controllerParams);

        ibFull = (ImageButton) findViewById(R.id.ib_full);
        sbProgress = (SeekBar) findViewById(R.id.sb_progress);

        pbBuffer = new ProgressBar(context);
        ivThumb = new ImageView(context);
        ivThumb.setImageResource(R.drawable.iv_thumb);

        FrameLayout.LayoutParams centerParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        centerParams.gravity = Gravity.CENTER;
        addView(ivThumb, centerParams);

        addView(pbBuffer, centerParams);

        rlPause = new RelativeLayout(context);

        ivPause = new ImageView(context);
        ivPause.setBackgroundResource(R.drawable.un_play);

        RelativeLayout.LayoutParams pauseParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        ivPause.setId(ivPauseId);
        rlPause.addView(ivPause, pauseParams);

        RelativeLayout.LayoutParams timeParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        timeParams.addRule(RelativeLayout.BELOW, ivPauseId);
        timeParams.topMargin = CommonUtil.dip2px(context, tvTimeMarginTopDp);
        tvTime = new TextView(context);
        tvTime.setText(CommonUtil.getTime(0));
        rlPause.addView(tvTime, timeParams);

        addView(rlPause, centerParams);

        pbBuffer.setVisibility(View.GONE);
        rlController.setVisibility(View.GONE);
        ivThumb.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ivThumb.isShown()) {
                    rlPause.setVisibility(View.GONE);
                    tvTime.setVisibility(View.GONE);
                    if (videoView == null) {
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

    private void initVideoView(final Context context) {

        FrameLayout.LayoutParams videoParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        videoView = new QihooVideoView(context);
        videoParams.gravity = Gravity.CENTER;
        addView(videoView, 0, videoParams);

        videoView.setOnPreparedListener(new OnPreparedListener() {
            @Override
            public void onPrepared(QihooMediaPlayer player) {
                videoView.start();
                pbBuffer.setVisibility(View.GONE);
                ivThumb.setVisibility(View.GONE);
                rlController.setVisibility(View.VISIBLE);
            }
        });

        videoView.setOnPositionChangeListener(new OnPositionChangeListener() {
            @Override
            public void onPlayPositionChanged(QihooMediaPlayer player, int position) {
                int progress = (int) (((float) position / (float) player.getDuration()) * 100);
                sbProgress.setProgress(progress);
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
                if (videoView.isPlaying()) {
                    videoView.pause();
                    rlPause.setVisibility(View.VISIBLE);
                } else {
                    videoView.start();
                    rlPause.setVisibility(View.GONE);
                }
            }
        });

        videoView.setOnCompletetionListener(new OnCompletionListener() {
            @Override
            public void onCompletion(QihooMediaPlayer player) {
                handler.sendEmptyMessage(MSG_RELEASE);
            }
        });

        videoView.setOnErrorListener(new OnErrorListener() {

            @Override
            public boolean onError(QihooMediaPlayer arg0, int arg1, int arg2) {
                Toast.makeText(context, "error", Toast.LENGTH_LONG).show();
                handler.sendEmptyMessage(MSG_RELEASE);
                return false;
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
        rlPause.setVisibility(View.VISIBLE);
        tvTime.setVisibility(View.VISIBLE);
    }
}
