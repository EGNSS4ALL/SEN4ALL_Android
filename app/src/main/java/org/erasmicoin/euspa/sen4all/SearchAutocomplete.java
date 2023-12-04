package org.erasmicoin.euspa.sen4all;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.util.ArrayList;

public class SearchAutocomplete extends androidx.appcompat.widget.AppCompatAutoCompleteTextView {
    private ArrayAdapter<String> searchAdapter;
    private ArrayList<String> searchItems;

    Handler autocompleteHandler = new Handler(Looper.getMainLooper());

    private String searchText;
    private ArrayList<GeocodingResult> searchResults = new ArrayList<>();

    public SearchAutocomplete(Context context) {
        super(context);
        init();
    }
    public SearchAutocomplete(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SearchAutocomplete(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }
    public String getItem(int position) {
        return searchAdapter.getItem(position);
    }

    SearchAutocomplete thisControl;
    private void init() {
        searchItems = new ArrayList<>();
        thisControl = this;
        searchAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_dropdown_item_1line, searchItems);
        this.setAdapter(searchAdapter);
        this.setThreshold(1);
    }

    @Override
    protected void performFiltering(CharSequence text, int keyCode) {
        String stext = text.toString();

        if( stext.length() >= 3 ){
            searchText = stext;
            Log.d("TAG", "STILL TYPING, SEARCH STOPPED");
            autocompleteHandler.removeCallbacks(searchRunnable);
            autocompleteHandler.postDelayed(searchRunnable, 500);
        }
    }


    final Runnable searchRunnable = new Runnable() {
        @Override
        public void run() {
            Log.d("TAG", "SEARCH LAUNCH FOR "+searchText);
            Geocoding.geocode(searchText, result -> {
                Log.d("TAG", "GOT "+result.size()+" RESULTS");
                searchResults = result;
                searchItems.clear();
                for (int i = 0; i < result.size(); i++) {
                    String tmp = result.get(i).getDisplayName();
                    searchItems.add(tmp);
                }
                updateUI();
            });
        }
    };

    private void updateUI(){
        this.searchAdapter = new ArrayAdapter<>(this.getContext(), android.R.layout.simple_dropdown_item_1line, searchItems);
        this.setAdapter(this.searchAdapter);
        this.searchAdapter.notifyDataSetChanged();
    }

    public GeocodingResult getResultAt(int index){
        return searchResults.get(index);
    }

}
