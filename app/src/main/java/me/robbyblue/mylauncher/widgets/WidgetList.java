package me.robbyblue.mylauncher.widgets;

import java.util.ArrayList;

public class WidgetList extends WidgetLayout {

    ArrayList<WidgetLayout> children;

    public WidgetList(){
        super();
        this.children = new ArrayList<>();
    }

    public ArrayList<WidgetLayout> getChildren() {
        return children;
    }

    public void addChild(WidgetLayout child) {
        this.children.add(child);
    }
}
