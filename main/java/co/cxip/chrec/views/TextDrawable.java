package co.cxip.chrec.views;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class TextDrawable extends Drawable {
    private final int mIntrinsicSize;
    private final TextView mTextView;

    public TextDrawable(Context context, CharSequence text) {
        mIntrinsicSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, DRAWABLE_SIZE, context.getResources().getDisplayMetrics());
        mTextView = createTextView(context, text);
        mTextView.setWidth(mIntrinsicSize);
        mTextView.setHeight(mIntrinsicSize);
        mTextView.measure(mIntrinsicSize, mIntrinsicSize);
        mTextView.layout(0, 0, mIntrinsicSize, mIntrinsicSize);
    }

    private TextView createTextView(Context context, CharSequence text) {
        TextView textView = new TextView(context);
//        textView.setId(View.generateViewId()); // API 17+
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.gravity = Gravity.CENTER;
        textView.setLayoutParams(lp);
        textView.setGravity(Gravity.CENTER);
        //textView.setBackgroundResource(R.drawable.ic_backtodate);
        textView.setTextColor(Color.DKGRAY);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, DEFAULT_TEXT_SIZE);
        textView.setText(text);
        return textView;
    }

    public void setText(CharSequence text) {
        mTextView.setText(text);
        invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        mTextView.draw(canvas);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.OPAQUE;
    }

    @Override
    public int getIntrinsicWidth() {
        return mIntrinsicSize;
    }

    @Override
    public int getIntrinsicHeight() {
        return mIntrinsicSize;
    }

    @Override
    public void setAlpha(int alpha) {
    }

    @Override
    public void setColorFilter(ColorFilter filter) {
    }

    private static final int DRAWABLE_SIZE = 32; // device-independent pixels (DP)
    private static final int DEFAULT_TEXT_SIZE = 8; // device-independent pixels (DP)
}

/*public class TextDrawable extends Drawable {

    private final String text;
    private final Paint paint;
    private int mIntrinsicWidth;
    private int mIntrinsicHeight;

    public TextDrawable(String text, Resources res) {
        this.text = text;

        this.paint = new Paint();
        paint.setColor(Color.DKGRAY);
        float textSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,8, res.getDisplayMetrics());
        paint.setTextSize(textSize);
        paint.setAntiAlias(true);
        //paint.setFakeBoldText(true);
        //paint.setShadowLayer(2f, 0, 0, Color.BLACK);
        //paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.CENTER);
        mIntrinsicWidth = (int) (paint.measureText(text, 0, text.length()) + .5);
        mIntrinsicHeight = paint.getFontMetricsInt(null);
    }

    @Override
    public void draw(Canvas canvas) {
        //canvas.drawText(text, 0, 0, paint);
        Rect bounds = getBounds();
        canvas.drawText(text, 0, text.length(), bounds.centerX(), bounds.centerY() - ((paint.descent() + paint.ascent()) / 2), paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public int getIntrinsicWidth() {
        return mIntrinsicWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mIntrinsicHeight;
    }
}*/