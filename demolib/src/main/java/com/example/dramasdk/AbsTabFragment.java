package com.example.dramasdk;

import android.content.Context;
import android.graphics.drawable.Drawable;

public abstract class AbsTabFragment extends AppFragment {
    public abstract String getTitle(Context context);

    public abstract Drawable getIcon(Context context);
}
