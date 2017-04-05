package xdx.fim.uhk.cz.notes_v2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;

import static android.R.drawable.ic_menu_delete;

public class DetailActivity extends AppCompatActivity {

    private int id;
    private Note note;
    private boolean isDelete;

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

        if(note.isDone()){
            Button btnDone = (Button) findViewById(R.id.btn_detail_done);
            btnDone.setBackgroundResource(R.drawable.grey_green_check_selector);
        }
    }

    private boolean getBoolean(int columnIndex, Cursor cursor){
        if (cursor.isNull(columnIndex) || cursor.getShort(columnIndex) == 0) {
            return false;
        } else {
            return true;
        }
    }

    public void onNoteDone(View v){

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

    public void onNoteDelete(View v){
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
                        Notes_DB notes = new Notes_DB(getApplicationContext());
                        if(notes.deleteNote(id)){

                            Intent i = new Intent(getApplicationContext(), MainActivity.class);
                            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            getApplicationContext().startActivity(i);
                            finish();
                            isDelete = true;
                            Toast.makeText(getApplicationContext(), R.string.note_deleted, Toast.LENGTH_SHORT).show();
                        } else{
                            Toast.makeText(getApplicationContext(), R.string.note_not_deleted, Toast.LENGTH_SHORT).show();
                        }
                        notes.close();
                        dialog.dismiss();

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
