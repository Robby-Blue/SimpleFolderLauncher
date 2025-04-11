package me.robbyblue.mylauncher.widgets;

public class WidgetElement extends WidgetLayout {

    int appWidgetId;

    public WidgetElement(int appWidgetId){
        super();
        this.appWidgetId = appWidgetId;
    }

    public int getAppWidgetId() {
        return appWidgetId;
    }
}
