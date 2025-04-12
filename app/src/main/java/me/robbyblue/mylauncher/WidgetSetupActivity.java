package me.robbyblue.mylauncher;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
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

    ActivityResultLauncher<Intent> pickWidgetLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != RESULT_OK) return;

        int appWidgetId = result.getData().getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

        WidgetElement element = new WidgetElement(appWidgetId, 1);

        FileDataStorage fs = FileDataStorage.getInstance();
        WidgetList widgetList = fs.getFolderContents(folder).getWidgetList();

        if (isInRow) {
            WidgetLayout lastElement = widgetList.getChildren().get(widgetList.getChildren().size() - 1);
            ((WidgetList) lastElement).addChild(element);

            showSizeDialog(element, this);
        } else {
            widgetList.addChild(element);
        }

        fs.storeFilesStructure();
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget);

        Intent intent = getIntent();
        this.folder = intent.getStringExtra("folder");

        FileDataStorage fs = FileDataStorage.getInstance();
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
            showSizeDialog(null, this);
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
        AppWidgetHost appWidgetHost = new AppWidgetHost(this, MainActivity.APPWIDGET_HOST_ID);

        FileDataStorage fs = FileDataStorage.getInstance();
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

    public void showSizeDialog(WidgetLayout widget, Context ctx) {
        EditText input = new EditText(ctx);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        input.setHint("size (0-100)");

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
        builder.setTitle("enter size");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            String numberText = input.getText().toString();
            try {
                double number = Double.parseDouble(numberText);
                if (number < 1 || number > 100) {
                    return;
                }
                double size = number / 100d;
                FileDataStorage fs = FileDataStorage.getInstance();
                if (widget != null) {
                    widget.setSize(size);
                } else {
                    WidgetList list = new WidgetList(size);
                    WidgetList widgetList = fs.getFolderContents(folder).getWidgetList();
                    widgetList.addChild(list);
                }
                fs.storeFilesStructure();
                showLayout();
            } catch (NumberFormatException e) {
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

}