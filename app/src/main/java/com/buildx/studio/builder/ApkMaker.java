package com.buildx.studio.builder;

import android.os.AsyncTask;
import java.io.File;
import android.content.Context;
import java.util.List;
import java.util.ArrayList;
import android.content.res.AssetManager;
import java.io.InputStream;
import java.io.IOException;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;
import com.buildx.studio.utils.FileUtil;
import java.io.OutputStream;
import java.io.FileOutputStream;
import org.apache.commons.io.IOUtils;
import net.lingala.zip4j.core.ZipFile;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import android.os.Build;
import net.lingala.zip4j.model.FileHeader;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

public class ApkMaker extends AsyncTask<String, String, String> {

    private BuildCallback callback;
    private String name, packageName, versionName, versionCode, minSdkVersion, targetSdkVersion = null;
    private boolean isDebug, isMinify, isJava8 = false;
    private File project;
    private Context c;
    private File bin;
    private File libs;
    private File sdk;
    private File keys;
    private File jars;
    private List<File> allLibs = new ArrayList<>();
    private File buildBin;
    private File lib;
    private File gen;
    private File classes;
    private File dexes;
    private File temp;
    
    public ApkMaker(Context c) {
        this.c = c;
    }

    public void setProjectDir(String str) {
        this.project = new File(str);
    }
    
    public void setBuildListener(BuildCallback callback) {
        this.callback = callback;
    }

    public void build() {
        bin = new File(c.getFilesDir(), "bin");
        libs = new File(c.getFilesDir(), "libs");
        sdk = new File(c.getFilesDir(), "sdk");
        keys = new File(c.getFilesDir(), "key");
        jars = new File(c.getFilesDir(), "jars");
        buildBin = new File(project, "app/src/build/bin");
        lib = new File(project, "app/src/build/lib");
        gen = new File(project, "app/src/build/bin/gen");
        classes = new File(project, "app/src/build/bin/classes");
        dexes = new File(project, "app/src/build/bin/dexes");
        temp = new File(project, "app/src/build/bin/temp");
        this.execute();
    }

