package me.robbyblue.mylauncher;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import me.robbyblue.mylauncher.files.AppFile;
import me.robbyblue.mylauncher.files.FileNode;
import me.robbyblue.mylauncher.files.Folder;
import me.robbyblue.mylauncher.files.icons.IconData;
import me.robbyblue.mylauncher.widgets.WidgetElement;
import me.robbyblue.mylauncher.widgets.WidgetLayout;
import me.robbyblue.mylauncher.widgets.WidgetList;

public class FileDataStorage {

    private static FileDataStorage instance;
    File structureFile;

    HashMap<String, Folder> files;

    private FileDataStorage(Context context) {
        structureFile = new File(context.getFilesDir(), "filesstructure.json");
        this.files = loadFilesStructure();
    }

    public static FileDataStorage getInstance() {
        return instance;
    }

    public static FileDataStorage getInstance(Context context) {
        if (instance == null) {
            instance = new FileDataStorage(context);
        }
        return instance;
    }

    public HashMap<String, Folder> loadFilesStructure() {
        try {
            HashMap<String, Folder> files = new HashMap<>();
            files.put("~", new Folder("~", "~"));

            if (!structureFile.exists()) return files;

            String fileContents = readFile(structureFile);
            if (fileContents == null) return files;

            JSONObject jsonData = new JSONObject(fileContents);
            Iterator<String> iterator = jsonData.keys();
            while (iterator.hasNext()) {
                String folderName = iterator.next();
                if (jsonData.get(folderName) instanceof JSONObject) {
                    JSONObject folderContentsJson = jsonData.getJSONObject(folderName);
                    files.put(folderName, parseFolder(folderContentsJson, folderName));
                } else {
                    JSONArray folderContentsJson = jsonData.getJSONArray(folderName);

                    Folder folder = new Folder(folderName, folderName);
                    folder.getFiles().addAll(parseFilesList(folderContentsJson, folderName));
                    files.put(folderName, folder);
                }
            }
            return files;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private Folder parseFolder(JSONObject folderContentsJson, String folderName) {
        try {
            Folder folder = new Folder(folderName, folderName);
            folder.getFiles().addAll(parseFilesList(folderContentsJson.getJSONArray("files"), folderName));

            if (folderContentsJson.has("widgetIds")) {
                JSONArray widgetIds = folderContentsJson.getJSONArray("widgetIds");
                for (int i = 0; i < widgetIds.length(); i++) {
                    WidgetElement widgetElement = new WidgetElement(widgetIds.getInt(i));
                    folder.getWidgetList().addChild(widgetElement);
                }
            } else if (folderContentsJson.has("widgets")) {
                folder.setWidgetList(parseWidgetList(folderContentsJson.getJSONObject("widgets")));
            }
            return folder;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ArrayList<FileNode> parseFilesList(JSONArray folderContentsJson, String folderName) {
        try {
            ArrayList<FileNode> files = new ArrayList<>();
            for (int i = 0; i < folderContentsJson.length(); i++) {
                JSONObject fileNodeJson = folderContentsJson.getJSONObject(i);
                String name = fileNodeJson.getString("name");
                if (fileNodeJson.getString("type").equals("file")) {
                    String packageName = fileNodeJson.getString("package");
                    if (AppsListCache.getInstance().getAppByPackage(packageName) == null)
                        continue;
                    IconData iconData = IconData.createIconDataFromJson(fileNodeJson);
                    files.add(new AppFile(name, packageName, iconData));
                } else {
                    files.add(new Folder(name, folderName + "/" + name));
                }
            }
            return files;
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private WidgetList parseWidgetList(JSONObject jsonObject) {
        WidgetList widgetList = new WidgetList();

        try {
            widgetList.setSize(jsonObject.getDouble("size"));

            JSONArray childrenArray = jsonObject.getJSONArray("children");
            for (int i = 0; i < childrenArray.length(); i++) {
                JSONObject child = childrenArray.getJSONObject(i);

                String type = child.getString("type");

                if (type.equals("widget")) {
                    WidgetElement element = parseWidgetElement(child);
                    widgetList.addChild(element);
                } else if (type.equals("list")) {
                    WidgetList nestedList = parseWidgetList(child);
                    widgetList.addChild(nestedList);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return widgetList;
    }

    private WidgetElement parseWidgetElement(JSONObject jsonObject) {
        try {
            WidgetElement widgetElement = new WidgetElement(jsonObject.getInt("widgetId"));
            widgetElement.setSize(jsonObject.getDouble("size"));
            return widgetElement;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void storeFilesStructure() {
        try {
            JSONObject jsonData = new JSONObject();
            for (String folderName : files.keySet()) {
                jsonData.put(folderName, jsonifyFilesStructureFolder(files.get(folderName)));
            }
            writeFile(structureFile, jsonData.toString(2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private JSONObject jsonifyFilesStructureFolder(Folder folder) {
        try {
            JSONObject folderObject = new JSONObject();
            JSONArray filesArray = new JSONArray();
            for (FileNode fileNode : folder.getFiles()) {
                JSONObject fileJson = new JSONObject();
                fileJson.put("name", fileNode.getName());
                fileJson.put("icon", fileNode.getIconData().toJson());
                if (fileNode instanceof AppFile) {
                    fileJson.put("type", "file");
                    fileJson.put("package", ((AppFile) fileNode).getPackageName());
                } else if (fileNode instanceof Folder) {
                    fileJson.put("type", "folder");
                }
                filesArray.put(fileJson);
            }
            folderObject.put("files", filesArray);

            JSONObject widgetsArray = jsonifyWidgetList(folder.getWidgetList());

            folderObject.put("widgets", widgetsArray);
            return folderObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private JSONObject jsonifyWidgetList(WidgetList widgetList) {
        JSONArray childrenArray = new JSONArray();
        for (WidgetLayout widgetLayout : widgetList.getChildren()) {
            if (widgetLayout instanceof WidgetElement) {
                childrenArray.put(jsonifyWidget((WidgetElement) widgetLayout));
            }
            if (widgetLayout instanceof WidgetList) {
                childrenArray.put(jsonifyWidgetList((WidgetList) widgetLayout));
            }
        }

        JSONObject widgetObject = new JSONObject();
        try {
            widgetObject.put("type", "list");
            widgetObject.put("size", widgetList.getSize());
            widgetObject.put("children", childrenArray);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return widgetObject;
    }

    private JSONObject jsonifyWidget(WidgetElement widgetElement) {
        JSONObject widgetObject = new JSONObject();
        try {
            widgetObject.put("type", "widget");
            widgetObject.put("size", widgetElement.getSize());
            widgetObject.put("widgetId", widgetElement.getAppWidgetId());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return widgetObject;
    }

    public void createFile(String parentFolder, AppFile appFile) {
        ArrayList<FileNode> parentFolderContents = getFolderContents(parentFolder).getFiles();
        parentFolderContents.add(appFile);
        storeFilesStructure();
    }

    public void createFolder(String parentFolder, String name) {
        ArrayList<FileNode> parentFolderContents = getFolderContents(parentFolder).getFiles();
        String fullPath = parentFolder + "/" + name;
        if (files.containsKey(fullPath)) return;
        parentFolderContents.add(new Folder(name, fullPath));
        files.put(fullPath, new Folder(name, fullPath));
        storeFilesStructure();
    }

    public void renameFile(String parentFolder, int fileIndex, String newName) {
        ArrayList<FileNode> contents = getFolderContents(parentFolder).getFiles();
        FileNode item = contents.get(fileIndex);
        item.setName(newName);
        if (item instanceof Folder) {
            // fix full path of folder and its subfolders
            Folder folder = ((Folder) item);
            String oldPath = folder.getFullPath();
            String newPath = oldPath.substring(0, oldPath.lastIndexOf("/") + 1) + newName;

            updateFolderPath(folder, oldPath, newPath);
        }
        storeFilesStructure();
    }

    private void updateFolderPath(Folder folder, String oldPath, String newPath) {
        String subfolderPath = folder.getFullPath();
        boolean shouldRename = subfolderPath.equals(oldPath) || subfolderPath.startsWith(oldPath + "/");
        if (!shouldRename)
            return;

        Folder folderContents = getFolderContents(folder);
        for (FileNode fileNode : folderContents.getFiles()) {
            if (!(fileNode instanceof Folder))
                continue;
            Folder subfolder = (Folder) fileNode;
            updateFolderPath(subfolder, oldPath, newPath);
        }

        folder.setFullPath(folder.getFullPath().replace(oldPath, newPath));

        files.put(folder.getFullPath(), folderContents);
        files.remove(subfolderPath);
    }

    public void removeFile(String parentFolder, int fileIndex) {
        ArrayList<FileNode> contents = getFolderContents(parentFolder).getFiles();
        FileNode item = contents.get(fileIndex);
        if (item instanceof Folder) {
            // its a folder, remove not just the reference but also the folder itself
            files.remove(((Folder) item).getFullPath());
        }
        contents.remove(fileIndex);
        storeFilesStructure();
    }

    public void moveFile(String parentFolder, int initialIndex, int moveIndex) {
        ArrayList<FileNode> contents = getFolderContents(parentFolder).getFiles();
        if (moveIndex < 0 || moveIndex >= contents.size()) return;
        FileNode item = contents.get(initialIndex);
        contents.remove(initialIndex);
        contents.add(moveIndex, item);
        storeFilesStructure();
    }

    public ArrayList<Folder> getFolders() {
        ArrayList<Folder> folders = new ArrayList<>();
        for (String folderPath : files.keySet()) {
            String[] parts = folderPath.split("/");
            String folderName = parts[parts.length - 1];
            folders.add(new Folder(folderName, folderPath));
        }
        return folders;
    }

    public Folder getFolderContents(Folder folder) {
        return getFolderContents(folder.getFullPath());
    }

    public Folder getFolderContents(String folder) {
        if (files.containsKey(folder)) return files.get(folder);
        return new Folder("null", "/dev/null");
    }

    private String readFile(File file) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

            StringBuilder res = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                res.append(line);
            }

            return res.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private void writeFile(File file, String content) {
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
