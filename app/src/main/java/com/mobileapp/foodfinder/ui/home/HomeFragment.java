package com.mobileapp.foodfinder.ui.home;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.mobileapp.foodfinder.databinding.FragmentHomeBinding;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private FragmentHomeBinding binding;
    private GoogleMap mMap;

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

        // Set up the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(binding.mapContainer.getId());
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Set up the search button functionality
        binding.addressSubmit.setOnClickListener(view -> {
            String address = binding.addressInput.getText().toString().trim();
            if (!address.isEmpty()) {
                searchFoodBanks(address);
            } else {
                Toast.makeText(getContext(), "Please enter a valid address.", Toast.LENGTH_SHORT).show();
            }
        });

        return root;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
    }

    @SuppressLint("MissingPermission")
    private void searchFoodBanks(String address) {
        try {
            LatLng userLatLng = convertAddressToLatLng(address);

            // Clear previous markers
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(userLatLng).title("Your Location"));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 14));

            // URL for Google Places Nearby Search API with keyword filter
            String apiKey = getApiKey();
            String nearbySearchUrl = String.format(
                    "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=%f,%f&radius=5000&keyword=food+bank&key=%s",
                    userLatLng.latitude,
                    userLatLng.longitude,
                    apiKey
            );
            // Fetch data and place markers
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder().url(nearbySearchUrl).build();
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

                        // Log the JSON response
                        Log.d("API Response", responseBody);

                        requireActivity().runOnUiThread(() -> {
                            try {
                                // Parse the response and add markers
                                JSONObject jsonObject = new JSONObject(responseBody);
                                JSONArray results = jsonObject.getJSONArray("results");

                                for (int i = 0; i < results.length(); i++) {
                                    JSONObject place = results.getJSONObject(i);
                                    String name = place.getString("name");

                                    // Filter for relevant results based on name

                                        JSONObject location = place.getJSONObject("geometry").getJSONObject("location");
                                        double lat = location.getDouble("lat");
                                        double lng = location.getDouble("lng");

                                        // Add a marker to the map
                                        LatLng placeLatLng = new LatLng(lat, lng);
                                        mMap.addMarker(new MarkerOptions()
                                                .position(placeLatLng)
                                                .title(name));

                                }
                                Toast.makeText(getContext(), "Food banks added to map.", Toast.LENGTH_SHORT).show();
                            } catch (Exception e) {
                                Log.e("HomeFragment", "Error parsing Places API response", e);
                                Toast.makeText(getContext(), "Error displaying food banks.", Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {
                        requireActivity().runOnUiThread(() -> Toast.makeText(getContext(), "Error fetching food banks.", Toast.LENGTH_SHORT).show());
                    }
                }

            });
        } catch (IOException e) {
            Toast.makeText(getContext(), "Invalid address. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private LatLng convertAddressToLatLng(String address) throws IOException {
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        return geocoder.getFromLocationName(address, 1).stream()
                .findFirst()
                .map(location -> new LatLng(location.getLatitude(), location.getLongitude()))
                .orElseThrow(() -> new IOException("Address not found"));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}