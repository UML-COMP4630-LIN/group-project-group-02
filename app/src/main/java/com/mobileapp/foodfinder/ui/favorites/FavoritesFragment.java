package com.mobileapp.foodfinder.ui.favorites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.mobileapp.foodfinder.databinding.FragmentFavoritesBinding;
import com.mobileapp.foodfinder.ui.home.MarkerData;
import com.mobileapp.foodfinder.ui.home.SharedViewModel;

import java.util.List;

public class FavoritesFragment extends Fragment {
    private SharedViewModel viewModel;
    private FragmentFavoritesBinding binding;
    private ArrayAdapter<String> favoritesAdapter;
    private ListView favoriteListView;

    /* 
    * brief:Initializes the fragment view, sets up the favorites list view, and observes the shared favorites data to update the list when changes occur.
    * param: inflater- LayoutInflater to inflate the fragment layout. container- Parent view into which the fragment UI will be added. savedInstanceState- Bundle containing the previous state of the fragment
    */
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        viewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);

        binding = FragmentFavoritesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        favoriteListView = binding.favoritesListView;

        viewModel.getFavorites().observe(getViewLifecycleOwner(), favorites -> {
                updateFavoriteList(favorites);
        });

        return root;
    }

    /* 
    * brief: Updates the favorites list displayed in the ListView with the data from the shared favorites list. Clears the existing list and repopulates it with updated data.
    * param: favorites- A list of MarkerData objects representing the user's favorite locations.
    */
    private void updateFavoriteList(List<MarkerData> favorites) {
        if (favoritesAdapter == null) {

            favoritesAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1);
            favoriteListView.setAdapter(favoritesAdapter);
        }

        favoritesAdapter.clear();  // Clear the existing list
        for (MarkerData marker : favorites) {
            favoritesAdapter.add(marker.getName()+"- "+marker.getAddress());


        }
        favoritesAdapter.notifyDataSetChanged();
    }
    
    /* 
    * brief: Cleans up the binding reference when the fragment's view is destroyed.
    * param: None
    */
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
