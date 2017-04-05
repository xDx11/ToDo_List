package xdx.fim.uhk.cz.notes_v2;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class EditActivity extends AppCompatActivity {

    private int id;
    private TextView dateView;
    private Button setDateBtn;
    private long dateUpd = 0;
    private DatePickerDialog datePickDiag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Toolbar tb = (Toolbar) findViewById(R.id.toolbar_inc);
        setSupportActionBar(tb);
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        id = getIntent().getIntExtra("id", 0);

        Context ctx = getApplicationContext();
        Notes_DB notes_db = new Notes_DB(ctx);

        Cursor c = notes_db.getNote((id));
        Note note = new Note();
        if (c != null) {
            while(c.moveToNext()) {
                note = (new Note(c.getInt(0),c.getString(1),c.getString(2),getBoolean(3,c)));
                note.setDeadDate(c.getLong(4));
                note.setDone(getBoolean(6,c));
            }
            c.close();
        }

        dateView = (TextView) findViewById(R.id.get_date_time_edit);
        dateUpd = note.getDeadDate();
        String date = Utils.getFormattedDate(Utils.DEFAULT, new Date(dateUpd));
        dateView.setText(date);

        setDateBtn = (Button) findViewById(R.id.btn_set_date_edit);


        Calendar newCalendar = Calendar.getInstance();
        datePickDiag = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                dateView.setText(Utils.getFormattedDate(Utils.DEFAULT, newDate.getTime()));
                dateUpd = newDate.getTime().getTime();
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));



        TextView textTitle = (TextView) findViewById(R.id.title_edit);
        TextView textDesc = (TextView) findViewById(R.id.textDesc_edit);
        CheckBox checkBox = (CheckBox) findViewById(R.id.chb_important_edit);
        CheckBox checkBox_done = (CheckBox) findViewById(R.id.ch_done_edit);

        textTitle.setText(note.getTitle());
        textDesc.setText(note.getDescription());
        checkBox.setChecked(note.isImportant());
        checkBox_done.setChecked(note.isDone());
    }

    public void onChangeDate(View v){
        datePickDiag.show();
    }

    public void onUpdateNote(View v){

        String title = ((EditText)findViewById(R.id.title_edit)).getText().toString();
        String text = ((EditText)findViewById(R.id.textDesc_edit)).getText().toString();
        boolean important2 = ((CheckBox)findViewById(R.id.chb_important_edit)).isChecked();
        boolean done2 = ((CheckBox)findViewById(R.id.ch_done_edit)).isChecked();

        EditText titleText = (EditText) findViewById(R.id.title_edit);
        EditText descText = (EditText) findViewById(R.id.textDesc_edit);


        if(TextUtils.isEmpty(title)) {
            titleText.setError("Prázdná hodnota není povolena!");
            return;
        }

        Notes_DB notes_db = new Notes_DB(this);

        boolean success = notes_db.updateNote(id,title,text,important2, dateUpd, done2);
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

    private boolean getBoolean(int columnIndex, Cursor cursor){
        if (cursor.isNull(columnIndex) || cursor.getShort(columnIndex) == 0) {
            return false;
        } else {
            return true;
        }
    }
}
