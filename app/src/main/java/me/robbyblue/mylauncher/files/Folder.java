package me.robbyblue.mylauncher.files;

import java.util.ArrayList;

public class Folder extends FileNode{

    String fullPath;

    public Folder(String name, String fullPath){
        super(name);
        this.fullPath = fullPath;
    }

    public String getFullPath() {
        return fullPath;
    }
}
