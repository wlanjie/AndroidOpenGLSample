package com.wlanjie.opengl.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by wlanjie on 2017/3/27.
 */
public class TextureActivity extends AppCompatActivity {

    private RenderView mRenderView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        setContentView(R.layout.activity_texture_triangles);
//
//        mRenderView = (RenderView) findViewById(R.id.render_view);
        mRenderView = new RenderView(getApplication(), RenderView.DrawType.Rectangle);
        setContentView(mRenderView);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mRenderView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRenderView.onPause();
    }
}
