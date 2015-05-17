/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2014-2015 Umeng, Inc
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.simple.injector;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;

import org.simple.injector.adapter.NullAdapter;
import org.simple.injector.adapter.InjectAdapter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * @author mrsimple
 */
public final class SimpleDagger {
    /**
     * 
     */
    public static final String SUFFIX = "$InjectAdapter";
    /**
     * 
     */
    static Map<Class<?>, InjectAdapter<?>> sInjectCache = new HashMap<Class<?>, InjectAdapter<?>>();

    /**
     * @param activity
     */
    public static void inject(Activity activity) {
        InjectAdapter<Activity> adapter = getViewAdapter(activity.getClass());
        adapter.injects(activity);
    }

    /**
     * @param fragment
     */
    public static void inject(Fragment fragment, View rootView) {
        InjectAdapter<Fragment> adapter = getViewAdapter(fragment.getClass());
        if (fuckTheFragment(fragment, rootView)) {
            adapter.injects(fragment);
        }
    }

    /**
     * 到Fragment 中找到mView字段,然后设置rootView给mView
     * 
     * @param fragment
     * @param rootView
     * @return
     */
    private static boolean fuckTheFragment(Fragment fragment, View rootView) {
        try {
            Class<?> v4fragmentClass = fragment.getClass();
            while (v4fragmentClass != Object.class
                    && !v4fragmentClass.equals(Fragment.class)) {
                v4fragmentClass = v4fragmentClass.getSuperclass();
            }
            Field rootViewField = v4fragmentClass.getDeclaredField("mView");
            rootViewField.setAccessible(true);
            rootViewField.set(fragment, rootView);
            Log.e("", "### getView " + fragment.getView()) ;
            return true;
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * @param view
     */
    public static void inject(View view) {

    }

    @SuppressWarnings({
            "unchecked", "rawtypes"
    })
    private static <T> InjectAdapter<T> getViewAdapter(Class<?> clazz) {
        InjectAdapter<?> adapter = sInjectCache.get(clazz);
        if (adapter == null) {
            String adapterClassName = clazz.getName() + SUFFIX;
            try {
                Class<?> adapterClass = Class.forName(adapterClassName);
                adapter = (InjectAdapter<?>) adapterClass.newInstance();
                sInjectCache.put(adapterClass, adapter);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        Log.e("", "### find adapter : " + adapter);

        return adapter == null ? new NullAdapter() : adapter;
    }
}
