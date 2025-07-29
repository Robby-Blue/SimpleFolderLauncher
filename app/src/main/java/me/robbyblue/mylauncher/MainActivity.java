package me.robbyblue.mylauncher;

import android.annotation.SuppressLint;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetHostView;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.LauncherApps;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import me.robbyblue.mylauncher.files.AppFile;
import me.robbyblue.mylauncher.files.FileAdapter;
import me.robbyblue.mylauncher.files.FileNode;
import me.robbyblue.mylauncher.files.Folder;
import me.robbyblue.mylauncher.files.icons.selection.EditFileIconActivity;
import me.robbyblue.mylauncher.files.icons.selection.IconPackManager;
import me.robbyblue.mylauncher.search.SearchActivity;
import me.robbyblue.mylauncher.settings.SettingsActivity;
import me.robbyblue.mylauncher.widgets.WidgetElement;
import me.robbyblue.mylauncher.widgets.WidgetLayout;
import me.robbyblue.mylauncher.widgets.WidgetList;
import me.robbyblue.mylauncher.widgets.WidgetSystem;


public class MainActivity extends AppCompatActivity {

    TextView folderPathView;
    RecyclerView recycler;
    String currentFolder;
    int longClickedId;

    GestureDetectorCompat gestureDetector;

    FileDataStorage dataStorage;
    AppsListCache appCache;

    static int APPWIDGET_HOST_ID = 418512;

    boolean isContextMenuOpen = false;

