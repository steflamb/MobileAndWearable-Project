package com.example.pathsocial;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        boolean runningQOrLater =
                android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q;

        // Ask for activity recognition permission
        if (runningQOrLater) {
            getActivity();
        }
        mAuth=FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(MainActivity.this, MapPage.class));
            finish();
        }
        setContentView(R.layout.activity_main);

        Button logB = findViewById(R.id.buttonPosition);
        Button regB = findViewById(R.id.button2);

        logB.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,LoginActivity.class )));
        regB.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,RegisterActivity.class )));

    }

    // Ask for permission
    private void getActivity() {
        final int REQUEST_ACTIVITY_RECOGNITION_PERMISSION = 45;
        if (ActivityCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACTIVITY_RECOGNITION},
                    REQUEST_ACTIVITY_RECOGNITION_PERMISSION);
        } else {
            return;        }
    }
}