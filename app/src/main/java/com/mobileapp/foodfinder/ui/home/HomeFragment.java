package com.mobileapp.foodfinder.ui.home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
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
import com.mobileapp.foodfinder.databinding.FragmentHomeBinding;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private FragmentHomeBinding binding;
    private GoogleMap mMap;
    private EditText addressInput;
    private Button submitButton;

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

    private void updateMapLocation(String address) throws IOException {
        // Geocoder converts the address to long/lat
        Geocoder geocoder = new Geocoder(getContext(), Locale.getDefault());

        if(address != null && !address.isEmpty()){
            Optional<Address> location = geocoder.getFromLocationName(address, 1).stream().findFirst();
            LatLng latLng = new LatLng(location.get().getLatitude(), location.get().getLongitude());
            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(latLng).title(address));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
        }else {
            Toast.makeText(getContext(), "Address not found", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
