package com.lyricslover.onelyrics.lrcview;


import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Scroller;

import com.lyricslover.onelyrics.R;
import com.lyricslover.onelyrics.misc.Constants;
import com.lyricslover.onelyrics.pojos.AppPreferences;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import androidx.annotation.NonNull;

/**
 * liteplayer by loader
 * Display lrc lyrics control
 */
public class LrcView extends View {

    private static final int HORIZONTAL_MSG_WHAT = -125;
    private static final int HORIZONTAL_TIME = 200;
    private static final int SCROLL_TIME = 500;
    private static final int HORIZONTAL_OFFSET = 2;
    private static final String DEFAULT_TEXT = "No lyrics found";

    private final List<LrcLine> mLrcLines = new LinkedList<>();

    private long mNextLineTime = 0L; // Save the start time of the next sentence
    private long currentLineTime = 0L;

    private int mLrcHeight; // height of the lrc interface
    private int mCurrentLine = 0; // current line
    private int mOffsetY;    // offset on y
    private int mMaxScroll; // Maximum sliding distance=one line of lyrics height + lyrics spacing
    private int mCurrentXOffset;

    private float mDividerHeight; // line spacing

    private Rect mTextBounds;

    private Paint mNormalPaint; // regular font
    private Paint mCurrentPaint; // The size of the current lyrics

    //private Bitmap mBackground;

    private final Scroller mScroller;

    //private ValueAnimator mAnimator;
    private AppPreferences appPreferences;

    //private WindowManager windowManager;

