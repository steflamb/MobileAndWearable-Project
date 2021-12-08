package com.example.pathsocial;

import static com.mapbox.mapboxsdk.style.layers.Property.ICON_ROTATION_ALIGNMENT_VIEWPORT;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.res.ResourcesCompat;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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
import com.mapbox.mapboxsdk.utils.BitmapUtils;
import com.mapbox.turf.TurfMeta;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapPage extends AppCompatActivity implements
        OnMapReadyCallback, PermissionsListener {

    private PermissionsManager permissionsManager;
    private MapboxMap mapboxMap;
    private MapView mapView;
    private FirebaseDatabase database;
    private GeoFire geoFire;
    private DatabaseReference refPath;
    private DatabaseReference refLocation;
    private List<TrailForDb> trailsFromDb= new ArrayList<>();;

    private SymbolManager symbolManager;
    private Symbol symbol;
    private Symbol symbol2;
    private String routeCoordinates;
    private FirebaseUser firebaseUser;

    private boolean owner;
    private String user_read;


    private List<Point> routeCoordinatesToFollow = new ArrayList<>();



    private java.util.HashMap<Long,String> trailsMap=new HashMap<Long,String>();
    private java.util.HashMap<Long,String> trailsName=new HashMap<Long,String>();
    private java.util.HashMap<Long,Integer> trailsSteps=new HashMap<Long,Integer>();
    private java.util.HashMap<Long,Integer> trailsCalories=new HashMap<Long,Integer>();
    private java.util.HashMap<Long,Double> trailsDistance=new HashMap<Long,Double>();
    private java.util.HashMap<Long,Double> trailsMedianSpeed=new HashMap<Long,Double>();
    private java.util.HashMap<Long,String> trailsDate=new HashMap<Long,String>();
    private java.util.HashMap<Long,String> trailsDuration=new HashMap<Long,String>();
    private java.util.HashMap<Long,String> trailsDescription=new HashMap<Long,String>();
    private java.util.HashMap<Long,String> trailsWeather=new HashMap<Long,String>();
    private java.util.HashMap<Long,TrailForDb> trailID=new HashMap<Long,TrailForDb>();
    private java.util.HashMap<Long,Symbol> symbolMap=new HashMap<Long,Symbol>();
    private java.util.HashMap<Long,String> usrMap=new HashMap<Long,String>();


    private Boolean clicked=false;

    public Symbol selectedSymbol;

    public TrailForDb selectedPath;


    List<Symbol> symbols = new ArrayList<>();



    Boolean isFABOpen=false;
    FloatingActionButton fab,fab1,fab2,fab3;
    Button search_btn,followButton;


    BottomSheetBehavior bottomSheetBehavior;
    TextView trailsNameTxt,trailsStepsTxt,trailsCaloriesTxt,userNameTxt,trailsDistanceTxt,trailsMedianSpeedTxt,trailsDateTxt,trailsDurationTxt,trailsDescriptionTxt,trailsWeatherTxt;


    private Menu menu;




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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        this.menu=menu;

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.action_logout:
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(MapPage.this, MainActivity.class)); //Go back to home page
                finish();
                return true;
            case R.id.action_settings:
                startActivity(new Intent(MapPage.this, RegisterMoreDataActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        //noback
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();











// Mapbox access token is configured here. This needs to be called either in your application
// object or in the same activity which contains the mapview.
        Mapbox.getInstance(this, "pk.eyJ1Ijoic3RlZmxhbWIiLCJhIjoiY2t0MWNkeXg0MGR6bzJvbzMwd2o2N2xvaSJ9.xzks_fIbucKQlpZlKSvTfA");

// This contains the MapView in XML and needs to be called after the access token is configured.
        setContentView(R.layout.activity_map_page);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab1 = (FloatingActionButton) findViewById(R.id.fab1);
        fab2 = (FloatingActionButton) findViewById(R.id.fab2);
        fab3 = (FloatingActionButton) findViewById(R.id.fab3);
        search_btn = (Button) findViewById(R.id.buttonSearch);
        followButton = (Button) findViewById(R.id.button_follow);
        fab1.setVisibility(View.INVISIBLE);
        fab2.setVisibility(View.INVISIBLE);
        fab3.setVisibility(View.INVISIBLE);


        ConstraintLayout bottomSheet = findViewById(R.id.bottom_sheet_2);
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
        userNameTxt= findViewById(R.id.textViewUser);

        bottomSheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        fab.setVisibility(View.VISIBLE);
                        MapPage.this.mapboxMap.getStyle().removeLayer("linelayer");
                        MapPage.this.mapboxMap.getStyle().removeSource("line-source");
                        clicked=false;
                        symbolManager.delete(symbol2);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        owner=false;
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




        database = FirebaseDatabase.getInstance();
        refLocation = FirebaseDatabase.getInstance().getReference("trails/locations");
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
                Intent intent = new Intent(MapPage.this, LeagueActivity.class);
                startActivity(intent);
                finish();
            }
        });

        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MapPage.this, RecordActivity.class);
                startActivity(intent);
                finish();
            }
        });
        fab3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MapPage.this, MyTrailActivities.class));
                finish();
            }
        });

        search_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                symbolManager.delete(symbols);
                trailsFromDb = new ArrayList<>();
                Location lastKnownLocation = mapboxMap.getLocationComponent().getLastKnownLocation();
                double lat = lastKnownLocation.getLatitude();
                double longitude = lastKnownLocation.getLongitude();
