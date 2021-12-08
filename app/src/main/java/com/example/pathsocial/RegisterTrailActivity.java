package com.example.pathsocial;

import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT;

import static java.lang.Math.abs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.ColumnInfo;
import androidx.room.Dao;
import androidx.room.Database;
import androidx.room.Delete;
import androidx.room.Entity;
import androidx.room.Insert;
import androidx.room.PrimaryKey;
import androidx.room.Query;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.Serializable;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.UUID;

import com.example.pathsocial.Trail;
import com.example.pathsocial.TrailData;
import com.example.pathsocial.TrailDao;




public class RegisterTrailActivity extends AppCompatActivity implements
         OnMapReadyCallback, PermissionsListener {

    Timer timer = new Timer();
    ArrayList<Double> arrLat= new ArrayList<Double>();
    ArrayList<Double> arrLng = new ArrayList<Double>();
    LocationManager locman;
    Location Location;

    private final long MIN_TIME = 10000; // 10 seconds
    private final long MIN_DIST = 5; // 5 Meters

    double lat;
    double lon;
    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private MapView mapView;
    private MaterialButtonToggleGroup materialButtonToggleGroup;
    private SymbolManager symbolManager;
    private Symbol symbol;
    private Button buttonSave;
    private Button buttonDelete;
    private static final String MAKI_ICON_MARKER = "marker-15";
    private View viewBox;
    private ArrayList<Point> routeCoordinates = new ArrayList<>();
    private Boolean doneLoadingMap=false;
    public Trail trailToAdd = new Trail();
    public AppDatabase db;
    public EditText editName;
    public EditText editDescription;
    private String timeElapsed;
    private int steps;
    private static final String SHARED_PREFS = "sharedPrefs";
    Boolean exit=false;

    private double length(List<Point> coords) {
        double travelled = 0;
        Point prevCoords = coords.get(0);
        Point curCoords;
        for (int i = 1; i < coords.size(); i++) {
            curCoords = coords.get(i);
            travelled +=CalculationByDistance(prevCoords.latitude(), curCoords.latitude(),prevCoords.longitude(), curCoords.longitude());
            prevCoords = curCoords;
        }
        return travelled;
    }


    public double CalculationByDistance(double lat1,double lat2,double lon1,double lon2) {
        int Radius = 6371;// radius of earth in Km
//        double lat1 = StartP.latitude;
//        double lat2 = EndP.latitude;
//        double lon1 = StartP.longitude;
//        double lon2 = EndP.longitude;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1))
                * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                * Math.sin(dLon / 2);
        double c = 2 * Math.asin(Math.sqrt(a));
        double valueResult = Radius * c;
        double km = valueResult / 1;
        DecimalFormat newFormat = new DecimalFormat("####");
        int kmInDec = Integer.valueOf(newFormat.format(km));
        double meter = valueResult % 1000;
        int meterInDec = Integer.valueOf(newFormat.format(meter));

        return Radius * c;
    }

    private AlertDialog AskOption()
    {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this)
                // set message, title, and icon
                .setTitle("Delete")
                .setMessage("Do you want to Delete")
                .setIcon(R.drawable.mapbox_compass_icon)

                .setPositiveButton("Delete", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        Intent intent = new Intent(RegisterTrailActivity.this, MapPage.class);
                        startActivity(intent);
                        finish();
                    }

                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();

                    }
                })
                .create();

        return myQuittingDialogBox;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, "pk.eyJ1Ijoic3RlZmxhbWIiLCJhIjoiY2t0MWNkeXg0MGR6bzJvbzMwd2o2N2xvaSJ9.xzks_fIbucKQlpZlKSvTfA");
        setContentView(R.layout.activity_register_trail);
        locman = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        mapView = findViewById(R.id.mapViewRecord);
        buttonSave = findViewById(R.id.buttonPosition);
        buttonDelete = findViewById(R.id.discardButton);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        editName= findViewById(R.id.editTextTrailName);
        editDescription= findViewById(R.id.editTextDescription);
        SharedPreferences sharedPreferences = this.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-trails").allowMainThreadQueries().build();

        TrailDao trailDao = db.trailDao();

        viewBox = findViewById(R.id.view2);
        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        routeCoordinates = (ArrayList<Point>) args.getSerializable("ARRAYLIST");
        timeElapsed = (String) args.getSerializable("TIME");
        long timeElapsedVal = (long) args.getSerializable("TIMEVAL");
        LocalDateTime datetime = (LocalDateTime) args.getSerializable("STARTTIME");
        steps = (int) args.getSerializable("STEPS");
        Log.d("tag",routeCoordinates.toString());

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name = editName.getText().toString().trim();
                String description= editDescription.getText().toString().trim();

                trailToAdd.pathid= UUID.randomUUID().toString();
                trailToAdd.firstName=name;
                trailToAdd.description=description;
                trailToAdd.startingPointLat=routeCoordinates.get(0).latitude();
                trailToAdd.startingPointLongit=routeCoordinates.get(0).longitude();
                trailToAdd.stepsNubers=steps;
                trailToAdd.duration= timeElapsed;
                trailToAdd.datetime=datetime.toString();
                trailToAdd.trailData=routeCoordinates;
                trailToAdd.weather="Always sunny in philadelphia";
                trailToAdd.personal=true;
                Double distance=length(routeCoordinates);
                int seconds= (int) (timeElapsedVal/1000);
                float hours = ((float) seconds)/(60*60);
                Double median=length(routeCoordinates)/hours;
                if(distance==Double.POSITIVE_INFINITY || distance==Double.NEGATIVE_INFINITY || distance==Double.NaN)
                {
                    trailToAdd.distance=0.00000001;
                }
                else
                {
                    trailToAdd.distance=distance;
                }
                if(median==Double.POSITIVE_INFINITY || median==Double.NEGATIVE_INFINITY || median==Double.NaN)
                {
                    trailToAdd.medianSpeed=0.00000001;
                }
                else
                {
                    trailToAdd.medianSpeed=median;
                }

                // get user data from shared preferences
                float userWeight = sharedPreferences.getFloat("weight", 0);
                float userHeight = sharedPreferences.getFloat("height", 0);
                String userGender = String.valueOf(sharedPreferences.getString("genderFull", ""));
                int userAge = sharedPreferences.getInt("age", 0);

                // calculate calories
                int genderFac;
                if (userGender.equals(getString(R.string.gender_f))) {
                    genderFac = -161;
                }
                else if (userGender.equals(getString(R.string.gender_m))) {
                    genderFac = 5;
                }
                else {
                    // take average of M and F factors in case of other gender
                    genderFac = -78;
                }

                Double caloriesREE = hours * (((10*userWeight) + (6.25*userHeight) - (5*userAge) + genderFac) / 24);
                // calculate MET while ensuring it is between 2-2.5 by interpolating using speed
                Double caloriesMETs = Math.min(2 + ((median / 1.6) / (5 - 2)), 2.5);
                Double caloriesAEE = userWeight * caloriesMETs * hours;
                trailToAdd.calories = (int) (caloriesREE + caloriesAEE);


                Log.d("distance:",String.valueOf(length(routeCoordinates)));
                Log.d("time:",String.valueOf(seconds));
                Log.d("time:",String.valueOf(hours));





                trailDao.insertAll(trailToAdd);
                Intent intent = new Intent(RegisterTrailActivity.this, MyTrailActivities.class);
                startActivity(intent);
                finish();
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog diaBox = AskOption();
                diaBox.show();

            }
        });


    }

    @Override
    public void onBackPressed() {
        //noback
    }

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        RegisterTrailActivity.this.mapboxMap = mapboxMap;
//        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar2);
//        setSupportActionBar(myToolbar);

        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/steflamb/ckuf5kglt9thk17o3b0navprv"),
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                        symbolManager = new SymbolManager(mapView, mapboxMap, style);
                        symbolManager.setIconAllowOverlap(true);
                        symbolManager.setIconTranslate(new Float[]{-4f,5f});
                        symbolManager.setIconRotationAlignment(ICON_ROTATION_ALIGNMENT_VIEWPORT);
                        style.addSource(new GeoJsonSource("line-source",
                                FeatureCollection.fromFeatures(new Feature[] {Feature.fromGeometry(
                                        LineString.fromLngLats(routeCoordinates)
                                )})));
                        style.addLayer(new LineLayer("linelayer", "line-source").withProperties(
                                PropertyFactory.lineDasharray(new Float[] {0.01f, 2f}),
                                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                                PropertyFactory.lineWidth(5f),
                                PropertyFactory.lineColor(Color.parseColor("#e55e5e"))
                        ));

                    }
                });
    }




    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

// Get an instance of the component
            LocationComponent locationComponent = mapboxMap.getLocationComponent();

// Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

// Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

// Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING);

// Set the component's render mode
            locationComponent.setRenderMode(RenderMode.COMPASS);
        } else {
            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {
        Toast.makeText(this, "allow location", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {
            mapboxMap.getStyle(new Style.OnStyleLoaded() {
                @Override
                public void onStyleLoaded(@NonNull Style style) {
                    enableLocationComponent(style);
                }
            });
        } else {
            Toast.makeText(this, "not allowed", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    @Override
    @SuppressWarnings( {"MissingPermission"})
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }




}