    @Override
    protected void onPreExecute() {
        callback.onStart();
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String[] p1) {
        
        try {
            FileUtil.deleteFile(buildBin.getAbsolutePath());
            if(!bin.exists())bin.mkdirs();
            if(!libs.exists())libs.mkdirs();
            if(!sdk.exists())sdk.mkdirs();
            if(!keys.exists())keys.mkdirs();
            if(!jars.exists())jars.mkdirs();
            if(!buildBin.exists())buildBin.mkdirs();
            if(!gen.exists())gen.mkdirs();
            if(!classes.exists())classes.mkdirs();
            if(!dexes.exists())dexes.mkdirs();
            if(!temp.exists())temp.mkdirs();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        if (!new File(bin, "aapt").exists()) {
            try {
                InputStream input = c.getAssets().open("bin/" + getAaptName());
                OutputStream output = new FileOutputStream(new File(bin, "aapt"));
                IOUtils.copy(input, output);
                input.close();
                output.close();
                new File(bin, "aapt").setExecutable(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!new File(c.getFilesDir(), "proguard.txt").exists()) {
            try {
                InputStream input = c.getAssets().open("proguard.txt");
                OutputStream output = new FileOutputStream(new File(c.getFilesDir(), "proguard.txt"));
                IOUtils.copy(input, output);
                input.close();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (sdk.listFiles().length == 0) {
            try {
                InputStream input = c.getAssets().open("libs/sdk.zip");
                OutputStream output = new FileOutputStream(new File(c.getFilesDir(), "sdk.zip"));
                IOUtils.copy(input, output);
                input.close();
                output.close();
                ZipFile zipFile = new ZipFile(new File(c.getFilesDir(), "sdk.zip").getAbsolutePath());
                zipFile.extractAll(sdk.getAbsolutePath());
                new File(c.getFilesDir(), "sdk.zip").delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (libs.listFiles().length == 0) {
            try {
                InputStream input = c.getAssets().open("libs/appcompat.zip");
                OutputStream output = new FileOutputStream(new File(c.getFilesDir(), "appcompat.zip"));
                IOUtils.copy(input, output);
                input.close();
                output.close();
                ZipFile zipFile = new ZipFile(new File(c.getFilesDir(), "appcompat.zip").getAbsolutePath());
                zipFile.extractAll(libs.getAbsolutePath());
                new File(c.getFilesDir(), "appcompat.zip").delete();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!new File(keys, "testkey.x509.pem").exists()) {
            try {
                InputStream input = c.getAssets().open("key/testkey.x509.pem");
                OutputStream output = new FileOutputStream(new File(keys, "testkey.x509.pem"));
                IOUtils.copy(input, output);
                input.close();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!new File(keys, "testkey.pk8").exists()) {
            try {
                InputStream input = c.getAssets().open("key/testkey.pk8");
                OutputStream output = new FileOutputStream(new File(keys, "testkey.pk8"));
                IOUtils.copy(input, output);
                input.close();
                output.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (jars.listFiles().length == 0) {
            try {
                String[] list = c.getAssets().list("jars");
                for (String s : list) {
                    InputStream input = c.getAssets().open("jars/" + s);
                    OutputStream output = new FileOutputStream(new File(jars, s));
                    IOUtils.copy(input, output);
                    input.close();
                    output.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            JSONObject json = new JSONObject(FileUtil.readFile(new File(project, "app/src/build/setting").getAbsolutePath()));
            name = json.getString("name");
            packageName = json.getString("packageName");
            versionName = json.getString("versionName");
            versionCode = json.getString("versionCode");
            minSdkVersion = json.getString("minSdkVersion");
            targetSdkVersion = json.getString("targetSdkVersion");
            isDebug = Boolean.parseBoolean(json.getString("debug"));
            isMinify = Boolean.parseBoolean(json.getString("minify"));
            isJava8 = Boolean.parseBoolean(json.getString("java8"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        
        try {
            for (String s : readFile(new File(project, "app/src/build/libs").getAbsolutePath())) {
                if (!allLibs.contains(s)) {
                    allLibs.add(new File(s));
                }
            }
            for (File f : libs.listFiles()) {
                if (!allLibs.contains(f.getName())) {
                    allLibs.add(f);
                }
            }
            StringBuilder s = new StringBuilder();
            for (File f : allLibs) {
                s.append(f.getAbsolutePath());
                s.append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        try {
            publishProgress("Manifest is merging...");
            mergeManifest();
            
            publishProgress("Aapt is runing...");
            runAapt();
            
            String buildConfig = "/**\r\n * Automatically generated file. DO NOT MODIFY\r\n */\r\npackage %pack%;\r\n\r\npublic final class BuildConfig {\r\n  public static final boolean DEBUG = Boolean.parseBoolean(\"%debug%\");\r\n}\r\n".replace("%pack%", packageName).replace("%debug%", String.valueOf(isDebug));
            FileUtil.writeFile(gen.getAbsolutePath() + "/" + packageName.replace(".", "/") + "/BuildConfig.java", buildConfig);
            
            publishProgress("Java is compiling...");
            runEcj();
            
            if (!isMinify) {
                publishProgress("Dexing...");
                runDex();
            }
            
            publishProgress("Merging classes...");
            if (isMinify) {
                mergeDexR8();
            } else {
                mergeDex();
            }
            
            if (!isMinify) {
                extractFileFromJars();
            }
            
            publishProgress("Building Apk...");
            addFilesInApk();
            
            publishProgress("Sign Apk...");
            signApk();
        } catch (Exception e) {
            e.printStackTrace();
            this.cancel(true);
            return e.getMessage();
        }
        
        return "";
    }
    
    private void signApk() throws Exception {
        StringBuilder cmd = new StringBuilder();
        cmd.append("dalvikvm -Xcompiler-option --compiler-filter=speed -Xmx256m -cp " + jars.getAbsolutePath() + "/apksigner.jar" + " com.android.apksigner.ApkSignerTool");
        cmd.append(" sign");
        cmd.append(" --key " + keys.getAbsolutePath() + "/testkey.pk8");
        cmd.append(" --cert " + keys.getAbsolutePath() + "/testkey.x509.pem");
        cmd.append(" --out " + buildBin.getAbsolutePath() + "/app-signed.apk");
        cmd.append(" --in " + buildBin.getAbsolutePath() + "/app.apk");
        Process dexProcess = Runtime.getRuntime().exec(cmd.toString());
        String error = readInputStreem(dexProcess.getErrorStream());
        if (!error.isEmpty()) {
            if (error.contains("ERROR:")) {
                throw new Exception(error);
            }
        }
    }
    
    private void addFilesInApk() throws Exception {
        List<File> allLib = new ArrayList<>();
        for (File f : allLibs) {
            File lib = new File(f, "lib");
            if (lib.exists()) {
                allLib.add(lib);
            }
        }
        try {
            ZipFile zip = new ZipFile(buildBin.getAbsolutePath() + "/app.apk");
            ZipParameters zipParameters = new ZipParameters();
            zipParameters.setCompressionMethod(Zip4jConstants.COMP_DEFLATE);
            zipParameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_NORMAL);
            for (File f : dexes.listFiles()) {
                if (f.isDirectory()) {
                    zip.addFolder(f, zipParameters);
                } else {
                    zip.addFile(f, zipParameters);
                }
            }
            if (lib.exists()) {
                if (lib.listFiles().length != 0) {
                    zip.addFolder(lib, zipParameters);
                }
            }
            for (File f : allLib) {
                zip.addFolder(f, zipParameters);
            }
        } catch (Exception e) {
            throw e;
        }
    }
    
    private void extractFileFromJars() {
        List<String> allJar = new ArrayList<>();
        for (File f : allLibs) {
            File jar = new File(f, "classes.jar");
            if (jar.exists()) {
                allJar.add(jar.getAbsolutePath());
            }
        }
        try {
            for (String s : allJar) {
                ZipFile zip = new ZipFile(s);
                for (FileHeader each : (List<FileHeader>) zip.getFileHeaders()) {
                    String fileName = each.getFileName();
                    if (fileName.endsWith(".class")) {
                        continue;
                    }
                    if (each.isDirectory()) {
                        continue;
                    } 
                    zip.extractFile(fileName, dexes.getAbsolutePath());
                }  
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void mergeDexR8() throws Exception {
        List<String> allJar = new ArrayList<>();
        for (File f : allLibs) {
            File jar = new File(f, "classes.jar");
            if (jar.exists()) {
                allJar.add(jar.getAbsolutePath());
            }
        }
        List<String> allPro = new ArrayList<>();
        for (File f : allLibs) {
            File pro = new File(f, "proguard.txt");
            if (pro.exists()) {
                allPro.add(pro.getAbsolutePath());
            }
        }
        StringBuilder cmd = new StringBuilder();
        cmd.append("dalvikvm -Xcompiler-option --compiler-filter=speed -Xmx512m -cp " + jars.getAbsolutePath() + "/d8.jar" + " com.android.tools.r8.R8");
        cmd.append(" --release");
        cmd.append(" --lib " + sdk.getAbsolutePath() + "/android.jar");
        if (isJava8) {
            cmd.append(" --classpath " + sdk.getAbsolutePath() + "/rt.jar");
        } else {
            cmd.append(" --no-desugaring");
        }
        cmd.append(" --min-api " + minSdkVersion);
        //cmd.append(" --no-tree-shaking");
        //cmd.append(" --no-minification");
        cmd.append(" --pg-map-output " + buildBin.getAbsolutePath() + "/mapping.txt");
        cmd.append(" --output " + dexes.getAbsolutePath());
        cmd.append(" --pg-conf " + buildBin.getAbsolutePath() + "/aapt-rules.txt");
        cmd.append(" --pg-conf " + c.getFilesDir() + "/proguard.txt");
        cmd.append(" --pg-conf " + new File(project, "app/proguard-rules.pro").getAbsolutePath());
        for (String s : allPro) {
            cmd.append(" --pg-conf " + s);
        }
        getAllFilesOfDir(".class", classes.getAbsolutePath(), cmd);
        for (String s : allJar) {
            cmd.append(" ");
            cmd.append(s);
        }
        Process dexProcess = Runtime.getRuntime().exec(cmd.toString());
        String error = readInputStreem(dexProcess.getErrorStream());
        if (!error.isEmpty()) {
            if (error.contains("Error")) {
                throw new Exception(error);
            }
        }
    }
    
    private void mergeDex() throws Exception {
        List<String> allDex = new ArrayList<>();
        for (File f : allLibs) {
            File dex = new File(f, "classes.dex");
            if (dex.exists()) {
                allDex.add(dex.getAbsolutePath());
            }
        }
        StringBuilder cmd = new StringBuilder();
        cmd.append("dalvikvm -Xcompiler-option --compiler-filter=speed -Xmx256m -cp " + jars.getAbsolutePath() + "/d8.jar" + " com.android.tools.r8.D8");
        cmd.append(" --release");
        cmd.append(" --min-api " + minSdkVersion);
        cmd.append(" --output " + dexes.getAbsolutePath());
        cmd.append(" --intermediate");
        getAllFilesOfDir(".dex", classes.getAbsolutePath(), cmd);
        for (String s : allDex) {
            cmd.append(" ");
            cmd.append(s);
        }
        Process dexProcess = Runtime.getRuntime().exec(cmd.toString());
        String error = readInputStreem(dexProcess.getErrorStream());
        if (!error.isEmpty()) {
            if (error.contains("Error")) {
                throw new Exception(error);
            }
        }
    }
    
    private void runDex() throws Exception {
        StringBuilder cmd = new StringBuilder();
        cmd.append("dalvikvm -Xcompiler-option --compiler-filter=speed -Xmx256m -cp " + jars.getAbsolutePath() + "/d8.jar" + " com.android.tools.r8.D8");
        cmd.append(" --release");
        cmd.append(" --lib " + sdk.getAbsolutePath() + "/android.jar");
        cmd.append(" --min-api " + minSdkVersion);
        if (isJava8) {
            cmd.append(" --classpath " + sdk.getAbsolutePath() + "/rt.jar");
        } else {
            cmd.append(" --no-desugaring");
        }
        cmd.append(" --output " + classes.getAbsolutePath());
        cmd.append(" --intermediate");
        cmd.append(" --file-per-class");
        getAllFilesOfDir(".class", classes.getAbsolutePath(), cmd);
        Process dexProcess = Runtime.getRuntime().exec(cmd.toString());
        String error = readInputStreem(dexProcess.getErrorStream());
        if (!error.isEmpty()) {
            if (error.contains("Error")) {
                throw new Exception(error);
            }
        }
    }
    
    private void getAllFilesOfDir(String s, String path, StringBuilder sb) {
        File file = new File(path);
        for (File x : file.listFiles()) {
            if (x.isDirectory()) {
                getAllFilesOfDir(s, x.getAbsolutePath(), sb);
            } else if (x.isFile()) {
                if (x.getName().endsWith(s)) {
                    sb.append(" ");
                    sb.append(x.getAbsolutePath());
                }
            }
        }
    }
    
    private void runEcj() throws Exception {
        StringBuilder allJars = new StringBuilder();
        for (File f : allLibs) {
            File jar = new File(f, "classes.jar");
            if (jar.exists()) {
                if (allJars.length() == 0) {
                    allJars.append(jar.getAbsolutePath());
                } else {
                    allJars.append(":");
                    allJars.append(jar.getAbsolutePath());
                }
            }
        }
        StringBuilder allJava = new StringBuilder();
        for (File f : allLibs) {
            File java = new File(f, "java");
            if (java.exists()) {
                if (allJava.length() == 0) {
                    allJava.append(java.getAbsolutePath());
                } else {
                    allJava.append(":");
                    allJava.append(java.getAbsolutePath());
                }
            }
        }
        List<String> allsource = new ArrayList<>();
        for (File f : allLibs) {
            File s = new File(f, "java");
            if (s.exists()) {
                allsource.add(s.getAbsolutePath());
            }
        }
        StringWriter errResult = new StringWriter();
        PrintWriter errWriter = new PrintWriter(errResult);
        List<String> cmd = new ArrayList<>();
        cmd.add("-proc:none");
        cmd.add("-nowarn");
        if (isJava8) {
            cmd.add("-8");
        } else {
            cmd.add("-7");
        }
        cmd.add("-deprecation");
        cmd.add("-d");
        cmd.add(classes.getAbsolutePath());
        cmd.add("-bootclasspath");
        cmd.add(sdk.getAbsolutePath() + "/android.jar");
        cmd.add("-cp");
        cmd.add(allJars.toString());
        if (isJava8) {
            cmd.add("-cp");
            cmd.add(sdk.getAbsolutePath() + "/rt.jar");
        }
        cmd.add("-sourcepath");
        cmd.add(gen.getAbsolutePath() + ":" + new File(project, "app/src/main/java").getAbsolutePath() + ":" + allJava.toString());
        cmd.add(gen.getAbsolutePath());
        cmd.add(new File(project, "app/src/main/java").getAbsolutePath());
        for (String s : allsource) {
            cmd.add(s);
        }
        Compiler cm = new Compiler();
        cm.main(c, cmd.toArray(new String[0]), errWriter);
        String error = errResult.toString();
        errWriter.close();
        if (!cm.isErrors()) {
            throw new Exception(error);
        }
    }
    
    private void runAapt() throws Exception {
        List<String> allRes = new ArrayList<>();
        for (File f : allLibs) {
            File res = new File(f, "res");
            if (res.exists()) {
                allRes.add(res.getAbsolutePath());
            }
        }
        List<String> allAssets = new ArrayList<>();
        for (File f : allLibs) {
            File assets = new File(f, "assets");
            if (assets.exists()) {
                allAssets.add(assets.getAbsolutePath());
            }
        }
        StringBuilder allPackages = new StringBuilder();
        for (File f : allLibs) {
            File pack = new File(f, "package.txt");
            if (pack.exists()) {
                if (allPackages.length() == 0) {
                    allPackages.append(FileUtil.readFile(pack.getAbsolutePath()));
                } else {
                    allPackages.append(":");
                    allPackages.append(FileUtil.readFile(pack.getAbsolutePath()));
                }
            }
        }
        List<String> ch = new ArrayList<>();
        ch.add("chmod");
        ch.add("744");
        ch.add(bin.getAbsolutePath() + "/aapt");
        Runtime.getRuntime().exec(ch.toArray(new String[0]));
        
        List<String> cmd = new ArrayList<>();
        cmd.add(bin.getAbsolutePath() + "/aapt");
        cmd.add("package");
        cmd.add("-f");
        cmd.add("--auto-add-overlay");
        cmd.add("-M");
        cmd.add(buildBin.getAbsolutePath() + "/AndroidManifest.xml");
        cmd.add("-F");
        cmd.add(buildBin.getAbsolutePath() + "/app.apk");
        cmd.add("-I");
        cmd.add(sdk.getAbsolutePath() + "/android.jar");
        if (new File(project, "app/src/main/assets").exists()) {
            cmd.add("-A");
            cmd.add(new File(project, "app/src/main/assets").getAbsolutePath());
        }
        if (new File(project, "app/src/main/res").exists()) {
            cmd.add("-S");
            cmd.add(new File(project, "app/src/main/res").getAbsolutePath());
        }
        cmd.add("-m"); 
        cmd.add("-J");
        cmd.add(gen.getAbsolutePath());
        for (String s : allRes) {
            cmd.add("-S");
            cmd.add(s);
        }
        for (String s : allAssets) {
            cmd.add("-A");
            cmd.add(s);
        }
        cmd.add("--extra-packages");
        cmd.add(allPackages.toString());
        cmd.add("--min-sdk-version");
        cmd.add(minSdkVersion);
        cmd.add("--target-sdk-version");
        cmd.add(targetSdkVersion);
        cmd.add("--version-code");
        cmd.add(versionCode);
        cmd.add("--version-name");
        cmd.add(versionName);
        cmd.add("--no-version-vectors");
        cmd.add("-G");
        cmd.add(buildBin.getAbsolutePath() + "/aapt-rules.txt");
        Process aaptProcess = Runtime.getRuntime().exec(cmd.toArray(new String[0]));
        String error = readInputStreem(aaptProcess.getErrorStream());
        if (!error.isEmpty()) {
            throw new Exception(error);
        }
    }
    
    private void mergeManifest() throws Exception {
        String main = project.getAbsolutePath() + "/app/src/main/AndroidManifest.xml";
        String output = buildBin.getAbsolutePath() + "/AndroidManifest.xml";
        int i = 0;
        for (File f : allLibs) {
            File f2 = new File(f, "AndroidManifest.xml");
            if (f2.exists()) {
                i++;
                FileUtil.writeFile(temp.getAbsolutePath() + "/" + i + ".xml", FileUtil.readFile(f2.getAbsolutePath()).replace("${applicationId}", packageName));
            }
        }
        List<String> manifests = new ArrayList<>();
        for (File f : temp.listFiles()) {
            manifests.add(f.getAbsolutePath());
        }
        String error = Merger.merge(c, output, main, new String[0], manifests.toArray(new String[0]));
        if (error != null) {
            throw new Exception(error);
        }
    }
    
    private String getAaptName() {
        String name = "";
        if (Build.CPU_ABI.toLowerCase().matches("x86")) {
            name = "aapt-x86";
        } else {
            name = "aapt-arm";
        }
        return name;
    }
    
    private String readInputStreem(InputStream in) {
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
                sb.append('\n');
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb.toString().trim();
    }
    
    private List<String> readFile(String str) {
        List<String> list = new ArrayList<>();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(str)));
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                list.add(line);
            }
            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    protected void onProgressUpdate(String[] values) {
        callback.onProgress(values[0]);
        super.onProgressUpdate(values);
    }

    @Override
    protected void onCancelled(String result) {
        if (!result.isEmpty()) {
            callback.onFailure(result);
        }
        super.onCancelled(result);
    }

    @Override
    protected void onPostExecute(String result) {
        callback.onSuccess(null);
        super.onPostExecute(result);
    }

}

