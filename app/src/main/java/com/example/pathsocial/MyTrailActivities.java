package com.example.pathsocial;

import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;
import androidx.room.Room;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
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
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.UUID;

import com.mapbox.mapboxsdk.utils.BitmapUtils;

public class MyTrailActivities extends AppCompatActivity implements
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
    private Symbol symbol2;
    private Button buttonStartRecording;
    private static final String MAKI_ICON_MARKER = "marker-15";
    private View viewBox;
    private List<Point> routeCoordinates = new ArrayList<>();
    private java.util.HashMap<Long,List<Point>> trailsMap=new HashMap<Long,List<Point>>();
    private java.util.HashMap<Long,String> trailsName=new HashMap<Long,String>();
    private java.util.HashMap<Long,Integer> trailsSteps=new HashMap<Long,Integer>();
    private java.util.HashMap<Long,Integer> trailsCalories=new HashMap<Long,Integer>();
    private java.util.HashMap<Long,Double> trailsDistance=new HashMap<Long,Double>();
    private java.util.HashMap<Long,Double> trailsMedianSpeed=new HashMap<Long,Double>();
    private java.util.HashMap<Long,String> trailsDate=new HashMap<Long,String>();
    private java.util.HashMap<Long,String> trailsDuration=new HashMap<Long,String>();
    private java.util.HashMap<Long,String> trailsDescription=new HashMap<Long,String>();
    private java.util.HashMap<Long,String> trailsWeather=new HashMap<Long,String>();
    private java.util.HashMap<Long,Boolean> trailsMine=new HashMap<Long,Boolean>();
    private java.util.HashMap<Long,Boolean> publishedMap=new HashMap<Long,Boolean>();
    private java.util.HashMap<Long,Trail> trailID=new HashMap<Long,Trail>();
    private java.util.HashMap<Long,Symbol> symbolMap=new HashMap<Long,Symbol>();

    public Trail selectedPath;
    public TrailForDb selectedPathFirebase;
    public Symbol selectedSymbol;
    public TrailDao trailDao;


    public Button btn_remove,btn_publish;
    private FirebaseDatabase database;
    private GeoFire geoFire;
    private DatabaseReference refPath;

    public Trail trailToAdd = new Trail();



    private Boolean doneLoadingMap=false;
    List<Trail> trails;
    private Boolean clicked=false;
    private Menu menu;
    BottomSheetBehavior bottomSheetBehavior;
    TextView trailsNameTxt,trailsStepsTxt,trailsCaloriesTxt,trailsDistanceTxt,trailsMedianSpeedTxt,trailsDateTxt,trailsDurationTxt,trailsDescriptionTxt,trailsWeatherTxt;


    Boolean isFABOpen=false;
    FloatingActionButton fab,fab1,fab2,fab3;
    Button search_btn;


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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        locman = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Mapbox.getInstance(this, "pk.eyJ1Ijoic3RlZmxhbWIiLCJhIjoiY2t0MWNkeXg0MGR6bzJvbzMwd2o2N2xvaSJ9.xzks_fIbucKQlpZlKSvTfA");
        setContentView(R.layout.activity_my_trail_activities);
        mapView = findViewById(R.id.mapViewActivities);
        btn_remove = findViewById(R.id.button_rmv);
        btn_publish = findViewById(R.id.publish_btn);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);




        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab3 = (FloatingActionButton) findViewById(R.id.fab3);
        search_btn = (Button) findViewById(R.id.buttonSearch);
        fab1.setVisibility(View.INVISIBLE);
        fab2.setVisibility(View.INVISIBLE);
        fab3.setVisibility(View.INVISIBLE);


        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-trails").allowMainThreadQueries().build();

        trailDao = db.trailDao();

        Log.d("tag",routeCoordinates.toString());
        trails = trailDao.getAll();

        ConstraintLayout bottomSheet = findViewById(R.id.bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        trailsNameTxt= findViewById(R.id.textNameTrail);
        trailsStepsTxt= findViewById(R.id.textViewSteps);
        trailsCaloriesTxt= findViewById(R.id.textViewCalories);
        trailsDistanceTxt= findViewById(R.id.textViewDistance);
        trailsMedianSpeedTxt= findViewById(R.id.textViewMedian);
        trailsDateTxt= findViewById(R.id.textViewDate);
        trailsDurationTxt= findViewById(R.id.textViewDuration);
        trailsDescriptionTxt= findViewById(R.id.textViewDesc);
        trailsWeatherTxt= findViewById(R.id.textViewWeather);

        database = FirebaseDatabase.getInstance();
        DatabaseReference refLocation = FirebaseDatabase.getInstance().getReference("trails/locations");
        refPath = FirebaseDatabase.getInstance().getReference("trails/path");
        geoFire = new GeoFire(refLocation);



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
                startActivity(new Intent(MyTrailActivities.this, MapPage.class));
            }
        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MyTrailActivities.this, RecordActivity.class);
                intent.putExtra("message", 0);
                startActivity(intent);
            }
        });
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Location lastKnownLocation = mapboxMap.getLocationComponent().getLastKnownLocation();
                double lat = lastKnownLocation.getLatitude();
                double longitude = lastKnownLocation.getLongitude();
                Intent intent = new Intent(MyTrailActivities.this, LeagueActivity.class);
                Bundle b = new Bundle();
                b.putDouble("lat",lat);
                b.putDouble("lon", longitude);
                intent.putExtras(b);
                startActivity(intent);
                finish();
            }
        });


