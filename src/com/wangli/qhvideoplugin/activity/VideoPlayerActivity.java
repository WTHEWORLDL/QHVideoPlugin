
package com.wangli.qhvideoplugin.activity;

import com.wangli.qhvideoplugin.R;
import com.wangli.qhvideoplugin.view.QHVideoController;

import android.os.Bundle;

public class VideoPlayerActivity extends BaseActivity {

    private QHVideoController controller;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_video);
        controller = (QHVideoController) findViewById(R.id.qhvc_controller);
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
        if(controller!=null){
            controller.release();
        }
    }
}
