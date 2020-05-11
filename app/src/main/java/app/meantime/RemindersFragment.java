package app.meantime;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.formats.UnifiedNativeAd;
import com.google.android.material.snackbar.Snackbar;


public class RemindersFragment extends Fragment {
    RecyclerView recyclerView;
    AdapterReminders adapterReminders;
    SharedPreferences sharedPreferences;
    LinearLayout searchNoResults;
    int filter = -1;
    boolean isSearching = false;
    String query = "";
    Snackbar snackBar;
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

        SwipeToDeleteCallback swipeToDeleteCallback = new SwipeToDeleteCallback(getContext()) {
            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                adapterReminders.removeItem(viewHolder.getAdapterPosition());
                snackBar = Snackbar.make(getActivity().findViewById(R.id.coordinator_layout),
                        "Reminder deleted!", 4000);
                TextView textView = snackBar.getView().findViewById(com.google.android.material.R.id.snackbar_text);
                textView.setTextColor(Color.WHITE);
                TextView textViewAction = snackBar.getView().findViewById(com.google.android.material.R.id.snackbar_action);
                textViewAction.setTypeface(textView.getTypeface(), Typeface.BOLD);
                snackBar.setAction("Undo", new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(mContext, "can't undo :(", Toast.LENGTH_SHORT).show();
                    }
                });
                snackBar.show();
            }
        };
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(swipeToDeleteCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);

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

    public AdapterReminders getAdapter(){
        return adapterReminders;
    }

    /*public void showAd(UnifiedNativeAd ad){
        this.ad = ad;
        adapterReminders.showAd(ad);
    }*/

}
