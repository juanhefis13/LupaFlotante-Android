package cl.canelillo.lupaflotante;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.MagnificationConfig;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.ImageView;
import android.widget.Toast;

/** Servicio sin lectura de ventanas ni registro de acciones: solo lupa y burbuja. */
public class LupaAccessibilityService extends AccessibilityService {
    public static LupaAccessibilityService instance;
    public static final String PREFS = "lupa_settings";
    public static final String KEY_SHOW = "show_bubble";
    public static final String KEY_SCALE = "scale";

    private SharedPreferences preferences;
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private ImageView bubble;
    private GestureDetector gestures;
    private float downX, downY;
    private int fromX, fromY;
    private boolean dragging = false;
    private boolean active = false;
    private long lastUpdate = 0;
    private int touchSlop;

    @Override public void onAccessibilityEvent(AccessibilityEvent event) { }
    @Override public void onInterrupt() { }

    @Override protected void onServiceConnected() {
        super.onServiceConnected();
        instance = this;
        preferences = getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        touchSlop = ViewConfiguration.get(this).getScaledTouchSlop();
        updateBubbleVisibility();
    }

    public void updateBubbleVisibility() {
        if (preferences == null) return;
        if (preferences.getBoolean(KEY_SHOW, true)) {
            if (bubble == null) createBubble();
        } else {
            disableMagnification();
            removeBubble();
        }
    }

    private void createBubble() {
        if (bubble != null || windowManager == null) return;
        int size = dp(66);
        Rect screen = getScreenBounds();
        params = new WindowManager.LayoutParams(size, size,
            WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP | Gravity.START;
        params.x = preferences.getInt("bubble_x", Math.max(0, screen.width() - size - dp(10)));
        params.y = preferences.getInt("bubble_y", screen.height() / 3);
        clamp();

        bubble = new ImageView(this);
        bubble.setImageResource(R.drawable.ic_lupa_blanca);
        bubble.setScaleType(ImageView.ScaleType.FIT_CENTER);
        bubble.setPadding(dp(12), dp(12), dp(12), dp(12));
        bubble.setElevation(dp(6));
        bubble.setContentDescription("Lupa flotante. Tocar para ampliar, arrastrar para mover, doble toque para cambiar el zoom.");
        updateBubbleColor();

        gestures = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDown(MotionEvent e) { return true; }
            @Override public boolean onSingleTapConfirmed(MotionEvent e) {
                toggleMagnification();
                return true;
            }
            @Override public boolean onDoubleTap(MotionEvent e) {
                nextScale();
                return true;
            }
        });

