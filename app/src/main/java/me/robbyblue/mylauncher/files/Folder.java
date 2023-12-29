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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Folder folder = (Folder) o;
        return getName().equals(folder.getName()) && getFullPath().equals(folder.getFullPath());
    }

}
