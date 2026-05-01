package com.android.masterdistributormdl.gskDistributor.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
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
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.TimeUnit;

public class Utils {

    public static String timeToAgo(String dateValue) {
        String convTime = null;
        String suffix = "Ago";

        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date pasTime = dateFormat.parse(dateValue);
            Date nowTime = new Date();

            long dateDiff = nowTime.getTime() - pasTime.getTime();

            long second = TimeUnit.MILLISECONDS.toSeconds(dateDiff);
            long minute = TimeUnit.MILLISECONDS.toMinutes(dateDiff);
            long hour = TimeUnit.MILLISECONDS.toHours(dateDiff);
            long day = TimeUnit.MILLISECONDS.toDays(dateDiff);

            if (second < 60) {
                convTime = second + " Seconds " + suffix;
            } else if (minute < 60) {
                convTime = minute + " Minutes " + suffix;
            } else if (hour < 24) {
                convTime = hour + " Hours " + suffix;
            } else if (day < 8) {
                convTime = day + " Days " + suffix;
            } else {
                if (day > 360) {
                    convTime = (day / 360) + " Years " + suffix;
                } else if (day > 30) {
                    convTime = (day / 30) + " Months " + suffix;
                } else {
                    convTime = (day / 7) + " Week " + suffix;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            convTime = dateValue;
        }

        return convTime;
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

    public void openFile(Context context, File url) {

        try {

            Uri uri = Uri.fromFile(url);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (url.toString().contains(".doc") || url.toString().contains(".docx")) {

                intent.setDataAndType(uri, "application/msword");
            } else if (url.toString().contains(".pdf")) {

                intent.setDataAndType(uri, "application/pdf");
            } else if (url.toString().contains(".ppt") || url.toString().contains(".pptx")) {

                intent.setDataAndType(uri, "application/vnd.ms-powerpoint");
            } else if (url.toString().contains(".xls") || url.toString().contains(".xlsx")) {

                intent.setDataAndType(uri, "application/vnd.ms-excel");
            } else if (url.toString().contains(".zip")) {

                intent.setDataAndType(uri, "application/zip");
            } else if (url.toString().contains(".rar")) {

                intent.setDataAndType(uri, "application/x-rar-compressed");
            } else if (url.toString().contains(".rtf")) {

                intent.setDataAndType(uri, "application/rtf");
            } else if (url.toString().contains(".wav") || url.toString().contains(".mp3")) {

                intent.setDataAndType(uri, "audio/x-wav");
            } else if (url.toString().contains(".gif")) {

                intent.setDataAndType(uri, "image/gif");
            } else if (url.toString().contains(".jpg") || url.toString().contains(".jpeg") || url.toString().contains(".png")) {

                intent.setDataAndType(uri, "image/jpeg");
            } else if (url.toString().contains(".txt")) {

                intent.setDataAndType(uri, "text/plain");
            } else if (url.toString().contains(".3gp") || url.toString().contains(".mpg") ||
                    url.toString().contains(".mpeg") || url.toString().contains(".mpe") || url.toString().contains(".mp4") || url.toString().contains(".avi")) {

                intent.setDataAndType(uri, "video/*");
            } else {
                intent.setDataAndType(uri, "*/*");
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(context, "No application found which can open the file", Toast.LENGTH_SHORT).show();
        }
    }

    private static Activity scanForActivity(Context context) {
        if (context instanceof Activity) return (Activity) context;
        else return scanForActivity(((ContextWrapper) context).getBaseContext());

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

    public static boolean isGooglePlayServicesAvailable(Context context) {
        GoogleApiAvailability availability = GoogleApiAvailability.getInstance();
        int resultCode = availability.isGooglePlayServicesAvailable(context);
        return resultCode == ConnectionResult.SUCCESS;
    }

    public static void openAppSetting(Context context, String message) {
        ConstantsKt.showToastShort(message);
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
}
