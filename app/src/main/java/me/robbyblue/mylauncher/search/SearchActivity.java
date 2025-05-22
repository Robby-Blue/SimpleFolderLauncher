package me.robbyblue.mylauncher.search;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.core.view.GestureDetectorCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import me.robbyblue.mylauncher.AppsListCache;
import me.robbyblue.mylauncher.FileDataStorage;
import me.robbyblue.mylauncher.R;
import me.robbyblue.mylauncher.SwipeListener;
import me.robbyblue.mylauncher.files.AppFile;
import me.robbyblue.mylauncher.files.FileNode;
import me.robbyblue.mylauncher.files.Folder;
import me.robbyblue.mylauncher.files.icons.AppIconData;

public class SearchActivity extends Activity {

    SearchEngine search = new SearchEngine();
    ArrayList<SearchResult> searchResults = null;
    ArrayList<NamedItem> searchableItems;

    RecyclerView recycler;
    LinearLayoutManager layoutManager;

    GestureDetectorCompat gestureDetector;
    boolean keyboardWasOpened = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean autoOpenOnlyResult = prefs.getBoolean("pref_search_auto_open_only", false);
        boolean showShortcuts = prefs.getBoolean("pref_search_show_shortcuts", true);

        int appTextColor = prefs.getInt("pref_app_text_color", Color.parseColor("#EEEEEE"));

        searchableItems = indexSearchableItems(showShortcuts);

        recycler = findViewById(R.id.app_recycler);

        EditText searchBar = findViewById(R.id.search_bar);
        searchBar.setTextColor(appTextColor);
        searchBar.getBackground().setColorFilter(0, PorterDuff.Mode.SRC_IN);

        layoutManager = new LinearLayoutManager(this) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        };
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(true);
        recycler.setLayoutManager(layoutManager);

        searchBar.requestFocus();

        final View rootView = findViewById(android.R.id.content);
        rootView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            Rect r = new Rect();
            rootView.getWindowVisibleDisplayFrame(r);
            int screenHeight = rootView.getRootView().getHeight();
            int keypadHeight = screenHeight - r.bottom;

            // its closed for a few ms at the beginning, we need to
            // make sure it was considered open at least once
            // to avoid premature closing
            if (keypadHeight < screenHeight * 0.15) {
                if (keyboardWasOpened) {
                    finish();
                }
            } else {
                keyboardWasOpened = true;
            }
        });

        searchBar.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                String query = editable.toString();

                if (query.contains(".")) {
                    searchResults = search.searchDots(query);
                } else {
                    searchResults = search.searchFiles(query, searchableItems);
                    if (autoOpenOnlyResult && searchResults.size() == 1) {
                        openFirstResult();
                        finish();
                        return;
                    }
                }
                displaySearchResults();
            }
        });

        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_SEARCH) return false;
            if (searchResults == null) return false;
            if (searchResults.size() == 0) return false;
            searchBar.clearFocus();
            openFirstResult();
            finish();
            return true;
        });

        SwipeListener swipeListener = new SwipeListener();
        gestureDetector = new GestureDetectorCompat(this, swipeListener);

        swipeListener.setOnSwipeListener((MotionEvent event1, MotionEvent event2, float velocityX, float velocityY) -> {
            if (Math.abs(velocityX) > Math.abs(velocityY) * 0.6) {
                return true;
            }

            if (velocityY > 1000) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
                if (getCurrentFocus() != null) {
                    imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
                }
                finish();
            }

            return false;
        });
    }

    private void openFirstResult() {
        searchResults.get(0).open(this);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        super.dispatchTouchEvent(event);
        return gestureDetector.onTouchEvent(event);
    }

    private ArrayList<NamedItem> indexSearchableItems(boolean showShortcuts) {
        // add apps by their actual names
        ArrayList<NamedItem> items = new ArrayList<>();

        ArrayList<AppFile> appList = new ArrayList<>();

        if (showShortcuts) {
            appList.addAll(AppsListCache.getInstance().getAppsFilesWithShortcuts());
        } else {
            appList.addAll(AppsListCache.getInstance().getAppsFiles());
        }

        for (FileNode fileNode : appList) {
            items.addAll(search.indexSearchableItem(fileNode));
        }

        // folders
        FileDataStorage fileSystem = FileDataStorage.getInstance();
        ArrayList<Folder> folderNames = fileSystem.getFolders();
        for (Folder folder : folderNames) {
            items.addAll(search.indexSearchableItem(folder));
            // apps by their custom names in the folders

            ArrayList<FileNode> contents = fileSystem.getFolderContents(folder).getFiles();
            for (FileNode fileNode : contents) {
                if (fileNode instanceof Folder) continue;
                String packageName = ((AppFile) fileNode).getPackageName();
                AppFile app = new AppFile(fileNode.getName(), packageName, new AppIconData(packageName));
                items.addAll(search.indexSearchableItem(app));
            }
        }
        return items;
    }

    public void showFolder(String path) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("folder", path);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private void displaySearchResults() {
        SearchResultAdapter adapter = new SearchResultAdapter(this, searchResults);
        recycler.setAdapter(adapter);
        recycler.scrollToPosition(0);
    }

    public String getSearchQuery() {
        EditText searchBar = findViewById(R.id.search_bar);
        return searchBar.getText().toString();
    }

}