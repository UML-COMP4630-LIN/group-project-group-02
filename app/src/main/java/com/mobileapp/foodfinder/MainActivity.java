package com.mobileapp.foodfinder;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.mobileapp.foodfinder.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private AppBarConfiguration appBarConfiguration;
    private SharedPreferences sharedPreferences;

    @SuppressLint("NonConstantResourceId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load saved theme preference
        sharedPreferences = getSharedPreferences("user_preferences", MODE_PRIVATE);
        boolean isDarkMode = sharedPreferences.getBoolean("dark_mode", false);

        // Apply saved theme
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize DrawerLayout
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navDrawer = findViewById(R.id.nav_drawer);

        // Initialize BottomNavigationView
        BottomNavigationView navView = findViewById(R.id.nav_view);

        // Setup NavController
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);

        // Configure AppBar with Drawer and BottomNavigationView
        appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_home, R.id.navigation_favorites)
                .setOpenableLayout(drawerLayout)
                .build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);
        NavigationUI.setupWithNavController(navDrawer, navController);

        // Setup Drawer Toggle
        toggle = new ActionBarDrawerToggle(this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Handle Navigation Drawer item clicks
        navDrawer.setNavigationItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.menu_dark_mode) {
                toggleDarkMode();
            } else if (id == R.id.menu_about) {
                // Open AboutFragment when the "About" menu item is clicked
                navController.navigate(R.id.navigation_about);
            }
            drawerLayout.closeDrawers();
            return true;
        });

        navView.setOnNavigationItemSelectedListener(item -> {
            if (item.getItemId() == R.id.navigation_home) {
                navController.navigate(R.id.navigation_home);
                return true;
            } else if (item.getItemId() == R.id.navigation_favorites) {
                navController.navigate(R.id.navigation_favorites);
                return true;
            }
            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Ensure the menu item title reflects the correct mode when the activity is resumed
        updateDarkModeMenuItem();
    }

    private void toggleDarkMode() {
        boolean isNightMode = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            editor.putBoolean("dark_mode", false);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            editor.putBoolean("dark_mode", true);
        }
        editor.apply();

        // Update the menu item title dynamically after toggling theme
        updateDarkModeMenuItem();
    }

    // Update the Dark/Light Mode menu item title based on the current theme
    private void updateDarkModeMenuItem() {
        NavigationView navDrawer = findViewById(R.id.nav_drawer);
        Menu menu = navDrawer.getMenu();
        MenuItem darkModeMenuItem = menu.findItem(R.id.menu_dark_mode);

        // Check if it's in Dark Mode or Light Mode and update the title
        boolean isNightMode = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;
        if (isNightMode) {
            darkModeMenuItem.setTitle("Light Mode");
        } else {
            darkModeMenuItem.setTitle("Dark Mode");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}
