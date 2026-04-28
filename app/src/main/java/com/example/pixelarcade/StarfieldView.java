package com.example.pixelarcade;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;
import java.util.Random;

public class StarfieldView extends View {
    private static final int NUM_STARS = 100;
    private Star[] stars;
    private Paint paint;
    private Random random;
    private boolean isRunning = true;

    private class Star {
        float x, y, speed, size;
        int alpha, color;
        boolean fadingOut;

        Star(int w, int h) { reset(w, h, true); }
        void reset(int w, int h, boolean randomY) {
            x = random.nextFloat() * w;
            y = randomY ? random.nextFloat() * h : -10;
            speed = 1f + random.nextFloat() * 3f;
            alpha = random.nextInt(255);
            fadingOut = random.nextBoolean();
            size = 2f + random.nextFloat() * 4f;
            int c = random.nextInt(10);
            if (c <= 7) color = Color.WHITE;
            else if (c == 8) color = Color.parseColor("#FF5555");
            else color = Color.parseColor("#5555FF");
        }
        void update(int w, int h) {
            y += speed;
            if (y > h) reset(w, h, false);
            if (fadingOut) { alpha -= 4; if (alpha <= 20) { alpha = 20; fadingOut = false; } }
            else { alpha += 4; if (alpha >= 255) { alpha = 255; fadingOut = true; } }
        }
    }

    public StarfieldView(Context context, AttributeSet attrs) {
        super(context, attrs);
        paint = new Paint();
        random = new Random();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        stars = new Star[NUM_STARS];
        for (int i = 0; i < NUM_STARS; i++) stars[i] = new Star(w, h);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (stars == null) return;
        for (Star star : stars) {
            paint.setColor(star.color);
            paint.setAlpha(star.alpha);
            canvas.drawRect(star.x, star.y, star.x + star.size, star.y + star.size, paint);
            star.update(getWidth(), getHeight());
        }
        if (isRunning) postInvalidateOnAnimation();
    }
}