    ActivityResultLauncher<Intent> addWidgetLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        dataStorage.storeFilesStructure();
        showFolder(currentFolder);
    });

    ActivityResultLauncher<Intent> addFileLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != RESULT_OK) return;
        Intent intent = result.getData();
        if (intent == null) return;

        FileType selectedType = FileType.valueOf(intent.getStringExtra("type"));
        String parentFolder = intent.getStringExtra("parent");
        String name = intent.getStringExtra("name");

        if (selectedType == FileType.APPFILE) {
            String appPackage = intent.getStringExtra("package");
            long serialNumber = intent.getLongExtra("userHandleSerialNumber", -1);
            UserManager manager = (UserManager) getSystemService(Context.USER_SERVICE);
            UserHandle user = manager.getUserForSerialNumber(serialNumber);
            dataStorage.createFile(parentFolder, new AppFile(name, appPackage, user));
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

    /**
     * reloads current folder when finishing activity
     * and saves the file system to the file
     */
    ActivityResultLauncher<Intent> reloadFolderLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != RESULT_OK) return;
        showFolder(currentFolder);
        dataStorage.storeFilesStructure();
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        folderPathView = findViewById(R.id.folder_path_text);

        IconPackManager.getInstance(getPackageManager());
        File structureFile = new File(getFilesDir(), "filesstructure.json");
        dataStorage = FileDataStorage.getInstance(structureFile, this.getApplicationContext());
        appCache = AppsListCache.getInstance();
        appCache.loadAppsInFolder(this, dataStorage.getFolderContents("~"));

        setupUi();

        new Thread(() -> {
            appCache.loadAllApps(this);
        }).start();
    }

    private void setupUi() {
        recycler = findViewById(R.id.app_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        registerForContextMenu(findViewById(R.id.background));
        registerForContextMenu(recycler);

        recycler.setOnLongClickListener((l) -> false);

        registerLauncherAppsCallback();

        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                showFolder("..");
            }
        };
        getOnBackPressedDispatcher().addCallback(this, callback);

        HomeGestureListener homeGestureListener = new HomeGestureListener();
        gestureDetector = new GestureDetectorCompat(this, homeGestureListener);

        homeGestureListener.setHomeGestureCallback(this::onSwipe, this::onDoubleTap);

        recycler.setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));
        findViewById(R.id.background).setOnTouchListener((v, event) -> gestureDetector.onTouchEvent(event));

        showFolder("~");
    }

    private boolean onSwipe(float velocityX, float velocityY) {
        if (Math.abs(velocityX) > Math.abs(velocityY) * 0.6) {
            return false;
        }

        if (velocityY < -1000) {
            Intent intent = new Intent(this, SearchActivity.class);
            searchLauncher.launch(intent);
            return true;
        } else if (velocityY > 1000) {
            String panelMethod = "expandNotificationsPanel";
            if (velocityY > 5000) {
                panelMethod = "expandSettingsPanel";
            }

            // as per https://stackoverflow.com/questions/31897920/how-to-open-the-android-quick-notification-setting
            try {
                @SuppressLint("WrongConstant") Object service = getSystemService("statusbar");
                Class<?> statusBarManager = Class.forName("android.app.StatusBarManager");

                Method expand = statusBarManager.getMethod(panelMethod);
                expand.invoke(service);
            } catch (Exception e) {
                String toastText = "couldn't open quick settings: " + e;
                Toast toast = Toast.makeText(this, toastText, Toast.LENGTH_SHORT);
                toast.show();
                e.printStackTrace();
            }
            return true;
        }
        return false;
    }

    private boolean onDoubleTap() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String action = prefs.getString("pref_gesture_doubletap", "none");

        switch (action) {
            case "none":
                return false;
            case "parent_folder":
                showFolder("..");
                break;
            case "search":
                Intent intent = new Intent(this, SearchActivity.class);
                searchLauncher.launch(intent);
                break;
        }
        return true;
    }

    private void registerLauncherAppsCallback() {
        LauncherApps launcherApps = (LauncherApps) getSystemService(LAUNCHER_APPS_SERVICE);
        launcherApps.registerCallback(new LauncherApps.Callback() {
            @Override
            public void onPackageAdded(String packageName, UserHandle user) {
                if (appCache == null) return;
                appCache.indexPackage(packageName, MainActivity.this);
            }

            @Override
            public void onPackageChanged(String packageName, UserHandle user) {
                if (appCache == null) return;
                appCache.indexPackage(packageName, MainActivity.this);
            }

            @Override
            public void onPackageRemoved(String packageName, UserHandle user) {
                if (appCache == null) return;
                appCache.removePackage(packageName);
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
        if (gestureDetector == null) {
            return super.onTouchEvent(event);
        }
        if (gestureDetector.onTouchEvent(event)) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo
            menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);

        if (isContextMenuOpen) {
            return;
        }
        isContextMenuOpen = true;

        MenuInflater inflater = getMenuInflater();

        int id = -1;
        if (v == findViewById(R.id.background)) {
            id = R.menu.addaction_menu;
        } else if (v == recycler) {
            id = R.menu.fileaction_menu;
        }

        inflater.inflate(id, menu);
    }

    @Override
    public void onContextMenuClosed(@NonNull Menu menu) {
        super.onContextMenuClosed(menu);
        isContextMenuOpen = false;
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_add_file) {
            Intent intent = new Intent(this, AddFileActivity.class);
            intent.putExtra("folder", currentFolder);
            addFileLauncher.launch(intent);
            return true;
        }
        if (item.getItemId() == R.id.action_add_widget) {
            Intent intent = new Intent(this, WidgetSetupActivity.class);
            intent.putExtra("folder", currentFolder);

            addWidgetLauncher.launch(intent);
            return true;
        }
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }

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

        showWidgets(folder.getWidgetList());
    }

    private void showWidgets(WidgetList widgets) {
        LinearLayout widgetContainer = findViewById(R.id.widget_container);

        if (widgetContainer.getWidth() == 0) {
            widgetContainer.post((Runnable) () -> {
                showWidgets(widgets);
            });
            return;
        }

        widgetContainer.removeAllViewsInLayout();

        Context ctx = getApplicationContext();
        AppWidgetHost appWidgetHost = new AppWidgetHost(ctx, APPWIDGET_HOST_ID);

        HashMap<WidgetLayout, LinearLayout> layouts = WidgetSystem.createLayout(widgets, widgetContainer, false);

        for (WidgetLayout widgetLayout : layouts.keySet()) {
            if (!(widgetLayout instanceof WidgetElement)) return;

            LinearLayout layout = layouts.get(widgetLayout);
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(ctx);

            int id = ((WidgetElement) widgetLayout).getAppWidgetId();

            AppWidgetProviderInfo appWidgetInfo = appWidgetManager.getAppWidgetInfo(id);
            if (appWidgetInfo == null) {
                continue;
            }

            AppWidgetHostView hostView = appWidgetHost.createView(ctx, id, appWidgetManager.getAppWidgetInfo(id));

            layout.addView(hostView);
            appWidgetHost.startListening();

            int minWidth = appWidgetInfo.minWidth;
            int minHeight = appWidgetInfo.minHeight;
            int maxWidth = appWidgetInfo.minWidth;
            int maxHeight = appWidgetInfo.minHeight;

            hostView.updateAppWidgetSize(new Bundle(), minWidth, minHeight, maxWidth, maxHeight);
        }
    }

    private void setSize(RelativeLayout layout, int screenWidth) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(screenWidth / 2, screenWidth / 2);
        layout.setLayoutParams(layoutParams);
    }

    public void setLongClickedID(int position) {
        this.longClickedId = position;
    }

}