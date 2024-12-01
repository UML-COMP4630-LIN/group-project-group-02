package com.mobileapp.foodfinder.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.model.PlaceLikelihood;
import com.google.android.libraries.places.api.model.RectangularBounds;
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.mobileapp.foodfinder.databinding.FragmentHomeBinding;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import com.google.android.libraries.places.api.Places;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private FragmentHomeBinding binding;
    private GoogleMap mMap;
    private EditText addressInput;
    private Button submitButton;

    private String getApiKey() {
        try {
            Bundle metaData = requireContext()
                    .getPackageManager()
                    .getApplicationInfo(requireContext().getPackageName(), PackageManager.GET_META_DATA)
                    .metaData;
            return metaData.getString("com.google.android.geo.API_KEY");
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.d("HomeFragment", "Failed to get API key");
            return null;
        }
    }



    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel =
                new ViewModelProvider(this).get(HomeViewModel.class);

        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        addressInput = binding.addressInput;
        submitButton = binding.addressSubmit;

        // Initialize the map
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(binding.mapContainer.getId());
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String address = addressInput.getText().toString();
                if(!address.isEmpty()){
                    try {
                        updateMapLocation(address);
                        fetchNearbyFoodBanks(address);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    Toast.makeText(getContext(), "Enter a valid address", Toast.LENGTH_SHORT).show();
                }
            }
        });
        return root;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void updateMapLocation(String address) {
        try {
            LatLng latLng = convertAddressToLatLng(address);
            // Update the map with the LatLng
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title(address));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        } catch (IOException e) {
            Toast.makeText(getContext(), "Address not found", Toast.LENGTH_SHORT).show();
        }
    }

    private LatLng convertAddressToLatLng(String address) throws IOException {
        // Geocoder converts address into LatLng
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());
        if (address != null && !address.isEmpty()) {
            Optional<Address> location = geocoder.getFromLocationName(address, 1).stream().findFirst();
            if (location.isPresent()) {
                return new LatLng(location.get().getLatitude(), location.get().getLongitude());
            }
        }
        throw new IOException("Address not found");
    }

    private void fetchNearbyFoodBanks(String address) throws IOException {
        String apiKey = getApiKey();
        if (!Places.isInitialized()) {
            Places.initialize(requireContext(), apiKey);
            Log.d("HomeFragment", "Places API initialized with API key");
        }
        PlacesClient placesClient = Places.createClient(requireContext());
        LatLng userLatLng = convertAddressToLatLng(address);
        RectangularBounds bounds = RectangularBounds.newInstance(
                new LatLng(userLatLng.latitude - 0.1, userLatLng.longitude - 0.1),
                new LatLng(userLatLng.latitude + 0.1, userLatLng.longitude + 0.1)
        );

        FindCurrentPlaceRequest request = FindCurrentPlaceRequest.builder(
                Arrays.asList(Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.TYPES)).build();

        placesClient.findCurrentPlace(request).addOnSuccessListener(response -> {
            for (PlaceLikelihood placeLikelihood : response.getPlaceLikelihoods()) {
                Place place = placeLikelihood.getPlace();
                if (place.getTypes().contains(Place.Type.FOOD)) {
                    LatLng foodBankLatLng = place.getLatLng();
                    mMap.addMarker(new MarkerOptions()
                            .position(foodBankLatLng)
                            .title(place.getName()));
                }
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(getContext(), "Error fetching food banks", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
