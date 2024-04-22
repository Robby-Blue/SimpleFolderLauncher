package me.robbyblue.mylauncher.search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import me.robbyblue.mylauncher.AppsListCache;
import me.robbyblue.mylauncher.FileDataStorage;
import me.robbyblue.mylauncher.R;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchableItems = indexSearchableItems();

        recycler = findViewById(R.id.app_recycler);
        EditText searchBar = findViewById(R.id.search_bar);

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

        long startTime = System.currentTimeMillis();

        searchBar.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            // it already triggers once automatically at around 80 ms on my phone
            // theres probably a better way to do this
            if (oldTop < top && System.currentTimeMillis() - startTime > 200) {
                finish();
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
                }
                displaySearchResults();
            }
        });

        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_SEARCH) return false;
            if (searchResults == null) return false;
            if (searchResults.size() == 0) return false;
            searchBar.clearFocus();
            searchResults.get(0).open(this);
            finish();
            return true;
        });
    }

    private ArrayList<NamedItem> indexSearchableItems() {
        // add apps by their actual names
        ArrayList<NamedItem> items = new ArrayList<>();
        for (FileNode fileNode : AppsListCache.getInstance(this).getAppsFiles()) {
            items.addAll(search.indexSearchableItem(fileNode));
        }
        // folders
        FileDataStorage fileSystem = FileDataStorage.getInstance(this);
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