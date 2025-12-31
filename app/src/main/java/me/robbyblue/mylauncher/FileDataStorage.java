package me.robbyblue.mylauncher;

import android.content.Context;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import me.robbyblue.mylauncher.files.AppFile;
import me.robbyblue.mylauncher.files.FileNode;
import me.robbyblue.mylauncher.files.Folder;
import me.robbyblue.mylauncher.files.icons.IconData;
import me.robbyblue.mylauncher.widgets.WidgetElement;
import me.robbyblue.mylauncher.widgets.WidgetLayout;
import me.robbyblue.mylauncher.widgets.WidgetList;

public class FileDataStorage {

    private Context context;
    private static FileDataStorage instance;
    File structureFile;

    HashMap<String, Folder> files;

    private FileDataStorage() {
    }

    private FileDataStorage(File structureFile, Context context) {
        this.structureFile = structureFile;
        this.context = context;
        if (this.structureFile != null && this.structureFile.exists()) {
            try {
                this.loadFromInputStream(new FileInputStream(this.structureFile));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            this.loadFromInputStream(null);
        }
    }

    private FileDataStorage(InputStream inputStream, Context context) {
        this.structureFile = null;
        this.context = context;
        this.loadFromInputStream(inputStream);
    }

    public static FileDataStorage getInstanceAssumeExists() {
        return instance;
    }

    public static FileDataStorage getInstanceOrCreate() {
        if (instance == null) {
            instance = new FileDataStorage();
        }
        return instance;
    }

    public static FileDataStorage getInstance() throws NotInitializedException {
        if (instance == null) {
            throw new NotInitializedException("AppListCache not loaded");
        }
        return instance;
    }

    public static FileDataStorage getInstance(File file, Context context) {
        if (instance == null) {
            instance = new FileDataStorage(file, context);
        }
        return instance;
    }

    public static FileDataStorage getNewInstance(InputStream inputStream, Context context) {
        return new FileDataStorage(inputStream, context);
    }

    public void loadFromInputStream(InputStream inputStream) {
        try {
            String fileContents = readInputStream(inputStream);
            if (fileContents == null) {
                return;
            }

            JSONObject jsonData = new JSONObject(fileContents);
            loadFromJson(jsonData);
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public void loadFromJson(JSONObject jsonData) {
        try {
            HashMap<String, Folder> files = new HashMap<>();
            files.put("~", new Folder("~", "~"));

            Iterator<String> iterator = jsonData.keys();
            while (iterator.hasNext()) {
                String folderName = iterator.next();
                if (jsonData.get(folderName) instanceof JSONObject) {
                    try {
                        JSONObject folderContentsJson = jsonData.getJSONObject(folderName);
                        files.put(folderName, parseFolder(folderContentsJson, folderName));
                    } catch (Exception e) {

                    }
                } else {
                    try {
                        JSONArray folderContentsJson = jsonData.getJSONArray(folderName);
                        Folder folder = new Folder(folderName, folderName);
                        folder.getFiles().addAll(parseFilesList(folderContentsJson, folderName));
                        files.put(folderName, folder);
                    } catch (Exception e) {

                    }
                }
            }

            this.files = files;
            deleteBadFolders();
            clearOrphans();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void clearOrphans() {
        ArrayList<String> usedPaths = new ArrayList<>();
        Queue<String> uncheckedPaths = new LinkedList<>();

        usedPaths.add("~");
        uncheckedPaths.add("~");

        while (uncheckedPaths.size() > 0) {
            String testFullPath = uncheckedPaths.remove();
            for (FileNode file : getFolderContents(testFullPath).getFiles()) {
                if (!(file instanceof Folder)) continue;
                String fullPath = ((Folder) file).getFullPath();
                usedPaths.add(fullPath);
                uncheckedPaths.add(fullPath);
            }
        }

        files.keySet().removeIf(pathKey -> !usedPaths.contains(pathKey));
    }

    /**
     * caused by some bug, it would happen that a folder links
     * to child while said child doesn't exist anymore.
     * this function deletes all those folder
     */
    private void deleteBadFolders() {
        for (Folder folder : files.values()) {
            ArrayList<FileNode> folderFiles = folder.getFiles();
            Iterator<FileNode> iterator = folderFiles.iterator();
            while (iterator.hasNext()) {
                FileNode fileNode = iterator.next();
                if (!(fileNode instanceof Folder)) {
                    continue;
                }
                String filePath = ((Folder) fileNode).getFullPath();
                if (files.containsKey(filePath)) {
                    continue;
                }
                iterator.remove();
            }
        }
    }

    private Folder parseFolder(JSONObject folderContentsJson, String folderName) throws JSONException {
        Folder folder = new Folder(folderName, folderName);
        try {
            folder.getFiles().addAll(parseFilesList(folderContentsJson.getJSONArray("files"), folderName));
        } catch (Exception e) {

        }

        if (folderContentsJson.has("widgetIds")) {
            JSONArray widgetIds = folderContentsJson.getJSONArray("widgetIds");
            for (int i = 0; i < widgetIds.length(); i++) {
                WidgetElement widgetElement = new WidgetElement(widgetIds.getInt(i));
                folder.getWidgetList().addChild(widgetElement);
            }
        } else if (folderContentsJson.has("widgets")) {
            try {
                folder.setWidgetList(parseWidgetList(folderContentsJson.getJSONObject("widgets")));
            } catch (Exception e) {

            }
        }
        return folder;
    }

    private ArrayList<FileNode> parseFilesList(JSONArray folderContentsJson, String folderName) throws JSONException {
        ArrayList<String> folderNames = new ArrayList<>();
        ArrayList<FileNode> files = new ArrayList<>();
        for (int i = 0; i < folderContentsJson.length(); i++) {
            try {
                JSONObject fileNodeJson = folderContentsJson.getJSONObject(i);
                String name = fileNodeJson.getString("name");
                if (!FileNode.isValidName(name)) {
                    continue;
                }
                if (fileNodeJson.getString("type").equals("file")) {
                    String packageName = fileNodeJson.getString("package");

                    UserHandle user;

                    if (fileNodeJson.has("userHandleSerialNumber")) {
                        UserManager manager = (UserManager) context.getSystemService(Context.USER_SERVICE);
                        user = manager.getUserForSerialNumber(fileNodeJson.getLong("userHandleSerialNumber"));
                    } else {
                        user = Process.myUserHandle();
                    }

                    IconData iconData = IconData.createIconDataFromJson(fileNodeJson);
                    files.add(new AppFile(name, packageName, iconData, user));
                } else {
                    if (folderNames.contains(name)) continue;
                    files.add(new Folder(name, folderName + "/" + name));
                    folderNames.add(name);
                }
            }catch (Exception e){

            }
        }
        return files;
    }

    private WidgetList parseWidgetList(JSONObject jsonObject) throws JSONException {
        WidgetList widgetList = new WidgetList();

        widgetList.setSize(jsonObject.getDouble("size"));

        JSONArray childrenArray = jsonObject.getJSONArray("children");
        for (int i = 0; i < childrenArray.length(); i++) {
            try {
                JSONObject child = childrenArray.getJSONObject(i);

                String type = child.getString("type");

                if (type.equals("widget")) {
                    WidgetElement element = parseWidgetElement(child);
                    widgetList.addChild(element);
                } else if (type.equals("list")) {
                    WidgetList nestedList = parseWidgetList(child);
                    widgetList.addChild(nestedList);
                }
            } catch (Exception e) {

            }
        }

        return widgetList;
    }

    private WidgetElement parseWidgetElement(JSONObject jsonObject) throws JSONException {
        WidgetElement widgetElement = new WidgetElement(jsonObject.getInt("widgetId"));
        widgetElement.setSize(jsonObject.getDouble("size"));
        return widgetElement;
    }

    public void storeFilesStructure() {
        try {
            JSONObject jsonData = getFileStructureAsJson();
            writeFile(structureFile, jsonData.toString(2));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public JSONObject getFileStructureAsJson() {
        try {
            JSONObject jsonData = new JSONObject();
            for (String folderName : files.keySet()) {
                jsonData.put(folderName, jsonifyFilesStructureFolder(files.get(folderName)));
            }
            return jsonData;
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    private JSONObject jsonifyFilesStructureFolder(Folder folder) throws JSONException {
        JSONObject folderObject = new JSONObject();
        JSONArray filesArray = new JSONArray();
        for (FileNode fileNode : folder.getFiles()) {
            JSONObject fileJson = new JSONObject();
            fileJson.put("name", fileNode.getName());
            fileJson.put("icon", fileNode.getIconData().toJson());
            if (fileNode instanceof AppFile) {
                UserManager manager = (UserManager) context.getSystemService(Context.USER_SERVICE);
                long serialNumber = manager.getSerialNumberForUser(((AppFile) fileNode).getUser());

                fileJson.put("type", "file");
                fileJson.put("package", ((AppFile) fileNode).getPackageName());
                fileJson.put("userHandleSerialNumber", serialNumber);
            } else if (fileNode instanceof Folder) {
                fileJson.put("type", "folder");
            }
            filesArray.put(fileJson);
        }
        folderObject.put("files", filesArray);

        try {
            JSONObject widgetsArray = jsonifyWidgetList(folder.getWidgetList());
            folderObject.put("widgets", widgetsArray);
        } catch (Exception e) {

        }

        return folderObject;
    }

    private JSONObject jsonifyWidgetList(WidgetList widgetList) throws JSONException {
        JSONArray childrenArray = new JSONArray();
        for (WidgetLayout widgetLayout : widgetList.getChildren()) {
            if (widgetLayout instanceof WidgetElement) {
                try {
                    childrenArray.put(jsonifyWidget((WidgetElement) widgetLayout));
                } catch (Exception e) {

                }
            }
            if (widgetLayout instanceof WidgetList) {
                try {
                    childrenArray.put(jsonifyWidgetList((WidgetList) widgetLayout));
                } catch (Exception e) {

                }
            }
        }

        JSONObject widgetObject = new JSONObject();
        widgetObject.put("type", "list");
        widgetObject.put("size", widgetList.getSize());
        widgetObject.put("children", childrenArray);

        return widgetObject;
    }

    private JSONObject jsonifyWidget(WidgetElement widgetElement) throws JSONException {
        JSONObject widgetObject = new JSONObject();
        widgetObject.put("type", "widget");
        widgetObject.put("size", widgetElement.getSize());
        widgetObject.put("widgetId", widgetElement.getAppWidgetId());

        return widgetObject;
    }

    public String getDataAsJsonString() {
        try {
            JSONObject data = getFileStructureAsJson();
            data = removeWidgetsFromJson(data);
            return data.toString(2);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    public void importFromInputStream(InputStream is){
        try {
            String contents = readInputStream(is);
            JSONObject data = new JSONObject(contents);

            data = removeWidgetsFromJson(data);

            loadFromJson(data);
            storeFilesStructure();
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private JSONObject removeWidgetsFromJson(JSONObject data){
        try {
            for (Iterator<String> it = data.keys(); it.hasNext(); ) {
                String key = it.next();
                data.getJSONObject(key).put("widgets", new JSONObject());
            }
        }catch (Exception e){
            throw new RuntimeException(e);
        }
        return data;
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
        if (item instanceof Folder) {
            String fullPath = parentFolder + "/" + newName;
            if (files.containsKey(fullPath)) return;

            // fix full path of folder and its subfolders
            Folder folder = ((Folder) item);
            String oldPath = folder.getFullPath();
            String newPath = oldPath.substring(0, oldPath.lastIndexOf("/") + 1) + newName;

            updateFolderPath(folder, oldPath, newPath);
        }
        item.setName(newName);
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
        folderContents.setFullPath(folderContents.getFullPath().replace(oldPath, newPath));

        files.put(folder.getFullPath(), folderContents);
        files.remove(subfolderPath);
    }

    public void removeFile(String parentFolder, int fileIndex) {
        ArrayList<FileNode> contents = getFolderContents(parentFolder).getFiles();
        FileNode item = contents.get(fileIndex);
        if (item instanceof Folder) {
            removeFolder(((Folder) item).getFullPath());
        }
        contents.remove(fileIndex);
        storeFilesStructure();
    }

    private void removeFolder(String path) {
        Folder folder = getFolderContents(path);
        for (FileNode file : folder.getFiles()) {
            if (!(file instanceof Folder)) continue;
            removeFolder(((Folder) file).getFullPath());
        }
        files.remove((folder).getFullPath());
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

    public Folder getFolderContents(String path) {
        if (files.containsKey(path)) return files.get(path);
        return null;
    }

    private String readInputStream(InputStream inputStream) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

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
        if (file == null) return;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(content);
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
