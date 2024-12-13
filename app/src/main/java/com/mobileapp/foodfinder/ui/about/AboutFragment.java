// AboutFragment.java
package com.mobileapp.foodfinder.ui.about;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.mobileapp.foodfinder.R;

public class AboutFragment extends Fragment {

    /*
     * brief: Called to create and return the view hierarchy associated with the fragment.
     * param: inflater - LayoutInflater object used to inflate any views in the fragment.
     *        container - The parent view that the fragment's UI should be attached to.
     *        savedInstanceState - Bundle object containing the fragment's previously saved state, if any.
     * return: The root view of the fragment's layout.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_about, container, false);

        // Get the current theme mode
        boolean isDarkMode = (getResources().getConfiguration().uiMode & android.content.res.Configuration.UI_MODE_NIGHT_MASK) == android.content.res.Configuration.UI_MODE_NIGHT_YES;

        // Set colors based on the theme
        if (isDarkMode) {
            view.setBackgroundColor(getResources().getColor(R.color.background)); // Dark background color
            TextView titleTextView = view.findViewById(R.id.about_title);
            TextView descriptionTextView = view.findViewById(R.id.about_description);
            titleTextView.setTextColor(getResources().getColor(R.color.text)); // White text for dark mode
            descriptionTextView.setTextColor(getResources().getColor(R.color.text)); // White text for dark mode
        } else {
            view.setBackgroundColor(getResources().getColor(R.color.background)); // Light background color
            TextView titleTextView = view.findViewById(R.id.about_title);
            TextView descriptionTextView = view.findViewById(R.id.about_description);
            titleTextView.setTextColor(getResources().getColor(R.color.text)); // Black text for light mode
            descriptionTextView.setTextColor(getResources().getColor(R.color.text)); // Black text for light mode
        }

        return view;
    }
}
