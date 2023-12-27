package me.robbyblue.mylauncher;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

import me.robbyblue.mylauncher.files.AppFile;
import me.robbyblue.mylauncher.files.FileAdapter;
import me.robbyblue.mylauncher.files.FileNode;
import me.robbyblue.mylauncher.files.Folder;


public class MainActivity extends AppCompatActivity {

    TextView folderPathView;
    RecyclerView recycler;
    HashMap<String, ArrayList<FileNode>> files;
    String currentFolder;
    int longClickedId;

    FileDataStorage dataStorage;

    ActivityResultLauncher<Intent> register = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != RESULT_OK)
            return;
        Intent intent = result.getData();
        if (intent == null)
            return;

        FileType selectedType = FileType.valueOf(intent.getStringExtra("type"));
        String parentFolder = intent.getStringExtra("parent");
        String name = intent.getStringExtra("name");
        String appPackage = intent.getStringExtra("package");

        ArrayList<FileNode> parentFolderContents = files.get(parentFolder);

        if (parentFolderContents == null)
            return;

        if (selectedType == FileType.APPFILE) {
            parentFolderContents.add(new AppFile(name, appPackage));
        } else if (selectedType == FileType.FOLDER) {
            String fullPath = parentFolder + "/" + name;
            if (files.containsKey(fullPath))
                return;
            parentFolderContents.add(new Folder(name, fullPath));
            files.put(fullPath, new ArrayList<>());
        }
        dataStorage.storeFilesStructure(files);
        showFolder(parentFolder);
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataStorage = new FileDataStorage(this);
        files = dataStorage.loadFilesStructure();

        folderPathView = findViewById(R.id.folderPath);
        recycler = findViewById(R.id.appRecycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        showFolder("~");

        registerForContextMenu(recycler);

        findViewById(R.id.addFileButton).setOnClickListener(view -> {
            Intent intent = new Intent(this, AddFileActivity.class);
            intent.putExtra("folder", currentFolder);
            register.launch(intent);
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showFolder("..");
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fileaction_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.actionDelete){
            ArrayList<FileNode> contents = files.get(currentFolder);
            if (contents == null)
                return true;
            contents.remove(longClickedId);
            dataStorage.storeFilesStructure(files);
            showFolder(currentFolder);
            // TODO: refactor this
            // probably move everything (both file adding and removing for now) to FileDataStorage
            // also make it work fully for folders, dont just delete the reference to the folder
            // but the folder itself too
        }
        return true;
    }

    public void showFolder(String folder) {
        if (folder.equals("..")) {
            if (!currentFolder.contains("/")) return;
            folder = currentFolder.substring(0, currentFolder.lastIndexOf("/"));
        }
        currentFolder = folder;
        folderPathView.setText(folder);

        ArrayList<FileNode> contents = files.get(folder);

        if (contents == null)
            return;

        ArrayList<FileNode> displayFiles = new ArrayList<>(contents);

        if (!folder.equals("~")) {
            displayFiles.add(new Folder("..", folder));
        }

        FileAdapter adapter = new FileAdapter(this, displayFiles);
        recycler.setAdapter(adapter);
    }

    public void setLongClickedID(int position) {
        this.longClickedId = position;
    }

}