package me.robbyblue.mylauncher;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import me.robbyblue.mylauncher.files.AppFile;
import me.robbyblue.mylauncher.files.AppSelectionAdapter;

public class AddFileActivity extends Activity {

    String parentFolder;
    FileType selectedType = FileType.UNSET;
    String appPackage = null;

    ArrayList<AppFile> apps = new ArrayList<>();
    LinearLayoutManager layoutManager;

    // TODO: make the ui not look sillily ugly

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfile);

        Intent intent = getIntent();
        parentFolder = intent.getStringExtra("folder");

        Button selectAppFileButton = findViewById(R.id.selectAppFileButton);
        Button selectFolderButton = findViewById(R.id.selectFolderButton);
        RecyclerView recycler = findViewById(R.id.appRecycler);

        // app/folder buttons
        selectAppFileButton.setOnClickListener(view -> {
            selectedType = FileType.APPFILE;
            selectAppFileButton.setBackgroundResource(R.drawable.selected_button_bg);
            selectFolderButton.setBackgroundResource(R.drawable.default_button_bg);
            recycler.setVisibility(View.VISIBLE);
        });
        selectFolderButton.setOnClickListener(view -> {
            selectedType = FileType.FOLDER;
            selectFolderButton.setBackgroundResource(R.drawable.selected_button_bg);
            selectAppFileButton.setBackgroundResource(R.drawable.default_button_bg);
            recycler.setVisibility(View.INVISIBLE);
        });

        // app selection recycler
        recycler.setItemAnimator(null); // fix crash https://stackoverflow.com/questions/35653439/
        layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        AppSelectionAdapter adapter = new AppSelectionAdapter(this);
        adapter.setApps(apps);
        recycler.setAdapter(adapter);

        new Thread(() -> {
            PackageManager pm = getPackageManager();

            Intent i = new Intent(Intent.ACTION_MAIN, null);
            i.addCategory(Intent.CATEGORY_LAUNCHER);

            List<ResolveInfo> allApps = pm.queryIntentActivities(i, 0);
            // TODO: maybe sort this later, its being silly right now
            for (ResolveInfo ri : allApps) {
                apps.add(new AppFile(ri.loadLabel(pm).toString(), ri.activityInfo.packageName));
                runOnUiThread(() -> {
                    adapter.setApps(apps);
                    adapter.notifyItemInserted(layoutManager.getItemCount() - 1);
                });
            }
        }).start();

        EditText nameField = findViewById(R.id.nameField);
        nameField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_DONE)
                return false;
            if (!isValidName(nameField.getText().toString()))
                return false;

            if(selectedType == FileType.UNSET)
                return false;
            if(selectedType == FileType.APPFILE && appPackage == null)
                return false;

            Intent resultIntent = new Intent();
            resultIntent.putExtra("parent", parentFolder);
            resultIntent.putExtra("type", selectedType.toString());
            resultIntent.putExtra("name", nameField.getText().toString());
            resultIntent.putExtra("package", appPackage);

            setResult(Activity.RESULT_OK, resultIntent);
            finish();
            return true;
        });
    }

    private boolean isValidName(String name) {
        if(name.contains("/"))
            return false;
        if(name.contains(".."))
            return false;
        return name.length() != 0;
    }

    public void select(int index){
        setBackgrounds(layoutManager, index);
        appPackage = apps.get(index).getPackageName();
    }

    private void setBackgrounds(LinearLayoutManager layoutManager, int index) {
        for(int i =0;i<layoutManager.getItemCount();i++) {
            int background = R.drawable.transparent_button_bg;
            if(i == index)
                background = R.drawable.selected_button_bg;
            View child = layoutManager.findViewByPosition(i);
            if(child == null) // shouldnt happen but makes android studio shut up
                continue;
            child.setBackgroundResource(background);
        }
    }

}