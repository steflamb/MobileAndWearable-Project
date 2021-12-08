package com.example.pathsocial;

import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.DialogInterface;
import android.content.Intent;
import android.app.PendingIntent;
import android.app.Notification;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.button.MaterialButton;
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
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class RecordActivity extends AppCompatActivity implements
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
    private MaterialButton buttonStart,buttonStop,buttonPause;
    private SymbolManager symbolManager;
    private Symbol symbol;
    private Button buttonStartRecording;
    private static final String MAKI_ICON_MARKER = "marker";
    private View viewBox;
    private List<Point> routeCoordinates = new ArrayList<>();
    private Boolean exit=false;
    private long timeWhenStopped = 0;
    private long duration = 0;
    private Chronometer mChronometer;
    private Button buttonSave,buttonDelete,buttonContinue, buttonStatistics;
    private RelativeLayout panelHide;
    private ConstraintLayout panelStats;
    private Boolean started = false;
    private Boolean statsPanelShow = false;
    private TextView statsTextSteps, statsTextCalories, statsTextSpeed;
    private static final String SHARED_PREFS = "sharedPrefs";
    private float userWeight, userHeight, hours;
    private int userAge, genderFac;
    private double median, caloriesREE, caloriesAEE, caloriesMETs;
    private int calories, seconds;


    // Step sensors.
    private SensorManager mSensorManager;
    private SensorEventListener stepListener;
    private Sensor mSensorStepDetector;
    public int recordedSteps = 0;

    private static Timer mTimer1;
    private TimerTask mTt1;
    private Handler mTimerHandler = new Handler();

    boolean runningQOrLater =
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q;

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

    private void stopTimer(){
        if(mTimer1 != null){
            mTimer1.cancel();
            mTimer1.purge();
            mTimer1=null;
        }
    }

    private void startTimer(){
        if(mTimer1 == null) {
            mTimer1 = new Timer();
            mTt1 = new TimerTask() {
                public void run() {
                    mTimerHandler.post(new Runnable() {
                        public void run() {
                            if (mapboxMap.getLocationComponent().getLastKnownLocation() != null) {
                                Location lastKnownLocation = mapboxMap.getLocationComponent().getLastKnownLocation();
                                double lat = lastKnownLocation.getLatitude();
                                double longitude = lastKnownLocation.getLongitude();
                                Log.d("latlang", String.valueOf(lat) + String.valueOf(longitude));
                                // Add symbol at specified lat/lon
                                symbol = symbolManager.create(new SymbolOptions()
                                        .withLatLng(new LatLng(lat, longitude))
                                        .withIconImage(MAKI_ICON_MARKER)
                                        .withIconSize(2.0f)
                                        .withDraggable(false));
                                routeCoordinates.add(Point.fromLngLat(longitude, lat));
                                mapboxMap.getStyle().removeLayer("linelayer");
                                mapboxMap.getStyle().removeSource("line-source");
                                mapboxMap.getStyle().addSource(new GeoJsonSource("line-source",
                                        FeatureCollection.fromFeatures(new Feature[]{Feature.fromGeometry(
                                                LineString.fromLngLats(routeCoordinates)
                                        )})));
                                mapboxMap.getStyle().addLayer(new LineLayer("linelayer", "line-source").withProperties(
                                        PropertyFactory.lineDasharray(new Float[]{0.01f, 2f}),
                                        PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                                        PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                                        PropertyFactory.lineWidth(5f),
                                        PropertyFactory.lineColor(Color.parseColor("#e55e5e"))
                                ));

                                // update live statistics
                                duration = SystemClock.elapsedRealtime() - mChronometer.getBase();
                                seconds= (int) (duration/1000);
                                hours = ((float) seconds)/(60*60);
                                median=length(routeCoordinates)/hours;

                                caloriesREE = hours * (((10*userWeight) + (6.25*userHeight) - (5*userAge) + genderFac) / 24);
                                // calculate MET while ensuring it is between 2-2.5 by interpolating using speed
                                caloriesMETs = Math.min(2 + ((median / 1.6) / (5 - 2)), 2.5);
                                caloriesAEE = userWeight * caloriesMETs * hours;
                                calories = (int) (caloriesREE + caloriesAEE);

                                statsTextSteps.setText(String.valueOf(recordedSteps));
                                statsTextCalories.setText(String.valueOf(calories));
                                statsTextSpeed.setText(String.valueOf(Math.round(median * 100.0) / 100.0));

                            }
                        }
                    });
                }
            };

            mTimer1.schedule(mTt1, 1, 10000);
        }
    }

    @Override
    public void onBackPressed() {
        if (!started)
        {
            Intent intent = new Intent(RecordActivity.this, MapPage.class);
            startActivity(intent);
            finish();
        }
    }

    public void showFinishPanel()
    {
        panelHide.setVisibility(View.VISIBLE);
        buttonSave.setVisibility(View.VISIBLE);
        buttonDelete.setVisibility(View.VISIBLE);
        buttonContinue.setVisibility(View.VISIBLE);
        buttonStatistics.setVisibility(View.INVISIBLE);
        panelStats.setVisibility(View.INVISIBLE);
    }

    public void hideFinishPanel()
    {
        panelHide.setVisibility(View.INVISIBLE);
        buttonSave.setVisibility(View.INVISIBLE);
        buttonDelete.setVisibility(View.INVISIBLE);
        buttonContinue.setVisibility(View.INVISIBLE);
        panelStats.setVisibility(View.INVISIBLE);
        buttonStatistics.setVisibility(View.VISIBLE);

    }

    // Ask for permission
    private void getActivity() {
        final int REQUEST_ACTIVITY_RECOGNITION_PERMISSION = 45;
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]
                            {Manifest.permission.ACTIVITY_RECOGNITION},
                    REQUEST_ACTIVITY_RECOGNITION_PERMISSION);
        } else {
            return;        }
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
                        Intent intent = new Intent(RecordActivity.this, MapPage.class);
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
        setContentView(R.layout.activity_record);
        locman = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Mapbox.getInstance(this, "pk.eyJ1Ijoic3RlZmxhbWIiLCJhIjoiY2t0MWNkeXg0MGR6bzJvbzMwd2o2N2xvaSJ9.xzks_fIbucKQlpZlKSvTfA");
        mapView = findViewById(R.id.mapViewGet);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        viewBox = findViewById(R.id.view2);

        SharedPreferences sharedPreferences = this.getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);

        buttonStart = findViewById(R.id.toggleStart);
        buttonStop = findViewById(R.id.toggleStop);
        buttonPause = findViewById(R.id.togglePause);
        buttonStartRecording = findViewById(R.id.buttonStartRecording);
        mChronometer = (Chronometer) findViewById(R.id.mChronometer);

        buttonSave = findViewById(R.id.buttonSaveImage);
        buttonDelete = findViewById(R.id.discardButtonImage);
        buttonContinue = findViewById(R.id.buttonContinueRecordingImage);
        buttonStatistics = findViewById(R.id.buttonStatistics);
        panelHide = findViewById(R.id.panelHide);
        panelStats = findViewById(R.id.panelStats);
        hideFinishPanel();

        statsTextSteps=findViewById(R.id.statsViewStepsVal);
        statsTextCalories=findViewById(R.id.statsViewCaloriesVal);
        statsTextSpeed=findViewById(R.id.statsViewMedianVal);

        buttonStop.setVisibility(View.INVISIBLE);
        buttonPause.setVisibility(View.INVISIBLE);
        buttonStart.setVisibility(View.INVISIBLE);
        viewBox.setVisibility(View.INVISIBLE);
        panelStats.setVisibility(View.INVISIBLE);

        // add objects for step counter

        // Ask for activity recognition permission
        if (runningQOrLater) {
            getActivity();
        }

        //  Get an instance of the sensor manager.
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        // Step detector instance
        mSensorStepDetector = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        //Instantiate the StepCounterListener
        stepListener = new StepCounterListener(recordedSteps);

        // get values from shared pref for calorie calc
        // get user data from shared preferences
        userWeight = sharedPreferences.getFloat("weight", 0);
        userHeight = sharedPreferences.getFloat("height", 0);
        String userGender = String.valueOf(sharedPreferences.getString("genderFull", ""));
        userAge = sharedPreferences.getInt("age", 0);

        // calculate calories
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

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog diaBox = AskOption();
                diaBox.show();

            }
        });
        buttonStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // hide / show statistics panel on click
                if (!statsPanelShow) {
                    panelStats.setVisibility(View.VISIBLE);
                    statsPanelShow = true;
                }
                else {
                    panelStats.setVisibility(View.INVISIBLE);
                    statsPanelShow = false;
                }

            }
        });
        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hideFinishPanel();
            }
        });

        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mapboxMap.getLocationComponent().getLastKnownLocation() != null) {
                    Location lastKnownLocation = mapboxMap.getLocationComponent().getLastKnownLocation();
                    double lat = lastKnownLocation.getLatitude();
                    double longitude = lastKnownLocation.getLongitude();
                    Log.d("latlang",String.valueOf(lat)+String.valueOf(longitude));
                    // Add symbol at specified lat/lon
                    symbol = symbolManager.create(new SymbolOptions()
                            .withLatLng(new LatLng(lat, longitude))
                            .withIconImage(MAKI_ICON_MARKER)
                            .withIconSize(2.0f)
                            .withDraggable(false));
                    routeCoordinates.add(Point.fromLngLat(longitude, lat));
                    mapboxMap.getStyle().removeLayer("linelayer");
                    mapboxMap.getStyle().removeSource("line-source");
                    mapboxMap.getStyle().addSource(new GeoJsonSource("line-source",
                            FeatureCollection.fromFeatures(new Feature[] {Feature.fromGeometry(
                                    LineString.fromLngLats(routeCoordinates)
                            )})));
                    mapboxMap.getStyle().addLayer(new LineLayer("linelayer", "line-source").withProperties(
                            PropertyFactory.lineDasharray(new Float[] {0.01f, 2f}),
                            PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                            PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                            PropertyFactory.lineWidth(5f),
                            PropertyFactory.lineColor(Color.parseColor("#e55e5e"))
                    ));
                }
                Intent intent = new Intent(RecordActivity.this, RegisterTrailActivity.class);
