package com.example.dramasdk;

import android.os.Looper;
import android.widget.Toast;

import androidx.fragment.app.FragmentActivity;

public class AppFragmentActivity extends FragmentActivity {

    protected void toast(String text) {
        if (Looper.getMainLooper() == Looper.myLooper()) {
            Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
        } else {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(AppFragmentActivity.this, text, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}
