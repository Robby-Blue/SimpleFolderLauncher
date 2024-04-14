package me.robbyblue.mylauncher.files;

import java.util.ArrayList;

public class Folder extends FileNode{

    String fullPath;
    ArrayList<FileNode> files;
    ArrayList<Integer> widgetIds;

    public Folder(String name, String fullPath){
        super(name);
        this.fullPath = fullPath;
        this.files = new ArrayList<>();
        this.widgetIds = new ArrayList<>();
    }

    public String getFullPath() {
        return fullPath;
    }

    public ArrayList<FileNode> getFiles() {
        return files;
    }

    public ArrayList<Integer> getWidgetIds() {
        return widgetIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Folder folder = (Folder) o;
        return getFullPath().equals(folder.getFullPath());
    }

}
