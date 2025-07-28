package me.robbyblue.mylauncher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.app.UiAutomation;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.SystemClock;

import androidx.preference.PreferenceManager;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;

import me.robbyblue.mylauncher.search.SearchActivity;
import me.robbyblue.mylauncher.settings.SettingsActivity;
import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.cleanstatusbar.CleanStatusBar;
import tools.fastlane.screengrab.locale.LocaleTestRule;

@RunWith(AndroidJUnit4.class)
public class ScreenshotTest {

    int whiteTextColor = Color.parseColor("#EEEEEE");
    int blackTextColor = Color.parseColor("#222222");
    int lightFolderColor = Color.parseColor("#8ed9ff");
    int darkFolderColor = Color.parseColor("#10863a");

    @Rule
    public final LocaleTestRule localeTestRule = new LocaleTestRule();

    @BeforeClass
    public static void beforeAll() throws InterruptedException {
        InputStream dataInputStream = ScreenshotTest.class.getResourceAsStream("/data_screenshot.json");
        FileDataStorage fs = FileDataStorage.getInstanceOrCreate();
        fs.loadFromInputStream(dataInputStream);

        ActivityScenario.launch(MainActivity.class);

        while (true) {
            try {
                onView(withText("~")).check(matches(isDisplayed()));
                break;
            } catch (Exception e) {
                Thread.sleep(50);
            }
        }

        new CleanStatusBar()
                .setClock("0800")
                .setShowNotifications(false)
                .enable();
    }

    @AfterClass
    public static void afterAll() {
        CleanStatusBar.disable();
    }

    @Test
    public void takeScreenshotOfHomeFolder() {
        setTheme(true);

        ActivityScenario.launch(MainActivity.class);

        onView(isRoot()).perform(click());

        Screengrab.screenshot("home_folder");
    }

    @Test
    public void takeScreenshotOfHomeFolderDark() {
        setTheme(false);

        ActivityScenario.launch(MainActivity.class);

        onView(isRoot()).perform(click());

        Screengrab.screenshot("home_folder_dark");
    }

    @Test
    public void takeScreenshotOfMediaFolder() {
        setTheme(false);

        ActivityScenario.launch(MainActivity.class);

        onView(withText("media"))
                .perform(click());

        Screengrab.screenshot("media_folder");
    }

    private void setWallpaper(String path) {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        InputStream backgroundInputStream = getClass().getResourceAsStream(path);
        WallpaperManager wallpaperManager = WallpaperManager.getInstance(context);
        Bitmap bitmap = BitmapFactory.decodeStream(backgroundInputStream);

        try {
            wallpaperManager.setBitmap(bitmap);
            waitForWallpaper();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void waitForWallpaper() {
        UiAutomation automation = InstrumentationRegistry.getInstrumentation().getUiAutomation();

        int waited = 0;
        while (waited < 10000) {
            SystemClock.sleep(50);
            waited += 50;

            Bitmap screen = automation.takeScreenshot();

            int totalBrightness = 0;
            for(int x = 0;x<screen.getWidth();x++) {
                int pixel = screen.getPixel(x, screen.getHeight() / 2);
                int r = Color.red(pixel);
                int g = Color.green(pixel);
                int b = Color.blue(pixel);

                int brightness = (r + g + b) / 3;
                totalBrightness += brightness;
            }

            int average = totalBrightness / screen.getWidth();
            if (average > 25) {
                return;
            }
        }
    }

    private void setPreference(String name, int value) {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().putInt(name, value).apply();
    }

    @Test
    public void takeScreenshotOfSearch() throws InterruptedException {
        setTheme(false);

        AppsListCache.getInstanceAssumeExists().loadTestApps();

        ActivityScenario.launch(SearchActivity.class);

        onView(withId(R.id.search_bar))
                .perform(click(), typeText("youtube"));
        Thread.sleep(100);

        Screengrab.screenshot("search");
    }

    @Test
    public void takeScreenshotOfSearchDots() throws InterruptedException {
        setTheme(true);

        ActivityScenario.launch(SearchActivity.class);

        onView(withId(R.id.search_bar))
                .perform(click(), typeText(".b example.org"));
        Thread.sleep(100);

        Screengrab.screenshot("search_dots");
    }

    @Test
    public void takeScreenshotOfSettings() {
        setTheme(true);

        ActivityScenario.launch(SettingsActivity.class);
        onView(withText("general")).perform(click());

        Screengrab.screenshot("settings");
    }

    public void setTheme(boolean dark) {
        if (dark) {
            setWallpaper("/background1.png");
            setPreference("pref_app_text_color", whiteTextColor);
            setPreference("pref_shortcut_text_color", whiteTextColor);
            setPreference("pref_folder_text_color", lightFolderColor);
        } else {
            setWallpaper("/background2.png");
            setPreference("pref_app_text_color", blackTextColor);
            setPreference("pref_shortcut_text_color", blackTextColor);
            setPreference("pref_folder_text_color", darkFolderColor);
        }
        SystemClock.sleep(150);
    }

}