// ..


        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        fab.setVisibility(View.VISIBLE);
                        MyTrailActivities.this.mapboxMap.getStyle().removeLayer("linelayer");
                        MyTrailActivities.this.mapboxMap.getStyle().removeSource("line-source");
                        clicked=false;
                        symbolManager.delete(symbol2);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        fab.setVisibility(View.INVISIBLE);
                        closeFABMenu();
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }
            @Override public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });


        btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                AlertDialog diaBox = AskOption();
                diaBox.show();

            }
        });

        btn_publish.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                AlertDialog diaBox = AskOptionPublish();
                diaBox.show();
            }
        });









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
                        trailDao.delete(selectedPath);
                        symbolManager.delete(selectedSymbol);
                        mapboxMap.getStyle().removeLayer("linelayer");
                        mapboxMap.getStyle().removeSource("line-source");
                        clicked=false;
                        symbolManager.delete(symbol2);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        dialog.dismiss();
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

    private AlertDialog AskOptionPublish()
    {
        AlertDialog myQuittingDialogBox = new AlertDialog.Builder(this)
                // set message, title, and icon
                .setTitle("Publish")
                .setMessage("Do you want to Publish? People in your area will be able to wiew your trail")
                .setIcon(R.drawable.mapbox_compass_icon)

                .setPositiveButton("Publish", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        String pathId = refPath.push().getKey();
                        LineString lineString = LineString.fromLngLats(selectedPath.trailData);
                        Feature feature = Feature.fromGeometry(lineString);
                        String geoJson = feature.toJson();
                        String usrId ="";
                        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                        if(firebaseUser != null) {
                            usrId = firebaseUser.getUid();

                        }



                        selectedPathFirebase = new TrailForDb(selectedPath.getPathid(),
                                                                selectedPath.getFirstName(),
                                                                selectedPath.getDescription(),
                                                                selectedPath.getDuration()+0.01,
                                                                selectedPath.getDatetime(),
                                                                selectedPath.getStartingPointLat(),
                                                                selectedPath.getStartingPointLongit(),
                                                                geoJson,
                                                                selectedPath.getStepsNubers(),
                                                                selectedPath.getCalories(),
                                                                selectedPath.getDistance()+0.01,
                                                                selectedPath.getMedianSpeed()+0.01,
                                                                selectedPath.getWeather(),usrId,0,0);

                        refPath.child(pathId).setValue(selectedPathFirebase);
                        geoFire.setLocation(pathId, new GeoLocation(selectedSymbol.getLatLng().getLatitude(),selectedSymbol.getLatLng().getLongitude()));
                        dialog.dismiss();
                        btn_publish.setVisibility(View.INVISIBLE);

                        trailDao.delete(selectedPath);
                        trailToAdd.pathid=selectedPath.getPathid();
                        trailToAdd.firstName=selectedPath.getFirstName();
                        trailToAdd.description=selectedPath.getDescription();
                        trailToAdd.startingPointLat=selectedPath.getStartingPointLat();
                        trailToAdd.startingPointLongit=selectedPath.getStartingPointLongit();
                        trailToAdd.stepsNubers=selectedPath.getStepsNubers();
                        trailToAdd.duration=selectedPath.getDuration();
                        trailToAdd.datetime=selectedPath.getDatetime();
                        trailToAdd.trailData=selectedPath.getTrailData();
                        trailToAdd.weather=selectedPath.getWeather();
                        trailToAdd.calories=selectedPath.getCalories();
                        trailToAdd.medianSpeed=selectedPath.getMedianSpeed();
                        trailToAdd.distance=selectedPath.getDistance();
                        trailToAdd.personal=true;
                        trailToAdd.published=true;
                        trailDao.insertAll(trailToAdd);
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
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        MyTrailActivities.this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/steflamb/ckuf5kglt9thk17o3b0navprv"),
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                        symbolManager = new SymbolManager(mapView, mapboxMap, style);
                        symbolManager.setIconAllowOverlap(true);

                        symbolManager.setIconTranslate(new Float[]{-4f,5f});
                        symbolManager.setIconRotationAlignment(ICON_ROTATION_ALIGNMENT_VIEWPORT);
                        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.mapbox_marker_icon_default, null);
                        Bitmap mBitmap = BitmapUtils.getBitmapFromDrawable(drawable);
                        Drawable drawable2 = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_here, null);
                        Bitmap mBitmap2 = BitmapUtils.getBitmapFromDrawable(drawable2);
                        style.addSource(new GeoJsonSource("line-source",
                                FeatureCollection.fromFeatures(new Feature[] {Feature.fromGeometry(
                                        LineString.fromLngLats(routeCoordinates)
                                )})));
                        style.addImage(
                                "space",
                                mBitmap
                        );
                        style.addImage(
                                "space2",
                                mBitmap2
                        );
                        style.addLayer(new LineLayer("linelayer", "line-source").withProperties(
                                PropertyFactory.lineDasharray(new Float[] {0.01f, 2f}),
                                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                                PropertyFactory.lineWidth(5f),
                                PropertyFactory.lineColor(Color.parseColor("#e55e5e"))
                        ));
                        for(Trail trail : trails)
                        {
                            symbol = symbolManager.create(new SymbolOptions()
                                    .withLatLng(new LatLng(trail.startingPointLat,trail.startingPointLongit))
                                    .withIconImage("space")
                                    .withIconSize(1.5f)
                                    .withDraggable(false));
                            trailsMap.put(symbol.getId(),trail.trailData);
                            trailsName.put(symbol.getId(),trail.firstName);
                            trailsCalories.put(symbol.getId(),trail.calories);
                            trailsSteps.put(symbol.getId(),trail.stepsNubers);
                            trailsDate.put(symbol.getId(),trail.datetime);
                            trailsDuration.put(symbol.getId(),trail.duration);
                            trailsMedianSpeed.put(symbol.getId(),trail.medianSpeed);
                            trailsDistance.put(symbol.getId(),trail.distance);
                            trailsDescription.put(symbol.getId(),trail.description);
                            trailsWeather.put(symbol.getId(),trail.weather);
                            trailsMine.put(symbol.getId(),trail.personal);
                            trailID.put(symbol.getId(),trail);
                            symbolMap.put(symbol.getId(),symbol);
                            routeCoordinates=trail.trailData;
                            publishedMap.put(symbol.getId(),trail.published);


                        }

                        symbolManager.addClickListener(new OnSymbolClickListener() {
                            @Override
                            public boolean onAnnotationClick(Symbol symbol) {
                                if(clicked)
                                {
                                    style.removeLayer("linelayer");
                                    style.removeSource("line-source");
                                    clicked=false;
                                    symbolManager.delete(symbol2);
                                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                                }
                                else {

                                    clicked=true;
                                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                                    style.removeLayer("linelayer");
                                    style.removeSource("line-source");
                                    routeCoordinates=trailsMap.get(symbol.getId());
                                    trailsNameTxt.setText(trailsName.get(symbol.getId()));
                                    trailsCaloriesTxt.setText(String.valueOf(trailsCalories.get(symbol.getId())));
                                    trailsDateTxt.setText(trailsDate.get(symbol.getId()));
                                    trailsDescriptionTxt.setText(trailsDescription.get(symbol.getId()));
                                    trailsDistanceTxt.setText(String.format("%.3f", trailsDistance.get(symbol.getId()))+" Km");
                                    trailsDurationTxt.setText(trailsDuration.get(symbol.getId()));
                                    trailsWeatherTxt.setText(trailsWeather.get(symbol.getId()));
                                    trailsStepsTxt.setText(String.valueOf(trailsSteps.get(symbol.getId())));
                                    trailsStepsTxt.setText(String.valueOf(trailsSteps.get(symbol.getId())));
                                    trailsMedianSpeedTxt.setText(String.format("%.3f", trailsMedianSpeed.get(symbol.getId()))+" Km/h");
                                    Point finalPosition=routeCoordinates.get(routeCoordinates.size()-1);
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
                                    symbol2 = symbolManager.create(new SymbolOptions()
                                            .withLatLng(new LatLng(finalPosition.latitude(),finalPosition.longitude()))
                                            .withIconImage("space2")
                                            .withIconSize(1.5f)
                                            .withDraggable(false));
                                    selectedPath=trailID.get(symbol.getId());
                                    selectedSymbol=symbolMap.get(symbol.getId());
                                    if (trailsMine.get(symbol.getId())==false)
                                    {
                                        btn_publish.setVisibility(View.INVISIBLE);
                                    }
                                    else if(selectedPath.published)
                                    {
                                        btn_publish.setVisibility(View.INVISIBLE);
                                    }
                                    else {
                                        btn_publish.setVisibility(View.VISIBLE);
                                    }

                                }


                                return true;
                            }
                        });

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