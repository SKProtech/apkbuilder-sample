package com.buildx.studio.activitys;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.buildx.studio.R;
import android.os.Build;
import android.graphics.Color;
import android.view.View;
import androidx.core.content.ContextCompat;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.content.Intent;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import android.widget.LinearLayout;
import com.google.android.material.textfield.TextInputLayout;
import android.widget.EditText;
import androidx.appcompat.app.AlertDialog;
import android.content.DialogInterface;
import com.buildx.studio.utils.Utils;
import com.buildx.studio.utils.FileUtil;
import java.io.File;
import com.buildx.studio.Const;
import org.json.JSONObject;
import org.json.JSONException;
import android.widget.Button;
import android.widget.TextView;
import com.buildx.studio.builder.ApkMaker;
import com.buildx.studio.builder.BuildCallback;
import android.app.ProgressDialog;
import android.os.PowerManager;
import android.content.Context;
import com.buildx.studio.utils.Prefs;

public class MainActivity extends AppCompatActivity {

    private FloatingActionButton fab;
    private EditText edittext;
    private Button button;
    private TextView error_textview;
    private ProgressDialog pd;
    private PowerManager pm;
    private PowerManager.WakeLock wl;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            getWindow().setStatusBarColor(Color.WHITE);
        }
        
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED
            || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        } else {
            initialize();
        }
        
        fab = findViewById(R.id.fab);
        edittext = findViewById(R.id.activitymainEditText1);
        button = findViewById(R.id.activitymainButton1);
        error_textview = findViewById(R.id.activitymainTextView1);
        
        edittext.setText(Prefs.getString("project", ""));
        
        fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View p1) {
                    showCreateProjDialog();
                }
            });
            
        button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View p1) {
                    File project = new File(edittext.getText().toString());
                    Prefs.putString("project", project.getAbsolutePath());
                    if (project.exists() || project.isDirectory() || new File(project, "app/src/build/setting").exists()) {
                        ApkMaker maker = new ApkMaker(MainActivity.this);
                        maker.setProjectDir(project.getAbsolutePath());
                        maker.setBuildListener(new BuildCallback() {
                                @Override
                                public void onStart() {
                                    pd = new ProgressDialog(MainActivity.this);
                                    pd.setCancelable(false);
                                    pd.setMessage("Please wait...");
                                    pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                                    pd.show();
                                    pm = (PowerManager)getSystemService(Context.POWER_SERVICE);
                                    wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "BUILDER");
                                    wl.acquire();
                                    error_textview.setText("no errors found.");
                                }

                                @Override
                                public void onFailure(String message) {
                                    pd.dismiss();
                                    wl.release();
                                    error_textview.setText(message);
                                }

                                @Override
                                public void onProgress(String progress) {
                                    pd.setMessage(progress);
                                }

                                @Override
                                public void onSuccess(File apk) {
                                    pd.dismiss();
                                    wl.release();
                                    Utils.toast(getApplicationContext(), "Build Complete.");
                                }
                            });
                        maker.build();
                    }
                }
            });
    }
    
    private void createProj(String n, String p) {
        final String name = n;
        final String pack = p;
        if (name.isEmpty()){
            Utils.toast(getApplicationContext(), "Project name empty");
        } else if (pack.isEmpty()){
            Utils.toast(getApplicationContext(), "Package name empty");
        } else if (!pack.contains(".")){
            Utils.toast(getApplicationContext(), "Something is wrong!");
        } else if (FileUtil.isExistFile(Const.PROJECT_DIR.getAbsolutePath() + "/" + name)){
            Utils.toast(getApplicationContext(), "A project with same name already exists" );
        } else {
            // make res path
            FileUtil.makeDir(Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/build/lib");
            FileUtil.makeDir(Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/assets/");
            FileUtil.makeDir(Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/res/drawable/");
            FileUtil.makeDir(Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/res/drawable-xhdpi/");
            FileUtil.makeDir(Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/res/layout");
            FileUtil.makeDir(Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/res/values");
            // make java path
            String package_path = pack.replace(".", "/") + File.separator;
            FileUtil.makeDir(Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/java/" + package_path);
            // copy res icons
            Utils.copyResources(R.drawable.app_icon, Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/res/drawable-xhdpi/" + "app_icon.png");
            // write files
            FileUtil.writeFile2(Utils.readAssest("templates/buildG2.txt"), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/build.gradle");
            FileUtil.writeFile2(Utils.readAssest("templates/settings.txt"), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/settings.gradle");
            FileUtil.writeFile2(Utils.readAssest("templates/proguard.txt"), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/proguard-rules.pro");
            FileUtil.writeFile2(Utils.readAssest("templates/buildG.txt").replace("$<YOUR APPLICATION ID>$", pack), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/build.gradle");
            // write xml files
            FileUtil.writeFile2(Utils.readAssest("templates/AndroidManifest.txt").replace("$pkg$", pack), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/AndroidManifest.xml");
            FileUtil.writeFile2(Utils.readAssest("templates/activity_main.txt"), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/res/layout/activity_main.xml");
            FileUtil.writeFile2(Utils.readAssest("templates/styles.txt"), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/res/values/styles.xml");
            FileUtil.writeFile2(Utils.readAssest("templates/colors.txt"), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/res/values/colors.xml");
            FileUtil.writeFile2(Utils.readAssest("templates/strings.txt").replace("$nam$", name), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/res/values/strings.xml");
            // write java files
            FileUtil.writeFile2(Utils.readAssest("templates/App.txt").replace("$pkg$", pack), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/java/" + package_path + "App.java");
            FileUtil.writeFile2(Utils.readAssest("templates/DebugActivity.txt").replace("$pkg$", pack), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/java/" + package_path + "DebugActivity.java");
            FileUtil.writeFile2(Utils.readAssest("templates/MainActivity.txt").replace("$pkg$", pack), Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/main/java/" + package_path + "MainActivity.java");
            // Message
            Utils.toast(getApplicationContext(), "Project Created");
            try {
                JSONObject json = new JSONObject();
                json.put("name", name);
                json.put("packageName", pack);
                json.put("versionName", "1.0");
                json.put("versionCode", "1");
                json.put("minSdkVersion", "23");
                json.put("targetSdkVersion", "30");
                json.put("debug", "false");
                json.put("minify", "false");
                json.put("java8", "false");
                FileUtil.writeFile(Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/build/setting", json.toString(4));
                FileUtil.writeFile(Const.PROJECT_DIR.getAbsolutePath() + File.separator + name + "/app/src/build/libs", "");
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    private void initialize() {
        
    }
    
    private void showCreateProjDialog() {
        LinearLayout linearlayout = new LinearLayout(this);
        linearlayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        linearlayout.setOrientation(LinearLayout.VERTICAL);
        linearlayout.setPadding(0, 0, 0, 0);

        TextInputLayout textInputLayout = new TextInputLayout(this);
        textInputLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textInputLayout.setPadding(19, 16, 19, 0);
        final EditText edittext = new EditText(this);
        edittext.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        edittext.setHint("Project Name");
        textInputLayout.addView(edittext);
        linearlayout.addView(textInputLayout);

        TextInputLayout textInputLayout2 = new TextInputLayout(this);
        textInputLayout2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        textInputLayout2.setPadding(19, 16, 19, 0);
        final EditText edittext2 = new EditText(this);
        edittext2.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        edittext2.setHint("Package Name");
        textInputLayout2.addView(edittext2);
        linearlayout.addView(textInputLayout2);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("New Project");
        builder.setView(linearlayout);
        builder.setPositiveButton("CREATE", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface p1, int p2) {
                    String name = edittext.getText().toString().trim();
                    String pack = edittext2.getText().toString().trim();
                    createProj(name, pack);
                }
            });
        builder.setNegativeButton("CANCEL", null);
        builder.create().show();
    }
    
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1000) {
            initialize();
        }
    }

    @Override
    public void onBackPressed()
    {
        new AlertDialog.Builder(this)
        .setMessage("Do you want to exit this app?")
            .setPositiveButton("YES", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface p1, int p2)
                {
                    MainActivity.super.onBackPressed();
                    finish();
                }
            })
        .setNegativeButton("NO", null)
        .create()
        .show();
    }
    
}
