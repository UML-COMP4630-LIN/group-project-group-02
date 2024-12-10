package com.mobileapp.foodfinder.ui.home;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.mobileapp.foodfinder.ui.home.MarkerData;

import java.util.ArrayList;
import java.util.List;

public class SharedViewModel extends ViewModel {
    private final MutableLiveData<List<MarkerData>> favorites = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<MarkerData>> getFavorites() {
        return favorites;
    }

    public void addFavorite(MarkerData favorite) {
        List<MarkerData> currentFavorites = favorites.getValue();
        if (currentFavorites != null) {
            currentFavorites.add(favorite);
            favorites.setValue(currentFavorites);
        }
    }
}
