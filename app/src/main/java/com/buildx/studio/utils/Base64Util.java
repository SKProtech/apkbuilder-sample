package com.buildx.studio.utils;

import android.util.Base64;
import java.io.UnsupportedEncodingException;

public class Base64Util {

    public static final String encode(String s) {
        try {
            return Base64.encodeToString(s.getBytes("UTF-8"), Base64.DEFAULT);
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static final String decode(String s) {
        try {
            return new String(Base64.decode(s, Base64.DEFAULT), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }
}

