package cl.canelillo.lupaflotante;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends Activity {
    private static final int AZUL = Color.rgb(37, 99, 235);
    private static final int TEXTO = Color.rgb(25, 34, 49);
    private SharedPreferences preferences;
    private TextView status;
    private TextView level;

    @Override public void onCreate(Bundle saved) {
        super.onCreate(saved);
        preferences = getSharedPreferences(LupaAccessibilityService.PREFS, MODE_PRIVATE);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(246, 248, 253));
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dp(24), dp(32), dp(24), dp(32));
        scroll.addView(layout);
        setContentView(scroll);

        layout.addView(text("🔎  LUPA FLOTANTE", 15, AZUL, true));
        gap(layout, 20);
        layout.addView(text("Amplía lo que estás viendo", 28, TEXTO, true));
        gap(layout, 12);
        layout.addView(text("Un botón flotante sobre otras aplicaciones. Tócalo para ampliar, arrástralo para mover y haz doble toque para cambiar el aumento.", 16, Color.DKGRAY, false));
        gap(layout, 24);
        status = text("Verificando accesibilidad…", 17, TEXTO, true);
        layout.addView(status);
        gap(layout, 10);

        Button accessibility = button("Activar en Accesibilidad", true);
        accessibility.setOnClickListener(v -> startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)));
        layout.addView(accessibility);
        gap(layout, 24);

        Switch show = new Switch(this);
        show.setText("Mostrar burbuja flotante");
        show.setTextSize(17);
        show.setTextColor(TEXTO);
        show.setChecked(preferences.getBoolean(LupaAccessibilityService.KEY_SHOW, true));
        show.setOnCheckedChangeListener((v, checked) -> {
            preferences.edit().putBoolean(LupaAccessibilityService.KEY_SHOW, checked).apply();
            if (LupaAccessibilityService.instance != null) LupaAccessibilityService.instance.updateBubbleVisibility();
        });
        layout.addView(show);
        gap(layout, 24);

        level = text("Aumento: 3×", 17, TEXTO, true);
        layout.addView(level);
        SeekBar seek = new SeekBar(this);
        seek.setMax(6);
        seek.setProgress(Math.round((preferences.getFloat(LupaAccessibilityService.KEY_SCALE, 3f) - 2f) * 2));
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int value, boolean fromUser) {
                float scale = 2f + value * 0.5f;
                level.setText("Aumento: " + String.format(java.util.Locale.forLanguageTag("es-CL"), "%.1f", scale) + "×");
                if (fromUser) {
                    preferences.edit().putFloat(LupaAccessibilityService.KEY_SCALE, scale).apply();
                    if (LupaAccessibilityService.instance != null) LupaAccessibilityService.instance.refreshScale();
                }
            }
            @Override public void onStartTrackingTouch(SeekBar bar) { }
            @Override public void onStopTrackingTouch(SeekBar bar) { }
        });
        layout.addView(seek);
        gap(layout, 14);

        Button test = button("Probar lupa", false);
        test.setOnClickListener(v -> {
            if (LupaAccessibilityService.instance != null) LupaAccessibilityService.instance.toggleMagnification();
            else startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        });
        layout.addView(test);
        gap(layout, 26);
        layout.addView(text("CÓMO USARLA", 17, AZUL, true));
        gap(layout, 12);
        layout.addView(text("• Un toque: encender o apagar la lupa.\n• Arrastrar: cambiar de lugar.\n• Doble toque: alternar 2×, 3× y 4×.\n• Pellizcar dentro de la lupa: si el teléfono lo permite.", 16, TEXTO, false));
        gap(layout, 22);
        layout.addView(text("Privacidad: la aplicación no lee mensajes, no toma capturas y no requiere Internet. El permiso de accesibilidad se usa solo para mostrar la burbuja y controlar el zoom.", 13, Color.DKGRAY, false));
    }

    @Override protected void onResume() {
        super.onResume();
        if (status != null) {
            boolean enabled = LupaAccessibilityService.instance != null;
            status.setText(enabled ? "● Lupa activada" : "● Falta activar el permiso de accesibilidad");
            status.setTextColor(enabled ? Color.rgb(4, 120, 87) : Color.rgb(174, 100, 7));
        }
    }

    private int dp(float value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private void gap(LinearLayout layout, int height) {
        View space = new View(this);
        layout.addView(space, new LinearLayout.LayoutParams(1, dp(height)));
    }
    private TextView text(String value, int size, int color, boolean strong) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        view.setLineSpacing(dp(3), 1f);
        if (strong) view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        return view;
    }
    private Button button(String label, boolean primary) {
        Button view = new Button(this);
        view.setText(label);
        view.setAllCaps(false);
        view.setTextSize(16);
        view.setTextColor(primary ? Color.WHITE : AZUL);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(primary ? AZUL : Color.rgb(227, 237, 255));
        bg.setCornerRadius(dp(14));
        view.setBackground(bg);
        view.setMinHeight(dp(54));
        return view;
    }
}
