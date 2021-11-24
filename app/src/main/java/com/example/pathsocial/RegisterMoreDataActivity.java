package com.example.pathsocial;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.room.Room;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.anychart.APIlib;
import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Column;
import com.anychart.enums.Anchor;
import com.anychart.enums.HoverMode;
import com.anychart.enums.Position;
import com.anychart.enums.TooltipPositionMode;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.FirebaseDatabase;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class RegisterMoreDataActivity extends AppCompatActivity {
    ImageView userimage;
    TextView textViewName,textViewHeight,textViewWeight,textViewMail;


    private TrailDao trailDao;
    private List<Trail> trails;
    MaterialButtonToggleGroup materialButtonToggleGroup;
    LocalDateTime currDatetime = LocalDateTime.now();
    int currYear = currDatetime.getYear();
    int currMon = currDatetime.getMonthValue();
    int currDay = currDatetime.getDayOfMonth();

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

    String name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_more_data);
        userimage=findViewById(R.id.imageView2);
        textViewName=findViewById(R.id.textView5);
        textViewHeight=findViewById(R.id.textView7);
        textViewWeight=findViewById(R.id.textView8);
        textViewMail=findViewById(R.id.textView6);
        userimage.setImageBitmap(createImage(200, 200,Color.LTGRAY ,"AA"));

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
                    textViewName.setText(name);
                    String initials = "";
                    for (String s : name.split(" ")) {
                        initials+=s.charAt(0);
                    }

                    userimage.setImageBitmap(createImage(200, 200,Color.LTGRAY ,initials));

                }
            }
        });
        database.getReference("users").child(firebaseUser.getUid()).child("height").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    textViewHeight.setText(String.valueOf(task.getResult().getValue()));
                }
            }
        });
        database.getReference("users").child(firebaseUser.getUid()).child("weight").get().addOnCompleteListener(new OnCompleteListener<DataSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DataSnapshot> task) {
                if (!task.isSuccessful()) {
                    Log.e("firebase", "Error getting data", task.getException());
                }
                else {
                    textViewWeight.setText(String.valueOf(task.getResult().getValue()));
                }
            }
        });

        textViewMail.setText(String.valueOf(email));

        // create plots

        // get data
        AppDatabase db = Room.databaseBuilder(getApplicationContext(),
                AppDatabase.class, "database-trails").allowMainThreadQueries().build();

        trailDao = db.trailDao();
        trails = trailDao.getAll();

        Map<String, Integer> dateStepsMap = new TreeMap<>();
        Map<String, Integer> dateCaloriesMap = new TreeMap<>();
        int stepTotal = 0;
        int stepYear = 0;
        int stepMon = 0;
        int stepDay = 0;
        int calTotal = 0;
        int calYear = 0;
        int calMon = 0;
        int calDay = 0;

        for(Trail trail : trails)
        {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");
            LocalDateTime dateTime = LocalDateTime.parse(trail.datetime, formatter);
            String date = String.valueOf(dateTime.getYear()) + '-' +
                    String.valueOf(dateTime.getMonthValue()) + '-' + String.valueOf(dateTime.getDayOfMonth());

            // show graph only for last 7 days
            LocalDateTime graphStartDatetime = LocalDateTime.now().minusDays(7);

            // get graph data
            if (dateTime.isAfter(graphStartDatetime)) {
                // add the number of steps for the day
                dateStepsMap.merge(date, trail.stepsNubers, Integer::sum);
                // add the number of calories for the day
                dateCaloriesMap.merge(date, trail.calories, Integer::sum);
            }

            // update year, month, total, and day counters
            stepTotal += trail.stepsNubers;
            calTotal += trail.calories;
            if (dateTime.getYear() == currYear) {
                stepYear += trail.stepsNubers;
                calYear += trail.calories;

                if (dateTime.getMonthValue() == currMon) {
                    stepMon += trail.stepsNubers;
                    calMon += trail.calories;

                    if (dateTime.getDayOfMonth() == currDay) {
                        stepDay += trail.stepsNubers;
                        calDay += trail.calories;
                    }
                }
            }
        }
        // 2. Create data entries for x and y axis of the graph
        List<DataEntry> dataStep = new ArrayList<>();
        List<DataEntry> dataCal = new ArrayList<>();

        for (Map.Entry<String,Integer> entry : dateStepsMap.entrySet())
            dataStep.add(new ValueDataEntry(entry.getKey(), entry.getValue()));
        for (Map.Entry<String,Integer> entry : dateCaloriesMap.entrySet())
            dataCal.add(new ValueDataEntry(entry.getKey(), entry.getValue()));

        //***** Create column chart using AnyChart library *********/
        // 1. Create and get the cartesian coordinate system for column chart

        // plot step chart
        AnyChartView anyChartViewStep = findViewById(R.id.dayStepChart);
        APIlib.getInstance().setActiveAnyChartView(anyChartViewStep);

        Cartesian cartesianStep = AnyChart.column();
        Column columnStep = cartesianStep.column(dataStep);
        cartesianStep = formatColumnChart(columnStep, cartesianStep, "steps");
        anyChartViewStep.setChart(cartesianStep);

        // plot calorie chart
        AnyChartView anyChartViewCalorie = findViewById(R.id.dayCalorieChart);
        APIlib.getInstance().setActiveAnyChartView(anyChartViewCalorie);

        Cartesian cartesianCal = AnyChart.column();
        Column columnCal = cartesianCal.column(dataCal);
        cartesianCal = formatColumnChart(columnCal, cartesianCal, "calories");
        anyChartViewCalorie.setChart(cartesianCal);

        FrameLayout stepFrame = findViewById(R.id.stepFrame);
        FrameLayout calFrame = findViewById(R.id.calFrame);
        calFrame.setVisibility(View.GONE);

        // set text values for steps and calories (total, year, month, and day)
        TextView stepTotalView = findViewById(R.id.stepTotal);
        stepTotalView.setText(String.valueOf(stepTotal));
        TextView stepYearView = findViewById(R.id.stepYear);
        stepYearView.setText(String.valueOf(stepYear));
        TextView stepMonView = findViewById(R.id.stepMonth);
        stepMonView.setText(String.valueOf(stepMon));
        TextView stepDayView = findViewById(R.id.stepDay);
        stepDayView.setText(String.valueOf(stepDay));

        TextView calTotalView = findViewById(R.id.calTotal);
        calTotalView.setText(String.valueOf(calTotal));
        TextView calYearView = findViewById(R.id.calYear);
        calYearView.setText(String.valueOf(calYear));
        TextView calMonView = findViewById(R.id.calMonth);
        calMonView.setText(String.valueOf(calMon));
        TextView calDayView = findViewById(R.id.calDay);
        calDayView.setText(String.valueOf(calDay));

        // Toggle group button
        materialButtonToggleGroup = findViewById(R.id.toggleButton);
        materialButtonToggleGroup.addOnButtonCheckedListener(new MaterialButtonToggleGroup.OnButtonCheckedListener() {
            @Override
            public void onButtonChecked(MaterialButtonToggleGroup group, int checkedId, boolean isChecked) {

                if (checkedId == R.id.stepsButton) {
                    //Place code related to steps button
                    calFrame.setVisibility(View.GONE);
                    stepFrame.setVisibility(View.VISIBLE);
                } else if (checkedId == R.id.caloriesButton) {
                    //Place code related to calories button
                    stepFrame.setVisibility(View.GONE);
                    calFrame.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    private Cartesian formatColumnChart(Column column, Cartesian cartesian, String dataType){
        //***** Modify the UI of the chart *********/
        // Change the color of column chart and its border
        column.fill("#1EB980");
        column.stroke("#1EB980");

        //Modifying properties of tooltip
        if (dataType.equals("steps")) {
            column.tooltip()
                    .titleFormat("At day: {%X}")
                    .format("{%Value}{groupsSeparator: } Steps")
                    .anchor(Anchor.RIGHT_TOP);
            cartesian.yAxis(0).title("Number of steps");
        }
        else if (dataType.equals("calories")) {
            column.tooltip()
                    .titleFormat("At day: {%X}")
                    .format("{%Value}{groupsSeparator: } Calories")
                    .anchor(Anchor.RIGHT_TOP);
            cartesian.yAxis(0).title("Number of calories");
        }

        // Modify column chart tooltip properties
        column.tooltip()
                .position(Position.RIGHT_TOP)
                .offsetX(0d)
                .offsetY(5);

        // Modifying properties of cartesian
        cartesian.tooltip().positionMode(TooltipPositionMode.POINT);
        cartesian.interactivity().hoverMode(HoverMode.BY_X);
        cartesian.yScale().minimum(0);

        // Modify the UI of the cartesian
        cartesian.xAxis(0).title("Day");
        cartesian.background().fill("#00000000");
        cartesian.animation(true);

        return cartesian;
    }

}