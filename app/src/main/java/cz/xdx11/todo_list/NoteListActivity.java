package cz.xdx11.todo_list;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class NoteListActivity extends AppCompatActivity {

    private long[] id_field;
    private int day;
    private int month;
    private int year;
    private static final int MENU_DELETE_ID = 0;
    private static final int MENU_UPDATE_ID = 1;
    private static final int MENU_DONE_ID = 2;
    private Note noteDel;
    private Activity activity;
    private ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_list);
        try {
            activity = this;
            id_field = getIntent().getLongArrayExtra("id_field");
            day = getIntent().getIntExtra("day", 0);
            month = getIntent().getIntExtra("month", 0);
            month += 1;
            year = getIntent().getIntExtra("year", 0);
            //setTitle(day+"."+month+"."+year);

            Toolbar tb = (Toolbar) findViewById(R.id.toolbar_inc);
            tb.setTitle(day+"."+month+"."+year);
            setSupportActionBar(tb);
            android.support.v7.app.ActionBar ab = getSupportActionBar();
            if(ab!=null){
                System.out.println("TEST ACTION BAR OPTIONS");
                ab.setDisplayShowCustomEnabled(true);
                ab.setDisplayShowTitleEnabled(true);
                ab.setDisplayHomeAsUpEnabled(true);
                ab.setHomeButtonEnabled(true);
                TextView appTitle = (TextView) findViewById(R.id.appTitle);
                appTitle.setText(day+"."+month+"."+year);
                month -= 1;
                //getSupportActionBar().setDisplayShowTitleEnabled(true);
                //getSupportActionBar().setTitle(day+"."+month+"."+year);
            }
            System.out.println("Action bar: " + getSupportActionBar().getTitle());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if(resultCode==RESULT_OK){
                long id = data.getLongExtra("id", 0);
                long date = data.getLongExtra("date", 0);
                Calendar cal = Calendar.getInstance();
                cal.setTime(new Date(date));
                int dayFromNote = cal.get(Calendar.DAY_OF_MONTH);
                if(id > 0){
                    if(id_field!=null){
                        if(day == dayFromNote){
                            long[] copyTo    = new long[id_field.length];
                            System.arraycopy(id_field, 0, copyTo, 0, id_field.length);

                            id_field = new long[copyTo.length+1];
                            System.arraycopy(copyTo, 0, id_field, 0, copyTo.length);

                            id_field[id_field.length-1] = id;
                        }
                    }
                }
            }
            loadData();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private void loadData(){
        TextView textView = (TextView) findViewById(R.id.textViewNoNotes);
        if(id_field.length>0){
            updateList();
            if(textView!=null){
                textView.setVisibility(View.INVISIBLE);
            }
        } else {
            if(textView!=null){
                textView.setVisibility(View.VISIBLE);
                textView.setText("Tento den neobsahuje žádné úkoly!");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return true;
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("month", month);
            returnIntent.putExtra("year", year);
            setResult(Activity.RESULT_CANCELED, returnIntent);
            finish();
        } catch (Exception e){
            e.printStackTrace();
        }

    }


    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        if (v.getId()==R.id.listNotesPerDay) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
            //getMenuInflater().inflate(R. , menu);
            ListView lv = (ListView) v;
            noteDel = (Note) lv.getItemAtPosition(info.position);
            menu.setHeaderIcon(android.R.drawable.ic_menu_manage);
            menu.setHeaderTitle("Možnosti");

            //String[] menuItems = getResources().getStringArray(R.array.menu);
            if(noteDel.isDone()){
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

    private void deleteNote(long id){
        AlertDialog diaBox = AskOption(id);
        diaBox.show();
    }

    private void editNote(long id){
        Intent i = new Intent(this, EditActivity.class);
        i.putExtra("id", id);
        startActivityForResult(i, 0);
    }

    private void updateList(){
        try {
            List<Note> notes = new ArrayList<>();
            for(int i = 0; i < id_field.length; i++){
                try {
                    Note note = Note.findById(Note.class, id_field[i]);
                    if(note!=null)
                        notes.add(note);
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            listView = (ListView) findViewById(R.id.listNotesPerDay);
            NotesAdapterPerDay adapter = new NotesAdapterPerDay(this,R.layout.list_item_note_detail,notes);
            listView.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            registerForContextMenu(listView);
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    view.showContextMenu();
                }
            });
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    private AlertDialog AskOption(final long id){
        return new AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle("Smazat")
                .setMessage("Opravdu si přejete smazat úkol?")
                .setIcon(android.R.drawable.ic_menu_delete)
                .setPositiveButton("Smazat", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        try {
                            Note note = Note.findById(Note.class, id);
                            if(note.delete()){
                                updateList();
                                Toast.makeText(getApplicationContext(), R.string.note_deleted, Toast.LENGTH_SHORT).show();
                            } else{
                                Toast.makeText(getApplicationContext(), R.string.note_not_deleted, Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        } catch (Exception e){
                            e.printStackTrace();
                        }

                    }
                })
                .setNegativeButton("Zrušit", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .create();
    }

    public void onAddNoteClicked(View v) {
        Intent i = new Intent(this, AddActivity.class);
        System.out.println("day: "+ day + " month: " + month + " year: "+ year);
        i.putExtra("day", day);
        i.putExtra("month", month);
        i.putExtra("year", year);
        startActivityForResult(i, 0);
    }
}
