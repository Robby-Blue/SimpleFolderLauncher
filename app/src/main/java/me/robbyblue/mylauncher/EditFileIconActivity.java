package me.robbyblue.mylauncher;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import me.robbyblue.mylauncher.files.AppFile;
import me.robbyblue.mylauncher.files.FileNode;
import me.robbyblue.mylauncher.files.Folder;
import me.robbyblue.mylauncher.files.icons.AppIconData;
import me.robbyblue.mylauncher.files.icons.DotIconData;
import me.robbyblue.mylauncher.files.icons.IconData;
import me.robbyblue.mylauncher.files.icons.NoIconData;

public class EditFileIconActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_file_icon);

        Intent intent = getIntent();
        String folder = intent.getStringExtra("folder");
        int fileIndex = intent.getIntExtra("fileIndex", -1);

        FileDataStorage fs = FileDataStorage.getInstance(this);
        ArrayList<FileNode> folderContents = fs.getFolderContents(folder).getFiles();

        FileNode file = folderContents.get(fileIndex);

        Button appIconButton = findViewById(R.id.select_icon_button);
        Button primaryColorButton = findViewById(R.id.select_primary_color_button);
        Button secondaryColorButton = findViewById(R.id.select_secondary_color_button);
        Button tertiaryColorButton = findViewById(R.id.select_tertiary_color_button);
        Button noIconButton = findViewById(R.id.select_no_icon_button);

        ImageView appIconImage = findViewById(R.id.icon_image);
        ImageView primaryColorImage = findViewById(R.id.primary_color_image);
        ImageView secondaryColorImage = findViewById(R.id.secondary_color_image);
        ImageView tertiaryColorImage = findViewById(R.id.tertiary_color_image);

        if (file instanceof Folder) {
            // its a folder, hide app icon related buttons
            appIconButton.setVisibility(View.GONE);
            primaryColorButton.setVisibility(View.GONE);
            secondaryColorButton.setVisibility(View.GONE);
            tertiaryColorButton.setVisibility(View.GONE);
        } else {
            String packageName = ((AppFile) file).getPackageName();
            Drawable iconDrawable = new AppIconData(packageName).getIconDrawable();

            HashMap<Integer, Integer> colorFrequencies = getColorFrequencies(iconDrawable);
            ArrayList<Integer> mostCommonColors = getMostCommonColors(colorFrequencies);

            appIconImage.setImageDrawable(iconDrawable);

            ImageView[] imageViews = {primaryColorImage, secondaryColorImage, tertiaryColorImage};
            Button[] buttons = {primaryColorButton, secondaryColorButton, tertiaryColorButton};

            for (int i = 0; i < imageViews.length; i++) {
                Button button = buttons[i];
                if (i >= mostCommonColors.size()) {
                    button.setVisibility(View.GONE);
                    break;
                }

                ImageView imageView = imageViews[i];
                DotIconData dotIconData = new DotIconData(mostCommonColors.get(i));
                Drawable drawable = dotIconData.getIconDrawable();
                imageView.setImageDrawable(drawable);

                button.setOnClickListener((e) -> setIcon(file, dotIconData));
            }
            appIconButton.setOnClickListener((e) -> setIcon(file, new AppIconData(packageName)));
        }
        noIconButton.setOnClickListener((e) -> setIcon(file, new NoIconData()));
    }

    private void setIcon(FileNode file, IconData iconData) {
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