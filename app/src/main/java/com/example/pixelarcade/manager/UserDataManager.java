package com.example.pixelarcade.manager;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Centralized data manager that syncs user data between
 * local SharedPreferences (fast, offline) and Firebase Firestore (cloud).
 *
 * Write path:  SharedPreferences → Firestore (async)
 * Read path:   SharedPreferences first, then Firestore overwrites on sync
 */
public class UserDataManager {
    private static final String TAG = "UserDataManager";
    private static final String PREFS_NAME = "PixelArcadePrefs";
    private static final String COLLECTION = "users";

    private static UserDataManager instance;
    private final SharedPreferences prefs;
    private final FirebaseFirestore db;

    // All synced keys
    public static final String KEY_COINS = "coins";
    public static final String KEY_TOTAL_EARNED = "total_coins_earned";
    public static final String KEY_HIGH_2048 = "high_score_2048";
    public static final String KEY_HIGH_GALAGA = "galaga_hi_score";
    public static final String KEY_PLAYS_2048 = "plays_2048";
    public static final String KEY_PLAYS_TTT = "plays_ttt";
    public static final String KEY_WINS_TTT = "wins_ttt";
    public static final String KEY_STREAK = "streak_days";
    public static final String KEY_LAST_CLAIM = "last_daily_claim";
    public static final String KEY_GALAGA_PLAYS = "galaga_plays";
    public static final String KEY_GALAGA_ENDLESS_HI = "galaga_endless_hi_score";
    public static final String KEY_GALAGA_ENDLESS_WAVE = "galaga_endless_best_wave";
    public static final String KEY_COINS_2048 = "coins_earned_2048";
    public static final String KEY_CHALLENGE_512_DONE = "challenge_512_done";
    public static final String KEY_CHALLENGE_512_CLAIMED = "challenge_512_claimed";
    public static final String KEY_CHALLENGE_TTT_DONE = "challenge_ttt_streak_done";
    public static final String KEY_CHALLENGE_TTT_CLAIMED = "challenge_ttt_streak_claimed";
    public static final String KEY_CHALLENGE_TTT_CONSEC = "challenge_ttt_consec_wins";
    public static final String KEY_CHALLENGE_LAST_RESET = "challenge_last_reset";

