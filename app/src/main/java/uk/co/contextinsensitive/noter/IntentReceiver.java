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

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;



public class IntentReceiver extends Activity {

    private static final String text_extension = ".txt";
    private Intent intent;
    private String noteDivider;

    SharedPreferences sharedPrefs;

    private SharedPreferences getSharedPrefs() {
        if (sharedPrefs == null)
            sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this.getApplicationContext());
        return sharedPrefs;
    }

    private boolean shouldUseNoteDivider() {
        return getSharedPrefs().getBoolean(
                this.getResources().getString(R.string.separator_enable_pref),
                true);
    }

    private String getNoteDivider() {
        if (noteDivider == null)
            noteDivider = getSharedPrefs().getString(
                    this.getResources().getString(R.string.separator_content_pref),
                    this.getResources().getString(R.string.note_separators_content_default));
        return noteDivider + "\u200e";
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intent = (Intent) getIntent().clone();
        intent.setComponent(null);

        processIntentContents();

        finish();

    }



    private void writeTextToFile(String file, String text) {
        File root = android.os.Environment.getExternalStorageDirectory();


        // See http://stackoverflow.com/questions/3551821/android-write-to-sd-card-folder

        String dir = getSharedPrefs().getString(
                this.getResources().getString(R.string.save_folder_pref),"/RK Noter");

        if (dir.charAt(0) != '/') {
            dir = '/' + dir;
        }

        File dirFile = new File(root.getAbsolutePath() + dir);
        Log.d("Dir check", "About to write to file " + file + " in dir " + dir + " in path " + root.toString());
        dirFile.mkdirs();

        String fileToWrite = stringFilterAlphaNumWhitelist(file) + text_extension;


        File note = new File(dirFile, fileToWrite);
        if (!note.exists())
        try {
            // NB check not a dir
            note.createNewFile();

        } catch (IOException e) {
            Toast.makeText(this, "File not written!\n" + fileToWrite, Toast.LENGTH_LONG).show();
            e.printStackTrace();
            return;
        }

        try {
            FileWriter pw = new FileWriter(note, true);
            pw.append(text);
            pw.flush();
            pw.close();
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "File not written!\n" + fileToWrite, Toast.LENGTH_LONG).show();
            e.printStackTrace();
            Log.i("RK Noter", "******* File not found. Did you add a WRITE_EXTERNAL_STORAGE permission to the  manifest?");
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    private String stringFilterAlphaNumWhitelistRegex (String in) {
        if (in == null)
            return null;
        return in.replaceAll("\\W+,\\S+", "");
    }

    // invalid: "/\*?<>|:\
    private final static String permittedChars = " ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_[]{},;.`~+";

    private String stringFilterAlphaNumWhitelist (String in) {
        if (in == null)
            return null;


        StringBuilder cleanFile = new StringBuilder();
        for (int ch = 0; ch < in.length(); ch++) {
            String c = Character.toString(in.charAt(ch));
            if (permittedChars.contains(c))
                cleanFile.append(c);
        }
        return cleanFile.toString();
    }
    private String stringFilterByBlacklist (String in)
    {
        String forbiddenChars = "/\\";
        StringBuilder cleanFile = new StringBuilder();
        for (int ch = 0; ch < in.length(); ch++) {
            String c = Character.toString(in.charAt(ch));
            if (!forbiddenChars.contains(c))
                cleanFile.append(c);
        }
        return cleanFile.toString();
    }
    public static String getValidFileName(String fileName) {
        String newFileName = fileName.replaceAll("^[.\\\\/:*?\"<>|]?[\\\\/:*?\"<>|]*", "");
        if(newFileName.length()==0)
            throw new IllegalStateException(
                    "File Name " + fileName + " results in a empty fileName!");
        return newFileName;
    }

    private void processIntentContents() {

        try {
            Bundle intentBundle = intent.getExtras();
            if (intentBundle != null) {
                Set<String> keySet = intentBundle.keySet();

                String defaultFile = "notes";

                String file = null;
                StringBuilder data = new StringBuilder();

                for (String key : keySet) {
                    //Log.d("TEST", key);
                    Object thisObject = intentBundle.get(key);

                    if (thisObject instanceof String
                            ) {
                        if (key.equals("android.intent.extra.SUBJECT"))
                            file = thisObject.toString();
                        else if (key.equals("android.intent.extra.TEXT")) {
                            if (data.length() != 0)
                                data.append("\n");
                            data.append(thisObject.toString());
                        }
                    }
                    // RK Not needed

                           /*
                           else if (thisObject instanceof ArrayList) {

						addTextToLayout("Values: ", Typeface.ITALIC, extrasLayout);
						ArrayList thisArrayList = (ArrayList) thisObject;
						for (Object thisArrayListObject : thisArrayList) {
							addTextToLayout(thisArrayListObject.toString(),
									Typeface.ITALIC, STANDARD_INDENT_SIZE_IN_DIP,
									extrasLayout);
						}*/
                }
                if (shouldUseNoteDivider())
                    data.append(getNoteDivider());

                if (file == null) {
                    file = getLastAppName();
                }
                writeTextToFile(file, data.toString());



                boolean shouldToast = getSharedPrefs().getBoolean(
                        this.getResources().getString(R.string.confirmation_toast_enable_pref),true);

                if (shouldToast) {
                    Toast.makeText(this, "Noted!", Toast.LENGTH_SHORT).show();
                }

            }

        } catch (Exception e)

        {
            e.printStackTrace();
        }
    }

    private String getLastAppName(){
        ActivityManager am = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RecentTaskInfo> recentTasks = am.getRecentTasks(10000, ActivityManager.RECENT_WITH_EXCLUDED);

        boolean debugAppList = false;
        if (debugAppList) {
            StringBuilder sb = new StringBuilder();
            Iterator<ActivityManager.RecentTaskInfo> it = recentTasks.iterator();
            while (it.hasNext()) {
                ActivityManager.RecentTaskInfo task = it.next();
                Intent taskIntent = task.baseIntent;
                ComponentName taskComponent = taskIntent.getComponent();
                String packageName = taskComponent.getPackageName();
                sb.append(packageName);
                sb.append(", ");
            }
            Log.d("debugAppList", sb.toString());
        }



        // Task zero is the share dialog??
        ActivityManager.RecentTaskInfo callingTask = recentTasks.get(0);
        Intent callingIntent = callingTask.baseIntent;

        ComponentName componentName = callingIntent.getComponent();
        String packageName = componentName.getPackageName();

        final PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo( packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            ai = null;
        }
        final String applicationName = (String) (ai != null ? pm.getApplicationLabel(ai) : "(unknown)");



        Log.d("CallingApp", applicationName);
        return applicationName;
    }
}