package com.android.masterdistributormdl.gskDistributor.signature;

import android.view.ViewTreeObserver;

public class ViewTreeObserverCompat {


    public static void removeOnGlobalLayoutListener(ViewTreeObserver observer, ViewTreeObserver.OnGlobalLayoutListener victim) {

        observer.removeOnGlobalLayoutListener(victim);
    }
}
