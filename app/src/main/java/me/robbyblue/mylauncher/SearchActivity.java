package me.robbyblue.mylauncher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import me.robbyblue.mylauncher.files.AppFile;
import me.robbyblue.mylauncher.files.FileNode;
import me.robbyblue.mylauncher.files.Folder;
import me.robbyblue.mylauncher.files.SearchFileAdapter;

public class SearchActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        RecyclerView recycler = findViewById(R.id.app_recycler);
        EditText searchBar = findViewById(R.id.search_bar);

        ArrayList<FileNode> apps = new ArrayList<>();

        apps.add(new AppFile("Youtube", "org.schabi.newpipe"));
        apps.add(new Folder("Media", "~/Media"));

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setReverseLayout(true);
        recycler.setLayoutManager(layoutManager);
        SearchFileAdapter adapter = new SearchFileAdapter(this, apps);
        recycler.setAdapter(adapter);

        searchBar.requestFocus();
    }

    public void showFolder(String path) {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("folder", path);
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }
}