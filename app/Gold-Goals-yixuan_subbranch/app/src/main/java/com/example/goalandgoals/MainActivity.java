package com.example.goalandgoals;

import android.os.Bundle;
import android.util.Log;

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



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.d(TAG, "onCreate: Initializing MainActivity");

        BottomNavigationView navView = findViewById(R.id.bottom_navigation);

        // Define top-level destinations for the AppBarConfiguration
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_tasks, R.id.nav_status, R.id.nav_shop, R.id.nav_pomodoro, R.id.nav_settings)
                .build();

        // Find the NavController from the NavHostFragment
        NavController navController;
        try {
            navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        } catch (IllegalStateException e) {
            Log.e(TAG, "onCreate: Failed to find NavController", e);
            return;
        }

        // Set up the BottomNavigationView with the NavController
        NavigationUI.setupWithNavController(navView, navController);

        // Log navigation changes
        navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
            Log.d(TAG, "Navigated to: " + destination.getLabel());
        });


    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }




}