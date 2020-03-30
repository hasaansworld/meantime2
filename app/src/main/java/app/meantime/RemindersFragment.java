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


public class RemindersFragment extends Fragment {
    RecyclerView recyclerView;
    AdapterReminders adapterReminders;
    SharedPreferences sharedPreferences;
    int filter = -1;

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
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean("updateMainList", false);
            editor.apply();
        }
    }

    public void setFilter(int filter){
        this.filter = filter;
        adapterReminders.setFilter(filter);
    }

    public void search(String query){
        adapterReminders.search(query);
    }

    public void cancelSearch(){
        adapterReminders.cancelSearch();
    }

}
