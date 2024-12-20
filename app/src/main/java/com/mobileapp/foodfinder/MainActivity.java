/***********************************************
 Authors: Andrew Jacobson, Noah Fay, Quang Tran, Brandon Nguyen
 Date: 12/12/24
 Purpose: Help people in need find affordable food options.
 What Learned: Java programming with APIs and different libraries, along with overall mobile app programming.
 Sources of Help:
 https://developer.android.com/guide/components/processes-and-threads
 https://developer.android.com/guide/components/processes-and-threads
 https://developers.google.com/maps/documentation/android-sdk/marker
 https://developer.android.com/reference/org/json/JSONObject
 https://square.github.io/okhttp/
 https://developers.google.com/maps/documentation/android-sdk/get-api-key
 https://developer.android.com/reference/android/location/Geocoder
 https://developers.google.com/maps/documentation/android-sdk/map
 Time Spent (Hours): Average of 6-10 hours a week
 ***********************************************/
/*
Mobile App Development I -- COMP.4630 Honor Statement
The practice of good ethical behavior is essential for maintaining good order
in the classroom, providing an enriching learning experience for students,
and training as a practicing computing professional upon graduation. This
practice is manifested in the University's Academic Integrity policy.
Students are expected to strictly avoid academic dishonesty and adhere to the
Academic Integrity policy as outlined in the course catalog. Violations will
be dealt with as outlined therein. All programming assignments in this class
are to be done by the student alone unless otherwise specified. No outside
help is permitted except the instructor and approved tutors.
I certify that the work submitted with this assignment is mine and was
generated in a manner consistent with this document, the course academic
policy on the course website on Blackboard, and the UMass Lowell academic
code.
Date: 12/12/24
Names: Andrew Jacobson, Noah Fay, Quang Tran, Brandon Nguyen
*/

package com.mobileapp.foodfinder;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
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

    /*
     * brief: Initializes the activity and sets up navigation components, themes, and listeners.
     * param: savedInstanceState - Bundle containing the activity's previously saved state.
     * return: void
     */
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

    /*
     * brief: Called when the activity resumes; updates the menu item to reflect the current mode.
     * param: None
     * return: void
     */
    @Override
    protected void onResume() {
        super.onResume();
        // Ensure the menu item title reflects the correct mode when the activity is resumed
        updateDarkModeMenuItem();
    }

    /*
     * brief: Toggles the app's theme between light and dark mode.
     * param: None
     * return: void
     */
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
    /*
     * brief: Updates the menu item title to match the current theme (Light Mode or Dark Mode).
     * param: None
     * return: void
     */
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

    /*
     * brief: Handles options menu item selection, including drawer toggle clicks.
     * param: item - The selected MenuItem.
     * return: boolean - True if the event is handled; otherwise, false.
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (toggle.onOptionsItemSelected(item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * brief: Handles navigation when the Up button is pressed.
     * param: None
     * return: boolean - True if navigation is handled; otherwise, false.
     */
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration) || super.onSupportNavigateUp();
    }
}
