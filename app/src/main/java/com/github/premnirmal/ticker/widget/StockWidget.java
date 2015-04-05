package com.github.premnirmal.ticker.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.widget.RemoteViews;

import com.github.premnirmal.ticker.Injector;
import com.github.premnirmal.ticker.Tools;
import com.github.premnirmal.ticker.model.IStocksProvider;
import com.github.premnirmal.ticker.ParanormalActivity;
import com.github.premnirmal.tickerwidget.R;

import javax.inject.Inject;

/**
 * Created by premnirmal on 12/21/14.
 */
public class StockWidget extends AppWidgetProvider {

    public static final String ACTION_NAME = "OPEN_APP";

    @Inject
    IStocksProvider stocksProvider;

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(ACTION_NAME)) {
            context.startActivity(new Intent(context, ParanormalActivity.class));
        }
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Injector.inject(this);
        for (final Integer widgetId : appWidgetIds) {
            final int min_width;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                final Bundle options = appWidgetManager.getAppWidgetOptions(widgetId);
                min_width = getMinWidgetWidth(options);
            } else { // TODO not sure if this works
                min_width = appWidgetManager.getAppWidgetInfo(widgetId).minWidth;
            }
            final RemoteViews remoteViews;
            if (min_width > 250) {
                remoteViews = new RemoteViews(context.getPackageName(),
                        R.layout.widget_4x1);
            } else {
                remoteViews = new RemoteViews(context.getPackageName(),
                        R.layout.widget_2x1);
            }
            updateWidget(context, appWidgetManager, widgetId, remoteViews);
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    private int getMinWidgetWidth(Bundle options) {
        if (options == null || !options.containsKey(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)) {
            return 0; // 2x1
        } else {
            return (int) options.get(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        Injector.inject(this);
        final int min_width = getMinWidgetWidth(newOptions);
        final RemoteViews remoteViews;
        if (min_width > 250) {
            remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_4x1);
        } else {
            remoteViews = new RemoteViews(context.getPackageName(),
                    R.layout.widget_2x1);
        }
        updateWidget(context, appWidgetManager, appWidgetId, remoteViews);

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
    }

    private void updateWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId, RemoteViews remoteViews) {
        remoteViews.setRemoteAdapter(R.id.list, new Intent(context, RemoteStockProviderService.class));
        final Intent startActivityIntent = new Intent(context, ParanormalActivity.class);
        final PendingIntent startActivityPendingIntent = PendingIntent.getActivity(context, 0, startActivityIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        remoteViews.setPendingIntentTemplate(R.id.list, startActivityPendingIntent);
        remoteViews.setOnClickPendingIntent(R.id.widget_layout, startActivityPendingIntent);
        remoteViews.setTextViewText(R.id.last_updated, "Last updated: " + stocksProvider.lastFetched());
        final float fontSize = Tools.getFontSize(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            remoteViews.setTextViewTextSize(R.id.last_updated, TypedValue.COMPLEX_UNIT_SP, fontSize);
        }
        appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.list);

        remoteViews.setInt(R.id.widget_layout, "setBackgroundColor", Tools.getBackgroundColor(context));
        remoteViews.setTextColor(R.id.last_updated, Tools.getTextColor());
    }
}
