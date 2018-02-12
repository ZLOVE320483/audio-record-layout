# audio-record-layout
## 前言
    前段时间说想静下心来学习一下有关音视频的知识，于是第一个需求就来了，需要做一个炫酷的录制音视频的控件。所以呢我就随手写了一个，
    顺便复习了一下Android自定义view的相关知识。
## 效果图
    废话不多说，先上一个效果图：
![](https://github.com/ZLOVE320483/audio-record-layout/blob/master/img/record.gif)
>
    怎么样，效果是不是还可以？
## 代码分析
    首先看一下这个view运动的整个过程，你会发现有四个状态：
    
    private static final int STATUS_IDLE = 0x01;
    private static final int STATUS_TRANSITION_RECORD = 0x02;
    private static final int STATUS_RECORDING = 0x03;
    private static final int STATUS_TRANSITION_IDLE = 0x04;
    
    STATUS_IDLE：闲置状态，即啥也不干
    STATUS_TRANSITION_RECORD：由闲置状态转为拍摄状态，即手指刚刚按下，实心圈变成空心圈的过程
    STATUS_RECORDING：拍摄状态，即手指按下并且可以随意移动，空心圈闪烁的过程
    STATUS_TRANSITION_IDLE：由拍摄转为闲置状态，即松开手指，空心圈再次变实心圈的过程
    
    初始化操作：
    private void init() {
        mCenterMaxRadius = UIUtils.dip2px(getContext(), 55); // 最大半径
        mCenterMinRadius = UIUtils.dip2px(getContext(), 40); // 最小半径
        mOriginRadius = UIUtils.dip2px(getContext(), 40); // 原始半径
        mStatus = STATUS_IDLE;
        mRadius = UIUtils.dip2px(getContext(), DEFAULT_RADIUS); // 默认半径
        mPaint = new Paint();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
    }
    
    绘制过程：
    @Override
    protected void dispatchDraw(Canvas canvas) {
        canvas.save();
        canvas.translate(mTranslationX, mTranslationY);

        long now = SystemClock.uptimeMillis();
        // 当手指按下的时间长度超过阈值，触发状态改变
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
        // 绘制空心圈
        drawBorder(canvas);
        canvas.restore();
        if (mStatus != STATUS_IDLE) {
            invalidate();
        }
    }
    
    onTouch事件处理：
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
>
这个自定义view是集成FrameLayout的，很多同学很奇怪为什么不是复写onDraw()方法，而是复写
dispatchDraw()方法，具体愿意请参考 [为什么自定义ViewGroup ondraw方法不会被调用](http://www.jcodecraeer.com/a/anzhuokaifa/androidkaifa/2014/1014/1765.html)

