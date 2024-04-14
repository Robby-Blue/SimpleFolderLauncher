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

public class FileDataStorage {

    private static FileDataStorage instance;
    File structureFile;

    HashMap<String, Folder> files;

    private FileDataStorage(Context context) {
        structureFile = new File(context.getFilesDir(), "filesstructure.json");
        this.files = loadFilesStructure();
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

            JSONArray widgetIds = folderContentsJson.getJSONArray("widgetIds");
            for (int i = 0; i < widgetIds.length(); i++) {
                folder.getWidgetIds().add(widgetIds.getInt(i));
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

            JSONArray widgetsArray = new JSONArray();
            for (int widgetId : folder.getWidgetIds()) {
                widgetsArray.put(widgetId);
            }

            folderObject.put("widgetIds", widgetsArray);
            return folderObject;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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

    public void addWidget(String parentFolder, int appWidgetId) {
        ArrayList<Integer> widgetIds = getFolderContents(parentFolder).getWidgetIds();
        widgetIds.add(appWidgetId);
        storeFilesStructure();
    }

    public void removeWidget(String parentFolder, int appWidgetId) {
        ArrayList<Integer> widgetIds = getFolderContents(parentFolder).getWidgetIds();
        widgetIds.remove((Integer) appWidgetId);
        storeFilesStructure();
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
            System.out.println("h");
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
