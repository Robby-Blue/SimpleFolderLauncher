package me.robbyblue.mylauncher.settings;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;

import me.robbyblue.mylauncher.FileDataStorage;
import me.robbyblue.mylauncher.R;

public class SettingsActivity extends AppCompatActivity {

    private ActivityResultLauncher<Intent> createJsonLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        findViewById(R.id.export_button).setOnClickListener(v -> {
            startExport();
        });

        createJsonLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
                        return;
                    }
                    Uri uri = result.getData().getData();
                    if (uri != null) writeJsonToUri(uri);
                }
        );
    }

    private void startExport() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/json");
        intent.putExtra(Intent.EXTRA_TITLE, "structure.json");
        createJsonLauncher.launch(intent);
    }

    private void writeJsonToUri(Uri uri) {
        FileDataStorage fs = FileDataStorage.getInstanceAssumeExists();

        try (OutputStream os = getContentResolver().openOutputStream(uri);
             OutputStreamWriter writer = new OutputStreamWriter(os, StandardCharsets.UTF_8)) {
            writer.write(fs.getDataAsJsonString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