        bubble.setOnTouchListener((view, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    downX = event.getRawX();
                    downY = event.getRawY();
                    fromX = params.x;
                    fromY = params.y;
                    dragging = false;
                    gestures.onTouchEvent(event);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    float dx = event.getRawX() - downX;
                    float dy = event.getRawY() - downY;
                    if (!dragging && Math.hypot(dx, dy) > touchSlop) dragging = true;
                    if (dragging) {
                        params.x = fromX + Math.round(dx);
                        params.y = fromY + Math.round(dy);
                        clamp();
                        try { windowManager.updateViewLayout(bubble, params); }
                        catch (RuntimeException ignored) { }
                        long now = SystemClock.uptimeMillis();
                        if (active && now - lastUpdate > 60) {
                            applyMagnification(false);
                            lastUpdate = now;
                        }
                    } else gestures.onTouchEvent(event);
                    return true;
                case MotionEvent.ACTION_UP:
                    if (dragging) {
                        preferences.edit().putInt("bubble_x", params.x)
                            .putInt("bubble_y", params.y).apply();
                        if (active) applyMagnification(false);
                    } else gestures.onTouchEvent(event);
                    return true;
                case MotionEvent.ACTION_CANCEL:
                    dragging = false;
                    return true;
                default: return gestures.onTouchEvent(event);
            }
        });

        try { windowManager.addView(bubble, params); }
        catch (RuntimeException e) {
            bubble = null;
            params = null;
            Toast.makeText(this, "No se pudo abrir la burbuja flotante", Toast.LENGTH_LONG).show();
        }
    }

    private Rect getScreenBounds() {
        if (Build.VERSION.SDK_INT >= 30) return windowManager.getMaximumWindowMetrics().getBounds();
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getRealMetrics(metrics);
        return new Rect(0, 0, metrics.widthPixels, metrics.heightPixels);
    }

    private void clamp() {
        if (params == null) return;
        Rect screen = getScreenBounds();
        params.x = Math.max(0, Math.min(screen.width() - params.width, params.x));
        params.y = Math.max(0, Math.min(screen.height() - params.height, params.y));
    }

    private void updateBubbleColor() {
        if (bubble == null) return;
        GradientDrawable background = new GradientDrawable();
        background.setShape(GradientDrawable.OVAL);
        background.setColor(active ? Color.rgb(5, 150, 105) : Color.rgb(37, 99, 235));
        background.setStroke(dp(2), Color.WHITE);
        bubble.setBackground(background);
    }

    public void toggleMagnification() {
        if (active) {
            disableMagnification();
        } else {
            active = applyMagnification(true);
            updateBubbleColor();
        }
    }

    private boolean applyMagnification(boolean animate) {
        if (params == null || preferences == null) return false;
        float scale = preferences.getFloat(KEY_SCALE, 3f);
        float cx = params.x + params.width / 2f;
        float cy = params.y + params.height / 2f;
        if (Build.VERSION.SDK_INT < 33) {
            try {
                MagnificationController controller = getMagnificationController();
                boolean scaleOk = controller.setScale(scale, animate);
                boolean centerOk = controller.setCenter(cx, cy, animate);
                return scaleOk && centerOk;
            } catch (RuntimeException ignored) {
                return false;
            }
        }
        boolean windowSupported = getPackageManager().hasSystemFeature(PackageManager.FEATURE_WINDOW_MAGNIFICATION);
        int mode = windowSupported ? MagnificationConfig.MAGNIFICATION_MODE_WINDOW
                                   : MagnificationConfig.MAGNIFICATION_MODE_FULLSCREEN;
        boolean ok = applyMode(mode, scale, cx, cy, animate);
        if (!ok && windowSupported) {
            ok = applyMode(MagnificationConfig.MAGNIFICATION_MODE_FULLSCREEN, scale, cx, cy, animate);
        }
        if (!ok && animate) {
            Toast.makeText(this, "No se pudo activar la lupa en este dispositivo", Toast.LENGTH_LONG).show();
        }
        return ok;
    }

    private boolean applyMode(int mode, float scale, float cx, float cy, boolean animate) {
        try {
            MagnificationConfig.Builder config = new MagnificationConfig.Builder()
                .setMode(mode).setScale(scale).setCenterX(cx).setCenterY(cy);
            if (Build.VERSION.SDK_INT >= 34) config.setActivated(true);
            return getMagnificationController().setMagnificationConfig(config.build(), animate);
        } catch (RuntimeException ignored) {
            return false;
        }
    }

    private void disableMagnification() {
        if (active) {
            try {
                if (Build.VERSION.SDK_INT >= 33) getMagnificationController().resetCurrentMagnification(true);
                else getMagnificationController().reset(true);
            } catch (RuntimeException ignored) { }
        }
        active = false;
        updateBubbleColor();
    }

    public void refreshScale() { if (active) applyMagnification(false); }

    private void nextScale() {
        float current = preferences.getFloat(KEY_SCALE, 3f);
        float next = current < 2.75f ? 3f : current < 3.75f ? 4f : 2f;
        preferences.edit().putFloat(KEY_SCALE, next).apply();
        if (active) applyMagnification(true);
        Toast.makeText(this, "Zoom " + (int) next + "×", Toast.LENGTH_SHORT).show();
    }

    private void removeBubble() {
        if (bubble != null) {
            try { windowManager.removeView(bubble); }
            catch (RuntimeException ignored) { }
            bubble = null;
            params = null;
        }
    }

    private int dp(float size) {
        return Math.round(size * getResources().getDisplayMetrics().density);
    }

    @Override public boolean onUnbind(Intent intent) {
        disableMagnification();
        removeBubble();
        if (instance == this) instance = null;
        return super.onUnbind(intent);
    }

    @Override public void onDestroy() {
        disableMagnification();
        removeBubble();
        if (instance == this) instance = null;
        super.onDestroy();
    }
}
