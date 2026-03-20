package com.example.dramasdk;

import android.os.Looper;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public abstract class AppFragment extends Fragment {

    protected void toast(String text) {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        if (Looper.getMainLooper() == Looper.myLooper()) {
            Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
        } else {
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    protected void runOnUIThread(Runnable runnable) {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        activity.runOnUiThread(runnable);
    }
}
