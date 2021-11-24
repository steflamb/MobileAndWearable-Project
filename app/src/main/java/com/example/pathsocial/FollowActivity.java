package com.example.pathsocial;

import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.res.ResourcesCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
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
import android.widget.RelativeLayout;
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
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.turf.TurfMeta;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class FollowActivity extends AppCompatActivity implements
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
    private String routeCoordinatesRecieved;
    private Boolean exit=false;
    private long timeWhenStopped = 0;
    private Chronometer mChronometer;
    private Button buttonSave,buttonContinue;
    private RelativeLayout panelHide;
    private Boolean started = false;
    private Button buttonCenterCamera;
    private LocationComponent locationComponent;
    private Button statisticsButton;

    private Symbol symbol2;
    private Symbol symbol3;

    private int steps;

    // Step sensors.
    private SensorManager mSensorManager;
    private SensorEventListener stepListener;
    private Sensor mSensorStepDetector;
    public int recordedSteps = 0;
    boolean runningQOrLater =
            android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q;

    private static Timer mTimer1;
    private TimerTask mTt1;
    private Handler mTimerHandler = new Handler();

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
            Intent intent = new Intent(FollowActivity.this, MapPage.class);
            startActivity(intent);
            finish();
        }
    }

    public void showFinishPanel()
    {
        panelHide.setVisibility(View.VISIBLE);
        buttonSave.setVisibility(View.VISIBLE);
        buttonContinue.setVisibility(View.VISIBLE);
    }

    public void hideFinishPanel()
    {
        panelHide.setVisibility(View.INVISIBLE);
        buttonSave.setVisibility(View.INVISIBLE);
        buttonContinue.setVisibility(View.INVISIBLE);

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follow);
        locman = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Mapbox.getInstance(this, "pk.eyJ1Ijoic3RlZmxhbWIiLCJhIjoiY2t0MWNkeXg0MGR6bzJvbzMwd2o2N2xvaSJ9.xzks_fIbucKQlpZlKSvTfA");
        mapView = findViewById(R.id.mapViewGet);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        viewBox = findViewById(R.id.view2);


        buttonStart = findViewById(R.id.toggleStart);
        buttonStop = findViewById(R.id.toggleStop);
        buttonPause = findViewById(R.id.togglePause);
        buttonCenterCamera = findViewById(R.id.centerBtn);
        buttonStartRecording = findViewById(R.id.buttonStartRecording);
        mChronometer = (Chronometer) findViewById(R.id.mChronometer);
        statisticsButton = findViewById(R.id.buttonStatistics);

        buttonSave = findViewById(R.id.buttonSaveImage);
        buttonContinue = findViewById(R.id.buttonContinueRecordingImage);
        panelHide = findViewById(R.id.panelHide);
        hideFinishPanel();
        Intent intent = getIntent();
        Bundle args = intent.getBundleExtra("BUNDLE");
        routeCoordinatesRecieved = (String) args.getSerializable("ARRAYLIST");

        // Ask for activity recognition permission
        if (runningQOrLater) {
            getActivity();
        }


        viewBox.setVisibility(View.INVISIBLE);
        buttonStop.setVisibility(View.INVISIBLE);
        buttonPause.setVisibility(View.INVISIBLE);
        buttonStart.setVisibility(View.INVISIBLE);
        statisticsButton.setVisibility(View.INVISIBLE);

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
                Intent intent = new Intent(FollowActivity.this, RegisterTrailActivityFollowed.class);
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


                } else if (group.getCheckedButtonId() == R.id.toggleStop) {
                    buttonStart.setVisibility(View.VISIBLE);
                    buttonPause.setVisibility(View.VISIBLE);
                    buttonStart.setText("RESUME");
                    buttonPause.setText("PAUSED");
                    timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
                    mChronometer.stop();
                    stopTimer();
                    showFinishPanel();






                } else if (group.getCheckedButtonId() == R.id.togglePause) {
                    buttonStart.setVisibility(View.VISIBLE);
                    buttonPause.setVisibility(View.VISIBLE);
                    buttonStart.setText("RESUME");
                    buttonPause.setText("PAUSED");
                    timeWhenStopped = mChronometer.getBase() - SystemClock.elapsedRealtime();
                    mChronometer.stop();
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
                Integer transitionTime=3000;
                Long lonTransitionTime=transitionTime.longValue();
                Integer zoom=20;
                double douZoom=zoom.doubleValue();
                Integer tilt=30;
                double douTilt=tilt.doubleValue();

                locationComponent.setCameraMode(CameraMode.TRACKING_COMPASS,lonTransitionTime,douZoom,null,douTilt,null);
                statisticsButton.setVisibility(View.VISIBLE);

            }
        });

        buttonCenterCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer transitionTime=3000;
                Long lonTransitionTime=transitionTime.longValue();
                Integer zoom=20;
                double douZoom=zoom.doubleValue();
                Integer tilt=30;
                double douTilt=tilt.doubleValue();

                locationComponent.setCameraMode(CameraMode.TRACKING_COMPASS,lonTransitionTime,douZoom,null,douTilt,null);
            }
        });






    }


    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        FollowActivity.this.mapboxMap = mapboxMap;
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

                        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_here, null);
                        Bitmap mBitmap = BitmapUtils.getBitmapFromDrawable(drawable);
                        Drawable drawable2 = ResourcesCompat.getDrawable(getResources(), R.drawable.mapbox_marker_icon_default, null);
                        Bitmap mBitmap2 = BitmapUtils.getBitmapFromDrawable(drawable2);

                        style.addImage(
                                "space",
                                mBitmap2
                        );
                        style.addImage(
                                "space2",
                                mBitmap
                        );

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

                        FeatureCollection featureCollection = FeatureCollection.fromFeatures(new Feature[] {Feature.fromJson(routeCoordinatesRecieved)
                        });
                        List<Point> pointList = TurfMeta.coordAll(featureCollection, true);
                        Point finalPosition=pointList.get(pointList.size()-1);
                        Point startingPosition=pointList.get(0);

                        style.addSource(new GeoJsonSource("line-source-to-follow",
                                FeatureCollection.fromFeatures(new Feature[] {Feature.fromJson(routeCoordinatesRecieved)
                                })));
                        style.addLayer(new LineLayer("linelayer2", "line-source-to-follow").withProperties(
                                PropertyFactory.lineDasharray(new Float[] {0.01f, 2f}),
                                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                                PropertyFactory.lineWidth(5f),
                                PropertyFactory.lineColor(Color.parseColor("#5ee5c6"))
                        ));
                        symbol2 = symbolManager.create(new SymbolOptions()
                                .withLatLng(new LatLng(finalPosition.latitude(),finalPosition.longitude()))
                                .withIconImage("space2")
                                .withIconSize(1.5f)
                                .withDraggable(false));
                        symbol3 = symbolManager.create(new SymbolOptions()
                                .withLatLng(new LatLng(startingPosition.latitude(),startingPosition.longitude()))
                                .withIconImage("space")
                                .withIconSize(1.5f)
                                .withDraggable(false));
                        Integer transitionTime=3000;
                        Long lonTransitionTime=transitionTime.longValue();
                        Integer zoom=20;
                        double douZoom=zoom.doubleValue();
                        Integer tilt=30;
                        double douTilt=tilt.doubleValue();

                        locationComponent.setCameraMode(CameraMode.TRACKING_COMPASS,lonTransitionTime,douZoom,null,douTilt,null);

                    }
                });
    }




    @SuppressWarnings( {"MissingPermission"})
    private void enableLocationComponent(@NonNull Style loadedMapStyle) {
// Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

// Get an instance of the component
            locationComponent = mapboxMap.getLocationComponent();

// Activate with options
            locationComponent.activateLocationComponent(
                    LocationComponentActivationOptions.builder(this, loadedMapStyle).build());

// Enable to make component visible
            locationComponent.setLocationComponentEnabled(true);

// Set the component's camera mode
            locationComponent.setCameraMode(CameraMode.TRACKING_COMPASS);

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