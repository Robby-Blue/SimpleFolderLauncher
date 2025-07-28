package me.robbyblue.mylauncher;

import android.appwidget.AppWidgetHost;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
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

    ActivityResultLauncher<Intent> pickWidgetLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        if (result.getResultCode() != RESULT_OK) return;

        FileDataStorage fs = FileDataStorage.getInstanceAssumeExists();

        int appWidgetId = result.getData().getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

        WidgetElement element = new WidgetElement(appWidgetId, 1);

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

    public void showSizeDialog(WidgetLayout widget, Context ctx) {
        boolean isWidget = widget != null;

        EditText input = new EditText(ctx);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        int maxNumber = isWidget ? 100 : 300;

        LinearLayout titleLayout = getTitleLayout(isWidget, maxNumber);

        AlertDialog.Builder builder = new AlertDialog.Builder(ctx);
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
                if (isWidget) {
                    widget.setSize(size);
                } else {
                    WidgetList list = new WidgetList(size);
                    WidgetList widgetList = fs.getFolderContents(folder).getWidgetList();
                    widgetList.addChild(list);
                }
                fs.storeFilesStructure();
                showLayout();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "invalid number", Toast.LENGTH_LONG).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
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