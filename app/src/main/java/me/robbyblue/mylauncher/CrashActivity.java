package me.robbyblue.mylauncher;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class CrashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);

        Intent intent = getIntent();
        String message = intent.getStringExtra("message");
        String stacktrace = intent.getStringExtra("stacktrace");

        ((TextView) findViewById(R.id.stacktrace)).setText(stacktrace);

        String device = android.os.Build.DEVICE + " (" + android.os.Build.VERSION.SDK_INT + ")";

        String version = BuildConfig.VERSION_NAME + "(" + BuildConfig.VERSION_CODE + ")";
        String body = message + "\n" +
                "steps to reproduce: \n" +
                "device: " + device + "\n" +
                "app: " + version + "\n" +
                "stacktrace:\n```\n" + stacktrace + "``` \n";

        String url = "https://github.com/Robby-Blue/SimpleFolderLauncher/issues/new";
        findViewById(R.id.github_button).setOnClickListener((l) -> {
            ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("data", body);
            clipboard.setPrimaryClip(clip);

            Toast.makeText(this, "copied data to clipboard. paste into issue body", Toast.LENGTH_LONG).show();

            Intent ghIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(ghIntent);
        });
    }
}
