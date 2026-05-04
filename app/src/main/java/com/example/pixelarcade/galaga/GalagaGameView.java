package com.example.pixelarcade.galaga;

import com.example.pixelarcade.R;
import com.example.pixelarcade.manager.SoundManager;

import android.content.Context;
import android.util.Log;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GalagaGameView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    // --- Thread ---
    private Thread gameThread;
    private boolean isRunning = false;
    private static final long TARGET_FRAME_TIME_MS = 1000 / 60;

    // --- Rendering ---
    private Paint paint;
    private Paint textPaint;
    private Random random;
    private static final int NUM_STARS = 120;
    private Star[] stars;
    private Bitmap playerBitmap;
    private Bitmap beeBitmap;
    private Bitmap mothBitmap;
    private Bitmap bossBitmap;
    private Bitmap blueEnemyClosed; // Blue enemy — wings closed
    private Bitmap blueEnemyOpen;   // Blue enemy — wings open
    private final RectF playerRenderRect = new RectF();

    // --- Entities ---
    private Player player;
    private final List<Enemy> enemies = new ArrayList<>();
    private final List<Bullet> playerBullets = new ArrayList<>();
    private final List<Bullet> enemyBullets = new ArrayList<>();
    private final List<Explosion> explosions = new ArrayList<>();
    private final List<FloatingText> floatingTexts = new ArrayList<>();
    private DeathExplosion deathExplosion = null; // final-life ring explosion

    // --- State Machine ---
    private enum GameState { PLAYING, WAVE_CLEARED, NEXT_WAVE_ANNOUNCE, ENEMY_ENTRY }
    private GameState gameState = GameState.PLAYING;
    private int stateTimer = 0;

    // WAVE_CLEARED lasts 120 frames (2s), NEXT_WAVE_ANNOUNCE lasts 90 frames (1.5s)
    private static final int WAVE_CLEARED_DURATION = 120;
    private static final int NEXT_WAVE_DURATION    = 90;

    // --- Game Data ---
    private int score = 0;
    private int lastMilestone = 0;
    private int frameCount = 0;
    private int currentWave = 1;
    private float formationOffsetX = 0f;
    private float formationSpeed = 2.5f;
    private boolean isEndless = false;
    private int enemiesDestroyed = 0;
    private int totalShots = 0;
    private int lives = 3;

    // --- Input ---
    private volatile boolean movingLeft  = false;
    private volatile boolean movingRight = false;
    private int shootCooldown = 0;                 // frames between shots
    private boolean playerDestroyed = false;

    // --- Callbacks (score, lives, level complete only) ---
    private GameListener listener;

    public interface GameListener {
        void onScoreUpdated(int newScore);
        void onPlayerHit();
        void onLevelComplete();
        void onWaveCleared(int nextWave);
    }

    /** Called by Activity when the last life is lost — triggers the death ring explosion. */
    public void triggerDeathExplosion() {
        if (player != null) {
            deathExplosion = new DeathExplosion(player.rect.centerX(), player.rect.centerY());
            playerDestroyed = true;
        }
    }

    // ────────────────────────────────────────────────────────
    public GalagaGameView(Context context, AttributeSet attrs) {
        super(context, attrs);
        getHolder().addCallback(this);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        // Load the actual game font (press_start_2p) to match the rest of the UI
        Typeface pressStart = ResourcesCompat.getFont(context, R.font.press_start_2p);
        textPaint.setTypeface(pressStart != null ? pressStart : Typeface.MONOSPACE);
        textPaint.setTextAlign(Paint.Align.CENTER);

        random = new Random();
        playerBitmap   = BitmapFactory.decodeResource(getResources(), R.drawable.ship_white);
        beeBitmap      = BitmapFactory.decodeResource(getResources(), R.drawable.enemy_bee);
        mothBitmap     = BitmapFactory.decodeResource(getResources(), R.drawable.enemy_moth);
        bossBitmap     = BitmapFactory.decodeResource(getResources(), R.drawable.galaga_red_no_bg);
        blueEnemyClosed = BitmapFactory.decodeResource(getResources(), R.drawable.blue_enemy_closed);
        blueEnemyOpen   = BitmapFactory.decodeResource(getResources(), R.drawable.blue_enemy_open);
        setFocusable(true);
    }

    public void setInitialWave(int wave) {
        this.currentWave = wave;
        if (wave >= 100) {
            isEndless = true;
            this.currentWave = 1; // start from wave 1 in endless visuals
        }
    }
    public void setGameListener(GameListener l) { this.listener = l; }

    // ── Surface lifecycle ──────────────────────────────────
    @Override public void surfaceCreated(SurfaceHolder h) {
        int w = getWidth(), hh = getHeight();
        if (w > 0 && hh > 0) startGame(w, hh);
        isRunning = true;
        gameThread = new Thread(this, "GameThread");
        gameThread.start();
    }
    @Override public void surfaceChanged(SurfaceHolder h, int f, int w, int hh) {}
    @Override public void surfaceDestroyed(SurfaceHolder h) { stopThread(); }

    public void stopThread() {
        isRunning = false;
        if (gameThread != null) {
            try { gameThread.join(500); } catch (InterruptedException ignored) {}
            gameThread = null;
        }
    }

    // ── Game loop ──────────────────────────────────────────
    @Override
    public void run() {
        while (isRunning) {
            long t0 = System.currentTimeMillis();
            synchronized (this) {
                updateGame();
            }
            renderFrame();
            long sleep = TARGET_FRAME_TIME_MS - (System.currentTimeMillis() - t0);
            if (sleep > 0) try { Thread.sleep(sleep); } catch (InterruptedException ignored) {}
        }
    }

    private void renderFrame() {
        Canvas canvas = null;
        SurfaceHolder holder = getHolder();
        try {
            canvas = holder.lockCanvas();
            if (canvas != null) {
                synchronized (this) {
                    drawFrame(canvas);
                }
            }
        } catch (Exception e) {
            Log.e("GalagaGameView", "Rendering error", e);
        } finally {
            if (canvas != null) {
                try {
                    holder.unlockCanvasAndPost(canvas);
                } catch (Exception ignored) {}
            }
        }
    }

    // ── Initialisation ─────────────────────────────────────
    private void startGame(int w, int h) {
        player = new Player(w / 2f, h - 250f);
        score = 0; frameCount = 0;
        gameState = GameState.ENEMY_ENTRY;
        stars = new Star[NUM_STARS];
        for (int i = 0; i < NUM_STARS; i++) stars[i] = new Star(w, h);
        prepareEntryWave(currentWave);
    }

    /** Build enemies off-screen above the top so they can fly in. */
    private void prepareEntryWave(int wave) {
        enemies.clear(); playerBullets.clear(); enemyBullets.clear(); explosions.clear();
        formationOffsetX = 0f;

        float w = getWidth();
        float cx = w / 2f;           // screen center
        float unitX = w / 10f;       // horizontal unit spacing
        float unitY = 110f;          // vertical row spacing
        float topY  = 250f;          // Y of the top row

        // Define the classic Galaga formation as {relativeColFromCenter, row, type}
        // type 0=Bee, 1=Moth, 2=Boss
        float[][] formation1 = {
            // 2 Boss Galagas — top center
            {-0.5f, 0, 2}, {0.5f, 0, 2},
            // 4 Moths — row 1
            {-1.5f, 1, 1}, {-0.5f, 1, 1}, {0.5f, 1, 1}, {1.5f, 1, 1},
            // 4 Moths — row 2 (same width)
            {-1.5f, 2, 1}, {-0.5f, 2, 1}, {0.5f, 2, 1}, {1.5f, 2, 1},
            // 8 Bees — row 3
            {-3.5f, 3, 0}, {-2.5f, 3, 0}, {-1.5f, 3, 0}, {-0.5f, 3, 0},
            { 0.5f, 3, 0}, { 1.5f, 3, 0}, { 2.5f, 3, 0}, { 3.5f, 3, 0},
            // 8 Bees — row 4
            {-3.5f, 4, 0}, {-2.5f, 4, 0}, {-1.5f, 4, 0}, {-0.5f, 4, 0},
            { 0.5f, 4, 0}, { 1.5f, 4, 0}, { 2.5f, 4, 0}, { 3.5f, 4, 0},
        };

        float[][] formation2 = {
            // 4 Boss Galagas — top
            {-1.5f, 0, 2}, {-0.5f, 0, 2}, {0.5f, 0, 2}, {1.5f, 0, 2},
            // 6 Moths — rows 1 & 2
            {-2.5f, 1, 1}, {-1.5f, 1, 1}, {-0.5f, 1, 1}, {0.5f, 1, 1}, {1.5f, 1, 1}, {2.5f, 1, 1},
            {-2.5f, 2, 1}, {-1.5f, 2, 1}, {-0.5f, 2, 1}, {0.5f, 2, 1}, {1.5f, 2, 1}, {2.5f, 2, 1},
            // 8 Blue Enemies — row 3 (new!)
            {-3.5f, 3, 3}, {-2.5f, 3, 3}, {-1.5f, 3, 3}, {-0.5f, 3, 3},
            { 0.5f, 3, 3}, { 1.5f, 3, 3}, { 2.5f, 3, 3}, { 3.5f, 3, 3},
            // 8 Bees — rows 4–5
            {-3.5f, 4, 0}, {-2.5f, 4, 0}, {-1.5f, 4, 0}, {-0.5f, 4, 0},
            { 0.5f, 4, 0}, { 1.5f, 4, 0}, { 2.5f, 4, 0}, { 3.5f, 4, 0},
            {-3.5f, 5, 0}, {-2.5f, 5, 0}, {-1.5f, 5, 0}, {-0.5f, 5, 0},
            { 0.5f, 5, 0}, { 1.5f, 5, 0}, { 2.5f, 5, 0}, { 3.5f, 5, 0},
        };

        float[][] formation3 = {
            // 4 Boss Galagas
            {-1.5f, 0, 2}, {-0.5f, 0, 2}, {0.5f, 0, 2}, {1.5f, 0, 2},
            // 8 Moths — rows 1 & 2
            {-3.5f, 1, 1}, {-2.5f, 1, 1}, {-1.5f, 1, 1}, {-0.5f, 1, 1},
            { 0.5f, 1, 1}, { 1.5f, 1, 1}, { 2.5f, 1, 1}, { 3.5f, 1, 1},
            {-3.5f, 2, 1}, {-2.5f, 2, 1}, {-1.5f, 2, 1}, {-0.5f, 2, 1},
            { 0.5f, 2, 1}, { 1.5f, 2, 1}, { 2.5f, 2, 1}, { 3.5f, 2, 1},
            // 8 Blue Enemies — row 3 (new!)
            {-3.5f, 3, 3}, {-2.5f, 3, 3}, {-1.5f, 3, 3}, {-0.5f, 3, 3},
            { 0.5f, 3, 3}, { 1.5f, 3, 3}, { 2.5f, 3, 3}, { 3.5f, 3, 3},
            // 8 Bees — rows 4–5
            {-3.5f, 4, 0}, {-2.5f, 4, 0}, {-1.5f, 4, 0}, {-0.5f, 4, 0},
            { 0.5f, 4, 0}, { 1.5f, 4, 0}, { 2.5f, 4, 0}, { 3.5f, 4, 0},
            {-3.5f, 5, 0}, {-2.5f, 5, 0}, {-1.5f, 5, 0}, {-0.5f, 5, 0},
            { 0.5f, 5, 0}, { 1.5f, 5, 0}, { 2.5f, 5, 0}, { 3.5f, 5, 0},
        };

        float[][] layout;
        if (isEndless) {
            // Cycle layouts in endless
            int cycle = (wave - 1) % 3;
            layout = cycle == 0 ? formation1 : cycle == 1 ? formation2 : formation3;
        } else {
            layout = wave == 1 ? formation1 : wave == 2 ? formation2 : formation3;
        }

        for (float[] entry : layout) {
            float relCol = entry[0];
            int   row    = (int) entry[1];
            int   type   = (int) entry[2];

            // Minimal jitter to prevent clutter/overlapping
            float targetX = cx + relCol * unitX + (random.nextFloat() * 30f - 15f);
            float targetY = topY + row * unitY + (random.nextFloat() * 20f - 10f);
            
            // Stagger entry from above: each row starts further up
            float startY  = -(row * unitY) - 300f - Math.abs(relCol) * 15f;

            enemies.add(new Enemy(targetX, targetY, type, targetX, startY));
        }

    }

    // ── Update ─────────────────────────────────────────────
    private void updateGame() {
        synchronized (getHolder()) {
            if (player == null) return;
            frameCount++;

            // Stars always scroll
            if (stars != null)
                for (Star s : stars) s.update(getWidth(), getHeight());

            switch (gameState) {
                case ENEMY_ENTRY:   updateEnemyEntry();    break;
                case PLAYING:       updatePlaying();       break;
                case WAVE_CLEARED:  updateWaveCleared();   break;
                case NEXT_WAVE_ANNOUNCE: updateNextWave(); break;
            }
        }
    }

    /** Enemies slide down from above until they reach their target Y. */
    private void updateEnemyEntry() {
        boolean allArrived = true;
        for (Enemy e : enemies) {
            if (e.entryY < e.baseY) {
                e.entryY += 8f; // slide speed
                if (e.entryY >= e.baseY) e.entryY = e.baseY;
                else allArrived = false;
            }
            e.currentX = e.baseX;
            e.y = e.entryY;
            e.hitbox.set(e.currentX - e.size/2, e.y - e.size/2, e.currentX + e.size/2, e.y + e.size/2);
        }
        if (allArrived) {
            gameState = GameState.PLAYING;
        }
    }

    private void updatePlaying() {
        player.update();
        if (movingLeft)  player.moveLeft(getWidth());
        if (movingRight) player.moveRight(getWidth());

        // Sway gets wider and faster each wave
        float speedMultiplier = 1.0f + (currentWave - 1) * 0.1f; // +10% speed per wave
        float actualSpeed = formationSpeed * speedMultiplier;
        float maxSway = 80f + currentWave * 30f;
        if (formationOffsetX > maxSway || formationOffsetX < -maxSway) formationSpeed *= -1;
        
        // Actually move with scaled speed
        formationOffsetX += actualSpeed;
        if (formationOffsetX > maxSway) formationOffsetX = maxSway;
        if (formationOffsetX < -maxSway) formationOffsetX = -maxSway;

        // Update + remove dead divers
        for (int i = enemies.size() - 1; i >= 0; i--) {
            Enemy e = enemies.get(i);
            e.update(formationOffsetX, getHeight(), player.rect.centerX());
            if (e.isDead) {
                enemies.remove(i);
                checkWaveCleared();
            }
        }

        // Trigger dive — up to 2 simultaneous divers, frequency scales with wave
        int maxDivers = 1 + currentWave;
        int currentDivers = 0;
        for (Enemy e : enemies) if (e.isDiving) currentDivers++;

        if (!enemies.isEmpty() && currentDivers < maxDivers && random.nextInt(50) < 1) {
            List<Enemy> nonDivers = new ArrayList<>();
            for (Enemy e : enemies) if (!e.isDiving) nonDivers.add(e);
            
            if (!nonDivers.isEmpty()) {
                final Enemy diver = nonDivers.get(random.nextInt(nonDivers.size()));
                diver.startDive(player.rect.centerX(), currentWave);
                SoundManager.getInstance(getContext()).playAlienFlyingSound();
                
                // Immediate shot when starting dive
                fireEnemyBullet(diver);
            }
        }

        // Player fire — AUTO-FIRE (always shooting)
        if (shootCooldown > 0) shootCooldown--;
        if (shootCooldown == 0 && !playerDestroyed) {
            playerBullets.add(new Bullet(player.rect.centerX() - 5, player.rect.top, -30f, true));
            totalShots++;
            SoundManager.getInstance(getContext()).playShootSound();
            shootCooldown = 18; // ~3 shots/sec at 60fps
        }

        // Enemy fire — AIMED at player's X
        int fireChance = Math.max(15, 60 - currentWave * 12);
        if (!enemies.isEmpty() && random.nextInt(fireChance) < 1) {
            List<Enemy> shooters = new ArrayList<>();
            for (Enemy e : enemies) if (e.type != 3) shooters.add(e);
            if (!shooters.isEmpty()) {
                Enemy shooter = shooters.get(random.nextInt(shooters.size()));
                fireEnemyBullet(shooter);
            }
        }

        // Blue enemy fire — large slow balls, aimed straight down at player
        int blueFireChance = Math.max(30, 90 - currentWave * 10);
        if (!enemies.isEmpty() && random.nextInt(blueFireChance) < 1) {
            List<Enemy> blues = new ArrayList<>();
            for (Enemy e : enemies) if (e.type == 3) blues.add(e);
            if (!blues.isEmpty()) {
                Enemy blue = blues.get(random.nextInt(blues.size()));
                float dx = player.rect.centerX() - blue.currentX;
                float dist = Math.max(Math.abs(dx), 1f);
                float spd = 14f;
                float vx = (dx / dist) * spd * 0.35f;
                enemyBullets.add(new BlueBall(blue.currentX, blue.y + 40, vx, spd));
            }
        }

        updateBullets();

        if (deathExplosion != null) {
            deathExplosion.update();
            if (deathExplosion.isDone()) deathExplosion = null;
        }

        for (int i = explosions.size() - 1; i >= 0; i--) {
            Explosion ex = explosions.get(i);
            ex.update();
            if (ex.isDead()) explosions.remove(i);
        }
    }

    private void updateWaveCleared() {
        stateTimer++;
        // Keep explosions going
        for (int i = explosions.size() - 1; i >= 0; i--) {
            explosions.get(i).update();
            if (explosions.get(i).isDead()) explosions.remove(i);
        }

        if (stateTimer >= WAVE_CLEARED_DURATION) {
            stateTimer = 0;
            // Check if level is complete (wave 3 = end of level)
            if (!isEndless && currentWave >= 3) {
                if (listener != null) post(() -> listener.onLevelComplete());
            } else {
                gameState = GameState.NEXT_WAVE_ANNOUNCE;
            }
        }
    }

    private void updateNextWave() {
        stateTimer++;
        if (stateTimer >= NEXT_WAVE_DURATION) {
            stateTimer = 0;
            currentWave++;
            if (listener != null) {
                final int w = currentWave;
                post(() -> {
                    listener.onScoreUpdated(score);
                    listener.onWaveCleared(w);
                });
            }
            prepareEntryWave(currentWave);
            gameState = GameState.ENEMY_ENTRY;
        }
    }

    public int getEnemiesDestroyed() { return enemiesDestroyed; }
    public int getAccuracy() {
        if (totalShots == 0) return 0;
        return (int)((enemiesDestroyed / (float)totalShots) * 100);
    }

    private void checkWaveCleared() {
        if (enemies.isEmpty() && gameState == GameState.PLAYING) {
            gameState = GameState.WAVE_CLEARED;
            stateTimer = 0;
        }
    }

    private void fireEnemyBullet(Enemy shooter) {
        float dx = player.rect.centerX() - shooter.currentX;
        float dist = Math.max(Math.abs(dx), 1f);
        float spd = 18f + currentWave * 3f;
        float vx = (dx / dist) * spd * 0.45f;

        if (shooter.type == 2) {
            // Boss (Red) spread: 3 bullets
            enemyBullets.add(new AimedBullet(shooter.currentX, shooter.y + 40, vx, spd));
            enemyBullets.add(new AimedBullet(shooter.currentX, shooter.y + 40, vx - 5f, spd));
            enemyBullets.add(new AimedBullet(shooter.currentX, shooter.y + 40, vx + 5f, spd));
        } else {
            enemyBullets.add(new AimedBullet(shooter.currentX, shooter.y + 40, vx, spd));
        }
    }

    // ── Bullets ────────────────────────────────────────────
    private void updateBullets() {
        for (int i = playerBullets.size() - 1; i >= 0; i--) {
            Bullet b = playerBullets.get(i);
            b.update();
            if (b.rect.bottom < 0) { playerBullets.remove(i); continue; }

            boolean hit = false;
            for (int j = enemies.size() - 1; j >= 0; j--) {
                Enemy e = enemies.get(j);
                if (RectF.intersects(b.rect, e.hitbox)) {
                    hit = true;
                    e.hp--;
                    if (e.hp <= 0) {
                        explosions.add(new Explosion(e.currentX, e.y));
                        enemies.remove(j); 
                        enemiesDestroyed++;
                        // Points increment based on bug difficulty (reduced by 100)
                        int pts = 25; // Base bug
                        if (e.type == 1) pts = 50;
                        else if (e.type == 3) pts = 100;
                        else if (e.type == 2) pts = 150;
                        
                        score += pts;

                        final int s = score;
                        if (listener != null) post(() -> listener.onScoreUpdated(s));
                        checkWaveCleared();
                    } else {
                        // Boss got hit but not destroyed — small particle explosion for the bullet impact
                        explosions.add(new Explosion(b.rect.centerX(), b.rect.top, 5));
                    }
                    break;
                }
            }
            if (hit) playerBullets.remove(i);
        }

        for (int i = enemyBullets.size() - 1; i >= 0; i--) {
            Bullet b = enemyBullets.get(i);
            b.update();
            if (b.rect.top > getHeight()) { enemyBullets.remove(i); continue; }
            if (RectF.intersects(b.rect, player.rect) && player.invulnFrames == 0) {
                explosions.add(new Explosion(player.rect.centerX(), player.rect.centerY()));
                enemyBullets.remove(i);
                player.invulnFrames = 60;
                if (listener != null) post(() -> listener.onPlayerHit());
            }
        }
    }

    // ── Drawing ────────────────────────────────────────────
    private void drawFrame(Canvas canvas) {
        canvas.drawColor(Color.BLACK);
        if (player == null) return;

        // Stars
        if (stars != null) {
            for (Star star : stars) {
                paint.setColor(star.color); paint.setAlpha(star.alpha);
                canvas.drawRect(star.x, star.y, star.x + star.size, star.y + star.size, paint);
            }
        }
        paint.setAlpha(255);

        // Player (blink when invulnerable)
        if (!playerDestroyed && (player.invulnFrames == 0 || (player.invulnFrames / 5) % 2 == 0)) {
            if (playerBitmap != null) {
                playerRenderRect.set(player.rect.left - 24, player.rect.top - 24,
                        player.rect.right + 24, player.rect.bottom + 24);
                canvas.drawBitmap(playerBitmap, null, playerRenderRect, paint);
            }
        }

        // Enemies
        for (int i = 0; i < enemies.size(); i++) {
            Enemy e = enemies.get(i);
            Bitmap bm;
            if (e.type == 0) {
                bm = beeBitmap;
            } else if (e.type == 1) {
                bm = mothBitmap;
            } else if (e.type == 2) {
                bm = bossBitmap;
            } else {
                // Blue enemy — toggle between closed/open wing frames
                bm = ((frameCount / 15) % 2 == 0) ? blueEnemyClosed : blueEnemyOpen;
            }
            if (bm == null) continue;
            float animScale = 1.0f + (float)Math.sin(frameCount * 0.18f) * 0.07f;
            float ds = e.size * animScale;
            playerRenderRect.set(e.currentX - ds/2, e.y - ds/2, e.currentX + ds/2, e.y + ds/2);
            
            if (e.rotation != 0f) {
                canvas.save();
                canvas.rotate(e.rotation, e.currentX, e.y);
                canvas.drawBitmap(bm, null, playerRenderRect, paint);
                canvas.restore();
            } else {
                canvas.drawBitmap(bm, null, playerRenderRect, paint);
            }
        }

        // Bullets — player bullets yellow, enemy bullets type-aware
        paint.setColor(Color.YELLOW);
        for (Bullet b : playerBullets) canvas.drawRect(b.rect, paint);
        for (Bullet b : enemyBullets) {
            if (b instanceof BlueBall) {
                BlueBall bb = (BlueBall) b;
                // Glowing ball: outer cyan ring + white core, pulsing size
                float pulse = 1f + (float)Math.sin(frameCount * 0.3f) * 0.12f;
                float r = bb.radius * pulse;
                float cx2 = bb.rect.centerX(), cy2 = bb.rect.centerY();
                paint.setColor(Color.parseColor("#4400FF")); paint.setAlpha(80);
                canvas.drawCircle(cx2, cy2, r * 1.6f, paint);
                paint.setColor(Color.CYAN); paint.setAlpha(200);
                canvas.drawCircle(cx2, cy2, r, paint);
                paint.setColor(Color.WHITE); paint.setAlpha(255);
                canvas.drawCircle(cx2, cy2, r * 0.45f, paint);
            } else {
                paint.setColor(Color.RED); paint.setAlpha(255);
                canvas.drawRect(b.rect, paint);
            }
        }
        paint.setAlpha(255);

        // Explosions
        for (Explosion ex : explosions) ex.draw(canvas, paint);

        // Death explosion ring (final life)
        if (deathExplosion != null) deathExplosion.draw(canvas, paint);

        // HUD Overlays
        drawStateOverlay(canvas);
    }

    /** Draw blinking wave transition text on the canvas. */
    private void drawStateOverlay(Canvas canvas) {
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;

        if (gameState == GameState.WAVE_CLEARED) {
            // Blink every 15 frames
            if ((stateTimer / 15) % 2 == 0) {
                textPaint.setTextSize(72f);
                textPaint.setColor(Color.parseColor("#00FF7F"));
                canvas.drawText("WAVE " + currentWave + " CLEARED!", cx, cy, textPaint);
            }
        } else if (gameState == GameState.NEXT_WAVE_ANNOUNCE) {
            if ((stateTimer / 12) % 2 == 0) {
                textPaint.setTextSize(56f);
                textPaint.setColor(Color.RED);
                canvas.drawText("WAVE " + (currentWave + 1) + " INCOMING!", cx, cy - 40, textPaint);
                textPaint.setTextSize(38f);
                textPaint.setColor(Color.WHITE);
                canvas.drawText("PREPARE YOURSELF", cx, cy + 20, textPaint);
            }
        }
    }

    // Left half  → move left
    // Right half → move right
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getActionMasked();
        float mid = getWidth() / 2f;

        // Reset movement flags, then re-evaluate all active pointers
        movingLeft = false; movingRight = false;

        if (action != MotionEvent.ACTION_UP && action != MotionEvent.ACTION_CANCEL) {
            for (int i = 0; i < event.getPointerCount(); i++) {
                float x = event.getX(i);
                if (x < mid)  movingLeft  = true;
                else          movingRight = true;
            }
        }
        return true;
    }

    // ══════════════════════════════════════════════════════
    // Inner classes
    // ══════════════════════════════════════════════════════

    private class Star {
        float x, y, speed, size; int alpha, color; boolean fadingOut;
        Star(int w, int h) { reset(w, h, true); }
        void reset(int w, int h, boolean rY) {
            x = random.nextFloat() * w;
            y = rY ? random.nextFloat() * h : -10;
            speed = 1.5f + random.nextFloat() * 4f;
            alpha = random.nextInt(255); fadingOut = random.nextBoolean();
            size = 2f + random.nextFloat() * 5f;
            int c = random.nextInt(10);
            color = c <= 6 ? Color.WHITE : c == 7 ? Color.RED : c == 8 ? Color.GREEN : Color.BLUE;
        }
        void update(int w, int h) {
            y += speed; if (y > h) reset(w, h, false);
            if (fadingOut) { alpha -= 8; if (alpha <= 20) { alpha = 20; fadingOut = false; } }
            else { alpha += 8; if (alpha >= 255) { alpha = 255; fadingOut = true; } }
        }
    }

    private class Player {
        RectF rect; float speed = 16f, size = 80f; int invulnFrames = 0;
        Player(float x, float y) { rect = new RectF(x-size/2, y-size/2, x+size/2, y+size/2); }
        void update() { if (invulnFrames > 0) invulnFrames--; }
        void moveLeft(int w)  { rect.offset(-speed, 0); if (rect.left < 0) rect.offsetTo(0, rect.top); }
        void moveRight(int w) { rect.offset(speed, 0);  if (rect.right > w) rect.offsetTo(w - size, rect.top); }
    }

    private class Enemy {
        float baseX, baseY;   // formation target position
        float currentX, y;    // current render position
        float entryY;         // used during ENEMY_ENTRY slide-in
        float size = 95f;
        RectF hitbox = new RectF();
        boolean isDiving = false, isDead = false;
        float diveX, diveY, diveSpeedX, diveSpeedY;
        int type;
        int hp;
        float rotation = 0f;

        Enemy(float targetX, float targetY, int type, float startX, float startY) {
            this.baseX = targetX; this.baseY = targetY;
            this.type = type;
            this.currentX = startX; this.y = startY; this.entryY = startY;
            this.hp = (type == 2) ? 2 : 1;
        }

        void startDive(float px, int wave) {
            isDiving = true;
            diveX = currentX; diveY = y;
            diveSpeedY = 12f + wave * 3f;   // faster each wave
            diveSpeedX = (px - currentX) * 0.025f;
        }

        void update(float offsetX, int height, float px) {
            if (isDiving) {
                // Subtle homing: adjust X velocity toward player
                float dx = px - currentX;
                diveSpeedX += dx * 0.0025f; 
                if (diveSpeedX > 12f) diveSpeedX = 12f;
                if (diveSpeedX < -12f) diveSpeedX = -12f;

                diveY += diveSpeedY; 
                currentX += diveSpeedX;
                y = diveY;

                // Occasional shot during dive
                if (random.nextInt(100) < 2) {
                    // This is a bit tricky since we don't have direct access to enemyBullets list here
                    // I'll handle this in the main update loop instead.
                }
                
                if (type == 2) {
                    // Slowly flip upside down while diving
                    rotation += 3f;
                    if (rotation > 180f) rotation = 180f;
                }

                if (y > height + 100) {
                    if (type == 2) {
                        // Boss survives going off screen and returns to formation to dive again later
                        isDiving = false;
                    } else {
                        isDead = true;
                    }
                }
            } else {
                currentX = baseX + offsetX; y = baseY;
                if (rotation > 0f) {
                    // Smoothly flip back to upright when returning to formation
                    rotation -= 6f;
                    if (rotation < 0f) rotation = 0f;
                }
            }
            hitbox.set(currentX - size/2, y - size/2, currentX + size/2, y + size/2);
        }
    }

    private class Bullet {
        RectF rect; float velocityY;
        Bullet(float x, float y, float vy, boolean isPlayer) {
            velocityY = vy;
            rect = new RectF(x, y, x + (isPlayer ? 10f : 16f), y + 30f);
        }
        void update() { rect.offset(0, velocityY); }
    }

    /** Aimed bullet with X velocity component. */
    private class AimedBullet extends Bullet {
        float vx;
        AimedBullet(float x, float y, float vx, float vy) {
            super(x, y, vy, false);
            this.vx = vx;
        }
        @Override void update() { rect.offset(vx, velocityY); }
    }

    /** Large glowing ball fired only by Blue enemies. */
    private class BlueBall extends Bullet {
        float vx;
        float radius = 28f;
        BlueBall(float x, float y, float vx, float vy) {
            super(x - 28f, y - 28f, vy, false);
            this.vx = vx;
            // Override rect to be square matching ball size
            rect = new RectF(x - radius, y - radius, x + radius, y + radius);
        }
        @Override void update() { rect.offset(vx, velocityY); }
    }

    private class Explosion {
        int N;
        float[] px, py, vx, vy;
        int[] color;
        int life = 25, max = 25;

        Explosion(float x, float y) { this(x, y, 20); }

        Explosion(float x, float y, int particleCount) {
            this.N = particleCount;
            px = new float[N]; py = new float[N]; vx = new float[N]; vy = new float[N];
            color = new int[N];
            for (int i = 0; i < N; i++) {
                px[i] = x; py[i] = y;
                float ang = (float)(random.nextDouble() * 2 * Math.PI);
                float spd = 2f + random.nextFloat() * 12f;
                vx[i] = (float)Math.cos(ang)*spd; vy[i] = (float)Math.sin(ang)*spd;
                color[i] = random.nextBoolean() ? Color.WHITE : Color.YELLOW;
            }
        }
        void update() { for (int i = 0; i < N; i++) { px[i] += vx[i]; py[i] += vy[i]; } life--; }
        void draw(Canvas c, Paint p) {
            int a = (int)(255f * life / max);
            for (int i = 0; i < N; i++) {
                p.setColor(color[i]); p.setAlpha(a);
                c.drawRect(px[i]-4, py[i]-4, px[i]+4, py[i]+4, p);
            }
            p.setAlpha(255);
        }
        boolean isDead() { return life <= 0; }
    }
    
    private class FloatingText {
        float x, y, textSize;
        String text;
        int life, maxLife, color;
        
        FloatingText(float x, float y, String text, float textSize, int color, int life) {
            this.x = x; this.y = y; this.text = text;
            this.textSize = textSize; this.color = color;
            this.life = life; this.maxLife = life;
        }
        void update() { y -= 1.5f; life--; }
        void draw(Canvas c, Paint p) {
            p.setColor(color);
            p.setTextSize(textSize);
            p.setAlpha((int)(255f * life / maxLife));
            c.drawText(text, x, y, p);
            p.setAlpha(255);
        }
        boolean isDead() { return life <= 0; }
    }

    /**
     * Classic Galaga death explosion — expanding rings of cyan/white/grey pixels
     * that spread outward and fade, matching the screenshot effect.
     */
    private class DeathExplosion {
        private static final int RINGS = 5;
        private static final int PIXELS_PER_RING = 24;
        private static final int TOTAL = RINGS * PIXELS_PER_RING;

        private final float cx, cy;
        private final float[] angle  = new float[TOTAL];
        private final float[] radius = new float[TOTAL]; // current radius per pixel
        private final float[] speed  = new float[TOTAL];
        private final int[]   color  = new int[TOTAL];
        private final int[]   size   = new int[TOTAL];

        private int frame = 0;
        private static final int DURATION = 90;

        // Palette matching the screenshot: cyan, white, grey, occasional red dot
        private final int[] PALETTE = {
            0xFF00FFFF, 0xFF00EEEE, 0xFFAAAAAA,
            0xFFCCCCCC, 0xFFFFFFFF, 0xFF888888,
            0xFFFF2222
        };

        DeathExplosion(float cx, float cy) {
            this.cx = cx; this.cy = cy;
            for (int ring = 0; ring < RINGS; ring++) {
                for (int p = 0; p < PIXELS_PER_RING; p++) {
                    int idx = ring * PIXELS_PER_RING + p;
                    angle[idx]  = (float)(2 * Math.PI * p / PIXELS_PER_RING)
                                  + ring * 0.15f;               // slight ring offset
                    radius[idx] = ring * 18f;                   // rings start at different radii
                    speed[idx]  = 3.5f + ring * 1.2f            // outer rings move faster
                                  + random.nextFloat() * 1.5f;
                    // Mostly cyan/white, occasional red
                    int ci = (random.nextInt(10) < 1) ? 6 : random.nextInt(6);
                    color[idx] = PALETTE[ci];
                    size[idx]  = 6 + ring * 2;                  // outer rings have bigger pixels
                }
            }
        }

        void update() {
            frame++;
            for (int i = 0; i < TOTAL; i++) {
                radius[i] += speed[i];
                // Outer pixels decelerate slightly
                speed[i] *= 0.98f;
            }
        }

        void draw(Canvas canvas, Paint p) {
            float alpha = 1f - (float) frame / DURATION;
            int a = (int)(255 * Math.max(0, alpha));
            for (int i = 0; i < TOTAL; i++) {
                float x = cx + (float)Math.cos(angle[i]) * radius[i];
                float y = cy + (float)Math.sin(angle[i]) * radius[i];
                float s = size[i] * 0.5f;
                p.setColor(color[i]);
                p.setAlpha(a);
                canvas.drawRect(x - s, y - s, x + s, y + s, p);
            }
            p.setAlpha(255);
        }

        boolean isDone() { return frame >= DURATION; }
    }
}

