package me.robbyblue.mylauncher.files;

public class AppFile extends FileNode {

    private final String packageName;

    public AppFile(String name, String packageName){
        super(name);
        this.packageName = packageName;
    }

    public String getPackageName() {
        return packageName;
    }
}
