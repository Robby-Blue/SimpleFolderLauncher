package me.robbyblue.mylauncher.widgets;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.util.HashMap;

public class WidgetSystem {

    static boolean showOutlines;
    static AppWidgetHost appWidgetHost;

    public static HashMap<WidgetLayout, FrameLayout> createLayout(WidgetList widgets, LinearLayout container, boolean hasOutlines) {
        return createLayout(widgets, container, hasOutlines, null);
    }

    public static HashMap<WidgetLayout, FrameLayout> createLayout(WidgetList widgets, LinearLayout container, boolean hasOutlines, AppWidgetHost host) {
        showOutlines = hasOutlines;
        appWidgetHost = host;

        container.removeAllViewsInLayout();
        container.setClickable(true);
        container.setFocusable(true);
        HashMap<WidgetLayout, FrameLayout> layouts = new HashMap<>();

        for (WidgetLayout widget : widgets.getChildren()) {
            if (widget instanceof WidgetElement) {
                FrameLayout layout = addTopLevelWidget((WidgetElement) widget, container);
                layouts.put(widget, layout);
            }
            if (widget instanceof WidgetList) {
                addRow((WidgetList) widget, container, layouts);
            }
        }

        return layouts;
    }

    private static FrameLayout addTopLevelWidget(WidgetElement widget, LinearLayout container) {
        Context ctx = container.getContext();
        FrameLayout wrapper = new FrameLayout(ctx);

        DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;

        double sizeValue = widget.getSize();
        double sizePercent = Math.max(0.05, (Math.min(sizeValue, 1.0)));

        int height = (int) (screenWidth * sizePercent);
        if (height < 50) height = 50;

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(screenWidth, height);
        wrapper.setLayoutParams(layoutParams);
        wrapper.setPadding(8, 8, 8, 8);

        if (!showOutlines && appWidgetHost != null) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ctx);
            AppWidgetProviderInfo appWidgetInfo = appWidgetManager.getAppWidgetInfo(widget.getAppWidgetId());
            if (appWidgetInfo != null) {
                AppWidgetHostView hostView = appWidgetHost.createView(ctx, widget.getAppWidgetId(), appWidgetInfo);
                hostView.setAppWidget(widget.getAppWidgetId(), appWidgetInfo);
                wrapper.addView(hostView);
            }
        }

        if (showOutlines) {
            wrapper.setBackground(createOutline(Color.MAGENTA));
            View overlay = new View(ctx);
            overlay.setBackgroundColor(0x4000FF00);
            wrapper.addView(overlay);
        }

        wrapper.setClickable(true);
        wrapper.setFocusable(true);
        wrapper.setFocusableInTouchMode(true);

        wrapper.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == android.view.MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return false;
        });

        container.addView(wrapper);
        return wrapper;
    }

    private static void addRow(WidgetList widget, LinearLayout container, HashMap<WidgetLayout, FrameLayout> layouts) {
        Context ctx = container.getContext();
        FrameLayout wrapper = new FrameLayout(ctx);

        DisplayMetrics metrics = ctx.getResources().getDisplayMetrics();
        int screenWidth = metrics.widthPixels;

        double sizeValue = widget.getSize();
        double sizePercent = Math.max(0.05, (Math.min(sizeValue, 1.0)));

        int height = (int) (screenWidth * sizePercent);
        if (height < 50) height = 50;

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(screenWidth, height);
        wrapper.setLayoutParams(layoutParams);

        if (showOutlines) {
            wrapper.setBackground(createOutline(Color.BLUE));
            View overlay = new View(ctx);
            overlay.setBackgroundColor(0x400000FF);
            wrapper.addView(overlay);
        }

        wrapper.setClickable(true);
        wrapper.setFocusable(true);
        wrapper.setFocusableInTouchMode(true);

        wrapper.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == android.view.MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return false;
        });

        LinearLayout rowContent = new LinearLayout(ctx);
        rowContent.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        rowContent.setOrientation(LinearLayout.HORIZONTAL);

        for (WidgetLayout child : widget.getChildren()) {
            addWidgetInRow(child, rowContent, layouts, screenWidth, height);
        }

        wrapper.addView(rowContent);

        container.addView(wrapper);
        layouts.put(widget, wrapper);
    }

    private static void addWidgetInRow(WidgetLayout widget, LinearLayout container, HashMap<WidgetLayout, FrameLayout> layouts, int parentWidth, int parentHeight) {
        Context ctx = container.getContext();
        FrameLayout wrapper = new FrameLayout(ctx);

        double sizeValue = widget.getSize();
        double sizePercent = Math.max(0.05, (Math.min(sizeValue, 1.0)));

        int width = (int) (parentWidth * sizePercent);
        if (width < 20) width = 20;

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, parentHeight);
        wrapper.setLayoutParams(layoutParams);

        if (!showOutlines && widget instanceof WidgetElement && appWidgetHost != null) {
            WidgetElement element = (WidgetElement) widget;
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ctx);
            AppWidgetProviderInfo appWidgetInfo = appWidgetManager.getAppWidgetInfo(element.getAppWidgetId());

            if (appWidgetInfo != null) {
                AppWidgetHostView hostView = appWidgetHost.createView(ctx, element.getAppWidgetId(), appWidgetInfo);
                hostView.setAppWidget(element.getAppWidgetId(), appWidgetInfo);
                wrapper.addView(hostView);
            }
        }

        if (showOutlines) {
            wrapper.setBackground(createOutline(Color.GREEN));
            View overlay = new View(ctx);
            overlay.setBackgroundColor(0x4000FF00);
            wrapper.addView(overlay);
        }

        wrapper.setClickable(true);
        wrapper.setFocusable(true);
        wrapper.setFocusableInTouchMode(true);

        wrapper.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == android.view.MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return false;
        });

        container.addView(wrapper);
        layouts.put(widget, wrapper);
    }

    private static GradientDrawable createOutline(int color) {
        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.TRANSPARENT);
        border.setStroke(4, color);
        border.setCornerRadius(16f);
        return border;
    }

}
