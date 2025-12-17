package me.robbyblue.mylauncher.search;

import me.robbyblue.mylauncher.files.FileNode;

public class NamedItem {

    private String name;
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

    public void normalizeUmlaute() {
        this.name = name.replace("ä", "a").replace("ö", "o")
                .replace("ü", "u");
    }
}
