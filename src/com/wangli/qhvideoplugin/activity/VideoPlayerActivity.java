
package com.wangli.qhvideoplugin.activity;

import com.wangli.qhvideoplugin.R;

import android.os.Bundle;

public class VideoPlayerActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video);

    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void finish() {
        super.finish();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
