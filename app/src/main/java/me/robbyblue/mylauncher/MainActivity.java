package me.robbyblue.mylauncher;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import me.robbyblue.mylauncher.files.AppFile;
import me.robbyblue.mylauncher.files.FileAdapter;
import me.robbyblue.mylauncher.files.FileNode;
import me.robbyblue.mylauncher.files.Folder;
import me.robbyblue.mylauncher.search.SearchActivity;


public class MainActivity extends AppCompatActivity {

    TextView folderPathView;
    RecyclerView recycler;
    String currentFolder;
    int longClickedId;

    GestureDetectorCompat gestureDetector;

    FileDataStorage dataStorage;

    static int APPWIDGET_HOST_ID = 418512;

    AppWidgetManager appWidgetManager;

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
        } else if (selectedType == FileType.WIDGET) {
            int appWidgetId = intent.getIntExtra("appWidgetId", -1);
            dataStorage.addWidget(parentFolder, appWidgetId);
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

    /**
     * reloads current folder when finishing activity
     * and saves the file system to the file
     */
    ActivityResultLauncher<Intent> reloadFolderLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != RESULT_OK) return;
        showFolder(currentFolder);
        FileDataStorage.getInstance(this).storeFilesStructure();
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        AppsListCache.getInstance(this);
        dataStorage = FileDataStorage.getInstance(this);

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
        if (item.getItemId() == R.id.action_change_icon) {
            Intent intent = new Intent(this, EditFileIconActivity.class);
            intent.putExtra("folder", currentFolder);
            intent.putExtra("fileIndex", longClickedId);
            reloadFolderLauncher.launch(intent);
            return true;
        }
        if (item.getItemId() == R.id.action_change_name) {
            Intent intent = new Intent(this, EditFileNameActivity.class);
            intent.putExtra("folder", currentFolder);
            intent.putExtra("fileIndex", longClickedId);
            reloadFolderLauncher.launch(intent);
            return true;
        }

        if (item.getItemId() == R.id.action_delete)
            dataStorage.removeFile(currentFolder, longClickedId);
        if (item.getItemId() == R.id.action_move_up)
            dataStorage.moveFile(currentFolder, longClickedId, longClickedId - 1);
        if (item.getItemId() == R.id.action_move_down)
            dataStorage.moveFile(currentFolder, longClickedId, longClickedId + 1);
        showFolder(currentFolder);
        return true;
    }

    public void showFolder(String folderPath) {
        if (folderPath.equals("..")) {
            if (!currentFolder.contains("/")) return;
            folderPath = currentFolder.substring(0, currentFolder.lastIndexOf("/"));
        }
        currentFolder = folderPath;
        folderPathView.setText(folderPath);

        Folder folder = dataStorage.getFolderContents(folderPath);
        ArrayList<FileNode> contents = folder.getFiles();
        ArrayList<FileNode> displayFiles = new ArrayList<>(contents);

        if (!folderPath.equals("~")) {
            displayFiles.add(new Folder("..", folderPath));
        }

        FileAdapter adapter = new FileAdapter(this, displayFiles);
        recycler.setAdapter(adapter);

        showWidgets(folder.getWidgetIds());
    }

    private void showWidgets(ArrayList<Integer> appWidgetIds) {
        LinearLayout widgetContainer = findViewById(R.id.widget_container);
        widgetContainer.removeAllViewsInLayout();

        Context ctx = getApplicationContext();

        for (int appWidgetId : appWidgetIds) {
            AppWidgetHost appWidgetHost = new AppWidgetHost(ctx, APPWIDGET_HOST_ID);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ctx);
            AppWidgetHostView hostView = appWidgetHost.createView(ctx, appWidgetId, appWidgetManager.getAppWidgetInfo(appWidgetId));

            widgetContainer.addView(hostView);
            appWidgetHost.startListening();

            AppWidgetProviderInfo appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
            int minWidth = appWidgetInfo.minWidth;
            int minHeight = appWidgetInfo.minHeight;
            int maxWidth = appWidgetInfo.minWidth;
            int maxHeight = appWidgetInfo.minHeight;
            hostView.updateAppWidgetSize(new Bundle(), minWidth, minHeight, maxWidth, maxHeight);

            hostView.setOnLongClickListener((l) -> {
                appWidgetHost.deleteAppWidgetId(appWidgetId);
                dataStorage.removeWidget(currentFolder, appWidgetId);
                showFolder(currentFolder);
                return true;
            });
        }
    }

    public void setLongClickedID(int position) {
        this.longClickedId = position;
    }

}