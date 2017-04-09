package cz.xdx11.todo_list;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CalendarView;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.Toast;

import com.orm.SugarContext;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidGridAdapter;
import com.roomorama.caldroid.CaldroidListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import hirondelle.date4j.DateTime;

public class CallendarActivity extends AppCompatActivity {

    private Switch switchDone;
    private SharedPreferences sharedpreferences;
    private CalendarView calendarView;
    private static final String MyPREFERENCES = "myPreferences";
    public static final String FILTER_DONE = "filter_done";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_callendar);

        try {

            SugarContext.init(this);
            sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
            // Always cast your custom Toolbar here, and set it as the ActionBar.
            Toolbar tb = (Toolbar) findViewById(R.id.toolbar_inc);
            switchDone = (Switch) findViewById(R.id.switch_done_filter);
            setSupportActionBar(tb);


            // Get the ActionBar here to configure the way it behaves.
            final ActionBar ab = getSupportActionBar();
            if(ab!=null){
                ab.setDisplayShowCustomEnabled(true); // enable overriding the default toolbar layout
                ab.setDisplayShowTitleEnabled(false); // disable the default title element here (for centered title)
            }

            //setupCallendar();
            extendCallendar();
            // TODO load data
            // updateList();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    final CaldroidListener listener = new CaldroidListener() {

        @Override
        public void onSelectDate(Date date, View view) {


            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int dayCall = cal.get(Calendar.DAY_OF_MONTH);
            System.out.println("DayCall: " + dayCall);

            List<Note> notes = Note.listAll(Note.class);
            List<Note> selectNotes = new ArrayList<>();
            for(Note note : notes){
                Date dateNote = new Date(note.getDeadDate());
                cal.setTime(dateNote);
                int dayNote = cal.get(Calendar.DAY_OF_MONTH);
                if(dayCall == dayNote) {
                    selectNotes.add(note);
                }

            }
            long[] id_field = new long[selectNotes.size()];
            for(int i = 0; i < selectNotes.size(); i++){
                id_field[i] = selectNotes.get(i).getId();
            }

            //TODO selectNotes VIEW
            Intent i = new Intent(getApplicationContext(), NoteListActivity.class);
            i.putExtra("id_field", id_field);
            startActivityForResult(i, 0);

            Toast.makeText(getApplicationContext(), date.toString() + " number notes: " + selectNotes.size(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onChangeMonth(int month, int year) {
            String text = "month: " + month + " year: " + year;
            Toast.makeText(getApplicationContext(), text,
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLongClickDate(Date date, View view) {
            Toast.makeText(getApplicationContext(),
                    "Long click " + date.toString(),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCaldroidViewCreated() {
            Toast.makeText(getApplicationContext(), "Caldroid view is created", Toast.LENGTH_SHORT).show();

        }

    };

    private void extendCallendar(){
        CaldroidFragment caldroidFragment = new CaldroidCustomFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        args.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.MONDAY);
        args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, false); // zmenseni
        args.putInt(CaldroidFragment.THEME_RESOURCE, com.caldroid.R.style.CaldroidDefaultDark);
        caldroidFragment.setArguments(args);
        caldroidFragment.setCaldroidListener(listener);

        android.support.v4.app.FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.frameLayout, caldroidFragment);
        t.commit();

        List<Note> notes = Note.listAll(Note.class);
        for(Note note : notes){
            long date = note.getDeadDate();
        }

        Map<Date, Drawable> map = new HashMap<Date,Drawable>();
        Map<Integer, List<Note>> mapGroupNotesInt = new HashMap<Integer, List<Note>>();

        // loop through the events
        for(Note note: notes){
            Date date = new Date(note.getDeadDate());
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(date);
            int day = cal2.get(Calendar.DAY_OF_MONTH);
            if(!mapGroupNotesInt.containsKey(day)){
                mapGroupNotesInt.put(day, new ArrayList<Note>());
            }
            mapGroupNotesInt.get(day).add(note);
        }

        for (Map.Entry<Integer, List<Note>> entry : mapGroupNotesInt.entrySet()) {
            System.out.println("Key = " + entry.getKey().toString() + ", Value size= " + entry.getValue().size());
            boolean allDone = true;
            for(Note note : entry.getValue()){
                if(!note.isDone()){
                    allDone = false;
                }
            }
            if(allDone){
                Date date = new Date(entry.getValue().get(0).getDeadDate());
                map.put(date, ContextCompat.getDrawable(getApplicationContext(), R.drawable.green_status));
            } else {
                Date date = new Date(entry.getValue().get(0).getDeadDate());
                map.put(date, ContextCompat.getDrawable(getApplicationContext(), R.drawable.red_status));
            }
        }




        caldroidFragment.setBackgroundDrawableForDates(map);
        caldroidFragment.refreshView();




    }
    /*
    private void setupCallendar(){
        CalendarView calendarView = (CalendarView) findViewById(R.id.calendarView); // get the reference of CalendarView
        calendarView.setShowWeekNumber(true);

        // perform setOnDateChangeListener event on CalendarView
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(CalendarView view, int year, int month, int dayOfMonth) {
                // display the selected date by using a toast
                Toast.makeText(getApplicationContext(), dayOfMonth + "/" + month + "/" + year, Toast.LENGTH_LONG).show();
            }
        });

    }
    */
}
