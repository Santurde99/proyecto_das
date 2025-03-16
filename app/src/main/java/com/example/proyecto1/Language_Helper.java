package com.example.proyecto1;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import java.util.Locale;

public class Language_Helper {
    public static void setLocale(Context context, String languageCode) {


        Locale newLocale = new Locale(languageCode);
        Locale.setDefault(newLocale);

        Resources resources = context.getResources();
        Configuration config = resources.getConfiguration();
        config.setLocale(newLocale);
        context.getResources().updateConfiguration(config, resources.getDisplayMetrics());

        // Guardar el idioma en SharedPreferences para recordar la selección
        context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
                .edit()
                .putString("App_Language", languageCode)
                .apply();
    }

    public static void loadLocale(Context context) {
        String language = context.getSharedPreferences("Settings", Context.MODE_PRIVATE)
                .getString("App_Language", Locale.getDefault().getLanguage());
        setLocale(context, language);
    }
}

