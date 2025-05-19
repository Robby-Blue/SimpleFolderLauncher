package me.robbyblue.mylauncher;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.InputStream;

import me.robbyblue.mylauncher.search.SearchActivity;
import me.robbyblue.mylauncher.settings.SettingsActivity;
import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.locale.LocaleTestRule;

@RunWith(AndroidJUnit4.class)
public class ScreenshotTest {

    @Rule
    public final LocaleTestRule localeTestRule = new LocaleTestRule();

    @Test
    public void takeScreenshotOfMainScreen() throws InterruptedException {
        InputStream input = getClass().getResourceAsStream("/data_screenshot.json");
        FileDataStorage fs = FileDataStorage.getInstance();
        fs.loadFromInputStream(input);
        ActivityScenario.launch(MainActivity.class);

        Thread.sleep(1000);

        Screengrab.screenshot("main_screen");
    }

    @Test
    public void takeScreenshotOfSearch() {
        ActivityScenario.launch(SearchActivity.class);

        onView(withId(R.id.search_bar))
                .perform(click(), typeText("youtube"));

        Screengrab.screenshot("search");
    }

    @Test
    public void takeScreenshotOfSettings() {
        ActivityScenario.launch(SettingsActivity.class);

        Screengrab.screenshot("settings");
    }
}