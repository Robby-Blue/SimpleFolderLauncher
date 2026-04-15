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
import android.widget.FrameLayout;
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
    AppWidgetHost appWidgetHost;
    HashMap<WidgetLayout, FrameLayout> currentLayouts;
    WidgetList currentWidgetList;

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
        if (appWidgetId == -1) return;

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        android.appwidget.AppWidgetProviderInfo widgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);

        pendingWidgetId = appWidgetId;
        pendingNeedsConfig = (widgetInfo != null && widgetInfo.configure != null);

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

        appWidgetHost = new AppWidgetHost(this, MainActivity.APPWIDGET_HOST_ID);
        appWidgetHost.startListening();

        FileDataStorage fs;
        try {
            fs = FileDataStorage.getInstance();
        }catch (Exception e){
            finish();
            return;
        }
        WidgetList widgetList = fs.getFolderContents(folder).getWidgetList();

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

        WidgetList widgetList = fs.getFolderContents(folder).getWidgetList();
        currentWidgetList = widgetList;
        LinearLayout container = findViewById(R.id.widget_container);
        currentLayouts = WidgetSystem.createLayout(widgetList, container, true, appWidgetHost);

        for (WidgetLayout widgetLayout : currentLayouts.keySet()) {
            FrameLayout layout = currentLayouts.get(widgetLayout);

            if (widgetLayout instanceof WidgetElement) {
                final WidgetElement element = (WidgetElement) widgetLayout;
                layout.setOnClickListener((l) -> {
                    showWidgetMenu(element, layout);
                });
                layout.setOnLongClickListener((l) -> {
                    showWidgetMenu(element, layout);
                    return true;
                });
            } else if (widgetLayout instanceof WidgetList) {
                final WidgetList row = (WidgetList) widgetLayout;
                layout.setOnClickListener((l) -> {
                    showRowMenu(row, layout);
                });
                layout.setOnLongClickListener((l) -> {
                    showRowMenu(row, layout);
                    return true;
                });
            }
        }
    }

    private void showWidgetMenu(WidgetElement element, FrameLayout layout) {
        final boolean isInRow;
        final WidgetList parentRow;
        boolean foundInRow = false;
        WidgetList foundRow = null;
        for (WidgetLayout child : currentWidgetList.getChildren()) {
            if (child instanceof WidgetList) {
                WidgetList row = (WidgetList) child;
                if (row.getChildren().contains(element)) {
                    foundInRow = true;
                    foundRow = row;
                    break;
                }
            }
        }
        isInRow = foundInRow;
        parentRow = foundRow;

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        android.appwidget.AppWidgetProviderInfo widgetInfo = appWidgetManager.getAppWidgetInfo(element.getAppWidgetId());
        boolean hasConfig = widgetInfo != null && widgetInfo.configure != null;

        String[] options;
        if (isInRow) {
            if (hasConfig) {
                options = new String[]{"✕ Delete", "✎ Re-configure", "▲ Move row up", "▼ Move row down", "◀ Move left", "▶ Move right"};
            } else {
                options = new String[]{"✕ Delete", "▲ Move row up", "▼ Move row down", "◀ Move left", "▶ Move right"};
            }
        } else {
            if (hasConfig) {
                options = new String[]{"✕ Delete", "✎ Re-configure", "▲ Move up", "▼ Move down"};
            } else {
                options = new String[]{"✕ Delete", "▲ Move up", "▼ Move down"};
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Widget options");
        builder.setItems(options, (dialog, which) -> {
            int actionIndex = hasConfig ? which : (which >= 1 ? which + 1 : which);
            if (isInRow) {
                handleWidgetInRowAction(element, parentRow, actionIndex, hasConfig);
            } else {
                handleTopLevelWidgetAction(element, actionIndex, hasConfig);
            }
        });
        builder.show();
    }

    private void handleWidgetInRowAction(WidgetElement element, WidgetList row, int which, boolean hasConfig) {
        FileDataStorage fs = FileDataStorage.getInstanceAssumeExists();
        WidgetList widgetList = fs.getFolderContents(folder).getWidgetList();

        if (hasConfig) {
            switch (which) {
                case 0:
                    deleteWidget(element);
                    break;
                case 1:
                    reconfigureWidget(element);
                    break;
                case 2:
                    moveRowUp(row, widgetList);
                    break;
                case 3:
                    moveRowDown(row, widgetList);
                    break;
                case 4:
                    moveWidgetLeft(element, row);
                    break;
                case 5:
                    moveWidgetRight(element, row);
                    break;
            }
        } else {
            switch (which) {
                case 0:
                    deleteWidget(element);
                    break;
                case 1:
                    moveRowUp(row, widgetList);
                    break;
                case 2:
                    moveRowDown(row, widgetList);
                    break;
                case 3:
                    moveWidgetLeft(element, row);
                    break;
                case 4:
                    moveWidgetRight(element, row);
                    break;
            }
        }
    }

    private void handleTopLevelWidgetAction(WidgetElement element, int which, boolean hasConfig) {
        FileDataStorage fs = FileDataStorage.getInstanceAssumeExists();
        WidgetList widgetList = fs.getFolderContents(folder).getWidgetList();

        if (hasConfig) {
            switch (which) {
                case 0:
                    deleteWidget(element);
                    break;
                case 1:
                    reconfigureWidget(element);
                    break;
                case 2:
                    moveWidgetUp(element, widgetList);
                    break;
                case 3:
                    moveWidgetDown(element, widgetList);
                    break;
            }
        } else {
            switch (which) {
                case 0:
                    deleteWidget(element);
                    break;
                case 1:
                    moveWidgetUp(element, widgetList);
                    break;
                case 2:
                    moveWidgetDown(element, widgetList);
                    break;
            }
        }
    }

    private void deleteWidget(WidgetElement element) {
        currentWidgetList.getChildren().removeIf((c) -> (c instanceof WidgetElement) && ((WidgetElement) c).getAppWidgetId() == element.getAppWidgetId());
        for (WidgetLayout childWidget : currentWidgetList.getChildren()) {
            if (!(childWidget instanceof WidgetList)) continue;
            ((WidgetList) childWidget).getChildren().removeIf((c) -> (c instanceof WidgetElement) && ((WidgetElement) c).getAppWidgetId() == element.getAppWidgetId());
        }

        appWidgetHost.deleteAppWidgetId(element.getAppWidgetId());
        FileDataStorage.getInstanceAssumeExists().storeFilesStructure();
        showLayout();
    }

    private void reconfigureWidget(WidgetElement element) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        android.appwidget.AppWidgetProviderInfo widgetInfo = appWidgetManager.getAppWidgetInfo(element.getAppWidgetId());

        if (widgetInfo != null && widgetInfo.configure != null) {
            pendingElement = element;
            isInRow = false;
            for (WidgetLayout child : currentWidgetList.getChildren()) {
                if (child instanceof WidgetList && ((WidgetList) child).getChildren().contains(element)) {
                    isInRow = true;
                    break;
                }
            }

            ComponentName configureComponent = widgetInfo.configure;
            Intent configureIntent = new Intent().setComponent(configureComponent);
            configureIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, element.getAppWidgetId());
            configureIntent.putExtra("folder", folder);
            configureIntent.setData(Uri.parse("widget:" + element.getAppWidgetId()));
            configureIntentLauncher.launch(configureIntent);
        }
    }

    private void moveRowUp(WidgetList row, WidgetList widgetList) {
        int index = widgetList.getChildren().indexOf(row);
        if (index > 0) {
            widgetList.getChildren().remove(index);
            widgetList.getChildren().add(index - 1, row);
            FileDataStorage.getInstanceAssumeExists().storeFilesStructure();
            showLayout();
        }
    }

    private void moveRowDown(WidgetList row, WidgetList widgetList) {
        int index = widgetList.getChildren().indexOf(row);
        if (index >= 0 && index < widgetList.getChildren().size() - 1) {
            widgetList.getChildren().remove(index);
            widgetList.getChildren().add(index + 1, row);
            FileDataStorage.getInstanceAssumeExists().storeFilesStructure();
            showLayout();
        }
    }

    private void moveWidgetLeft(WidgetElement element, WidgetList row) {
        int index = row.getChildren().indexOf(element);
        if (index > 0) {
            row.getChildren().remove(index);
            row.getChildren().add(index - 1, element);
            FileDataStorage.getInstanceAssumeExists().storeFilesStructure();
            showLayout();
        }
    }

    private void moveWidgetRight(WidgetElement element, WidgetList row) {
        int index = row.getChildren().indexOf(element);
        if (index >= 0 && index < row.getChildren().size() - 1) {
            row.getChildren().remove(index);
            row.getChildren().add(index + 1, element);
            FileDataStorage.getInstanceAssumeExists().storeFilesStructure();
            showLayout();
        }
    }

    private void moveWidgetUp(WidgetElement element, WidgetList widgetList) {
        int index = widgetList.getChildren().indexOf(element);
        if (index > 0) {
            widgetList.getChildren().remove(index);
            widgetList.getChildren().add(index - 1, element);
            FileDataStorage.getInstanceAssumeExists().storeFilesStructure();
            showLayout();
        }
    }

    private void moveWidgetDown(WidgetElement element, WidgetList widgetList) {
        int index = widgetList.getChildren().indexOf(element);
        if (index >= 0 && index < widgetList.getChildren().size() - 1) {
            widgetList.getChildren().remove(index);
            widgetList.getChildren().add(index + 1, element);
            FileDataStorage.getInstanceAssumeExists().storeFilesStructure();
            showLayout();
        }
    }

    private void showRowMenu(WidgetList row, FrameLayout layout) {
        String[] options = {"✕ Delete", "▲ Move up", "▼ Move down"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Row options");
        builder.setItems(options, (dialog, which) -> {
            FileDataStorage fs = FileDataStorage.getInstanceAssumeExists();
            WidgetList widgetList = fs.getFolderContents(folder).getWidgetList();

            switch (which) {
                case 0:
                    widgetList.getChildren().remove(row);
                    fs.storeFilesStructure();
                    showLayout();
                    break;
                case 1:
                    moveRowUp(row, widgetList);
                    break;
                case 2:
                    moveRowDown(row, widgetList);
                    break;
            }
        });
        builder.show();
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

        builder.setPositiveButton("OK", null);

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            if (needsConfig && widgetId != -1) {
                AppWidgetHost host = new AppWidgetHost(this, MainActivity.APPWIDGET_HOST_ID);
                host.deleteAppWidgetId(widgetId);
                pendingElement = null;
            }
        });

        AlertDialog dialog = builder.show();

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            FileDataStorage fs;
            try {
                fs = FileDataStorage.getInstanceAssumeExists();
            } catch (Exception e) {
                return;
            }

            String numberText = input.getText().toString();
            try {
                double number = Double.parseDouble(numberText);
                if (number < 1 || number > maxNumber) {
                    Toast.makeText(this, "Please enter a number between 1 and " + maxNumber, Toast.LENGTH_SHORT).show();
                    return;
                }
                double size = number / 100d;

                if (creatingRow) {
                    WidgetList list = new WidgetList(size);
                    WidgetList widgetList = fs.getFolderContents(folder).getWidgetList();
                    widgetList.addChild(list);
                    fs.storeFilesStructure();
                    showLayout();
                } else if (needsConfig) {
                    launchConfigForWidget(widgetId, size, isWidget);
                } else {
                    addWidgetDirectly(widgetId, size, isWidget);
                }
                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Invalid number", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addWidgetDirectly(int appWidgetId, double size, boolean inRow) {
        WidgetElement element = new WidgetElement(appWidgetId, size);

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
            pendingElement = new WidgetElement(appWidgetId, size);
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