    private UserDataManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        db = FirebaseFirestore.getInstance();
    }

    public static synchronized UserDataManager getInstance(Context context) {
        if (instance == null) {
            instance = new UserDataManager(context);
        }
        return instance;
    }

    // ─── Getters (read from local SharedPreferences for speed) ───

    public int getInt(String key, int defaultVal) {
        try {
            return prefs.getInt(key, defaultVal);
        } catch (ClassCastException e) {
            // Value was stored as long (from cloud sync) — read as long and cast to int
            try {
                return (int) prefs.getLong(key, defaultVal);
            } catch (ClassCastException e2) {
                return defaultVal;
            }
        }
    }

    public long getLong(String key, long defaultVal) {
        try {
            return prefs.getLong(key, defaultVal);
        } catch (ClassCastException e) {
            // Value was stored as int (old data or cloud sync) — read as int and return as long
            try {
                return (long) prefs.getInt(key, (int) defaultVal);
            } catch (ClassCastException e2) {
                return defaultVal;
            }
        }
    }

    public boolean getBoolean(String key, boolean defaultVal) {
        try {
            return prefs.getBoolean(key, defaultVal);
        } catch (ClassCastException e) {
            return defaultVal;
        }
    }

    public String getString(String key, String defaultVal) {
        try {
            return prefs.getString(key, defaultVal);
        } catch (ClassCastException e) {
            return defaultVal;
        }
    }

    // ─── Setters (write locally + push to cloud) ───

    public void putInt(String key, int value) {
        prefs.edit().putInt(key, value).apply();
        pushToCloud(key, value);
    }

    public void putLong(String key, long value) {
        prefs.edit().putLong(key, value).apply();
        pushToCloud(key, value);
    }

    public void putBoolean(String key, boolean value) {
        prefs.edit().putBoolean(key, value).apply();
        pushToCloud(key, value);
    }

    public void putString(String key, String value) {
        prefs.edit().putString(key, value).apply();
        pushToCloud(key, value);
    }

    public void remove(String key) {
        prefs.edit().remove(key).apply();
        // Also remove from cloud
        DocumentReference doc = getUserDoc();
        if (doc != null) {
            Map<String, Object> updates = new HashMap<>();
            updates.put(key, com.google.firebase.firestore.FieldValue.delete());
            doc.update(updates)
                .addOnFailureListener(e -> Log.e(TAG, "Cloud remove failed for " + key, e));
        }
    }

    // Batch update (for multiple fields at once)
    public void putMultiple(Map<String, Object> data) {
        SharedPreferences.Editor editor = prefs.edit();
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            Object val = entry.getValue();
            if (val instanceof Integer) editor.putInt(entry.getKey(), (Integer) val);
            else if (val instanceof Long) editor.putLong(entry.getKey(), (Long) val);
            else if (val instanceof Boolean) editor.putBoolean(entry.getKey(), (Boolean) val);
            else if (val instanceof String) editor.putString(entry.getKey(), (String) val);
        }
        editor.apply();
        pushMapToCloud(data);
    }

    // ─── Cloud Sync ───

    private DocumentReference getUserDoc() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return null;
        return db.collection(COLLECTION).document(user.getUid());
    }

    private void pushToCloud(String key, Object value) {
        DocumentReference doc = getUserDoc();
        if (doc == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put(key, value);
        doc.set(data, SetOptions.merge())
                .addOnFailureListener(e -> Log.e(TAG, "Cloud sync failed for " + key, e));
    }

    private void pushMapToCloud(Map<String, Object> data) {
        DocumentReference doc = getUserDoc();
        if (doc == null) return;

        doc.set(data, SetOptions.merge())
                .addOnFailureListener(e -> Log.e(TAG, "Cloud batch sync failed", e));
    }

    /**
     * Pull all data from Firestore and overwrite local SharedPreferences.
     * Call this once after login to restore cloud data to a new device.
     */
    public interface SyncCallback {
        void onSyncComplete(boolean success);
    }

    public void syncFromCloud(SyncCallback callback) {
        DocumentReference doc = getUserDoc();
        if (doc == null) {
            if (callback != null) callback.onSyncComplete(false);
            return;
        }

        doc.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DocumentSnapshot snap = task.getResult();
                if (snap != null && snap.exists()) {
                    SharedPreferences.Editor editor = prefs.edit();
                    Map<String, Object> data = snap.getData();
                    if (data != null) {
                        for (Map.Entry<String, Object> entry : data.entrySet()) {
                            Object val = entry.getValue();
                            if (val instanceof Long) {
                                long longVal = (Long) val;
                                // Timestamps stay as Long; game stats get cast to int
                                if (entry.getKey().equals(KEY_LAST_CLAIM)
                                        || entry.getKey().equals(KEY_CHALLENGE_LAST_RESET)
                                        || entry.getKey().equals("last_free_gift_claim")) {
                                    editor.putLong(entry.getKey(), longVal);
                                } else {
                                    editor.putInt(entry.getKey(), (int) longVal);
                                }
                            } else if (val instanceof Boolean) {
                                editor.putBoolean(entry.getKey(), (Boolean) val);
                            } else if (val instanceof String) {
                                editor.putString(entry.getKey(), (String) val);
                            }
                        }
                        editor.apply();
                        Log.d(TAG, "Cloud data synced to local storage");
                    }
                }
            }
            if (callback != null) callback.onSyncComplete(task.isSuccessful());
        });
    }

    /**
     * Push ALL current local data to the cloud.
     * Call this once after first login to seed the cloud with existing data.
     */
    /**
     * Wipes all local and cloud data for the current user.
     */
    public void clearAllData() {
        // Get doc ref BEFORE signing out (getUserDoc needs the current user)
        DocumentReference doc = getUserDoc();
        prefs.edit().clear().apply();
        FirebaseAuth.getInstance().signOut();
        if (doc != null) {
            doc.delete()
                .addOnSuccessListener(aVoid -> Log.d(TAG, "Cloud data successfully deleted"))
                .addOnFailureListener(e -> Log.e(TAG, "Error deleting cloud data", e));
        }
    }

    public void pushAllToCloud() {
        DocumentReference doc = getUserDoc();
        if (doc == null) return;

        Map<String, Object> data = new HashMap<>();

        // ── Identity ────────────────────────────────────────────────────────
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            data.put("email", user.getEmail());
        }
        data.put("playerName",    getString("playerName", "PLAYER"));
        data.put("playerTagline", getString("playerTagline", ""));
        data.put("playerAvatarEmoji", getString("playerAvatarEmoji", "👾"));

        // ── Economy ─────────────────────────────────────────────────────────
        data.put(KEY_COINS,         getInt(KEY_COINS, 0));
        data.put(KEY_TOTAL_EARNED,  getInt(KEY_TOTAL_EARNED, 0));

        // ── 2048 ────────────────────────────────────────────────────────────
        data.put(KEY_HIGH_2048,   getInt(KEY_HIGH_2048, 0));
        data.put(KEY_PLAYS_2048,  getInt(KEY_PLAYS_2048, 0));
        data.put(KEY_COINS_2048,  getInt(KEY_COINS_2048, 0));

        // ── Tic-Tac-Toe ─────────────────────────────────────────────────────
        data.put(KEY_PLAYS_TTT, getInt(KEY_PLAYS_TTT, 0));
        data.put(KEY_WINS_TTT,  getInt(KEY_WINS_TTT, 0));

        // ── Galaga ──────────────────────────────────────────────────────────
        data.put(KEY_HIGH_GALAGA,       getInt(KEY_HIGH_GALAGA, 0));
        data.put(KEY_GALAGA_PLAYS,      getInt(KEY_GALAGA_PLAYS, 0));
        data.put(KEY_GALAGA_ENDLESS_HI, getInt(KEY_GALAGA_ENDLESS_HI, 0));
        data.put(KEY_GALAGA_ENDLESS_WAVE, getInt(KEY_GALAGA_ENDLESS_WAVE, 0));

        // ── Streak / Daily ───────────────────────────────────────────────────
        data.put(KEY_STREAK,     getInt(KEY_STREAK, 0));
        data.put(KEY_LAST_CLAIM, getLong(KEY_LAST_CLAIM, 0L));

        // ── Shop Gift ───────────────────────────────────────────────────────
        data.put("last_free_gift_claim", getLong("last_free_gift_claim", 0L));

        // ── Challenges ──────────────────────────────────────────────────────
        data.put(KEY_CHALLENGE_512_DONE,    getBoolean(KEY_CHALLENGE_512_DONE, false));
        data.put(KEY_CHALLENGE_512_CLAIMED, getBoolean(KEY_CHALLENGE_512_CLAIMED, false));
        data.put(KEY_CHALLENGE_TTT_DONE,    getBoolean(KEY_CHALLENGE_TTT_DONE, false));
        data.put(KEY_CHALLENGE_TTT_CLAIMED, getBoolean(KEY_CHALLENGE_TTT_CLAIMED, false));
        data.put(KEY_CHALLENGE_TTT_CONSEC,  getInt(KEY_CHALLENGE_TTT_CONSEC, 0));
        data.put(KEY_CHALLENGE_LAST_RESET,  getLong(KEY_CHALLENGE_LAST_RESET, 0L));

        doc.set(data, SetOptions.merge())
                .addOnSuccessListener(v -> Log.d(TAG, "All local data pushed to cloud"))
                .addOnFailureListener(e -> Log.e(TAG, "Failed to push all data", e));
    }
}
