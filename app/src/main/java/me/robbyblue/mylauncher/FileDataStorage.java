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

    HashMap<String, ArrayList<FileNode>> files;

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

    public HashMap<String, ArrayList<FileNode>> loadFilesStructure() {
        try {
            HashMap<String, ArrayList<FileNode>> files = new HashMap<>();
            files.put("~", new ArrayList<>());

            if (!structureFile.exists()) return files;

            String fileContents = readFile(structureFile);
            if (fileContents == null) return files;

            JSONObject jsonData = new JSONObject(fileContents);
            Iterator<String> iterator = jsonData.keys();
            while (iterator.hasNext()) {
                String folderName = iterator.next();
                JSONArray folderContentsJson = jsonData.getJSONArray(folderName);
                files.put(folderName, parseFilesStructureFolder(folderContentsJson, folderName));
            }
            return files;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private ArrayList<FileNode> parseFilesStructureFolder(JSONArray folderContentsJson, String folderName) {
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

    private JSONArray jsonifyFilesStructureFolder(ArrayList<FileNode> fileNodes) {
        try {
            JSONArray jsonArray = new JSONArray();
            for (FileNode fileNode : fileNodes) {
                JSONObject fileJson = new JSONObject();
                fileJson.put("name", fileNode.getName());
                fileJson.put("icon", fileNode.getIconData().toJson());
                if (fileNode instanceof AppFile) {
                    fileJson.put("type", "file");
                    fileJson.put("package", ((AppFile) fileNode).getPackageName());
                } else if (fileNode instanceof Folder) {
                    fileJson.put("type", "folder");
                }
                jsonArray.put(fileJson);
            }
            return jsonArray;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void createFile(String parentFolder, AppFile appFile) {
        ArrayList<FileNode> parentFolderContents = getFolderContents(parentFolder);
        parentFolderContents.add(appFile);
        storeFilesStructure();
    }

    public void createFolder(String parentFolder, String name) {
        ArrayList<FileNode> parentFolderContents = getFolderContents(parentFolder);
        String fullPath = parentFolder + "/" + name;
        if (files.containsKey(fullPath)) return;
        parentFolderContents.add(new Folder(name, fullPath));
        files.put(fullPath, new ArrayList<>());
        storeFilesStructure();
    }

    public void removeFile(String parentFolder, int fileIndex) {
        ArrayList<FileNode> contents = getFolderContents(parentFolder);
        FileNode item = contents.get(fileIndex);
        if (item instanceof Folder) {
            // its a folder, remove not just the reference but also the folder itself
            files.remove(((Folder) item).getFullPath());
        }
        contents.remove(fileIndex);
        storeFilesStructure();
    }

    public void moveFile(String parentFolder, int initialIndex, int moveIndex) {
        ArrayList<FileNode> contents = getFolderContents(parentFolder);
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

    public ArrayList<FileNode> getFolderContents(Folder folder) {
        return getFolderContents(folder.getFullPath());
    }

    public ArrayList<FileNode> getFolderContents(String folder) {
        if (files.containsKey(folder)) return files.get(folder);
        return new ArrayList<>();
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
