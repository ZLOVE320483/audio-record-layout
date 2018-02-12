package com.github.record.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.SystemClock;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.FrameLayout;

import com.github.record.utils.UIUtils;

/**
 * Created by zlove on 2018/2/11.
 */

public class RecordLayout extends FrameLayout {

    private static final int STATUS_IDLE = 0x01;
    private static final int STATUS_TRANSITION_RECORD = 0x02;
    private static final int STATUS_RECORDING = 0x03;
    private static final int STATUS_TRANSITION_IDLE = 0x04;
    private static final int TRANSITION_DURATION = 300;
    private static final int BREATHE_DURATION = 700;

    private static final int DEFAULT_RADIUS = 50;

    private int mStatus;
    private Paint mPaint;
    private int mRadius;

    private float mDownX;
    private float mDownY;

    private float mTranslationX;
    private float mTranslationY;

    private long mStatusChangeTime;

    private int mCenterMaxRadius;
    private int mCenterMinRadius;
    private int mOriginRadius;

    public interface IRecordListener {
        void onRecordStart();
        void onRecordEnd();
    }

    private IRecordListener mRecordListener;

    public void setRecordListener(IRecordListener recordListener) {
        this.mRecordListener = recordListener;
    }

    public RecordLayout(@NonNull Context context) {
        this(context, null);
    }

    public RecordLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecordLayout(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mCenterMaxRadius = UIUtils.dip2px(getContext(), 55);
        mCenterMinRadius = UIUtils.dip2px(getContext(), 40);
        mOriginRadius = UIUtils.dip2px(getContext(), 40);
        mStatus = STATUS_IDLE;
        mRadius = UIUtils.dip2px(getContext(), DEFAULT_RADIUS);
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(mTranslationX, mTranslationY);

        long now = SystemClock.uptimeMillis();
        if (mStatus == STATUS_TRANSITION_RECORD && now - mStatusChangeTime > TRANSITION_DURATION) {
            changeStatusTo(STATUS_RECORDING);
        }

        if (mStatus == STATUS_TRANSITION_IDLE) {
            if (now - mStatusChangeTime > TRANSITION_DURATION) {
                changeStatusTo(STATUS_IDLE);
                mTranslationX = 0;
                mTranslationY = 0;
                invalidate();
            } else {
                mTranslationX = (1 - 1.0f * (now - mStatusChangeTime) / TRANSITION_DURATION) * mTranslationX;
                mTranslationY = (1 - 1.0f * (now - mStatusChangeTime) / TRANSITION_DURATION) * mTranslationY;
            }
        }

        drawBorder(canvas);
        canvas.restore();
        if (mStatus != STATUS_IDLE) {
            invalidate();
        }
    }

    private void drawBorder(Canvas canvas) {
        int centerRadius = calculateCenterRadius(mStatus, mStatusChangeTime);
        int borderRadius = calculateBorderRadius(mStatus, mStatusChangeTime);
        int radius = (borderRadius + centerRadius) / 2;
        mPaint.setStrokeWidth(borderRadius - centerRadius);

        int width = getWidth();
        int height = getHeight();
        float cx = width / 2;
        float cy = height - mRadius - UIUtils.dip2px(getContext(), 35);
        canvas.drawCircle(cx, cy, radius, mPaint);
    }

    private int calculateCenterRadius(int status, long statusChangeTime) {
        long now = SystemClock.uptimeMillis();
        if (status == STATUS_TRANSITION_RECORD) {
            return (int) (mOriginRadius * (now - statusChangeTime) / TRANSITION_DURATION);
        }
        if (status == STATUS_RECORDING) {
            return (int) (mOriginRadius + (Math.sin(Math.PI * (now - statusChangeTime) / BREATHE_DURATION) + 1) * (mCenterMaxRadius - mCenterMinRadius) * 0.3f);
        }
        if (status == STATUS_TRANSITION_IDLE) {
            return (int) (mOriginRadius * (1 - 1f * (now - statusChangeTime) / TRANSITION_DURATION));
        }
        if (status == STATUS_IDLE) {
            return 0;
        }
        return 0;
    }

    private int calculateBorderRadius(int status, long statusChangeTime) {
        long now = SystemClock.uptimeMillis();
        if (status == STATUS_TRANSITION_RECORD) {
            return mCenterMinRadius + (int) ((mCenterMaxRadius - mCenterMinRadius) * (now - statusChangeTime) / TRANSITION_DURATION);
        }
        if (status == STATUS_RECORDING) {
            return mCenterMinRadius + (mCenterMaxRadius - mCenterMinRadius);
        }
        if (status == STATUS_TRANSITION_IDLE) {
            return mCenterMinRadius + (int) ((mCenterMaxRadius - mCenterMinRadius) * (1 - 1f * (now - statusChangeTime) / TRANSITION_DURATION));
        }
        if (status == STATUS_IDLE) {
            return mCenterMinRadius;
        }
        return 0;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mDownX = event.getX();
                mDownY = event.getY();
                changeStatusTo(STATUS_TRANSITION_RECORD);
                if (mRecordListener != null) {
                    mRecordListener.onRecordStart();
                }
                invalidate();
                break;
            case MotionEvent.ACTION_MOVE:
                mTranslationX = event.getX() - mDownX;
                mTranslationY = event.getY() - mDownY;
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (mRecordListener != null) {
                    mRecordListener.onRecordEnd();
                }
                changeStatusTo(STATUS_TRANSITION_IDLE);
                break;
        }
        return true;
    }


    private void changeStatusTo(int newStatus) {
        mStatus = newStatus;
        mStatusChangeTime = SystemClock.uptimeMillis();
    }
}
