package com.example.pathsocial;

import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class UserPathsActivity extends Activity {
    private TextView username_text;
    private String title;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.activity_user_paths);
        Intent intent = getIntent();
        ArrayList<String> user_paths_array = (ArrayList<String>)intent.getSerializableExtra("paths");
        String user_name = (String)intent.getSerializableExtra("user_name");
        username_text = findViewById(R.id.user_paths_title);
        title = "Created by: " + user_name;

        ArrayAdapter adapter = new ArrayAdapter<String>(this,
                R.layout.activity_listview, user_paths_array);

        ListView listView = (ListView) findViewById(R.id.user_paths_list);
        listView.setAdapter(adapter);
        username_text.setText(title);
    }
}
