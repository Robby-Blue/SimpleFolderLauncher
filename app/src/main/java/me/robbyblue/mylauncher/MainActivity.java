package me.robbyblue.mylauncher;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.pm.LauncherApps;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

import me.robbyblue.mylauncher.files.AppFile;
import me.robbyblue.mylauncher.files.FileAdapter;
import me.robbyblue.mylauncher.files.FileNode;
import me.robbyblue.mylauncher.files.Folder;


public class MainActivity extends AppCompatActivity {

    TextView folderPathView;
    RecyclerView recycler;
    String currentFolder;
    int longClickedId;

    GestureDetectorCompat gestureDetector;

    FileDataStorage dataStorage;

    ActivityResultLauncher<Intent> addFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != RESULT_OK) return;
        Intent intent = result.getData();
        if (intent == null) return;

        FileType selectedType = FileType.valueOf(intent.getStringExtra("type"));
        String parentFolder = intent.getStringExtra("parent");
        String name = intent.getStringExtra("name");
        String appPackage = intent.getStringExtra("package");

        if (selectedType == FileType.APPFILE) {
            dataStorage.createFile(parentFolder, new AppFile(name, appPackage));
        } else if (selectedType == FileType.FOLDER) {
            dataStorage.createFolder(parentFolder, name);
        }
        showFolder(parentFolder);
    });

    ActivityResultLauncher<Intent> searchLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != RESULT_OK) return;
        Intent intent = result.getData();
        if (intent == null) return;

        String folder = intent.getStringExtra("folder");
        showFolder(folder);
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataStorage = FileDataStorage.getInstance(this);
        AppsListCache.getInstance(this);

        folderPathView = findViewById(R.id.folder_path_text);
        recycler = findViewById(R.id.app_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        showFolder("~");

        registerForContextMenu(recycler);
        registerLauncherAppsCallback();

        findViewById(R.id.add_file_button).setOnClickListener(view -> {
            Intent intent = new Intent(this, AddFileActivity.class);
            intent.putExtra("folder", currentFolder);
            addFileLauncher.launch(intent);
        });

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showFolder("..");
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        SwipeListener swipeListener = new SwipeListener();
        gestureDetector = new GestureDetectorCompat(this, swipeListener);

        swipeListener.setOnSwipeListener((MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) -> {
            if (velocityY < -1000 && Math.abs(velocityX) < Math.abs(velocityY) * 0.6) {
                Intent intent = new Intent(this, SearchActivity.class);
                searchLauncher.launch(intent);
            }
            return false;
        });
    }

    private void registerLauncherAppsCallback() {
        LauncherApps launcherApps = (LauncherApps) getSystemService(LAUNCHER_APPS_SERVICE);
        launcherApps.registerCallback(new LauncherApps.Callback() {
            @Override
            public void onPackageAdded(String packageName, UserHandle user) {
                AppsListCache.getInstance().indexPackage(packageName, MainActivity.this);
            }

            @Override
            public void onPackageChanged(String packageName, UserHandle user) {
                AppsListCache.getInstance().indexPackage(packageName, MainActivity.this);
            }

            @Override
            public void onPackageRemoved(String packageName, UserHandle user) {
                AppsListCache.getInstance().removePackage(packageName);
            }

            @Override
            public void onPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing) {
            }

            @Override
            public void onPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing) {
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.fileaction_menu, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_delete)
            dataStorage.removeFile(currentFolder, longClickedId);
        if (item.getItemId() == R.id.action_move_up)
            dataStorage.moveFile(currentFolder, longClickedId, longClickedId - 1);
        if (item.getItemId() == R.id.action_move_down)
            dataStorage.moveFile(currentFolder, longClickedId, longClickedId + 1);
        showFolder(currentFolder);
        return true;
    }

    public void showFolder(String folder) {
        if (folder.equals("..")) {
            if (!currentFolder.contains("/")) return;
            folder = currentFolder.substring(0, currentFolder.lastIndexOf("/"));
        }
        currentFolder = folder;
        folderPathView.setText(folder);

        ArrayList<FileNode> contents = dataStorage.getFolderContents(folder);
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