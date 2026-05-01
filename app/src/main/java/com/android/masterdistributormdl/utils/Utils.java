package com.android.masterdistributormdl.utils;

import static com.android.masterdistributormdl.utils.ConstantKt.showToastShort;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Insets;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowInsets;
import android.view.WindowMetrics;

import androidx.annotation.NonNull;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

public class Utils {
    public static Bitmap getBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable drawable = view.getBackground();
        if (drawable != null) {
            drawable.draw(canvas);
        } else {
            canvas.drawColor(Color.WHITE);
        }
        view.draw(canvas);
        return bitmap;
    }
    private static Activity scanForActivity(Context context) {
        if (context instanceof Activity) return (Activity) context;
        else return scanForActivity(((ContextWrapper) context).getBaseContext());

    }

    public static void copyFile(File sourceFile, File destFile) throws IOException {
        System.out.println("COPY FILE: " + sourceFile.getAbsolutePath() + " TO: " + destFile.getAbsolutePath());
        if (!destFile.exists()) {
            destFile.createNewFile();
        }

        FileChannel sourceChannel = null;
        FileChannel destChannel = null;

        try {
            sourceChannel = new FileInputStream(sourceFile).getChannel();
            destChannel = new FileOutputStream(destFile).getChannel();
            sourceChannel.transferTo(0, sourceChannel.size(), destChannel);
        } finally {
            if (sourceChannel != null) {
                sourceChannel.close();
            }
            if (destChannel != null) {
                destChannel.close();
            }
        }
    }

    public static int getScreenWidth(@NonNull Context context) {
        Activity activity = scanForActivity(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowMetrics windowMetrics = activity.getWindowManager().getCurrentWindowMetrics();
            Insets insets = windowMetrics.getWindowInsets().getInsetsIgnoringVisibility(WindowInsets.Type.systemBars());
            return windowMetrics.getBounds().width() - insets.left - insets.right;
        } else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            return displayMetrics.widthPixels;
        }
    }



    public static void openAppSetting(Context context, String message) {
        showToastShort(message);
        Uri uri = Uri.fromParts("package", context.getPackageName(), null);
        Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(uri);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }
}
