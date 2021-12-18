package com.example.pathsocial;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.HashMap;

public class UserPathsActivity extends AppCompatActivity {
    private TextView username_text;
    private String title;
    private Button mapViewButton;
    private Button fab;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_user_paths);
        Intent intent = getIntent();
        // get data from intent
        ArrayList<String> user_paths_array = (ArrayList<String>)intent.getSerializableExtra("paths");
        String user_name = (String)intent.getSerializableExtra("user_name");
        ArrayList<String> user_path_keys_array = (ArrayList<String>)intent.getSerializableExtra("path_keys");

        username_text = findViewById(R.id.user_paths_title);
        mapViewButton = findViewById(R.id.user_paths_map_btn);
        fab = (Button) findViewById(R.id.go_back);

        title = "Created by: " + user_name;

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, user_paths_array);

        ListView listView = (ListView) findViewById(R.id.user_paths_list);
        listView.setAdapter(adapter);
        username_text.setText(title);

        mapViewButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent mapViewIntent = new Intent(getApplicationContext(), UserPathsMapActivity.class);
                mapViewIntent.putExtra("paths", user_paths_array);
                mapViewIntent.putExtra("user_name", user_name);
                mapViewIntent.putExtra("path_keys", user_path_keys_array);
                startActivity(mapViewIntent);
                UserPathsActivity.this.finish();
            }
        });

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UserPathsActivity.this.finish();
            }
        });

    }
}
