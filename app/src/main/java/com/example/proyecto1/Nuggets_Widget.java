package com.example.proyecto1;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;
import android.widget.Toast;

public class Nuggets_Widget extends AppWidgetProvider {
    public static final String ACTION_UPDATE_WIDGET = "com.example.proyecto1.ACTION_UPDATE_WIDGET";
    public static final String ACTION_WIDGET_CLICKED = "com.example.proyecto1.ACTION_WIDGET_CLICKED";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static void actualizarWidgets(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisWidget = new ComponentName(context, Nuggets_Widget.class);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);

        for (int appWidgetId : appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId);
        }
    }

    static void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_nuggets);

        // Obtener el valor actual de nuggets
        int nuggets = Main_Activity.getNuggets();
        views.setTextViewText(R.id.widget_nuggets_value, String.valueOf(nuggets));

        // Configurar intent para actualización manual
        Intent updateIntent = new Intent(context, Nuggets_Widget.class);
        updateIntent.setAction(ACTION_UPDATE_WIDGET);
        PendingIntent updatePendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                updateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Configurar intent para cuando se pulse el widget
        Intent clickIntent = new Intent(context, Nuggets_Widget.class);
        clickIntent.setAction(ACTION_WIDGET_CLICKED);
        PendingIntent clickPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                clickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        // Asignar el PendingIntent al layout completo del widget o a un elemento específico
        views.setOnClickPendingIntent(R.id.widget_layout, clickPendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if (ACTION_UPDATE_WIDGET.equals(intent.getAction())) {
            actualizarWidgets(context);
        } else if (ACTION_WIDGET_CLICKED.equals(intent.getAction())) {
            handleWidgetClick();
        }
    }

    private void handleWidgetClick() {
        Main_Activity.simulate_click();
    }
}