package com.mobileapp.foodfinder.ui.home;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.mobileapp.foodfinder.ui.home.MarkerData;

import java.util.ArrayList;
import java.util.List;

public class SharedViewModel extends ViewModel {

    private final MutableLiveData<List<MarkerData>> favorites = new MutableLiveData<>(new ArrayList<>());

    /*
     * brief: Retrieves the list of favorite markers.
     * param: None.
     * return: LiveData<List<MarkerData>> - The LiveData object containing the list of favorite markers.
     */
    public LiveData<List<MarkerData>> getFavorites() {
        return favorites;
    }

    /*
     * brief: Adds a new favorite marker to the list and updates the LiveData object.
     * param: favorite - The MarkerData object to be added as a favorite.
     * return: None.
     */
    public void addFavorite(MarkerData favorite) {
        List<MarkerData> currentFavorites = favorites.getValue();
        if (currentFavorites != null) {
            currentFavorites.add(favorite);
            favorites.setValue(currentFavorites);
        }
    }
}
