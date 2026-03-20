package com.example.dramasdk;

import android.os.Bundle;

public class ApiTestActivity extends AppFragmentActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_api_test);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new ApiTestFragment()).commit();
    }
}
