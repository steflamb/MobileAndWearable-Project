package com.example.pathsocial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    Button btn2_signup,btn_lgn;
    EditText user_name, pass_word,height_txt,weight_txt,name_txt,last_name_txt,age_txt;;
    FirebaseAuth mAuth;
    private DatabaseReference database;

    Switch mSwitch1;
    Switch mSwitch2;
    TextView unit1;
    TextView unit2;
    Boolean changed1;
    Boolean changed2;
    RadioGroup radioSexGroup;
    private RadioButton radioSexButton;



    double kg = 0.0;
    double lbs = 0.0;
    final double change2 = 2.2046226218; //this is the change between kg and lbs, 1kg = this amount of lbs
    final double change1= 2.54;

    private static final String SHARED_PREFS = "sharedPrefs";


    public static boolean isNumeric(String strNum) {
        if (strNum == null) {
            return false;
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException nfe) {
            return false;
        }
        return true;
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth=FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null) {
            startActivity(new Intent(RegisterActivity.this, MapPage.class));
            finish();
        }
        setContentView(R.layout.activity_register);
        user_name=findViewById(R.id.username);
        pass_word=findViewById(R.id.password1);
        btn2_signup=findViewById(R.id.sign);
        btn_lgn=findViewById(R.id.btn_log);
        height_txt=findViewById(R.id.height);
        weight_txt=findViewById(R.id.weight);
        name_txt=findViewById(R.id.nameTextView);
        age_txt=findViewById(R.id.age);
        mSwitch2 = findViewById(R.id.switch2);
        mSwitch1 = findViewById(R.id.switch1);
        unit1=findViewById(R.id.unit1);
        unit2=findViewById(R.id.unit2);
        radioSexGroup=(RadioGroup)findViewById(R.id.radioSex);
        unit2.setText("Lbs");
        unit1.setText("inches");
        changed1=false;
        changed2=false;



        mSwitch2.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean convertToKg) {
                if(convertToKg){
                    unit2.setText("Kg");
                    changed2=true;

                }else{
                    unit2.setText("Lbs");
                    changed2=false;
                }
            }
        });

        mSwitch1 = findViewById(R.id.switch1);


        mSwitch1.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean convertToCm) {

                if(convertToCm){
                    unit1.setText("cm");
                    changed1=true;

                }else{
                    unit1.setText("inches");
                    changed1=false;
                }
            }
        });



        btn_lgn.setOnClickListener(v -> startActivity(new Intent(RegisterActivity.this,LoginActivity.class )));
        btn2_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = user_name.getText().toString().trim();
                String password= pass_word.getText().toString().trim();
                String height=height_txt.getText().toString().trim();
                String weight=weight_txt.getText().toString().trim();
                String name=name_txt.getText().toString().trim();
                int selectedId=radioSexGroup.getCheckedRadioButtonId();
                radioSexButton=(RadioButton)findViewById(selectedId);
                String sex=radioSexButton.getText().toString().trim();
                String age=age_txt.getText().toString().trim();

                if(email.isEmpty())
                {
                    user_name.setError("Email is empty");
                    user_name.requestFocus();
                    return;
                }
                if(name.isEmpty())
                {
                    name_txt.setError("Name is empty");
                    name_txt.requestFocus();
                    return;
                }
                if(height.isEmpty())
                {
                    height_txt.setError("Height is empty");
                    height_txt.requestFocus();
                    return;
                }
                if(weight.isEmpty())
                {
                    weight_txt.setError("Weight is empty");
                    weight_txt.requestFocus();
                    return;
                }
                if(!Patterns.EMAIL_ADDRESS.matcher(email).matches())
                {
                    user_name.setError("Enter the valid email address");
                    user_name.requestFocus();
                    return;
                }
                if(password.isEmpty())
                {
                    pass_word.setError("Enter the password");
                    pass_word.requestFocus();
                    return;
                }
                if(password.length()<6)
                {
                    pass_word.setError("Length of the password should be more than 6");
                    pass_word.requestFocus();
                    return;
                }
                if(!isNumeric(weight))
                {
                    weight_txt.setError("Check your weight!");
                    weight_txt.requestFocus();
                    return;
                }
                if(!isNumeric(height))
                {
                    height_txt.setError("Check your height!");
                    height_txt.requestFocus();
                    return;
                }
                if(!isNumeric(age))
                {
                    age_txt.setError("Check your height!");
                    age_txt.requestFocus();
                    return;
                }
                mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful())
                        {
                            Toast.makeText(RegisterActivity.this,"You are successfully Registered", Toast.LENGTH_SHORT).show();
                            mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(task2 -> {
                                if(task2.isSuccessful())
                                {

                                    FirebaseUser firebaseUser = mAuth.getInstance().getCurrentUser();
                                    FirebaseDatabase database = FirebaseDatabase.getInstance();
                                    DatabaseReference nameRef = database.getReference("users").child(firebaseUser.getUid()).child("full_name");
                                    DatabaseReference heightRef = database.getReference("users").child(firebaseUser.getUid()).child("height");
                                    DatabaseReference weightRef = database.getReference("users").child(firebaseUser.getUid()).child("weight");
                                    DatabaseReference ageRef = database.getReference("users").child(firebaseUser.getUid()).child("age");
                                    DatabaseReference genderRef = database.getReference("users").child(firebaseUser.getUid()).child("gender");

                                    nameRef.setValue(String.valueOf(name));

                                    double weightValue = Double.parseDouble(weight_txt.getText().toString());
                                    double heightValue = Double.parseDouble(height_txt.getText().toString());
                                    int ageValue = Integer.valueOf(age_txt.getText().toString());
                                    String nameFull=String.valueOf(name);
                                    String genderFull=radioSexButton.getText().toString().trim();

                                    if(!changed1){
                                        heightValue=(heightValue / change1);

                                    }

                                    if(!changed2){
                                        weightValue=(weightValue / change2);

                                    }


                                    heightRef.setValue(String.valueOf(heightValue));
                                    weightRef.setValue(String.valueOf(weightValue));
                                    ageRef.setValue(String.valueOf(ageValue));
                                    genderRef.setValue(genderFull);

                                    startActivity(new Intent(RegisterActivity.this, MapPage.class));
                                    SharedPreferences sharedPref = RegisterActivity.this.getSharedPreferences(SHARED_PREFS, Context.MODE_PRIVATE);
                                    SharedPreferences.Editor editor = sharedPref.edit();

                                    editor.putString("email",email);
                                    editor.putString("full_name",nameFull);
                                    editor.putFloat("weight",(float) weightValue);
                                    editor.putFloat("height",(float) heightValue);
                                    editor.putInt("age",(Integer) ageValue);
                                    editor.putString("genderFull",genderFull);
                                    editor.apply();
                                }
                                else
                                {
                                    startActivity(new Intent(RegisterActivity.this,LoginActivity.class ));
                                    finish();
                                }

                            });
                        }
                        else
                        {
                            Toast.makeText(RegisterActivity.this,"A problem occurred. Try again",Toast.LENGTH_SHORT).show();
                        }
                    }
                });

            }
        });

    }
}