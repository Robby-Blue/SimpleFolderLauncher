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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AppFile appFile = (AppFile) o;
        return getName().equals(appFile.getName()) && getPackageName().equals(appFile.getPackageName());
    }

}
