package me.robbyblue.mylauncher;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import android.content.Context;
import android.os.Process;
import android.os.UserHandle;
import android.os.UserManager;

import androidx.test.core.app.ApplicationProvider;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import me.robbyblue.mylauncher.files.AppFile;
import me.robbyblue.mylauncher.files.Folder;
import me.robbyblue.mylauncher.files.icons.AppIconData;
import me.robbyblue.mylauncher.files.icons.NoIconData;

@RunWith(RobolectricTestRunner.class)
public class DataStorageTest {

    @Test
    public void reads_json_v0() {
        InputStream input = getClass().getResourceAsStream("/data_v0.json");
        FileDataStorage fs = FileDataStorage.getNewInstance(input, ApplicationProvider.getApplicationContext());

        Folder homeFolder = fs.getFolderContents("~");
        assertEquals("Folder1", homeFolder.getFiles().get(0).getName());
        assertEquals("App1", homeFolder.getFiles().get(1).getName());

        AppFile file = (AppFile) homeFolder.getFiles().get(1);
        assertEquals(file.getUser(), Process.myUserHandle());

        assertTrue(homeFolder.getFiles().get(0).getIconData() instanceof NoIconData);
        assertTrue(homeFolder.getFiles().get(1).getIconData() instanceof AppIconData);

        assertEquals(2, homeFolder.getWidgetList().getChildren().size());
    }

    /*
    tests that the editJson function works
     */
    @Test
    public void reads_json_edit() {
        InputStream input = getClass().getResourceAsStream("/data_v0.json");
        InputStream editedInput = editJson(input, (json) -> {
            return json;
        });

        FileDataStorage fs = FileDataStorage.getNewInstance(editedInput, ApplicationProvider.getApplicationContext());

        Folder homeFolder = fs.getFolderContents("~");
        assertEquals("Folder1", homeFolder.getFiles().get(0).getName());
        assertEquals("App1", homeFolder.getFiles().get(1).getName());

        AppFile file = (AppFile) homeFolder.getFiles().get(1);
        assertEquals(file.getUser(), Process.myUserHandle());

        assertTrue(homeFolder.getFiles().get(0).getIconData() instanceof NoIconData);
        assertTrue(homeFolder.getFiles().get(1).getIconData() instanceof AppIconData);

        assertEquals(2, homeFolder.getWidgetList().getChildren().size());
    }

    /*
    tests user data v0 with users
     */
    @Test
    public void reads_json_edit_v0user() {
        UserManager mockUserManager = mock(UserManager.class);
        UserHandle user1 = UserHandle.getUserHandleForUid(1);
        UserHandle user2 = UserHandle.getUserHandleForUid(2);

        when(mockUserManager.getSerialNumberForUser(user1)).thenReturn(1001L);
        when(mockUserManager.getSerialNumberForUser(user2)).thenReturn(1002L);

        Context mockContext = mock(Context.class);
        when(mockContext.getSystemService(Context.USER_SERVICE)).thenReturn(mockUserManager);

        when(mockUserManager.getUserForSerialNumber(1001L)).thenReturn(user1);
        when(mockUserManager.getUserForSerialNumber(1002L)).thenReturn(user2);

        InputStream input = getClass().getResourceAsStream("/data_v0.json");
        InputStream editedInput = editJson(input, (json) -> {
            try {
                JSONArray files = json.getJSONObject("~").getJSONArray("files");
                files.getJSONObject(1).put("userHandleSerialNumber", 1001L);
                files.getJSONObject(2).put("userHandleSerialNumber", 1002L);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
            return json;
        });

        FileDataStorage fs = FileDataStorage.getNewInstance(editedInput, mockContext);

        Folder homeFolder = fs.getFolderContents("~");
        assertEquals("Folder1", homeFolder.getFiles().get(0).getName());
        assertEquals("App1", homeFolder.getFiles().get(1).getName());

        AppFile file1 = (AppFile) homeFolder.getFiles().get(1);
        assertEquals(file1.getUser(), user1);
        AppFile file2 = (AppFile) homeFolder.getFiles().get(2);
        assertEquals(file2.getUser(), user2);

        assertTrue(homeFolder.getFiles().get(0).getIconData() instanceof NoIconData);
        assertTrue(homeFolder.getFiles().get(1).getIconData() instanceof AppIconData);

        assertEquals(2, homeFolder.getWidgetList().getChildren().size());
    }

    @Test
    public void delete_recursive() {
        InputStream input = getClass().getResourceAsStream("/data_delete_recursive.json");
        FileDataStorage fs = FileDataStorage.getNewInstance(input, ApplicationProvider.getApplicationContext());

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
        FileDataStorage fs = FileDataStorage.getNewInstance(input, ApplicationProvider.getApplicationContext());

        assertNotNull(fs.getFolderContents("~/used_folder"));
        assertNotNull(fs.getFolderContents("~/used_folder/used_subfolder"));

        assertNull(fs.getFolderContents("~/folder"));
        assertNull(fs.getFolderContents("~/folder/subfolder"));
        assertNull(fs.getFolderContents("~/folder/subfolder/subsubfolder"));
    }

    @Test
    public void delete_bad_names() {
        InputStream input = getClass().getResourceAsStream("/data_bad_names.json");
        FileDataStorage fs = FileDataStorage.getNewInstance(input, ApplicationProvider.getApplicationContext());

        Folder contents = fs.getFolderContents("~");
        assertEquals(2, contents.getFiles().size());
    }

    @Test
    public void delete_duplicate_names() {
        InputStream input = getClass().getResourceAsStream("/data_duplicate_names.json");
        FileDataStorage fs = FileDataStorage.getNewInstance(input, ApplicationProvider.getApplicationContext());

        Folder contents = fs.getFolderContents("~");
        assertEquals(4, contents.getFiles().size());
    }

    @Test
    public void delete_bad_folders() {
        InputStream input = getClass().getResourceAsStream("/data_delete_bad_folders.json");
        FileDataStorage fs = FileDataStorage.getNewInstance(input, ApplicationProvider.getApplicationContext());

        Folder homeContents = fs.getFolderContents("~");
        assertEquals(2, homeContents.getFiles().size());
        Folder subContents = fs.getFolderContents("~");
        assertEquals(2, subContents.getFiles().size());
    }

    public static InputStream editJson(InputStream input, Function<JSONObject, JSONObject> editor) {
        try {
            String jsonStr;
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null)
                    sb.append(line);
                jsonStr = sb.toString();
            }

            JSONObject original = new JSONObject(jsonStr);
            JSONObject modified = editor.apply(original);

            byte[] outputBytes = modified.toString().getBytes(StandardCharsets.UTF_8);
            return new ByteArrayInputStream(outputBytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}