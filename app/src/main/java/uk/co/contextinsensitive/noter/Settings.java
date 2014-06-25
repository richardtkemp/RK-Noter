//   Copyright 2012-2014 Intrications (intrications.com)
//
//   Licensed under the Apache License, Version 2.0 (the "License");
//   you may not use this file except in compliance with the License.
//   You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
//   Unless required by applicable law or agreed to in writing, software
//   distributed under the License is distributed on an "AS IS" BASIS,
//   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//   See the License for the specific language governing permissions and
//   limitations under the License.

package uk.co.contextinsensitive.noter;

import android.annotation.TargetApi;

import android.os.Build;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;


public class Settings extends PreferenceActivity implements
        OnPreferenceChangeListener {

    //private Preference saveEnabledPreference;
    private PreferenceFragment prefFragment;

    /**
     * Wraps legacy {@link #onCreate(Bundle)} code for Android < 3 (i.e. API lvl
     * < 11).
     */
    @SuppressWarnings("deprecation")
    private void onCreatePreferenceActivity() {
        addPreferencesFromResource(R.xml.settings);

        /*saveEnabledPreference = findPreference(getString(R.string.save_intent_enable_pref));
        saveEnabledPreference.setOnPreferenceChangeListener(this);

        Preference separatorEnablePreference = findPreference(getString(R.string.separator_enable_pref));
        separatorEnablePreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent intent = ShareCompat.IntentBuilder.from(Settings.this).setChooserTitle
                        ("Select Intent Intercept").setType("plain/text")
                        .setText("Test Intent").createChooserIntent();
                startActivity(intent);
                return true;
            }
        });*/
    }

    /**
     * Wraps {@link #onCreate(Bundle)} code for Android >= 3 (i.e. API lvl >=
     * 11).
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private void onCreatePreferenceFragment() {
        prefFragment = new MyPreferenceFragment();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, prefFragment)
                .commit();

        //saveEnabledPreference = prefFragment.findPreference(getString(R.string.save_intent_enable_pref));
        //saveEnabledPreference.setOnPreferenceChangeListener(this);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
            onCreatePreferenceActivity();
        } else {
            onCreatePreferenceFragment();
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class MyPreferenceFragment extends PreferenceFragment {
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings);
        }
    }


    public boolean onPreferenceChange(Preference preference, Object newValue) {
        /* This isn't relevant right now, but is a nice reference
        if (preference == saveEnabledPreference) {

            Boolean enabled = (Boolean) newValue;
            int flag = (enabled ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                    : PackageManager.COMPONENT_ENABLED_STATE_DISABLED);
            ComponentName component = new ComponentName(Settings.this,
                    IntentReceiver.class);

            getPackageManager().setComponentEnabledSetting(component, flag,
                    PackageManager.DONT_KILL_APP);
        }*/
        return true;
    }

}
