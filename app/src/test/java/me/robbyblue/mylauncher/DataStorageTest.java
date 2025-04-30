package me.robbyblue.mylauncher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.io.InputStream;

import me.robbyblue.mylauncher.files.Folder;
import me.robbyblue.mylauncher.files.icons.AppIconData;
import me.robbyblue.mylauncher.files.icons.NoIconData;

public class DataStorageTest {

    @Test
    public void reads_json() {
        InputStream input = getClass().getResourceAsStream("/data_v0.json");
        FileDataStorage fs = FileDataStorage.getInstance(input);
        Folder homeFolder = fs.getFolderContents("~");
        assertEquals("Folder1", homeFolder.getFiles().get(0).getName());
        assertEquals("App1", homeFolder.getFiles().get(1).getName());

        assertTrue(homeFolder.getFiles().get(0).getIconData() instanceof NoIconData);
        assertTrue(homeFolder.getFiles().get(1).getIconData() instanceof AppIconData);

        assertEquals(2, homeFolder.getWidgetList().getChildren().size());
    }

}