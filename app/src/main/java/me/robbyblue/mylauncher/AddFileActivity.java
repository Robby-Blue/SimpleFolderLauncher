package me.robbyblue.mylauncher;

import android.app.Activity;
import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import me.robbyblue.mylauncher.files.AppFile;
import me.robbyblue.mylauncher.files.AppSelectionAdapter;

public class AddFileActivity extends AppCompatActivity {

    String parentFolder;
    FileType selectedType = FileType.UNSET;
    String appPackage = null;

    ArrayList<AppFile> apps = new ArrayList<>();
    LinearLayoutManager layoutManager;

    EditText nameField;

    // TODO: make the ui not look sillily ugly

    ActivityResultLauncher<Intent> pickWidgetLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() == RESULT_OK) {
            int appWidgetId = result.getData().getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

            Intent resultIntent = new Intent();
            resultIntent.putExtra("parent", parentFolder);
            resultIntent.putExtra("type", FileType.WIDGET.toString());
            resultIntent.putExtra("name", "");
            resultIntent.putExtra("package", "");
            resultIntent.putExtra("appWidgetId", appWidgetId);

            setResult(Activity.RESULT_OK, resultIntent);
            finish();
        }
    });

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
        apps = AppsListCache.getInstance().getAppsFiles();

        layoutManager = new LinearLayoutManager(this);
        recycler.setLayoutManager(layoutManager);
        recycler.setAdapter(new AppSelectionAdapter(this, apps));

        // name field validation and finishing
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