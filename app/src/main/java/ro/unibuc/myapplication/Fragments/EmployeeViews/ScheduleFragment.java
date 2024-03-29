package ro.unibuc.myapplication.Fragments.EmployeeViews;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ro.unibuc.myapplication.Adapters.ScheduleAdapter;
import ro.unibuc.myapplication.Dao.RestaurantDatabase;
import ro.unibuc.myapplication.Fragments.OnItemClickListener;
import ro.unibuc.myapplication.Models.Schedule;
import ro.unibuc.myapplication.R;

public class ScheduleFragment extends Fragment implements OnItemClickListener {
    RecyclerView recyclerView;
    Calendar calendar;
    TextView noSchMessage;
    public ScheduleFragment(){
        super(R.layout.fragment_schedule);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Bundle bundle = this.getArguments();

        calendar = Calendar.getInstance();
        calendar.setTime(new Date());

        StringBuilder date = new StringBuilder();
        if (bundle != null){
            String bundleDate = bundle.getString(CalendarFragment.DayDate);

            int currentDay = calendar.get(Calendar.DATE);
            CalendarFragment cf = new CalendarFragment();

            if (bundleDate.equals(CalendarFragment.Today)){
                date.append(String.valueOf(currentDay));
            }

            if (bundleDate.equals(CalendarFragment.Yesterday)){
                date.append(String.valueOf(currentDay - 1));
            }

            if (bundleDate.equals(CalendarFragment.Tomorrow)){
                date.append(String.valueOf(currentDay + 1));
            }

            String dateStr = date.toString();

            if(dateStr.length() == 0){
                date.append(bundleDate);
            }
        }

        String dateStr = date.toString();
        StringBuilder dateBld = new StringBuilder();
        if (dateStr.length() > 2){
            dateBld.append(parseDate(dateStr));
        }else {
            dateBld.append(dateStr);
        }

        String parsedDate = dateBld.toString();

        // Check which schedules from database are on this day and store them in list
        RestaurantDatabase db = RestaurantDatabase.getInstance(requireContext());
        List<Schedule> scheduleList = db.scheduleDAO().getAllSchedules();

        List<Schedule> schedulesOnDate = new ArrayList<>();
        for (Schedule sch : scheduleList){
            String day = parseDate(sch.getDate());

            if (day.equals(parsedDate)){
                schedulesOnDate.add(sch);
            }
        }

        // afiseaza schedule
        recyclerView = view.findViewById(R.id.scheduleOnDateView);
        LinearLayoutManager manager = new LinearLayoutManager(requireContext());
        recyclerView.setLayoutManager(manager);
        ScheduleAdapter adapter = new ScheduleAdapter(schedulesOnDate, this);
        recyclerView.setAdapter(adapter);

        if(schedulesOnDate.size() == 0){
            noSchMessage = view.findViewById(R.id.noSchedulesMessage);
            noSchMessage.setVisibility(View.VISIBLE);
        }
    }

    public String parseDate(String date){
        int month = calendar.get(Calendar.MONTH);

        String day = String.valueOf(date.charAt(0)) + String.valueOf(date.charAt(1));
        return day;
    }

    @Override
    public void onItemClick(Schedule schedule) {
        Toast.makeText(requireContext(), "todo", Toast.LENGTH_SHORT).show();
    }
}