//              Log.d("latlang", String.valueOf(lat) + String.valueOf(longitude));
                GeoQuery geoQuery = geoFire.queryAtLocation(new GeoLocation(lat,longitude), 2);
                findLocations(geoQuery);

            }
        });


        followButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!owner) {
                    Intent intent = new Intent(MapPage.this, FollowActivity.class);
                    Log.d("tag", routeCoordinates.toString());
                    Bundle args = new Bundle();
                    args.putSerializable("ARRAYLIST", (Serializable) routeCoordinates);
                    intent.putExtra("BUNDLE", args);
                    startActivity(intent);
                }
                else
                {
//                    delete logic
                }
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
                                //Get map of users in datasnapshot
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



                                TrailForDb newTrail=new TrailForDb(pathid,firstName,description,duration,datetime,startingPointLat,startingPointLongit,trailData,(int)stepsNubers.intValue(),(int)calories.intValue(),distance.doubleValue(),medianSpeed.doubleValue(),weather,usrId);
                                trailsFromDb.add(newTrail);
                                symbolManager.delete(symbols);
                                trailsMap=new HashMap<Long,String>();
                                trailsName=new HashMap<Long,String>();
                                trailsSteps=new HashMap<Long,Integer>();
                                trailsCalories=new HashMap<Long,Integer>();
                                trailsDistance=new HashMap<Long,Double>();
                                trailsMedianSpeed=new HashMap<Long,Double>();
                                trailsDate=new HashMap<Long,String>();
                                trailsDuration=new HashMap<Long,String>();
                                trailsDescription=new HashMap<Long,String>();
                                trailsWeather=new HashMap<Long,String>();
                                trailID=new HashMap<Long,TrailForDb>();
                                symbolMap=new HashMap<Long,Symbol>();
                                usrMap=new HashMap<Long,String>();
                                for(TrailForDb trail : trailsFromDb)
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
                                    trailID.put(symbol.getId(),trail);
                                    symbolMap.put(symbol.getId(),symbol);
                                    usrMap.put(symbol.getId(),trail.usrId);
                                    symbols.add(symbol);
                                    routeCoordinates=trail.trailData;


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
//line 425 changed!!!

    @Override
    public void onMapReady(@NonNull final MapboxMap mapboxMap) {
        MapPage.this.mapboxMap = mapboxMap;

        mapboxMap.setStyle(new Style.Builder().fromUri("mapbox://styles/steflamb/ckuf5kglt9thk17o3b0navprv"),
                new Style.OnStyleLoaded() {
                    @Override
                    public void onStyleLoaded(@NonNull Style style) {
                        enableLocationComponent(style);
                        symbolManager = new SymbolManager(mapView, mapboxMap, style);
                        symbolManager.setIconAllowOverlap(false);

                        symbolManager.setIconTranslate(new Float[]{-4f,5f});
                        symbolManager.setIconRotationAlignment(ICON_ROTATION_ALIGNMENT_VIEWPORT);
                        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.mapbox_marker_icon_default, null);
                        Bitmap mBitmap = BitmapUtils.getBitmapFromDrawable(drawable);
                        Drawable drawable2 = ResourcesCompat.getDrawable(getResources(), R.drawable.ic_here, null);
                        Bitmap mBitmap2 = BitmapUtils.getBitmapFromDrawable(drawable2);

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
                                    owner=false;
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
                                    user_read="";
                                    database.getReference().child("users").child(usrMap.get(symbol.getId())).child("full_name").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DataSnapshot> task) {
                                            if (!task.isSuccessful()) {
                                                Log.e("firebase", "Error getting data", task.getException());
                                            }
                                            else {
                                                userNameTxt.setText(String.valueOf(task.getResult().getValue()));

                                            }
                                        }
                                    });
                                    user_read=usrMap.get(symbol.getId());
                                    if(user_read.equals(firebaseUser.getUid()))
                                    {
                                        owner=true;
                                        followButton.setText("Unpublish");

                                    }
                                    else
                                    {
                                        owner=false;
                                        followButton.setText("Follow trail");
                                    }




                                    FeatureCollection featureCollection = FeatureCollection.fromFeatures(new Feature[] {Feature.fromJson(routeCoordinates)
                                    });
                                    List<Point> pointList = TurfMeta.coordAll(featureCollection, true);
                                    Point finalPosition=pointList.get(pointList.size()-1);

                                    style.addSource(new GeoJsonSource("line-source",
                                            FeatureCollection.fromFeatures(new Feature[] {Feature.fromJson(routeCoordinates)
                                            })));
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