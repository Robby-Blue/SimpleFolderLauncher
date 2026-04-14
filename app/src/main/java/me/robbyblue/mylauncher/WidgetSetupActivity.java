package me.robbyblue.mylauncher;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.ComponentName;
import android.os.Bundle;
import android.text.InputType;
import android.net.Uri;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;

import me.robbyblue.mylauncher.widgets.WidgetElement;
import me.robbyblue.mylauncher.widgets.WidgetLayout;
import me.robbyblue.mylauncher.widgets.WidgetList;
import me.robbyblue.mylauncher.widgets.WidgetSystem;

public class WidgetSetupActivity extends AppCompatActivity {

    boolean isInRow = false;
    String folder;
    int pendingWidgetId = -1;
    boolean pendingNeedsConfig = false;
    WidgetElement pendingElement = null;

    ActivityResultLauncher<Intent> configureIntentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (pendingElement == null) return;

        if (result.getResultCode() != RESULT_OK) {
            AppWidgetHost host = new AppWidgetHost(this, MainActivity.APPWIDGET_HOST_ID);
            host.deleteAppWidgetId(pendingElement.getAppWidgetId());
            pendingElement = null;
            return;
        }

        addConfiguredWidget(pendingElement, isInRow);
        pendingElement = null;
    });

    ActivityResultLauncher<Intent> pickWidgetLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != RESULT_OK) return;

        int appWidgetId = result.getData().getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        android.appwidget.AppWidgetProviderInfo widgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);

        pendingWidgetId = appWidgetId;
        pendingNeedsConfig = (widgetInfo != null && widgetInfo.configure != null);

        if (pendingNeedsConfig) {
            pendingElement = new WidgetElement(appWidgetId, 100);
        }

        if (isInRow) {
            showSizeDialog(true, pendingWidgetId, pendingNeedsConfig);
        } else {
            showSizeDialog(false, pendingWidgetId, pendingNeedsConfig);
        }
    });

    private void addConfiguredWidget(WidgetElement element, boolean inRow) {
        FileDataStorage fs = FileDataStorage.getInstanceAssumeExists();
        WidgetList widgetList = fs.getFolderContents(folder).getWidgetList();

        if (inRow) {
            WidgetLayout lastElement = widgetList.getChildren().get(widgetList.getChildren().size() - 1);
            ((WidgetList) lastElement).addChild(element);
        } else {
            widgetList.addChild(element);
        }

        fs.storeFilesStructure();
        showLayout();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget);

        Intent intent = getIntent();
        this.folder = intent.getStringExtra("folder");

        FileDataStorage fs;
        try {
            fs = FileDataStorage.getInstance();
        }catch (Exception e){
            finish();
            return;
        }
        WidgetList widgetList = fs.getFolderContents(folder).getWidgetList();

        widgetList.getChildren().removeIf((child) -> {
            if (!(child instanceof WidgetList)) return false;
            return ((WidgetList) child).getChildren().size() == 0;
        });

        LinearLayout container = findViewById(R.id.widget_container);

        container.post((Runnable) () -> {
            showLayout();
        });

        findViewById(R.id.add_widget).setOnClickListener((l) -> {
            pickWidget(false);
        });
        findViewById(R.id.add_row).setOnClickListener((l) -> {
            showSizeDialog(false, -1, false);
        });
        findViewById(R.id.add_widget_to_row).setOnClickListener((l) -> {
            if (widgetList.getChildren().size() == 0) {
                return;
            }
            WidgetLayout lastElement = widgetList.getChildren().get(widgetList.getChildren().size() - 1);
            if (!(lastElement instanceof WidgetList)) {
                return;
            }
            pickWidget(true);
        });
    }

    private void showLayout() {
        FileDataStorage fs = FileDataStorage.getInstanceAssumeExists();
        AppWidgetHost appWidgetHost = new AppWidgetHost(this, MainActivity.APPWIDGET_HOST_ID);

        WidgetList widgetList = fs.getFolderContents(folder).getWidgetList();
        LinearLayout container = findViewById(R.id.widget_container);
        HashMap<WidgetLayout, LinearLayout> layouts = WidgetSystem.createLayout(widgetList, container, true);

        for (WidgetLayout widgetLayout : layouts.keySet()) {
            if (!(widgetLayout instanceof WidgetElement)) return;

            LinearLayout layout = layouts.get(widgetLayout);
            layout.setOnLongClickListener((l) -> {
                widgetList.getChildren().removeIf((c) -> (c instanceof WidgetElement) && ((WidgetElement) c).getAppWidgetId() == ((WidgetElement) widgetLayout).getAppWidgetId());
                for (WidgetLayout childWidget : widgetList.getChildren()) {
                    if (!(childWidget instanceof WidgetList)) continue;
                    ((WidgetList) childWidget).getChildren().removeIf((c) -> (c instanceof WidgetElement) && ((WidgetElement) c).getAppWidgetId() == ((WidgetElement) widgetLayout).getAppWidgetId());
                }

                appWidgetHost.deleteAppWidgetId(((WidgetElement) widgetLayout).getAppWidgetId());
                fs.storeFilesStructure();
                showLayout();
                return true;
            });
        }
    }

    private void pickWidget(boolean isInRow) {
        AppWidgetHost appWidgetHost = new AppWidgetHost(this, MainActivity.APPWIDGET_HOST_ID);
        int appWidgetId = appWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        pickWidgetLauncher.launch(pickIntent);
        this.isInRow = isInRow;
    }

    private void showSizeDialog(boolean isWidget, int widgetId, boolean needsConfig) {
        boolean creatingRow = !isWidget && widgetId == -1;

        EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        int maxNumber = isWidget ? 100 : 300;

        LinearLayout titleLayout = getTitleLayout(isWidget, maxNumber);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("enter size (0-" + maxNumber + ")");
        builder.setView(input);
        builder.setCustomTitle(titleLayout);

        builder.setPositiveButton("OK", (dialog, which) -> {
            FileDataStorage fs = FileDataStorage.getInstanceAssumeExists();

            String numberText = input.getText().toString();
            try {
                double number = Double.parseDouble(numberText);
                if (number < 1 || number > maxNumber) {
                    return;
                }
                double size = number / 100d;

                if (creatingRow) {
                    WidgetList list = new WidgetList(number / 100d);
                    WidgetList widgetList = fs.getFolderContents(folder).getWidgetList();
                    widgetList.addChild(list);
                    fs.storeFilesStructure();
                    showLayout();
                } else if (needsConfig) {
                    launchConfigForWidget(widgetId, number, isWidget);
                } else {
                    addWidgetDirectly(widgetId, number, isWidget);
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "invalid number", Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            if (needsConfig && widgetId != -1) {
                AppWidgetHost host = new AppWidgetHost(this, MainActivity.APPWIDGET_HOST_ID);
                host.deleteAppWidgetId(widgetId);
                pendingElement = null;
            }
        });
        builder.show();
    }

    private void addWidgetDirectly(int appWidgetId, double size, boolean inRow) {
        WidgetElement element = new WidgetElement(appWidgetId, size / 100d);

        FileDataStorage fs = FileDataStorage.getInstanceAssumeExists();
        WidgetList widgetList = fs.getFolderContents(folder).getWidgetList();

        if (inRow) {
            WidgetLayout lastElement = widgetList.getChildren().get(widgetList.getChildren().size() - 1);
            ((WidgetList) lastElement).addChild(element);
        } else {
            widgetList.addChild(element);
        }

        fs.storeFilesStructure();
        showLayout();
    }

    private void launchConfigForWidget(int appWidgetId, double size, boolean inRow) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        android.appwidget.AppWidgetProviderInfo widgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);

        if (widgetInfo != null && widgetInfo.configure != null) {
            pendingElement = new WidgetElement(appWidgetId, size / 100d);
            this.isInRow = inRow;

            ComponentName configureComponent = widgetInfo.configure;
            Intent configureIntent = new Intent().setComponent(configureComponent);
            configureIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            configureIntent.putExtra("folder", folder);
            configureIntent.setData(Uri.parse("widget:" + appWidgetId));
            configureIntentLauncher.launch(configureIntent);
        }
    }

    @NonNull
    private LinearLayout getTitleLayout(boolean isWidget, int maxNumber) {
        String explanation = isWidget ? "height in relation to screen width as a percentage (eg. 100 is as tall as wide, a perfect square)" :
                "width as a percentage (eg 50 takes up half of the screen)";

        LinearLayout layout = new LinearLayout(this);
        layout.setPadding(8, 8, 8, 8);
        layout.setOrientation(LinearLayout.VERTICAL);

        TextView titleView = new TextView(this);
        titleView.setText("Enter size (0-" + maxNumber + ")");
        titleView.setTextSize(18);

        TextView explanationView = new TextView(this);
        explanationView.setText(explanation);
        explanationView.setTextSize(14);

        layout.addView(titleView);
        layout.addView(explanationView);
        return layout;
    }

}