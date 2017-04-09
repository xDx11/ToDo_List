package cz.xdx11.todo_list;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.menu.ActionMenuItemView;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.orm.SugarContext;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;

public class MainActivity extends AppCompatActivity {

    private static final int MENU_DELETE_ID = 0;
    private static final int MENU_UPDATE_ID = 1;
    private static final int MENU_DONE_ID = 2;
    private static final char POP_START = 's';
    private static final char POP_END = 'e';
    private static final char POP_TITLE = 't';
    private static final char POP_DESC = 'd';
    private Note noteDel;
    private ListView listView;
    private Switch switchDone;
    private SharedPreferences sharedpreferences;
    private int switchNum;
    private int orderWay;
    private static final String MyPREFERENCES = "myPreferences";
    public static final String FILTER_DONE = "filter_done";
    public static final String ORDER_NUM = "order_num";
    public static final String ORDER_WAY = "order_way";
    public static final String STYLE_TYPE = "style_type";
    private static final int LIST = 1;
    private static final int CALLENDAR = 2;
    private int styleType = 2; // start from list to callendar
    private CaldroidFragment caldroidFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            SugarContext.init(this);
            setContentView(R.layout.activity_main);

            sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
            Toolbar tb = (Toolbar) findViewById(R.id.toolbar_inc);
            switchDone = (Switch) findViewById(R.id.switch_done_filter);
            setSupportActionBar(tb);

            final ActionBar ab = getSupportActionBar();
            if (ab != null) {
                ab.setDisplayShowCustomEnabled(true);
                ab.setDisplayShowTitleEnabled(false);
            }

            switchDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(FILTER_DONE, String.valueOf(switchDone.isChecked()));
                    editor.apply();
                    updateList();
                }
            });
            //updateList();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        onChangeStyle(null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            int month = getIntent().getIntExtra("month", 0);
            int year = getIntent().getIntExtra("year", 0);
            if(month > 0 && year > 0){
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, month, 1);
                Date date = newDate.getTime();
                if(caldroidFragment!=null)
                    caldroidFragment.setCalendarDate(date);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    //RIGHT BUTTON
    public void onAddNoteClicked(View v) {
        Intent i = new Intent(this, AddActivity.class);
        startActivityForResult(i, 0);
    }

    //LEFT BUTTON
    public void onChangeStyle(View v) {
        if (v != null) {
            if (styleType == 1) styleType = 2;
            else styleType = 1;
        } else {
            sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
            styleType = sharedpreferences.getInt(STYLE_TYPE, 1);
            System.out.println("shared pref style type: " + styleType);
        }

        try {
            switch (styleType) {
                case LIST:
                    showListType();
                    updateList();
                    styleType = 1;
                    break;
                case CALLENDAR:
                    showCallendarType();
                    extendCallendar();
                    styleType = 2;
                    break;
            }
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.putInt(STYLE_TYPE, styleType);
            editor.apply();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    // PART - SWITCHER
    private void showListType() {
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayoutMain);
        if (frameLayout != null)
            frameLayout.setVisibility(INVISIBLE);

        changeButtonStyle(R.drawable.sel_btn_change_style);
        unShow_controll_buttons(VISIBLE);
    }

    private void showCallendarType() {
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.frameLayoutMain);
        if (frameLayout != null)
            frameLayout.setVisibility(View.VISIBLE);
        changeButtonStyle(R.drawable.sel_btn_change_style_list);
        unShow_controll_buttons(INVISIBLE);
    }

    private void changeButtonStyle(int drawable) {
        ImageButton btn = (ImageButton) findViewById(R.id.imageButtonChangeStyle);
        btn.setImageResource(drawable);
    }

    private void unShow_controll_buttons(int visibility) {
        if (listView != null) listView.setVisibility(visibility);
        Switch switcher = (Switch) findViewById(R.id.switch_done_filter);
        if (switcher != null) switcher.setVisibility(visibility);
        ActionMenuItemView menuItem = (ActionMenuItemView) findViewById(R.id.action_sort);
        if (menuItem != null) menuItem.setVisibility(visibility);
    }

    /* ------------------------------------------- */
    // PART - LIST TYPE
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort:
                try {
                    sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                    styleType = sharedpreferences.getInt(STYLE_TYPE, 1);
                    if(styleType==1)
                        orderByPopupMenu();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return true;
        }
        return true;
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        if (v.getId() == R.id.listNotes) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            //getMenuInflater().inflate(R. , menu);
            ListView lv = (ListView) v;
            noteDel = (Note) lv.getItemAtPosition(info.position);
            menu.setHeaderIcon(android.R.drawable.ic_menu_manage);
            menu.setHeaderTitle("Možnosti");

            //String[] menuItems = getResources().getStringArray(R.array.menu);
            if (noteDel.isDone()) {
                menu.add(0, MENU_DONE_ID, 0, R.string.notDone);
            } else {
                menu.add(0, MENU_DONE_ID, 0, R.string.done);
            }
            menu.add(0, MENU_UPDATE_ID, 0, R.string.edit);
            menu.add(0, MENU_DELETE_ID, 0, R.string.delete);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case MENU_DELETE_ID:
                //Toast.makeText(getApplicationContext(), "Option 1: ID "+info.id+", position "+info.position, Toast.LENGTH_SHORT).show();
                deleteNote(noteDel.getId());
                return true;
            case MENU_UPDATE_ID:
                //Toast.makeText(getApplicationContext(), "Option 1: ID "+info.id+", position "+info.position, Toast.LENGTH_SHORT).show();
                editNote(noteDel.getId());
                return true;
            case MENU_DONE_ID:
                noteDel.setDone(!noteDel.isDone());
                noteDel.save();
                updateList();
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void deleteNote(long id) {
        AlertDialog diaBox = AskOption(id);
        diaBox.show();
    }

    private void editNote(long id) {
        Intent i = new Intent(this, EditActivity.class);
        i.putExtra("id", id);
        startActivityForResult(i, 0);
    }

    private void updateList() {
        List<Note> notes = sortedList();
        listView = (ListView) findViewById(R.id.listNotes);
        NotesAdapter adapter = new NotesAdapter(this, R.layout.list_item_note, notes);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Note note = (Note) parent.getItemAtPosition(position);
                Intent intent = new Intent(MainActivity.this, DetailActivity.class);
                intent.putExtra("id", note.getId());
                //intent.putExtra("food", (Serializable) food);
                startActivity(intent);
            }


        });
        registerForContextMenu(listView);
    }

    private List<Note> sortedList() {
        String testSwitched = sharedpreferences.getString(FILTER_DONE, null);
        switchDone.setChecked(Boolean.parseBoolean(testSwitched));
        switchNum = sharedpreferences.getInt(ORDER_NUM, 0);
        orderWay = sharedpreferences.getInt(ORDER_WAY, 0);
        List<Note> notes;
        if (switchDone.isChecked()) {
            //notes = Note.listAll(Note.class);
            if (orderWay == 1) {
                switch (switchNum) {
                    case 1: // START DATE
                        notes = Note.findWithQuery(Note.class, "Select * from note order by start_date asc");
                        break;
                    case 2: // END DATE
                        notes = Note.findWithQuery(Note.class, "Select * from note order by dead_date asc");
                        break;
                    case 3: // TITLE
                        notes = Note.findWithQuery(Note.class, "Select * from note order by title asc");
                        break;
                    default:
                        notes = Note.listAll(Note.class);
                        break;
                }
            } else {
                switch (switchNum) {
                    case 1: // START DATE
                        notes = Note.findWithQuery(Note.class, "Select * from note order by start_date desc");
                        break;
                    case 2: // END DATE
                        notes = Note.findWithQuery(Note.class, "Select * from note order by dead_date desc");
                        break;
                    case 3: // TITLE
                        notes = Note.findWithQuery(Note.class, "Select * from note order by title desc");
                        break;
                    default:
                        notes = Note.listAll(Note.class);
                        break;
                }
            }
        } else {
            if (orderWay == 1) {
                switch (switchNum) {
                    case 1: // START DATE
                        notes = Note.findWithQuery(Note.class, "Select * from note where is_done = '0' order by start_date asc");
                        break;
                    case 2: // END DATE
                        notes = Note.findWithQuery(Note.class, "Select * from note where is_done = '0'  order by dead_date asc");
                        break;
                    case 3: // TITLE
                        notes = Note.findWithQuery(Note.class, "Select * from note where is_done = '0'  order by title asc");
                        break;
                    default:
                        notes = Note.listAll(Note.class);
                        break;
                }
            } else {
                switch (switchNum) {
                    case 1: // START DATE
                        notes = Note.findWithQuery(Note.class, "Select * from note where is_done = '0'  order by start_date desc");
                        break;
                    case 2: // END DATE
                        notes = Note.findWithQuery(Note.class, "Select * from note where is_done = '0'  order by dead_date desc");
                        break;
                    case 3: // TITLE
                        notes = Note.findWithQuery(Note.class, "Select * from note where is_done = '0'  order by title desc");
                        break;
                    default:
                        notes = Note.listAll(Note.class);
                        break;
                }
            }
        }
        return notes;
    }

    private AlertDialog AskOption(final long id) {
        return new AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle("Smazat")
                .setMessage("Opravdu si přejete smazat úkol?")
                .setIcon(android.R.drawable.ic_menu_delete)
                .setPositiveButton("Smazat", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        Note note = Note.findById(Note.class, id);
                        if (note.delete()) {
                            updateList();
                            Toast.makeText(getApplicationContext(), R.string.note_deleted, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.note_not_deleted, Toast.LENGTH_SHORT).show();
                        }
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("Zrušit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();

    }

    public void orderByPopupMenu() {
        View menuItemView = findViewById(R.id.action_sort);
        PopupMenu popup = new PopupMenu(MainActivity.this, menuItemView);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.order_menu1, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getNumericShortcut()) {
                    case POP_START:
                        switchNum = 1;
                        Toast.makeText(MainActivity.this, "Seřazeno dle data přidání.", Toast.LENGTH_SHORT).show();
                        orderProcess();
                        return true;
                    case POP_END:
                        switchNum = 2;
                        Toast.makeText(MainActivity.this, "Seřazeno dle data terminu.", Toast.LENGTH_SHORT).show();
                        orderProcess();
                        return true;
                    case POP_TITLE:
                        switchNum = 3;
                        Toast.makeText(MainActivity.this, "Seřazeno dle názvu úkolů.", Toast.LENGTH_SHORT).show();
                        orderProcess();
                        return true;
                    case POP_DESC:
                        if (orderWay == 1) {
                            orderWay = 2;
                            item.setTitle("Sestupne");
                        } else {
                            orderWay = 1;
                            item.setTitle("Vzestupne");

                        }
                        Toast.makeText(MainActivity.this, "Seřazeno opacne.", Toast.LENGTH_SHORT).show();
                        orderProcess();
                        return true;
                    default:
                        switchNum = 1;
                        Toast.makeText(MainActivity.this, "item: " + item.getItemId(), Toast.LENGTH_SHORT).show();
                        orderProcess();
                        return true;
                }
            }
        });
        popup.show();//showing popup menu
    }

    private void orderProcess() {
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt(ORDER_NUM, switchNum);
        editor.putInt(ORDER_WAY, orderWay);
        editor.apply();
        updateList();
    }

    /* ------------------------------------------- */
    // PART - CALLENDAR TYPE
    final CaldroidListener listener = new CaldroidListener() {

        @Override
        public void onSelectDate(Date date, View view) {


            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int dayCall = cal.get(Calendar.DAY_OF_MONTH);
            int monthCall = cal.get(Calendar.MONTH);
            int yearCall = cal.get(Calendar.YEAR);
            System.out.println("DayCall: " + dayCall + "MonthCall: " + monthCall + "YearCall: " + yearCall);

            List<Note> notes = Note.listAll(Note.class);
            List<Note> selectNotes = new ArrayList<>();
            for (Note note : notes) {
                Date dateNote = new Date(note.getDeadDate());
                cal.setTime(dateNote);
                int dayNote = cal.get(Calendar.DAY_OF_MONTH);
                int monthNote = cal.get(Calendar.MONTH);
                int yearNote = cal.get(Calendar.YEAR);
                if (dayCall == dayNote && monthCall == monthNote && yearCall == yearNote) {
                    selectNotes.add(note);
                }

            }
            long[] id_field = new long[selectNotes.size()];
            for (int i = 0; i < selectNotes.size(); i++) {
                id_field[i] = selectNotes.get(i).getId();
            }

            //TODO selectNotes VIEW
            Intent i = new Intent(getApplicationContext(), NoteListActivity.class);
            i.putExtra("id_field", id_field);
            i.putExtra("day", dayCall);
            i.putExtra("month", monthCall);
            i.putExtra("year", yearCall);
            startActivityForResult(i, 0);

            //Toast.makeText(getApplicationContext(), date.toString() + " number notes: " + selectNotes.size(), Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onChangeMonth(int month, int year) {
            loadCallendarData(month);
        }

        @Override
        public void onLongClickDate(Date date, View view) {
            Toast.makeText(getApplicationContext(),
                    "Long click " + date.toString(),
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCaldroidViewCreated() {

        }

    };

    private void extendCallendar() {
        if(caldroidFragment == null){
            caldroidFragment = new CaldroidCustomFragment();
            Bundle args = new Bundle();
            Calendar cal = Calendar.getInstance();
            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
            args.putInt(CaldroidFragment.START_DAY_OF_WEEK, CaldroidFragment.MONDAY);
            args.putBoolean(CaldroidFragment.SQUARE_TEXT_VIEW_CELL, false); // zmenseni
            args.putInt(CaldroidFragment.THEME_RESOURCE, com.caldroid.R.style.CaldroidDefaultDark);
            args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, true);
            caldroidFragment.setArguments(args);
            caldroidFragment.setCaldroidListener(listener);

            android.support.v4.app.FragmentTransaction t = getSupportFragmentManager().beginTransaction();
            t.replace(R.id.frameLayoutMain, caldroidFragment);
            t.commit();
        }
        System.out.println("Caldroid select month:" + caldroidFragment.getMonth());
        loadCallendarData(caldroidFragment.getMonth());
    }

    private void loadCallendarData(int monthCallendar) {
        List<Note> notes = Note.listAll(Note.class);
        Map<Integer, List<Note>> mapGroupNotesIntPrevious = new HashMap<Integer, List<Note>>();
        Map<Integer, List<Note>> mapGroupNotesInt = new HashMap<Integer, List<Note>>();
        Map<Integer, List<Note>> mapGroupNotesIntNext = new HashMap<Integer, List<Note>>();

        // loop through the events
        for (Note note : notes) {
            Date date = new Date(note.getDeadDate());
            Calendar cal2 = Calendar.getInstance();
            cal2.setTime(date);
            int day = cal2.get(Calendar.DAY_OF_MONTH);
            int month = cal2.get(Calendar.MONTH)+1;
            System.out.println("Caldroid selectedMonth: " + monthCallendar);
            System.out.println("Note month:" + month);
            if(monthCallendar == month){
                if (!mapGroupNotesInt.containsKey(day)) {
                    mapGroupNotesInt.put(day, new ArrayList<Note>());
                }
                mapGroupNotesInt.get(day).add(note);
            }

            if(monthCallendar == month-1 || (monthCallendar==1 && month ==12) ){
                if (!mapGroupNotesIntPrevious.containsKey(day)) {
                    mapGroupNotesIntPrevious.put(day, new ArrayList<Note>());
                }
                mapGroupNotesIntPrevious.get(day).add(note);
            }

            if(monthCallendar == month+1 || (monthCallendar==12 && month ==1) ){
                if (!mapGroupNotesIntNext.containsKey(day)) {
                    mapGroupNotesIntNext.put(day, new ArrayList<Note>());
                }
                mapGroupNotesIntNext.get(day).add(note);
            }
        }


        MonthCallendarData monthPrevious = processCellsData(mapGroupNotesIntPrevious);
        MonthCallendarData monthCurrent = processCellsData(mapGroupNotesInt);
        MonthCallendarData monthNext = processCellsData(mapGroupNotesIntNext);


        Map<Date, Drawable> mapPrevious = monthPrevious.getMap();
        Map<Date, Drawable> map = monthCurrent.getMap();
        Map<Date, Drawable> mapNext = monthNext.getMap();

        Map<Date, Integer> mapTextPrevious = monthPrevious.getMapText();
        Map<Date, Integer> mapText = monthCurrent.getMapText();
        Map<Date, Integer> mapTextNext = monthNext.getMapText();

        Map<Date, Drawable> mapComplete = new HashMap<>();
        mapComplete.putAll(mapPrevious);
        mapComplete.putAll(map);
        mapComplete.putAll(mapNext);
        Map<Date, Integer> mapTextComplete = new HashMap<>();
        mapTextComplete.putAll(mapTextPrevious);
        mapTextComplete.putAll(mapText);
        mapTextComplete.putAll(mapTextNext);

        caldroidFragment.setTextColorForDates(mapTextComplete);
        caldroidFragment.setBackgroundDrawableForDates(mapComplete);
        caldroidFragment.refreshView();
    }

    private MonthCallendarData processCellsData(Map<Integer, List<Note>> mapGroupNotesInt){
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        Date todayDate = new Date();
        String todayStringDate =  dateFormat.format(todayDate);

        Map<Date, Drawable> map= new HashMap<Date, Drawable>();
        Map<Date, Integer> mapText = new HashMap<Date, Integer>();

        for (Map.Entry<Integer, List<Note>> entry : mapGroupNotesInt.entrySet()) {
            //System.out.println("Key = " + entry.getKey().toString() + ", Value size= " + entry.getValue().size());
            boolean allDone = true;
            for (Note note : entry.getValue()) {
                if (!note.isDone()) {
                    allDone = false;
                }
            }
            if (allDone) {
                Date date = new Date(entry.getValue().get(0).getDeadDate());
                if(todayStringDate.contains(dateFormat.format(date))){
                    map.put(date, ContextCompat.getDrawable(getApplicationContext(), R.drawable.cell_bg_green_today));
                    mapText.put(date, R.color.colorWhite);
                } else {
                    map.put(date, ContextCompat.getDrawable(getApplicationContext(), R.drawable.cell_bg_green));
                    mapText.put(date, R.color.colorWhite);
                }
            } else {
                Date date = new Date(entry.getValue().get(0).getDeadDate());
                if(todayStringDate.contains(dateFormat.format(date))){
                    map.put(date, ContextCompat.getDrawable(getApplicationContext(), R.drawable.cell_bg_red_today));
                    mapText.put(date, R.color.colorWhite);
                } else {
                    map.put(date, ContextCompat.getDrawable(getApplicationContext(), R.drawable.cell_bg_red));
                    mapText.put(date, R.color.colorWhite);
                }
            }
        }

        return new MonthCallendarData(map, mapText);
    }

}
