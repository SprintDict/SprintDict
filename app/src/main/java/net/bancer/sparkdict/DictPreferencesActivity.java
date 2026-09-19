package net.bancer.sparkdict;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

/**
 * DictPreferencesActivity displays a list of editable preferences.
 */
public class DictPreferencesActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dict_preferences);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        applySystemBarsInsets(findViewById(R.id.dict_preferences_top_layout));

        if (savedInstanceState == null) {
            Fragment fragment = new DictPreferencesFragment();
            getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.preferences_container, fragment)
                .commit();
        }
    }
}
