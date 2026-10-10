package net.bancer.sparkdict.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class SparkDictPreferencesTest {

    private SparkDictPreferences preferences;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        preferences = new SparkDictPreferences(context);
        // Clear custom dict names map before each test
        preferences.saveCustomDictNames(null);
    }

    @Test
    public void testGetAndSaveCustomDictName() {
        boolean saved = preferences.saveCustomDictName("Dict 1", "Custom Name 1");
        assertTrue(saved);

        Map<String, String> names = preferences.getCustomDictNames();
        assertEquals(1, names.size());
        assertEquals("Custom Name 1", names.get("Dict 1"));

        // Remove custom dict name by setting to empty
        preferences.saveCustomDictName("Dict 1", "");
        names = preferences.getCustomDictNames();
        assertFalse(names.containsKey("Dict 1"));
    }

    @Test
    public void testGetAndSaveCustomDictNamesMap() {
        Map<String, String> inputMap = new HashMap<>();
        inputMap.put("Dict A", "Custom A");
        inputMap.put("Dict B", "Custom B");

        boolean saved = preferences.saveCustomDictNames(inputMap);
        assertTrue(saved);

        Map<String, String> loadedMap = preferences.getCustomDictNames();
        assertEquals(2, loadedMap.size());
        assertEquals("Custom A", loadedMap.get("Dict A"));
        assertEquals("Custom B", loadedMap.get("Dict B"));
    }
}
