package xdx.fim.uhk.cz.todo_list;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DetailActivity extends AppCompatActivity {

    private int id;
    private Note note;
    private boolean isDelete;
    private MenuItem menuItem_Delete;
    private MenuItem menuItem_Done;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Toolbar tb = (Toolbar) findViewById(R.id.toolbar_inc);
        setSupportActionBar(tb);
        final ActionBar ab = getSupportActionBar();
        //ab.setHomeAsUpIndicator(R.drawable.ic_menu); // set a custom icon for the default home button
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setHomeButtonEnabled(true);
        //ab.setDisplayShowHomeEnabled(true); // show or hide the default home button
        //ab.setDisplayShowCustomEnabled(true); // enable overriding the default toolbar layout
        //ab.setDisplayShowTitleEnabled(true); // disable the default title element here (for centered title)

        id = getIntent().getIntExtra("id", 0);

        Context ctx = getApplicationContext();
        Notes_DB notes_db = new Notes_DB(ctx);

        Cursor c = notes_db.getNote((id));
        note = new Note();
        if (c != null) {
            while(c.moveToNext()) {
                note = (new Note(c.getInt(0),c.getString(1),c.getString(2),getBoolean(3, c)));
                note.setDeadDate(new Date(c.getLong(4)).getTime());
                note.setStartDate(new Date(c.getLong(5)).getTime());
                note.setDone(getBoolean(6,c));
            }
            c.close();
        }
        notes_db.close();

        TextView textTitle = (TextView) findViewById(R.id.textViewTitle);
        TextView textDesc = (TextView) findViewById(R.id.textViewDesc);
        TextView textDate = (TextView) findViewById(R.id.textViewDate);
        TextView textDateStart = (TextView) findViewById(R.id.textViewStart);

        textTitle.setText(note.getTitle());
        textDesc.setText(note.getDescription());
        String date = Utils.getFormattedDate(Utils.DEFAULT, new Date(note.getDeadDate()));
        textDate.setText(date);
        SimpleDateFormat newFormat = new SimpleDateFormat("dd/MM/yyyy");
        String formatedDate = newFormat.format(note.getStartDate()*1000L);
        textDateStart.setText(formatedDate);

        try {
            if(note.isDone()){
                if(menuItem_Done!=null){
                    menuItem_Done.setIcon(R.drawable.button_pressed_done);
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.detail_menu, menu);
        menuItem_Delete = menu.findItem(R.id.action_delete);
        menuItem_Done = menu.findItem(R.id.action_done);

        try {
            if(note!=null){
                if(note.isDone()){
                    if(menuItem_Done!=null){
                        menuItem_Done.setIcon(R.drawable.button_pressed_done);
                    }
                }
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                onNoteDelete();
                return true;
            case R.id.action_done:
                onNoteDone();
                return true;
            case android.R.id.home:
                try {
                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_CANCELED, returnIntent);
                    finish();
                    return true;
                } catch (Exception e){
                    e.printStackTrace();
                }
        }
        return true;
    }

    private boolean getBoolean(int columnIndex, Cursor cursor){
        if (cursor.isNull(columnIndex) || cursor.getShort(columnIndex) == 0) {
            return false;
        } else {
            return true;
        }
    }

    public void onNoteDone(){

        Notes_DB notes_db = new Notes_DB(this);

        boolean success = notes_db.updateNoteDone(id, note.isDone());
        notes_db.close();
        if (success) {
            Intent i = new Intent(this, MainActivity.class);
            //i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(i, 0);
            finish();
        }else{
            Toast.makeText(this, R.string.note_not_added, Toast.LENGTH_LONG).show();
        }
    }

    public void onNoteDelete(){
        AlertDialog diaBox = AskOption();
        diaBox.show();
    }

    private  AlertDialog AskOption()
    {
        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle("Smazat")
                .setMessage("Opravdu si přejete smazat úkol?")
                .setIcon(android.R.drawable.ic_menu_delete)
                .setPositiveButton("Smazat", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        try{
                            Notes_DB notes = new Notes_DB(getApplicationContext());
                            if(notes.deleteNote(id)){
                                isDelete = true;
                                Intent returnIntent = new Intent();
                                setResult(Activity.RESULT_OK, returnIntent);
                                Toast.makeText(getApplicationContext(), R.string.note_deleted, Toast.LENGTH_SHORT).show();
                                finish();

                            } else{
                                Toast.makeText(getApplicationContext(), R.string.note_not_deleted, Toast.LENGTH_SHORT).show();
                            }
                            notes.close();
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
        return myQuittingDialogBox;

    }
}
