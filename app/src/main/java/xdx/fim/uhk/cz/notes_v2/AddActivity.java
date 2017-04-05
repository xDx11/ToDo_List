package xdx.fim.uhk.cz.notes_v2;

import android.app.DatePickerDialog;
import android.content.Intent;
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

public class AddActivity extends AppCompatActivity {

    private TextView dateView;
    private Button setDateBtn;
    private long dateUpd = 0;
    private DatePickerDialog datePickDiag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        Toolbar tb = (Toolbar) findViewById(R.id.toolbar_inc);
        setSupportActionBar(tb);
        final ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        dateView = (TextView) findViewById(R.id.get_date_time);
        String date = Utils.getFormattedDate(Utils.DEFAULT, new Date());
        dateView.setText(date);

        setDateBtn = (Button) findViewById(R.id.btn_set_date);


        Calendar newCalendar = Calendar.getInstance();
        datePickDiag = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                dateView.setText(Utils.getFormattedDate(Utils.DEFAULT, newDate.getTime()));
                dateUpd = newDate.getTime().getTime();
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

    }

    public void onChangeDate(View v){
        datePickDiag.show();
    }

    public void onAddNote(View v){

        String title = ((EditText)findViewById(R.id.title)).getText().toString();
        String text = ((EditText)findViewById(R.id.text)).getText().toString();
        boolean important2 = ((CheckBox)findViewById(R.id.chb_important)).isChecked();
        boolean important = true;

        EditText titleText = (EditText) findViewById(R.id.title);
        EditText descText = (EditText) findViewById(R.id.text);


        if(TextUtils.isEmpty(title)) {
            titleText.setError("Prázdná hodnota není povolena!");
            return;
        }

        if(dateUpd==0){
            dateUpd = System.currentTimeMillis();
        }

        Notes_DB notes_db = new Notes_DB(this);
        //long id = notes_db.insertNote(title, text,important2, new Date().getTime());
        long id = notes_db.insertNote(title, text,important2, dateUpd);

        notes_db.close();
        if (id > -1) {
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivityForResult(i, 0);
            finish();
        }else{
            Toast.makeText(this, R.string.note_not_added, Toast.LENGTH_LONG).show();
        }
    }
}
