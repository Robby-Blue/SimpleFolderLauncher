package me.robbyblue.mylauncher;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;

import me.robbyblue.mylauncher.files.FileNode;
import me.robbyblue.mylauncher.files.Folder;

public class EditFileNameActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_name_icon);

        Intent intent = getIntent();
        String parentFolder = intent.getStringExtra("folder");
        int fileIndex = intent.getIntExtra("fileIndex", -1);

        FileDataStorage fs;
        try {
            fs = FileDataStorage.getInstance();
        } catch (Exception e) {
            finish();
            return;
        }
        Folder folder = fs.getFolderContents(parentFolder);
        FileNode file = folder.getFiles().get(fileIndex);
        String currentName = file.getName();

        EditText nameField = findViewById(R.id.new_name_field);
        nameField.requestFocus();
        nameField.setText(currentName);

        long startTime = System.currentTimeMillis();

        nameField.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            // it already triggers once automatically at around 80 ms on my phone
            // theres probably a better way to do this
            if (oldTop < top && System.currentTimeMillis() - startTime > 200) {
                finish();
            }
        });

        nameField.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId != EditorInfo.IME_ACTION_DONE) return false;
            nameField.clearFocus();

            String newName = nameField.getText().toString();
            fs.renameFile(parentFolder, fileIndex, newName);
            finish();
            return true;
        });
    }

}