package me.robbyblue.mylauncher.files;

import java.util.ArrayList;

import me.robbyblue.mylauncher.widgets.WidgetList;

public class Folder extends FileNode {

    String fullPath;
    ArrayList<FileNode> files;
    WidgetList widgetList;

    public Folder(String name, String fullPath) {
        super(name);
        this.fullPath = fullPath;
        this.files = new ArrayList<>();
        this.widgetList = new WidgetList();
    }

    public String getFullPath() {
        return fullPath;
    }

    public ArrayList<FileNode> getFiles() {
        return files;
    }

    public WidgetList getWidgetList() {
        return widgetList;
    }

    public void setWidgetList(WidgetList widgetList) {
        this.widgetList = widgetList;
    }

    public void setFullPath(String fullPath) {
        this.fullPath = fullPath;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Folder folder = (Folder) o;
        return getFullPath().equals(folder.getFullPath());
    }

}