    public LrcView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LrcView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        appPreferences = AppPreferences.getInstance(context);
        mScroller = new Scroller(context, new LinearInterpolator());
        init(attrs);
    }

    // Initialize the operation
    private void init(AttributeSet attrs) {

        DisplayMetrics displayMetrics = appPreferences.getDisplayMetrics();
        //windowManager = appPreferences.getWindowManager();
        //windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        // <begin>
        // Resolve custom attributes
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.LrcView);
        float textSize = ta.getDimension(R.styleable.LrcView_android_textSize, 10.0f);
        // how many lines
        //int mRows = ta.getInteger(R.styleable.LrcView_rows, 50);
        mDividerHeight = ta.getDimension(R.styleable.LrcView_dividerHeight, 0.0f);

        int normalTextColor = ta.getColor(R.styleable.LrcView_normalTextColor, 0xffffffff);
        int currentTextColor = ta.getColor(R.styleable.LrcView_currentTextColor, 0xff00ffde);
        ta.recycle();
        // </end>

        //if (mRows != 0) {
        // Calculate the height of the lrc panel
        //mLrcHeight = (int) (textSize + mDividerHeight) * mRows + 5;

        mLrcHeight = (appPreferences.getPreference(Constants.PANEL_HEIGHT, 48) * displayMetrics.heightPixels / 100);
        //}

        mNormalPaint = new Paint();
        mCurrentPaint = new Paint();

        // Initialize paint
        mNormalPaint.setTextSize(textSize);
        mNormalPaint.setColor(normalTextColor);
        mNormalPaint.setAntiAlias(true);

        mCurrentPaint.setTextSize(textSize + 12);
        mCurrentPaint.setFakeBoldText(true);
        mCurrentPaint.setColor(currentTextColor);
        mCurrentPaint.setAntiAlias(true);

        mTextBounds = new Rect();
        mCurrentPaint.getTextBounds(DEFAULT_TEXT, 0, DEFAULT_TEXT.length(), mTextBounds);
        computeMaxScroll();
    }

    /**
     * calculate the scrolling distance
     */
    private void computeMaxScroll() {
        mMaxScroll = (int) (mTextBounds.height() + mDividerHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        // Set a fixed number of rows, reset the height of the view
        int measuredHeightSpec = MeasureSpec.makeMeasureSpec(mLrcHeight, MeasureSpec.AT_MOST);
        super.onMeasure(widthMeasureSpec, measuredHeightSpec);
    }

    @SuppressLint("DrawAllocation")
    @Override
    protected void onDraw(Canvas canvas) {
        int width = getMeasuredWidth();

        /*if (mBackground != null) {
            canvas.drawBitmap(mBackground, new Matrix(), null);
        }*/

        float centerY = mTextBounds.height() + mDividerHeight;
        if (mLrcLines.isEmpty()) {
            canvas.drawText(DEFAULT_TEXT,
                    (width - mCurrentPaint.measureText(DEFAULT_TEXT)) / 2,
                    centerY, mCurrentPaint);
            return;
        }

        float offsetY = mTextBounds.height() + mDividerHeight;
        drawCurrentLine(canvas, width, centerY - mOffsetY);


        int firstLine = mCurrentLine - mLrcLines.size();
        firstLine = Math.max(firstLine, 0);
        int lastLine = mCurrentLine + mLrcLines.size() + 2;
        lastLine = Math.min(lastLine, mLrcLines.size() - 1);

        // Draw the current line above
        for (int i = mCurrentLine - 1, j = 1; i >= firstLine; i--, j++) {
            String lrc = mLrcLines.get(i).lrc;
            float x = (width - mNormalPaint.measureText(lrc)) / 2;
            canvas.drawText(lrc, x, centerY - j * offsetY - mOffsetY, mNormalPaint);
        }

        // Draw the current line below
        for (int i = mCurrentLine + 1, j = 1; i <= lastLine; i++, j++) {
            String lrc = mLrcLines.get(i).lrc;
            float x = (width - mNormalPaint.measureText(lrc)) / 2;
            canvas.drawText(lrc, x, centerY + j * offsetY - mOffsetY, mNormalPaint);
        }
    }

    private void drawCurrentLine(Canvas canvas, int width, float y) {
        mHandler.removeMessages(1);
        String currentLrc = mLrcLines.get(mCurrentLine).lrc;
        float contentWidth = mCurrentPaint.measureText(currentLrc);
        if (contentWidth > width) {
            canvas.drawText(currentLrc, mCurrentXOffset, y, mCurrentPaint);
            if (contentWidth - Math.abs(mCurrentXOffset) < width) {
                mCurrentXOffset = 0;
            } else {
                mHandler.sendEmptyMessageDelayed(HORIZONTAL_MSG_WHAT, HORIZONTAL_TIME);
            }
        } else {
            float currentX = (width - mCurrentPaint.measureText(currentLrc)) / 2;
            // draw the current line
            canvas.drawText(currentLrc, currentX, y, mCurrentPaint);
        }
    }

    @Override
    public void computeScroll() {
        if (mScroller.computeScrollOffset()) {
            mOffsetY = mScroller.getCurrY();
            if (mScroller.isFinished()) {
                int cur = mScroller.getCurrX();
                mCurrentLine = cur <= 1 ? 0 : cur - 1;
                mOffsetY = 0;
            }
            postInvalidate();
        }
    }


    /**
     * Parse time
     *
     * @param time String
     * @return long time in millisecond
     */
    public long parseTimeRegExp(String time) {
        //03:02.120 OR 3:2:120 OR 03.02:100
        Pattern pattern = Pattern.compile("(\\d{1,2})[.:](\\d{1,2})[.:](\\d{2,3})");
        Matcher matcher = pattern.matcher(time);
        if (matcher.matches()) {
            return Long.parseLong(matcher.group(1)) * 60000
                    + Long.parseLong(matcher.group(2)) * 1000
                    + Long.parseLong(matcher.group(3));
        } else {
            throw new IllegalArgumentException("Invalid format " + time);
        }
    }

    /**
     * Parse each line
     */
    private List<LrcLine> parseLine(String line) {
        // If the shape is like: [xxx] there is no, then return empty
        //[01:02.000]this is text [01:02.200]on single line
        List<LrcLine> ret = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\[\\d.+].+").matcher(line);
        if (matcher.matches()) {
            line = line.replaceAll("]\\[", "]-[");
            //line = line.replaceAll("\\[", "");

            //[01:02.000]this is text [01:02.200]
            if (line.endsWith("]")) {
                line += "-";
            }

            String[] result = line.split("\\[");
            //int size = result.length;
            //int i = 0;
            //while(i < size){
            for (String s : result) {
                if (!s.isEmpty()) {
                    String[] subResult = s.split("]");
                    LrcLine lrcLine = new LrcLine();
                    lrcLine.time = parseTimeRegExp(subResult[0]);
                    lrcLine.lrc = subResult[1].equalsIgnoreCase("-")? " " : subResult[1];
                    ret.add(lrcLine);
                    //i += 2;
                }
            }
        }

        return ret;
    }

    private int findShowLine(long time, int size) {
        int left = 0;
        int right = size;
        while (left <= right) {
            int middle = (left + right) / 2;
            long middleTime = mLrcLines.get(middle).time;
            if (time < middleTime) {
                right = middle - 1;
            } else {
                if (middle + 1 >= mLrcLines.size() || time < mLrcLines.get(middle + 1).time) {
                    return middle;
                }
                left = middle + 1;
            }
        }
        return 0;
    }

    /**
     * Called in music playback callback
     *
     * @param progressTime current playing time
     */
    public synchronized void onProgress(long progressTime) {
        // Every time you come in, it traverses the storage time
        //int showLine = findShowLine(progressTime, mLrcLines.size());

        /*mNextLineTime =  mLrcLines.get(showLine).time;
        if (currentLineTime != mNextLineTime) {
            mScroller.abortAnimation();
            mScroller.startScroll(showLine, 0, 0, mMaxScroll, SCROLL_TIME);
            postInvalidate();
            currentLineTime = mNextLineTime;
        }*/

        int size = mLrcLines.size();
        for (int i = 0; i < size; i++) {
            long lrcLineTime = mLrcLines.get(i).time;

            if (lrcLineTime >= progressTime) {
                mNextLineTime = i == 0 ? 0 : mLrcLines.get(i - 1).time;

                if (currentLineTime != mNextLineTime) {
                    mScroller.abortAnimation();
                    mScroller.startScroll(i, 0, 0, mMaxScroll, SCROLL_TIME);
                    postInvalidate();
                    currentLineTime = mNextLineTime;
                }
                break;
            }
        }
    }

    public void setLrc(String lrc) {
        reset();
        if (TextUtils.isEmpty(lrc)) {
            return;
        }
        parseLrc(new ByteArrayInputStream(lrc.getBytes()));
    }


    private void parseLrc(InputStream inputStream) {

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            List<LrcLine> perLine;
            List<LrcLine> allLines = new LinkedList<>();

            while (null != (line = reader.readLine())) {
                perLine = parseLine(line);
                allLines.addAll(perLine);
            }

            // sort by time
            Collections.sort(allLines);

            mLrcLines.clear();
            if (allLines.isEmpty()) {
                return;
            }

            LrcLine lastLine = allLines.get(allLines.size() - 1);
            if (TextUtils.isEmpty(lastLine.lrc) || lastLine.lrc.trim().isEmpty()) {
                allLines.remove(allLines.size() - 1);
            }

            mLrcLines.addAll(allLines);
            postInvalidate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void reset() {
        mLrcLines.clear();
        mCurrentLine = 0;
        mNextLineTime = 0L;
        mOffsetY = 0;
        postInvalidate();
    }


    /**
     * On behalf of each line, implement the Comparable interface for sorting
     */
    public static class LrcLine implements Comparable<LrcLine> {
        long time;
        String lrc;

        @Override
        public int compareTo(@NonNull LrcLine another) {
            return (int) (time - another.time);
        }

        @Override
        public boolean equals(Object obj) {
            return (obj != null) && (this.getClass() != obj.getClass())
                    && ((LrcLine) obj).time == this.time && ((LrcLine) obj).lrc.equalsIgnoreCase(this.lrc);
        }

        @Override
        public int hashCode() {
            return (int) (this.time * 37 + this.lrc.hashCode() * 29);
        }

    }

    private final MarqueeHandler mHandler = new MarqueeHandler(this);

    private static class MarqueeHandler extends Handler {
        private final WeakReference<LrcView> mLrcViewRef;

        MarqueeHandler(LrcView view) {
            mLrcViewRef = new WeakReference<>(view);
        }

        @Override
        public void handleMessage(Message msg) {
            if (msg.what == HORIZONTAL_MSG_WHAT && mLrcViewRef.get() != null) {
                mLrcViewRef.get().mCurrentXOffset -= HORIZONTAL_OFFSET;
                mLrcViewRef.get().invalidate();
            }
        }
    }
}