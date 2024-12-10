package com.mobileapp.foodfinder.ui.home;

import com.mobileapp.foodfinder.ui.home.MarkerData;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.lifecycle.ViewModelProvider;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.mobileapp.foodfinder.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONObject;
import java.io.IOException;
import java.util.Locale;
import java.util.Objects;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment implements OnMapReadyCallback {
    private FragmentHomeBinding binding;
    private GoogleMap mMap;
    private Marker selectedMarker;
    private SharedViewModel viewModel;

    private void initializePlacesApi() {
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), getApiKey());
        }
    }

    private String getApiKey() {
        try {
            return requireContext()
                    .getPackageManager()
                    .getApplicationInfo(requireContext().getPackageName(), PackageManager.GET_META_DATA)
                    .metaData.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            Log.e("HomeFragment", "API Key not found", e);
            throw new RuntimeException("API Key not found");
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize the Google Places API
        initializePlacesApi();

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        // Set up the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(binding.mapContainer.getId());
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up the search button functionality
        binding.addressSubmit.setOnClickListener(view -> {
            String address = binding.addressInput.getText().toString().trim();
            String distance_userIn = binding.distanceInput.getText().toString().trim();
            if (!address.isEmpty()) {
                String distance_metersStr;
                if (!distance_userIn.isEmpty()) {
                    // Convert miles to meters
                    int distance_miles = Integer.parseInt(distance_userIn);
                    int distance_meters = distance_miles * 1610;
                    distance_metersStr = String.valueOf(distance_meters);
                } else {
                    distance_metersStr = "5000";
                }
                searchFoodBanks(address, distance_metersStr);
            } else {
                Toast.makeText(getContext(), "Please enter a valid address.", Toast.LENGTH_SHORT).show();
            }
        });
        // Zoom buttons functionality
        binding.zoomInButton.setOnClickListener(v -> zoomMap(true)); // Zoom in
        binding.zoomOutButton.setOnClickListener(v -> zoomMap(false)); // Zoom out

        binding.favoriteButton.setOnClickListener(v -> {
            if (selectedMarker != null) {
                MarkerData markerData = (MarkerData) selectedMarker.getTag();
                if (markerData != null) {
                    viewModel.addFavorite(markerData);  // Add the marker data to the favorites list
                    Toast.makeText(getContext(), "Added to favorites", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getContext(), "No marker selected", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.setOnMarkerClickListener(marker -> {
            MarkerData data = (MarkerData) marker.getTag();
            if (data != null) {
                selectedMarker = marker; // Set the selected marker
            }
            return false;
        });
    }

    @SuppressLint("MissingPermission")
    private void searchFoodBanks(String address, String distance) {
        String keyword_URL;
        String searchURL;
        try {
            LatLng userLatLng = convertAddressToLatLng(address);

            // Clear previous markers
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(userLatLng).title("Your Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14));
            // URL for Google Places Nearby Search API with keyword filter
            if (distance.isEmpty()) { // Distance defaults to 5000 meters
                distance = "5000";
            }
            String apiKey = getApiKey();
            searchURL = String.format("https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=%s&keyword=food+bank&key=%s",
                    userLatLng.latitude,
                    userLatLng.longitude,
                    distance,
                    apiKey);

            fetchAndDisplayFoodBanks(searchURL);

        } catch (IOException e) {
            Toast.makeText(getContext(), "Invalid address. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    // Fetch data and place markers
    private void fetchAndDisplayFoodBanks(String url) {
        OkHttpClient client = new OkHttpClient();

        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                requireActivity().runOnUiThread(() ->
                        Toast.makeText(getContext(), "Failed to fetch food banks.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String responseBody = response.body().string();

                    Log.d("API Response", responseBody);

                    requireActivity().runOnUiThread(() -> {
                        try {
                            // Parse the response and add markers
                            JSONObject jsonObject = new JSONObject(responseBody);
                            JSONArray results = jsonObject.getJSONArray("results");

                            // Loop through the results and add markers
                            for (int i = 0; i < results.length(); i++) {
                                JSONObject place = results.getJSONObject(i);
                                String name = place.getString("name");
                                String address = place.optString("vicinity", place.optString("formatted_address"));

                                // Check the types array and skip places with unwanted types
                                JSONArray types = place.getJSONArray("types");
                                boolean excludePlace = false;
                                for (int j = 0; j < types.length(); j++) {
                                    String type = types.getString(j);
                                    if (type.equals("gas_station") || type.equals("shopping_mall") ||
                                            type.equals("grocery_or_supermarket") || type.equals("atm") || type.equals("restaurant")) {
                                        excludePlace = true;
                                        break; // No need to check further types
                                    }
                                }

                                if (!excludePlace) {
                                    // Add a marker for valid places
                                    JSONObject location = place.getJSONObject("geometry").getJSONObject("location");
                                    double lat = location.getDouble("lat");
                                    double lng = location.getDouble("lng");

                                    LatLng placeLatLng = new LatLng(lat, lng);

                                    // Add marker with address and set the tag
                                    MarkerOptions markerOptions = new MarkerOptions()
                                            .position(placeLatLng)
                                            .title(name)
                                            .snippet(address);

                                    Marker marker = mMap.addMarker(markerOptions);

                                    if (marker != null) {
                                        marker.setTag(new MarkerData(name, address));
                                    }
                                }
                            }

                            // Handle pagination with next_page_token if available
                            if (jsonObject.has("next_page_token")) {
                                String nextPageToken = jsonObject.getString("next_page_token");

                                // Delay fetching the next page to ensure the token is active
                                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                    String nextPageUrl = String.format(
                                            "https://maps.googleapis.com/maps/api/place/nearbysearch/json?pagetoken=%s&key=%s",
                                            nextPageToken,
                                            getApiKey()
                                    );
                                    fetchAndDisplayFoodBanks(nextPageUrl); // Recursive call for the next page
                                }, 2000); // Delay of 2 seconds
                            } else {
                                Toast.makeText(getContext(), "Food banks added to map.", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e("HomeFragment", "Error parsing Places API response", e);
                            Toast.makeText(getContext(), "Error displaying food banks.", Toast.LENGTH_SHORT).show();
                        }
                    });
                } else {
                    requireActivity().runOnUiThread(() ->
                            Toast.makeText(getContext(), "Error fetching food banks.", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    @SuppressWarnings("ConstantConditions")
    private LatLng convertAddressToLatLng(String address) throws IOException {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        //no need for null check here because of the 'optional' class. The 'optional' object will never be null.
        // The stream invocation may produce a nullPointerException, changed to call 'requireNonNull'
        return Objects.requireNonNull(geocoder.getFromLocationName(address, 1)).stream()
                .findFirst()
                .map(location -> new LatLng(location.getLatitude(), location.getLongitude()))
                .orElseThrow(() -> new IOException("Address not found"));
    }

    private void zoomMap(boolean zoomIn) {
        if (mMap != null) {
            float currentZoom = mMap.getCameraPosition().zoom;
            float newZoom = zoomIn ? currentZoom + 1 : currentZoom - 1;
            mMap.moveCamera(CameraUpdateFactory.zoomTo(newZoom));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
