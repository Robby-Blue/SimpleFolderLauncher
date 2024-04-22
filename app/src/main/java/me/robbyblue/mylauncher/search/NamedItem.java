package me.robbyblue.mylauncher.search;

import me.robbyblue.mylauncher.files.FileNode;

public class NamedItem {

    private final String name;
    private final FileNode fileNode;

    public NamedItem(String name, FileNode fileNode) {
        this.name = name.toLowerCase();
        this.fileNode = fileNode;
    }

    public String getName() {
        return name;
    }

    public FileNode getFileNode() {
        return fileNode;
    }
}
