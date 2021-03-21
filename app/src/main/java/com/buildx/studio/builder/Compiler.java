package com.buildx.studio.builder;

import java.io.PrintWriter;
import dalvik.system.DexClassLoader;
import java.io.File;
import android.content.Context;
import java.lang.reflect.Method;

public class Compiler {
    
    public Compiler() {}
    
    private static boolean isErrors;
     
    public static void main(Context c, String[] cmd, PrintWriter print) {
        DexClassLoader dexClassLoader = new DexClassLoader(new File(c.getFilesDir(), "jars/ecj.jar").getAbsolutePath(), c.getDir("dex", 0).getPath(), null, ClassLoader.getSystemClassLoader());
        try {
            Class<Object> cp = (Class<Object>) dexClassLoader.loadClass("com.buildx.studio.builder.Compiler");
            Object ob = cp.newInstance();
            Method m = cp.getDeclaredMethod("main", new Class[]{String[].class, PrintWriter.class});
            m.invoke(ob, new Object[]{cmd, print});
            Method m2 = cp.getDeclaredMethod("isErrors");
            m2.setAccessible(true);
            isErrors = (boolean) m2.invoke(cp.newInstance());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static boolean isErrors() {
        return isErrors;
    }
}
