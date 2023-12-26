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

public class FileDataStorage {

    Context context;
    File structureFile;

    public FileDataStorage(Context context) {
        this.context = context;
        structureFile = new File(context.getFilesDir(), "filesstructure.json");
    }

    public HashMap<String, ArrayList<FileNode>> loadFilesStructure() {
        try {
            HashMap<String, ArrayList<FileNode>> files = new HashMap<>();
            files.put("~", new ArrayList<>());

            if (!structureFile.exists())
                return files;

            String fileContents = readFile(structureFile);
            if (fileContents == null)
                return files;

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
                    files.add(new AppFile(name, fileNodeJson.getString("package")));
                } else {
                    files.add(new Folder(name, folderName + "/" + name));
                }
            }
            return files;
        } catch (Exception e) {
            return null;
        }
    }

    public void storeFilesStructure(HashMap<String, ArrayList<FileNode>> files) {
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
                if (fileNode instanceof AppFile) {
                    fileJson.put("type", "file");
                    fileJson.put("package", ((AppFile) fileNode).getPackageName());
                }else if (fileNode instanceof Folder){
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
