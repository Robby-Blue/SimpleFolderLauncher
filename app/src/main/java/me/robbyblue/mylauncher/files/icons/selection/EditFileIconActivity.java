package me.robbyblue.mylauncher.files.icons.selection;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.robbyblue.mylauncher.FileDataStorage;
import me.robbyblue.mylauncher.R;
import me.robbyblue.mylauncher.files.AppFile;
import me.robbyblue.mylauncher.files.FileNode;
import me.robbyblue.mylauncher.files.icons.AppIconData;
import me.robbyblue.mylauncher.files.icons.DotIconData;
import me.robbyblue.mylauncher.files.icons.IconData;
import me.robbyblue.mylauncher.files.icons.NoIconData;

public class EditFileIconActivity extends Activity {

    FileNode file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_file_icon);

        Intent intent = getIntent();
        String folder = intent.getStringExtra("folder");
        int fileIndex = intent.getIntExtra("fileIndex", -1);

        FileDataStorage fs = FileDataStorage.getInstance();
        ArrayList<FileNode> folderContents = fs.getFolderContents(folder).getFiles();

        this.file = folderContents.get(fileIndex);

        ArrayList<IconOption> options = new ArrayList<>();

        if (file instanceof AppFile) {
            String packageName = ((AppFile) file).getPackageName();
            Drawable iconDrawable = new AppIconData(packageName).getIconDrawable();

            HashMap<Integer, Integer> colorFrequencies = getColorFrequencies(iconDrawable);
            ArrayList<Integer> mostCommonColors = getMostCommonColors(colorFrequencies);

            options.add(new IconOption("app icon", new AppIconData(packageName)));

            String[] numbers = {"primary", "secondary", "tertiary"};

            for (int i = 0; i < numbers.length; i++) {
                if (i >= mostCommonColors.size()) {
                    break;
                }

                DotIconData dotIconData = new DotIconData(mostCommonColors.get(i));

                options.add(new IconOption(numbers[i] + " color", dotIconData));
            }

            options.addAll(IconPackManager.getInstance().getIconOptions(packageName));
        }

        options.add(new IconOption("no icon", new NoIconData()));

        IconOptionAdapter adapter = new IconOptionAdapter(this, options);

        RecyclerView recycler = findViewById(R.id.options_recycler);
        recycler.setLayoutManager(new LinearLayoutManager(this));
        recycler.setAdapter(adapter);
    }

    public void setIcon(IconData iconData) {
        file.setIconData(iconData);
        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    private ArrayList<Integer> getMostCommonColors(HashMap<Integer, Integer> colorFrequencies) {
        ArrayList<Map.Entry<Integer, Integer>> sortedFrequencyList = new ArrayList<>(colorFrequencies.entrySet());
        sortedFrequencyList.sort((entry1, entry2) -> entry2.getValue().compareTo(entry1.getValue()));

        ArrayList<Integer> mostCommonColors = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : sortedFrequencyList) {
            int value = entry.getKey();
            if (Color.alpha(value) < 250) // don't add transparent colors
                continue;

            mostCommonColors.add(value);
            if (mostCommonColors.size() == 3) {
                return mostCommonColors;
            }
        }
        return mostCommonColors;
    }

    private HashMap<Integer, Integer> getColorFrequencies(Drawable drawable) {
        Bitmap bitmap = getBitmapFromDrawable(drawable);

        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        HashMap<Integer, Integer> colorFrequencies = new HashMap<>();
        for (int value : pixels) {
            colorFrequencies.put(value, colorFrequencies.getOrDefault(value, 0) + 1);
        }

        return colorFrequencies;
    }

    private Bitmap getBitmapFromDrawable(Drawable drawable) {
        final Bitmap bmp = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bmp);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);
        return bmp;
    }

}