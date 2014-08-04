
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
import com.wangli.qhvideoplugin.utils.HttpUtil;

import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
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

/**
 * 用于在列表页显示的一个视频播放控件
 * 
 * @author wangli
 */
public class QHVideoController extends FrameLayout {

    private QihooVideoView videoView;
    private RelativeLayout rlController;
    private SeekBar sbProgress;
    private ImageButton ibFull;
    private ImageView ivThumb;
    private ProgressBar pbBuffer;
    private ImageView ivPause;
    private TextView tvTime;

    private final int videoWidthRatio = 16;
    private final int videoHeightRatio = 9;
    private final int controllerHeightDp = 30;
    private final int ivPauseId = 0x12345678;
    private final int tvTimeMarginTopDp = 5;

    private int videoWidth;
    private int videoHeight;

    private OnStartPlayListener onStartPlayListener;

    private String url;// http%3A%2F%2Fv.youku.com%2Fv_show%2Fid_XNzMzNjkwMjQ4.html
    private String title;

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
        int width = MeasureSpec.getSize(widthMeasureSpec);

        if (width > 0) {
            videoWidth = width;
            videoHeight = videoHeightRatio * width / videoWidthRatio;
        }
        if (ivThumb != null) {
            android.view.ViewGroup.LayoutParams thumbParams = ivThumb.getLayoutParams();
            thumbParams.width = videoWidth;
            thumbParams.height = videoHeight;
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * 初始化数据
     * 
     * @param title
     * @param website
     * @param videoUrl
     * @param time
     */
    public void init(String title, String videoUrl, String thumbUrl, String time) {
        if (ivThumb != null) {
            ivPause.setBackgroundResource(R.drawable.un_play_no_thumb);
            ivThumb.setImageDrawable(null);
            ivThumb.setBackgroundColor(getResources().getColor(R.color.bg_video));
        }
        setThumb(thumbUrl);
        setTime(time);
        this.title = title;
        this.url = videoUrl;
    }

    private void setThumb(final String thumbUrl) {
        if (thumbUrl != null && !"".equals(thumbUrl.trim())) {
            if (ivThumb != null) {
                new AsyncTask<Void, Integer, byte[]>() {

                    @Override
                    protected byte[] doInBackground(Void... params) {
                        byte[] bitmap = HttpUtil.getImage(thumbUrl);
                        return bitmap;
                    }

                    protected void onPostExecute(byte[] result) {
                        if (ivThumb != null && result != null) {
                            ivThumb.setImageBitmap(BitmapFactory.decodeByteArray(result, 0,
                                    result.length));
                            ivPause.setBackgroundResource(R.drawable.un_play_video);
                        }
                    };
                }.execute();
            }
        }
    }

    private void setTime(String time) {
        if (tvTime != null) {
            tvTime.setText(time);
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
        rlController.setBackgroundColor(Color.TRANSPARENT);
        addView(rlController, controllerParams);

        ibFull = (ImageButton) findViewById(R.id.ib_full);
        sbProgress = (SeekBar) findViewById(R.id.sb_progress);

        pbBuffer = new ProgressBar(context);
        pbBuffer.setIndeterminateDrawable(getResources().getDrawable(
                R.drawable.loading_video_progress));
        ivThumb = new ImageView(context);
        ivThumb.setId(1234554321);
        ivThumb.setBackgroundColor(getResources().getColor(R.color.bg_video));

        FrameLayout.LayoutParams thumbParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        thumbParams.gravity = Gravity.CENTER;
        addView(ivThumb, thumbParams);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        params.width = (int) CommonUtil.dip2px(getContext(), 30);
        params.height = params.width;
        addView(pbBuffer, params);

        rlPause = new RelativeLayout(context);

        ivPause = new ImageView(context);
        ivPause.setBackgroundResource(R.drawable.un_play_no_thumb);

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

        FrameLayout.LayoutParams centerParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        centerParams.gravity = Gravity.CENTER;
        addView(rlPause, centerParams);
        pbBuffer.setVisibility(View.GONE);
        rlController.setVisibility(View.GONE);
        ivThumb.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ivThumb.isShown()) {
                    if (onStartPlayListener != null) {
                        onStartPlayListener.onStart();
                    }
                    rlPause.setVisibility(View.GONE);
                    tvTime.setVisibility(View.GONE);
                    pbBuffer.setVisibility(View.VISIBLE);
                    if (videoView == null) {
                        initVideoView(context);
                    }
                    videoView.setVisibility(View.VISIBLE);
                    videoView.initVideoWidAndHeight(videoWidth, videoHeight);
                    videoView.setDataSource(url);
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
                intent.putExtra("title", title);

                context.startActivity(intent);
            }
        });
    }

    public boolean isPlay() {
        if (videoView != null) {
            return videoView.isPlaying();
        }
        return false;
    }

    public QihooVideoView getVideoView() {
        return videoView;
    }

    private void initVideoView(final Context context) {

        FrameLayout.LayoutParams videoParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);
        videoView = new QihooVideoView(context);
        videoParams.gravity = Gravity.CENTER;
        addView(videoView, 0, videoParams);
        videoView.setKeepScreenOn(true);
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
                    pause();
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
                Toast.makeText(context, R.string.can_not_play, Toast.LENGTH_LONG).show();
                handler.sendEmptyMessage(MSG_RELEASE);
                return false;
            }
        });
        requestFocus();
    }

    public void pause() {
        if (videoView != null) {
            videoView.pause();
            ivPause.setBackgroundResource(R.drawable.un_play_video);
            rlPause.setVisibility(View.VISIBLE);
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
        if (ivThumb.getDrawable() != null) {
            ivPause.setBackgroundResource(R.drawable.un_play_video);
        } else {
            ivPause.setBackgroundResource(R.drawable.un_play_no_thumb);
        }
        rlPause.setVisibility(View.VISIBLE);
        tvTime.setVisibility(View.VISIBLE);
    }

    public void setOnStartPlayListener(OnStartPlayListener onStartPlayListener) {
        this.onStartPlayListener = onStartPlayListener;
    }

    public interface OnStartPlayListener {
        public void onStart();
    }
}
