package io.github.izzyleung.zhihudailypurify.ui.activity;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import io.github.izzyleung.zhihudailypurify.R;
import io.github.izzyleung.zhihudailypurify.ui.dialog.ApacheLicenseDialog;

@SuppressWarnings("deprecation")
public class PrefsActivity extends PreferenceActivity implements Preference.OnPreferenceClickListener {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs);

        //noinspection ConstantConditions
        findPreference("about").setOnPreferenceClickListener(this);

        PackageManager pm = getPackageManager();
        try {
            if (pm != null) {
                pm.getPackageInfo("com.zhihu.android", PackageManager.GET_ACTIVITIES);
            }
        } catch (PackageManager.NameNotFoundException e) {
            //noinspection ConstantConditions
            ((PreferenceCategory) findPreference("settings_settings")).removePreference(findPreference("using_client?"));
        }
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        //noinspection ConstantConditions
        if (preference.getKey().equals("about")) {
            new ApacheLicenseDialog(PrefsActivity.this).show();
            return true;
        }

        return false;
    }
}