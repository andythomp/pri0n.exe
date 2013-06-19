package com.paradopolis.global;

import java.util.Hashtable;

import android.content.Context;
import android.graphics.Typeface;
import android.util.Log;

/**
 * TypeFace wrapper, adds more detailed error messages and makes it simpler
 * to create and utilize type faces.
 * @author Andrew Thompson
 *
 */
public class Typefaces {
    private static final String TAG = "Typefaces";

    private static final Hashtable<String, Typeface> cache = new Hashtable<String, Typeface>();

    
    /**
     * Given a context and assetPath, will create and return the appropriate Type face.
     * If not found, returns null and logs errors.
     * @param c - Context to be utilized.
     * @param assetPath - Asset path of the Typeface
     * @return - Typeface
     */
    public static Typeface get(Context c, String assetPath) {
        synchronized (cache) {
            if (!cache.containsKey(assetPath)) {
                try {
                    Typeface t = Typeface.createFromAsset(c.getAssets(),
                            assetPath);
                    cache.put(assetPath, t);
                } catch (Exception e) {
                    Log.e(TAG, "Could not get typeface '" + assetPath
                            + "' because " + e.getMessage());
                    return null;
                }
            }
            return cache.get(assetPath);
        }
    }
}