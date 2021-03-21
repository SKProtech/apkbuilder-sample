package com.buildx.studio.builder;

import android.content.Context;
import dalvik.system.DexClassLoader;
import java.lang.reflect.Method;
import java.io.File;

public class Merger {
    
    private static String error;
    
    public static String merge(Context c, String s, String s2, String[] sa, String[] sa2) {
        DexClassLoader dexClassLoader = new DexClassLoader(new File(c.getFilesDir(), "jars/merger.jar").getAbsolutePath(), c.getDir("dex", 0).getPath(), null, ClassLoader.getSystemClassLoader());
        try {
            Class<Object> cp = (Class<Object>) dexClassLoader.loadClass("com.aide.merger.AndroidManifestMerger");
            Object ob = cp.newInstance();
            Method m = cp.getDeclaredMethod("merge", new Class[]{String.class, String.class, String[].class, String[].class});
            error = (String) m.invoke(ob, new Object[]{s, s2, sa, sa2});
        } catch (Exception e) {
            e.printStackTrace();
            //error = e.getMessage();
        }
        return error;
    }
}

