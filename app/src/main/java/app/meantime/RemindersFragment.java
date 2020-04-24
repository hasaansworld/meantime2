package app.meantime;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.google.android.gms.ads.formats.UnifiedNativeAd;


public class RemindersFragment extends Fragment {
    RecyclerView recyclerView;
    AdapterReminders adapterReminders;
    SharedPreferences sharedPreferences;
    LinearLayout searchNoResults;
    int filter = -1;
    boolean isSearching = false;
    String query = "";
    //UnifiedNativeAd ad;

    public RemindersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPreferences = getContext().getSharedPreferences("data", Context.MODE_PRIVATE);
        View v = inflater.inflate(R.layout.fragment_reminders, container, false);
        recyclerView = v.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapterReminders = new AdapterReminders(getContext(), 0);
        recyclerView.setAdapter(adapterReminders);

        searchNoResults = v.findViewById(R.id.search_no_results);

        return v;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(sharedPreferences.getBoolean("updateMainList", false)) {
            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            adapterReminders = new AdapterReminders(getContext(), 0);
            recyclerView.setAdapter(adapterReminders);
            if (filter != -1)
                adapterReminders.setFilter(filter);
            if(isSearching)
                search(query);
            //if(ad != null)
                //adapterReminders.showAd(ad);
        }
    }

    public void setFilter(int filter){
        this.filter = filter;
        adapterReminders.setFilter(filter);
        //adapterReminders.showAd(ad);
    }

    public void search(String query){
        this.query = query;
        isSearching = true;
        adapterReminders.search(query);
        if(adapterReminders.getItemCount() == 0)
            searchNoResults.setVisibility(View.VISIBLE);
        else
            searchNoResults.setVisibility(View.GONE);
    }

    public void cancelSearch(){
        isSearching = false;
        adapterReminders.cancelSearch();
        searchNoResults.setVisibility(View.GONE);
        //adapterReminders.showAd(ad);
    }

    /*public void showAd(UnifiedNativeAd ad){
        this.ad = ad;
        adapterReminders.showAd(ad);
    }*/

}
