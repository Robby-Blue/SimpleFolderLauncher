package me.robbyblue.mylauncher;

import android.app.Application;
import android.content.Intent;
import android.util.Log;

public class LauncherApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            String stackTraceString = Log.getStackTraceString(throwable);

            Intent intent = new Intent(getApplicationContext(), CrashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("message", throwable.getClass().getName());
            intent.putExtra("stacktrace", stackTraceString);

            startActivity(intent);

            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        });
    }
}
