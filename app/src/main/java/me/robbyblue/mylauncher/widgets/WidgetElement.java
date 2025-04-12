package me.robbyblue.mylauncher.widgets;

public class WidgetElement extends WidgetLayout {

    int appWidgetId;

    public WidgetElement(int appWidgetId, int size){
        super();
        this.appWidgetId = appWidgetId;
        this.size = size;
    }

    public WidgetElement(int appWidgetId){
        super();
        this.appWidgetId = appWidgetId;
    }

    public int getAppWidgetId() {
        return appWidgetId;
    }
}
