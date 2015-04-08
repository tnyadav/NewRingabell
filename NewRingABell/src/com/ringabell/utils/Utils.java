package com.ringabell.utils;


import com.share2people.ringabell.R;
import com.share2people.ringabell.R.id;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

public class Utils {
    public static void setBadgeCount(Context context, LayerDrawable icon, int count) {
    	
        BadgeDrawable badge;

        // Reuse drawable if possible
        Drawable reuse = icon.findDrawableByLayerId(R.id.badge);
        if (reuse != null && reuse instanceof BadgeDrawable) {
            badge = (BadgeDrawable) reuse;
        } else {
            badge = new BadgeDrawable(context);
        }

        badge.setCount(count);
        icon.mutate();
        icon.setDrawableByLayerId(R.id.badge, badge);
    }
}