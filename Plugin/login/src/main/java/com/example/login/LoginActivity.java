package com.example.login;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.plugin_core.BaseActivity;

public class LoginActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toast.makeText(LoginActivity.this,"Login中的MainActivity",Toast.LENGTH_SHORT).show();
        Log.e("MN-------->",getClassLoader().getClass().getName());
    }
}
