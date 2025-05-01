package com.example.goalandgoals;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.example.goalandgoals.Model.ToDoModel;
import com.example.goalandgoals.Model.UserProgress;
import com.example.goalandgoals.Utils.AppDatabase;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private ImageView btnPomodoro; //

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Initializing MainActivity");

        // 初始化原有导航组件
        BottomNavigationView navView = findViewById(R.id.bottom_navigation);
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_tasks, R.id.nav_status, R.id.nav_shop, R.id.nav_settings)
                .build();

        // 初始化图片按钮（替换原FloatingActionButton）
        btnPomodoro = findViewById(R.id.btn_pomodoro);

        // 设置图片按钮点击监听
        btnPomodoro.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, PomodoroActivity.class))
        );

        try {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
            NavigationUI.setupWithNavController(navView, navController);

            navController.addOnDestinationChangedListener((controller, destination, arguments) ->
                    Log.d(TAG, "Navigated to: " + destination.getLabel())
            );

        } catch (IllegalStateException e) {
            Log.e(TAG, "onCreate: Failed to find NavController", e);
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }
}