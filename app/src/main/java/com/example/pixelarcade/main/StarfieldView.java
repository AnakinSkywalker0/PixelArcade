package com.example.pixelarcade.main;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Random;

public class StarfieldView extends View {
    private Paint starPaint;
    private Star[] stars;
    private Random random;
    private static final int STAR_COUNT = 80;
    private static final int STAR_SIZE = 2;

    public StarfieldView(Context context) {
        super(context);
        init();
    }

    public StarfieldView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StarfieldView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        starPaint = new Paint();
        starPaint.setColor(0xFFFFFFFF);
        starPaint.setStrokeWidth(STAR_SIZE);
        starPaint.setStrokeCap(Paint.Cap.ROUND);
        random = new Random(42);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        generateStars(w, h);
    }

    private void generateStars(int width, int height) {
        stars = new Star[STAR_COUNT];
        for (int i = 0; i < STAR_COUNT; i++) {
            stars[i] = new Star(
                random.nextInt(width),
                random.nextInt(height),
                random.nextInt(3) + 1
            );
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (stars != null) {
            for (Star star : stars) {
                starPaint.setAlpha(star.brightness);
                canvas.drawPoint(star.x, star.y, starPaint);
            }
        }
    }

    private static class Star {
        float x, y;
        int brightness;
        int size;

        Star(float x, float y, int size) {
            this.x = x;
            this.y = y;
            this.size = size;
            this.brightness = 200 + new Random().nextInt(55);
        }
    }
}
