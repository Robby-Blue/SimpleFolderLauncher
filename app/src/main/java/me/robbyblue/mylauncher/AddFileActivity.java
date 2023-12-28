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
import java.util.Comparator;
import java.util.List;

import me.robbyblue.mylauncher.files.AppFile;
import me.robbyblue.mylauncher.files.AppSelectionAdapter;

public class AddFileActivity extends Activity {

    String parentFolder;
    FileType selectedType = FileType.UNSET;
    String appPackage = null;

    ArrayList<AppFile> apps = new ArrayList<>();
    LinearLayoutManager layoutManager;

    EditText nameField;

    // TODO: make the ui not look sillily ugly

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addfile);

        Intent intent = getIntent();
        parentFolder = intent.getStringExtra("folder");

        nameField = findViewById(R.id.name_field);

        Button selectAppFileButton = findViewById(R.id.select_appfile_button);
        Button selectFolderButton = findViewById(R.id.select_folder_button);
        RecyclerView recycler = findViewById(R.id.app_recycler);

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

            Intent getAppsIntent = new Intent(Intent.ACTION_MAIN, null);
            getAppsIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            List<ResolveInfo> allApps = pm.queryIntentActivities(getAppsIntent, 0);
            for (ResolveInfo ri : allApps) {
                apps.add(new AppFile(ri.loadLabel(pm).toString(), ri.activityInfo.packageName));
                // to lowwer case everything bc by default "s" comes after "Y"
                apps.sort(Comparator.comparing(app -> app.getName().toLowerCase()));
                // find new index of the added app
                int index = -1;
                for(int i = 0;i<apps.size();i++){
                    if(apps.get(i).getPackageName().equals(ri.activityInfo.packageName)){
                        index = i;
                        break;
                    }
                }
                int finalIndex = index;
                runOnUiThread(() -> {
                    adapter.setApps(apps);
                    adapter.notifyItemInserted(finalIndex);
                    recycler.scrollToPosition(0);
                });
            }
        }).start();

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
        nameField.setText(apps.get(index).getName());
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