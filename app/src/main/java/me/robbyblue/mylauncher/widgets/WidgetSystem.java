package me.robbyblue.mylauncher.widgets;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.widget.LinearLayout;

import java.util.HashMap;

public class WidgetSystem {

    static boolean showOutlines;

    public static HashMap<WidgetLayout, LinearLayout> createLayout(WidgetList widgets, LinearLayout container, boolean hasOutlines) {
        showOutlines = hasOutlines;

        container.removeAllViewsInLayout();
        HashMap<WidgetLayout, LinearLayout> layouts = new HashMap<>();

        for (WidgetLayout widget : widgets.getChildren()) {
            if (widget instanceof WidgetElement) {
                LinearLayout layout = addTopLevelWidget((WidgetElement) widget, container);
                layouts.put(widget, layout);
            }
            if (widget instanceof WidgetList) {
                addRow((WidgetList) widget, container, layouts);
            }
        }

        return layouts;
    }

    private static LinearLayout addTopLevelWidget(WidgetElement widget, LinearLayout container) {
        Context ctx = container.getContext();
        LinearLayout childLayout = new LinearLayout(ctx);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ctx);
        AppWidgetProviderInfo appWidgetInfo = appWidgetManager.getAppWidgetInfo(((WidgetElement) widget).getAppWidgetId());
        int minWidth = appWidgetInfo.minWidth;
        int minHeight = appWidgetInfo.minHeight;

        int screenWidth = container.getWidth();
        int height = (int) (screenWidth * minHeight / minWidth);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(screenWidth, height);
        childLayout.setLayoutParams(layoutParams);

        if (showOutlines) {
            childLayout.setBackground(createOutline(Color.MAGENTA));
        }

        childLayout.setGravity(Gravity.CENTER);

        container.addView(childLayout);
        return childLayout;
    }

    private static void addRow(WidgetList widget, LinearLayout container, HashMap<WidgetLayout, LinearLayout> layouts) {
        Context ctx = container.getContext();
        LinearLayout childLayout = new LinearLayout(ctx);

        int screenWidth = container.getWidth();
        int height = (int) (screenWidth * widget.getSize());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(screenWidth, height);
        childLayout.setLayoutParams(layoutParams);

        if (showOutlines) {
            childLayout.setBackground(createOutline(Color.BLUE));
        }

        for (WidgetLayout child : widget.getChildren()) {
            addWidgetInRow(child, childLayout, layouts, screenWidth, height);
        }

        container.addView(childLayout);
    }

    private static void addWidgetInRow(WidgetLayout widget, LinearLayout container, HashMap<WidgetLayout, LinearLayout> layouts, int parentWidth, int parentHeight) {
        Context ctx = container.getContext();
        LinearLayout childLayout = new LinearLayout(ctx);

        int width = (int) (parentWidth * widget.getSize());

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, parentHeight);
        childLayout.setLayoutParams(layoutParams);

        if (showOutlines) {
            childLayout.setBackground(createOutline(Color.GREEN));
        }

        container.addView(childLayout);
        layouts.put(widget, childLayout);
    }

    private static GradientDrawable createOutline(int color) {
        GradientDrawable border = new GradientDrawable();
        border.setColor(Color.TRANSPARENT);
        border.setStroke(4, color);
        border.setCornerRadius(16f);
        return border;
    }

}
