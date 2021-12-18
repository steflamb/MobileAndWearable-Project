package com.example.pathsocial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LeagueActivity extends AppCompatActivity {

    Boolean isFABOpen=false;
    FloatingActionButton fab,fab1,fab2,fab3;
    Button search_btn;
    Button follow_easy;
    Button follow_medium;
    Button follow_hard;


    String name;
    TextView textViewName,meanDisplay,easyName,mediumName,hardName,easyDistance,mediumDistance,hardDistance,easyCalories,mediumCalories,hardCalories;
    ImageView userimage;


    private FirebaseDatabase database;
    private GeoFire geoFire;
    private DatabaseReference refPath;
    private DatabaseReference refLocation;
    private List<TrailForDb> trailsFromDb= new ArrayList<>();

    private TrailDao trailDao;
    private List<Trail> trails;


    private int topRatingEasy;
    private int topRatingMedium;
    private int topRatingHard;

    private double caloriesEasy;
    private double caloriesMedium;
    private double caloriesHard;

    private double distanceEasy;
    private double distanceMedium;
    private double distanceHard;

    private String namesEasy;
    private String nameMedium;
    private String nameHard;


    private Double easyrangeHigh;
    private Double mediumrangeHigh;
    private Double hardrangeHigh;

    private Double easyrangeLow;
    private Double mediumrangeLow;
    private Double hardrangeLow;


    private String dataEasy;
    private String dataMedium;
    private String dataHard;

    private String idEasy;
    private String idMedium;
    private String idHard;









    private void showFABMenu(){
        isFABOpen=true;
        fab1.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fab2.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
        fab3.animate().translationY(-getResources().getDimension(R.dimen.standard_155));
        fab1.setVisibility(View.VISIBLE);
        fab2.setVisibility(View.VISIBLE);
        fab3.setVisibility(View.VISIBLE);
    }

    private void closeFABMenu(){
        isFABOpen=false;
        fab1.animate().translationY(0);
        fab2.animate().translationY(0);
        fab3.animate().translationY(0);
        fab1.setVisibility(View.INVISIBLE);
        fab2.setVisibility(View.INVISIBLE);
        fab3.setVisibility(View.INVISIBLE);

    }

    public Bitmap createImage(int width, int height, int color, String name) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint2 = new Paint();
        paint2.setColor(color);
        canvas.drawCircle(100, 100,100, paint2);
        Paint paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(72);
        paint.setTextScaleX(1);
        canvas.drawText(name, 79 - 25, 105 + 20, paint);
        return bitmap;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getIntent().getExtras();
        double latitude = b.getDouble("lat");
        double longitude = b.getDouble("lon");


        setContentView(R.layout.activity_league);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab3 = (FloatingActionButton) findViewById(R.id.fab3);
        search_btn = (Button) findViewById(R.id.buttonSearch);
        textViewName = (TextView) findViewById(R.id.textViewName);
        meanDisplay = (TextView) findViewById(R.id.mean_distance);
        easyName = (TextView) findViewById(R.id.easy_name);
        mediumName = (TextView) findViewById(R.id.medium_name);
        hardName = (TextView) findViewById(R.id.hard_name);
        easyDistance = (TextView) findViewById(R.id.easy_distance);
        mediumDistance = (TextView) findViewById(R.id.medium_distance);
        hardDistance = (TextView) findViewById(R.id.hard_distance);
        easyCalories = (TextView) findViewById(R.id.easy_calories);
        mediumCalories = (TextView) findViewById(R.id.medium_calories);
        hardCalories = (TextView) findViewById(R.id.hard_calories);
        follow_easy = (Button) findViewById(R.id.followEasy);;
        follow_medium = (Button) findViewById(R.id.followMedium);;
        follow_hard = (Button) findViewById(R.id.followHard);;





        userimage = (ImageView) findViewById(R.id.userImage);



        fab1.setVisibility(View.INVISIBLE);
        fab2.setVisibility(View.INVISIBLE);
        fab3.setVisibility(View.INVISIBLE);

        database = FirebaseDatabase.getInstance();
        refLocation = FirebaseDatabase.getInstance().getReference("trails/locations");
        refPath = FirebaseDatabase.getInstance().getReference("trails/path");
        geoFire = new GeoFire(refLocation);









        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        String email="error";

        if(firebaseUser != null) {
            email = firebaseUser.getEmail();

        }
        FirebaseDatabase database = FirebaseDatabase.getInstance();

        database.getReference("users").child(firebaseUser.getUid()).child("full_name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    name=String.valueOf(task.getResult().getValue());
                    textViewName.setText(name+"'s \n motivation zone");
                    String initials = "";
                    for (String s : name.split(" ")) {
                        initials+=s.charAt(0);
                    }


                    userimage.setImageBitmap(createImage(200, 200,Color.LTGRAY ,initials));

                }
            }
        });

        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-trails").allowMainThreadQueries().build();

        trailDao = db.trailDao();
        trails = trailDao.getAll();


        int trail_count=0;
        Double distance_count= (double) 0;
        for(Trail trail : trails)
        {

            trail_count += 1;
            distance_count += trail.distance;

        }
        Double meanDistance=distance_count/trail_count;


        meanDisplay.setText(String.format("%.3f",meanDistance)+" Km");

        easyrangeHigh=meanDistance/1.1;
        mediumrangeHigh=meanDistance*1.2;
        hardrangeHigh=meanDistance*5;

        easyrangeLow=0.0;
        mediumrangeLow=meanDistance/1.09;
        hardrangeLow=meanDistance*1.21;

        topRatingEasy=0;
        topRatingMedium=0;
        topRatingHard=0;


        trailsFromDb = new ArrayList<>();
        GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(latitude,longitude), 2);
        findLocations(geoQuery);















        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                }else{
                    closeFABMenu();
                }
            }
        });
        fab1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LeagueActivity.this, MapPage.class);
                startActivity(intent);
                finish();
            }
        });
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LeagueActivity.this, RecordActivity.class);
                startActivity(intent);
                finish();
            }
        });
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LeagueActivity.this, MyTrailActivities.class));
                finish();
            }
        });


        follow_easy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                        Intent intent = new Intent(LeagueActivity.this, FollowActivity.class);
                        Bundle args = new Bundle();
                        args.putSerializable("ARRAYLIST", (Serializable) dataEasy);
                        args.putSerializable("REF", (Serializable) idEasy);
                        intent.putExtra("BUNDLE", args);
                        startActivity(intent);
            }
        });

        follow_medium.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LeagueActivity.this, FollowActivity.class);
                Bundle args = new Bundle();
                args.putSerializable("ARRAYLIST", (Serializable) dataMedium);
                args.putSerializable("REF", (Serializable) idMedium);
                intent.putExtra("BUNDLE", args);
                startActivity(intent);
            }
        });

        follow_hard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(LeagueActivity.this, FollowActivity.class);
                Bundle args = new Bundle();
                args.putSerializable("ARRAYLIST", (Serializable) dataHard);
                args.putSerializable("REF", (Serializable) idHard);
                intent.putExtra("BUNDLE", args);
                startActivity(intent);
            }
        });







    }

    public void findLocations(GeoQuery geoQuery)
    {
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                Log.d("query",String.format("Key %s entered the search area at [%f,%f]", key, location.latitude, location.longitude));


                refPath.child(key).addListenerForSingleValueEvent(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                Map<String,Object> data = (Map<String,Object>) dataSnapshot.getValue();
                                String pathid=(String) data.get("pathid");
                                String firstName=(String) data.get("firstName");
                                String description=(String) data.get("description");
                                String duration=(String) data.get("duration");
                                String datetime=(String) data.get("datetime");
                                double startingPointLat=(double) data.get("startingPointLat");
                                double startingPointLongit=(double) data.get("startingPointLongit");
                                Long stepsNubers=(Long) data.get("stepsNubers");
                                Long calories=(Long) data.get("calories");
                                Double distance=(Double) data.get("distance");
                                Double medianSpeed=(Double) data.get("medianSpeed");
                                String weather=(String) data.get("weather");
                                String trailData=(String) data.get("trailData");
                                String usrId=(String) data.get("usrId");
                                Integer reviewPositive=Integer.valueOf(((Long) data.get("reviewPositive")).intValue());
                                Integer reviewNegative=Integer.valueOf(((Long) data.get("reviewNegative")).intValue());



                                TrailForDb newTrail=new TrailForDb(pathid,firstName,description,duration,datetime,startingPointLat,startingPointLongit,trailData,(int)stepsNubers.intValue(),(int)calories.intValue(),distance.doubleValue(),medianSpeed.doubleValue(),weather,usrId,reviewPositive,reviewNegative);
                                trailsFromDb.add(newTrail);

                                for(TrailForDb trail : trailsFromDb)
                                {
                                    if(trail.distance<easyrangeHigh && trail.distance>=easyrangeLow && topRatingEasy<=trail.reviewPositive)
                                    {
                                        topRatingEasy=trail.reviewPositive;
                                        caloriesEasy=trail.calories;
                                        distanceEasy=trail.distance;
                                        namesEasy=trail.firstName;
                                        idEasy=trail.pathid;
                                        dataEasy=trail.trailData;
                                    }
                                    else if(trail.distance<mediumrangeHigh && trail.distance>=mediumrangeLow && topRatingMedium<=trail.reviewPositive)
                                    {
                                        topRatingMedium=trail.reviewPositive;
                                        caloriesMedium=trail.calories;
                                        distanceMedium=trail.distance;
                                        nameMedium=trail.firstName;
                                        idMedium=trail.pathid;
                                        dataMedium=trail.trailData;
                                    }
                                    else if(trail.distance<hardrangeHigh && trail.distance>=hardrangeLow && topRatingHard<=trail.reviewPositive)
                                    {
                                        topRatingHard=trail.reviewPositive;
                                        caloriesHard=trail.calories;
                                        distanceHard=trail.distance;
                                        nameHard=trail.firstName;
                                        idHard=trail.pathid;
                                        dataHard=trail.trailData;
                                    }
                                }

                                if(namesEasy==null)
                                {
                                    easyName.setText("Nothing to show here yet!");
                                    easyDistance.setVisibility(View.INVISIBLE);
                                    easyCalories.setVisibility(View.INVISIBLE);
                                    follow_easy.setVisibility(View.INVISIBLE);
                                }
                                else
                                {
                                    easyName.setText(String.valueOf(namesEasy));
                                    easyDistance.setVisibility(View.VISIBLE);
                                    easyCalories.setVisibility(View.VISIBLE);
                                    follow_easy.setVisibility(View.VISIBLE);
                                    easyDistance.setText(String.format("%.3f",distanceEasy)+" Km");
                                    easyCalories.setText(String.valueOf(caloriesEasy)+" calories");
                                }
                                if(nameMedium==null)
                                {
                                    mediumName.setText("Nothing to show here yet!");
                                    mediumDistance.setVisibility(View.INVISIBLE);
                                    mediumCalories.setVisibility(View.INVISIBLE);
                                    follow_medium.setVisibility(View.INVISIBLE);
                                }
                                else
                                {
                                    mediumName.setText(String.valueOf(nameMedium));
                                    mediumDistance.setVisibility(View.VISIBLE);
                                    mediumCalories.setVisibility(View.VISIBLE);
                                    follow_medium.setVisibility(View.VISIBLE);
                                    mediumDistance.setText(String.format("%.3f",distanceMedium)+" Km");
                                    mediumCalories.setText(String.valueOf(caloriesMedium)+" calories");
                                }

                                if(nameHard==null)
                                {
                                    hardName.setText("Nothing to show here yet!");
                                    hardDistance.setVisibility(View.INVISIBLE);
                                    hardCalories.setVisibility(View.INVISIBLE);
                                    follow_hard.setVisibility(View.INVISIBLE);
                                }
                                else
                                {
                                    hardName.setText(String.valueOf(nameHard));
                                    hardDistance.setVisibility(View.VISIBLE);
                                    hardCalories.setVisibility(View.VISIBLE);
                                    follow_hard.setVisibility(View.VISIBLE);
                                    hardDistance.setText(String.format("%.3f",distanceHard)+" Km");
                                    hardCalories.setText(String.valueOf(caloriesHard)+" calories");
                                }



                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //
                            }
                        });
            }




            @Override
            public void onKeyExited(String key) {
                Log.d("query",String.format("Key %s is no longer in the search area", key));
            }

            @Override
            public void onKeyMoved(String key, GeoLocation location) {
                Log.d("query",String.format("Key %s moved within the search area to [%f,%f]", key, location.latitude, location.longitude));
            }

            @Override
            public void onGeoQueryReady() {
                Log.d("query","All initial data has been loaded and events have been fired!");

            }

            @Override
            public void onGeoQueryError(DatabaseError error) {
                Log.d("query","There was an error with this query: " + error);
            }
        });
    }





}