//                    Log.d("tag",routeCoordinates.toString());
                Bundle args = new Bundle();
                args.putSerializable("ARRAYLIST",(Serializable)routeCoordinates);
                timeWhenStopped = SystemClock.elapsedRealtime() - mChronometer.getBase();
                mChronometer.stop();

                args.putSerializable("TIME",(Serializable)mChronometer.getText().toString());
                args.putSerializable("TIMEVAL",(Serializable)(timeWhenStopped));
                args.putSerializable("STARTTIME",(Serializable)LocalDateTime.now());
                args.putSerializable("STEPS",(Serializable) recordedSteps);
                intent.putExtra("BUNDLE",args);
                startActivity(intent);

            }
        });

        materialButtonToggleGroup = (MaterialButtonToggleGroup) findViewById(R.id.toggleButtonGroup);;
        materialButtonToggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {
                if (group.getCheckedButtonId() == R.id.toggleStart) {
                    buttonStop.setVisibility(View.VISIBLE);
                    buttonPause.setVisibility(View.VISIBLE);
                    buttonStart.setVisibility(View.VISIBLE);
                    buttonStart.setText("RECORDING");
                    buttonPause.setText("PAUSE");
                    mChronometer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                    mChronometer.start();
                    startTimer();

                    // Register the step counter listener
                    mSensorManager.registerListener(stepListener, mSensorStepDetector, SensorManager.SENSOR_DELAY_NORMAL);


                } else if (group.getCheckedButtonId() == R.id.toggleStop) {
                    buttonStart.setVisibility(View.VISIBLE);
                    buttonPause.setVisibility(View.VISIBLE);
                    buttonStart.setText("RESUME");
                    buttonPause.setText("PAUSED");
                    timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
                    mChronometer.stop();
                    stopTimer();

                    // Unregister the listener
                    mSensorManager.unregisterListener(stepListener);
                    showFinishPanel();

                } else if (group.getCheckedButtonId() == R.id.togglePause) {
                    buttonStart.setVisibility(View.VISIBLE);
                    buttonPause.setVisibility(View.VISIBLE);
                    buttonStart.setText("RESUME");
                    buttonPause.setText("PAUSED");
                    timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
                    mChronometer.stop();

                    // Unregister the listener
                    mSensorManager.unregisterListener(stepListener);
                    stopTimer();

                }
            }
        });

        buttonStartRecording.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startTimer();
                viewBox.setVisibility(View.VISIBLE);
                buttonStop.setVisibility(View.VISIBLE);
                buttonPause.setVisibility(View.VISIBLE);
                buttonStart.setVisibility(View.VISIBLE);
                buttonStart.setText("RECORDING");
                buttonStartRecording.setVisibility(View.INVISIBLE);
                buttonStart.setChecked(true);

                mChronometer.setBase(SystemClock.elapsedRealtime());
                mChronometer.start();
                started=true;

                // Register the step counter listener
                mSensorManager.registerListener(stepListener, mSensorStepDetector, SensorManager.SENSOR_DELAY_NORMAL);

            }
        });
    }


    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        RecordActivity.this.mapboxMap = mapboxMap;
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

    @SuppressLint("ResourceType")
    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @SuppressLint("ResourceType")
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

    @SuppressLint("ResourceType")
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

    // Sensor event listener
    class StepCounterListener<stepsCompleted> implements SensorEventListener {

        private long lastUpdate = 0;

        // Android step detector
        public int mAndroidStepCounter = 0;

        // Get the database, TextView and ProgressBar as args
        public StepCounterListener(int steps){
            mAndroidStepCounter = steps;
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            // Calculate the number of steps
            countSteps(event.values[0]);

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            //
        }

        // Calculate the number of steps from the step detector
        private void countSteps(float step) {

            //Step count
            mAndroidStepCounter += (int) step;
            recordedSteps = mAndroidStepCounter;
            Log.d("NUM STEPS ANDROID", "Num.steps: " + String.valueOf(mAndroidStepCounter));
        }

    }




}
