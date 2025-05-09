package me.robbyblue.mylauncher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
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
        FileDataStorage fs = FileDataStorage.getInstance();
        fs.loadFromInputStream(input);

        Folder homeFolder = fs.getFolderContents("~");
        assertEquals("Folder1", homeFolder.getFiles().get(0).getName());
        assertEquals("App1", homeFolder.getFiles().get(1).getName());

        assertTrue(homeFolder.getFiles().get(0).getIconData() instanceof NoIconData);
        assertTrue(homeFolder.getFiles().get(1).getIconData() instanceof AppIconData);

        assertEquals(2, homeFolder.getWidgetList().getChildren().size());
    }

    @Test
    public void delete_recursive() {
        InputStream input = getClass().getResourceAsStream("/data_delete_recursive.json");
        FileDataStorage fs = FileDataStorage.getInstance();
        fs.loadFromInputStream(input);

        assertNotEquals(null, fs.getFolderContents("~/folder"));
        assertNotEquals(null, fs.getFolderContents("~/folder/subfolder"));
        assertNotEquals(null, fs.getFolderContents("~/folder/subfolder/subsubfolder"));

        fs.removeFile("~", 0);

        assertNull(fs.getFolderContents("~/folder"));
        assertNull(fs.getFolderContents("~/folder/subfolder"));
        assertNull(fs.getFolderContents("~/folder/subfolder/subsubfolder"));
    }

    @Test
    public void clean_orphans() {
        InputStream input = getClass().getResourceAsStream("/data_clean_orphans.json");
        FileDataStorage fs = FileDataStorage.getInstance();
        fs.loadFromInputStream(input);

        assertNotNull(fs.getFolderContents("~/used_folder"));
        assertNotNull(fs.getFolderContents("~/used_folder/used_subfolder"));

        assertNull(fs.getFolderContents("~/folder"));
        assertNull(fs.getFolderContents("~/folder/subfolder"));
        assertNull(fs.getFolderContents("~/folder/subfolder/subsubfolder"));
    }

}