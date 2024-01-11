package me.robbyblue.mylauncher;

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
import java.util.Comparator;
import java.util.Objects;

import me.robbyblue.mylauncher.files.AppFile;
import me.robbyblue.mylauncher.files.FileNode;
import me.robbyblue.mylauncher.files.Folder;
import me.robbyblue.mylauncher.files.SearchFileAdapter;

public class SearchActivity extends Activity {

    ArrayList<FileNode> searchResults = null;
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
                searchResults = search(editable.toString());
                displaySearchResults();
            }
        });

        searchBar.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_SEARCH) return false;
            if (searchResults == null) return false;
            if (searchResults.size() == 0) return false;
            openFileNode(searchResults.get(0));
            return true;
        });
    }

    private ArrayList<NamedItem> indexSearchableItems() {
        // add apps by their actual names
        ArrayList<NamedItem> items = new ArrayList<>();
        for (FileNode fileNode : AppsListCache.getInstance(this).getApps()) {
            items.addAll(indexSearchableItem(fileNode));
        }
        // folders
        FileDataStorage fileSystem = FileDataStorage.getInstance(this);
        ArrayList<Folder> folderNames = fileSystem.getFolders();
        for (Folder folder : folderNames) {
            items.addAll(indexSearchableItem(folder));
            // apps by their custom names in the folders

            ArrayList<FileNode> contents = fileSystem.getFolderContents(folder);
            for (FileNode fileNode : contents) {
                if (fileNode instanceof Folder) continue;
                items.addAll(indexSearchableItem(fileNode));
            }
        }
        return items;
    }

    // TODO: add more here
    // like "YouTube" -> "yt"
    private ArrayList<NamedItem> indexSearchableItem(FileNode fileNode) {
        ArrayList<NamedItem> items = new ArrayList<>();
        items.add(new NamedItem(fileNode.getName(), fileNode));

        // initials "Clash Royale" -> "cr"
        String[] words = fileNode.getName().split(" ");
        if (words.length > 1) {
            StringBuilder initials = new StringBuilder();
            for (String word : words)
                initials.append(word.charAt(0));
            items.add(new NamedItem(initials.toString(), fileNode));
        }
        return items;
    }

    // TODO: make better
    // obvious idea: after finding matches, sort by how much they match
    // also consider usage when sorting if possible
    private ArrayList<FileNode> search(String query) {
        query = query.toLowerCase();

        ArrayList<FileMatchResult> results = new ArrayList<>();

        for (NamedItem namedItem : searchableItems) {
            FileMatchResult result = matchesQuery(namedItem, query);
            if (result.getPoints() == 0) continue;
            if (results.contains(result)) {
                FileMatchResult currentResult = results.get(results.indexOf(result));
                if (result.getPoints() > currentResult.getPoints())
                    currentResult.updatePoints(result.getPoints());
                continue;
            }
            results.add(result);
        }

        results.sort(Comparator.comparing(FileMatchResult::getPoints, Comparator.reverseOrder()));
        results = new ArrayList<>(results.subList(0, Math.min(10, results.size())));

        ArrayList<FileNode> fileNodes = new ArrayList<>(10);

        for (FileMatchResult result : results) {
            fileNodes.add(result.getFileNode());
        }

        return fileNodes;
    }

    // TODO: probably add more
    private FileMatchResult matchesQuery(NamedItem namedItem, String query) {
        String name = namedItem.getName();
        FileNode fileNode = namedItem.getFileNode();

        FileMatchResult result = new FileMatchResult(fileNode);

        if (name.equals(query)) result.updatePoints(100);
        if (name.contains(query)) result.updatePoints(50);
        if (name.startsWith(query)) result.updatePoints(75);

        for (String word : name.split(" ")) {
            if (word.startsWith(query)) result.updatePoints(25);
        }

        return result;
    }

    private void displaySearchResults() {
        SearchFileAdapter adapter = new SearchFileAdapter(this, searchResults);
        recycler.setAdapter(adapter);
        recycler.scrollToPosition(0);
    }

    public void openFileNode(FileNode fileNode) {
        if (fileNode instanceof Folder) {
            String fullPath = ((Folder) fileNode).getFullPath();
            showFolder(fullPath);
        } else {
            AppFile appFile = (AppFile) fileNode;
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage(appFile.getPackageName());
            startActivity(launchIntent);
        }
        finish();
    }

    public void showFolder(String path) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("folder", path);
        setResult(Activity.RESULT_OK, resultIntent);
    }

    private static class FileMatchResult {

        private final FileNode fileNode;
        private int points;

        private FileMatchResult(FileNode fileNode) {
            this.fileNode = fileNode;
            this.points = 0;
        }

        public void updatePoints(int points) {
            if (points > this.points) {
                this.points = points;
            }
        }

        public FileNode getFileNode() {
            return fileNode;
        }

        public int getPoints() {
            return points;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            FileMatchResult that = (FileMatchResult) o;
            return Objects.equals(getFileNode(), that.getFileNode());
        }

    }

    private static class NamedItem {

        private final String name;
        private final FileNode fileNode;

        public NamedItem(String name, FileNode fileNode) {
            this.name = name.toLowerCase();
            this.fileNode = fileNode;
        }

        public String getName() {
            return name;
        }

        public FileNode getFileNode() {
            return fileNode;
        }

    }

}