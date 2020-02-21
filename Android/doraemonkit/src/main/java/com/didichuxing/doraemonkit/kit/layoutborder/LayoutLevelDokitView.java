package com.didichuxing.doraemonkit.kit.layoutborder;

import android.app.Activity;
import android.content.Context;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.FrameLayout;

import com.blankj.utilcode.util.ActivityUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.didichuxing.doraemonkit.R;
import com.didichuxing.doraemonkit.config.LayoutBorderConfig;
import com.didichuxing.doraemonkit.ui.UniversalActivity;
import com.didichuxing.doraemonkit.ui.base.AbsDokitView;
import com.didichuxing.doraemonkit.ui.base.DokitViewLayoutParams;
import com.didichuxing.doraemonkit.ui.base.DokitViewManager;
import com.didichuxing.doraemonkit.ui.layoutborder.ScalpelFrameLayout;
import com.didichuxing.doraemonkit.util.LifecycleListenerUtil;
import com.didichuxing.doraemonkit.util.LogHelper;
import com.didichuxing.doraemonkit.util.UIUtils;

/**
 * Created by jintai on 2019/09/26.
 */
public class LayoutLevelDokitView extends AbsDokitView {
    private static final String TAG = "LayoutLevelDokitView";
    private CheckBox mSwitchButton;
    private View mClose;

    private ScalpelFrameLayout mScalpelFrameLayout;

    private boolean mIsCheck;
    private LifecycleListenerUtil.LifecycleListener mLifecycleListener = new LifecycleListenerUtil.LifecycleListener() {
        @Override
        public void onActivityResumed(Activity activity) {
            resolveActivity(activity);
        }

        @Override
        public void onActivityPaused(Activity activity) {

        }

        @Override
        public void onFragmentAttached(Fragment f) {

        }

        @Override
        public void onFragmentDetached(Fragment f) {

        }
    };

    private void resolveActivity(Activity activity) {
        if (activity == null || (activity instanceof UniversalActivity)) {
            return;
        }
        Window window = activity.getWindow();
        if (window == null) {
            return;
        }
        ViewGroup appContentView;
        if (isNormalMode()) {
            appContentView = (ViewGroup) UIUtils.getDokitAppContentView(activity);
        } else {
            appContentView = (ViewGroup) window.getDecorView();
        }

        if (appContentView == null) {
            ToastUtils.showShort("当前根布局功能不支持");
            return;
        }

        if (appContentView.toString().contains("SwipeBackLayout")) {
            LogHelper.i(TAG, "普通模式下布局层级功能暂不支持以SwipeBackLayout为根布局,请改用系统模式");
            ToastUtils.showShort("普通模式下布局层级功能暂不支持以SwipeBackLayout为根布局");
            return;
        }

        //将所有控件放入到ScalpelFrameLayout中
        mScalpelFrameLayout = new ScalpelFrameLayout(appContentView.getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        while (appContentView.getChildCount() != 0) {
            View child = appContentView.getChildAt(0);
            if (child instanceof ScalpelFrameLayout) {
                mScalpelFrameLayout = (ScalpelFrameLayout) child;
                return;
            }
            appContentView.removeView(child);
            mScalpelFrameLayout.addView(child);
        }
        mScalpelFrameLayout.setLayerInteractionEnabled(mIsCheck);
        mScalpelFrameLayout.setLayoutParams(params);
        appContentView.addView(mScalpelFrameLayout);
    }

    @Override
    public View onCreateView(Context context, FrameLayout view) {
        return LayoutInflater.from(context).inflate(R.layout.dk_float_layout_level, view, false);
    }

    @Override
    public void onViewCreated(FrameLayout view) {
        mSwitchButton = findViewById(R.id.switch_btn);
        mSwitchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (mScalpelFrameLayout != null) {
                        mScalpelFrameLayout.setLayerInteractionEnabled(true);
                    }
                } else {
                    if (mScalpelFrameLayout != null) {
                        mScalpelFrameLayout.setLayerInteractionEnabled(false);
                    }
                }
                mIsCheck = isChecked;
            }
        });
        mClose = findViewById(R.id.close);
        mClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mScalpelFrameLayout != null) {
                    mScalpelFrameLayout.setLayerInteractionEnabled(false);
                }
                LayoutBorderConfig.setLayoutLevelOpen(false);

                LayoutBorderConfig.setLayoutBorderOpen(false);
                LayoutBorderManager.getInstance().stop();

                DokitViewManager.getInstance().detach(LayoutLevelDokitView.this);
            }
        });

    }


    @Override
    public void initDokitViewLayoutParams(DokitViewLayoutParams params) {
        params.gravity = Gravity.CENTER_HORIZONTAL;
        params.x = 0;
        params.y = UIUtils.getHeightPixels() - UIUtils.dp2px(getContext(), 125);
        //解决页面跳转是view的宽度会发生变化
        params.width = getScreenShortSideLength();
        params.height = DokitViewLayoutParams.WRAP_CONTENT;
    }

    @Override
    public void onCreate(Context context) {
        resolveActivity(ActivityUtils.getTopActivity());
        LifecycleListenerUtil.registerListener(mLifecycleListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mScalpelFrameLayout != null) {
            mScalpelFrameLayout.setLayerInteractionEnabled(false);
            mScalpelFrameLayout = null;
        }
        LifecycleListenerUtil.unRegisterListener(mLifecycleListener);
    }

}
