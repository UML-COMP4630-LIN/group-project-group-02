package com.mobileapp.foodfinder.ui.favorites;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FavoritesViewModel extends ViewModel {

    private final MutableLiveData<String> mText;

    /*
     * brief: Constructor for the FavoritesViewModel class. Initializes the MutableLiveData with a default value.
     * param: None.
     * return: None.
     */
    public FavoritesViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is favorites fragment");
    }

    /*
     * brief: Retrieves the text stored in the LiveData object.
     * param: None.
     * return: LiveData<String> - The LiveData object containing the text.
     */
    public LiveData<String> getText() {
        return mText;
    }
}