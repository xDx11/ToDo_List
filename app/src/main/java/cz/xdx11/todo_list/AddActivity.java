package cz.xdx11.todo_list;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;

public class AddActivity extends AppCompatActivity {

    private TextView dateView;
    private ImageView setDateBtn;
    private long endDate = 0;
    private DatePickerDialog datePickDiag;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        Toolbar tb = (Toolbar) findViewById(R.id.toolbar_inc);
        setSupportActionBar(tb);
        final ActionBar ab = getSupportActionBar();
        if(ab!=null)
            ab.setDisplayHomeAsUpEnabled(true);



        dateView = (TextView) findViewById(R.id.get_date_time);
        String date = Utils.getFormattedDate(Utils.DEFAULT, new Date());
        dateView.setText(date);

        setDateBtn = (ImageView) findViewById(R.id.btn_set_date);

        dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setDateBtn.setPressed(true);
                datePickDiag.show();
            }
        });

        dateView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                setDateBtn.setPressed(true);
                datePickDiag.show();
                return true;
            }
        });

        Calendar newCalendar = Calendar.getInstance();
        datePickDiag = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {

            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, monthOfYear, dayOfMonth);
                System.out.println("TEST ADD MONTH: " + monthOfYear);
                dateView.setText(Utils.getFormattedDate(Utils.DEFAULT, newDate.getTime()));
                endDate = newDate.getTime().getTime();
                setDateBtn.setPressed(false);
            }

        },newCalendar.get(Calendar.YEAR), newCalendar.get(Calendar.MONTH), newCalendar.get(Calendar.DAY_OF_MONTH));

        int day = getIntent().getIntExtra("day", 0);
        int month = getIntent().getIntExtra("month", -1);
        int year = getIntent().getIntExtra("year", 0);
        if(day > 0 && month > -1 && year > 0){
            Calendar newDate = Calendar.getInstance();
            newDate.set(year, month, day);
            dateView.setText(Utils.getFormattedDate(Utils.DEFAULT, newDate.getTime()));
            datePickDiag.updateDate(year, month, day);
            newDate.set(year, month, day);
            endDate = newDate.getTimeInMillis();
            System.out.println("TEST ADD MONTH intExtra: " + newDate.getTime().getMonth());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                onAddNote();
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

    public void onChangeDate(View v){
        datePickDiag.show();
    }

    public void onAddNote(){

        String title = ((EditText)findViewById(R.id.title)).getText().toString();
        String desc = ((EditText)findViewById(R.id.text)).getText().toString();
        boolean isImportant = ((CheckBox)findViewById(R.id.chb_important)).isChecked();

        //Validate
        EditText titleText = (EditText) findViewById(R.id.title);
        if(TextUtils.isEmpty(title)) {
            titleText.setError("Prázdná hodnota není povolena!");
            return;
        }

        if(endDate ==0){
            endDate = System.currentTimeMillis();
        }

        long startDate = new Date().getTime()/1000L;
        Note note = new Note(title, desc, isImportant, startDate, endDate, false );
        long id = note.save();

        if (id > -1) {
            Intent returnIntent = new Intent();
            returnIntent.putExtra("id", id);
            returnIntent.putExtra("date", endDate);
            setResult(Activity.RESULT_OK, returnIntent);
            finish();
        }else{
            Toast.makeText(this, R.string.note_not_added, Toast.LENGTH_LONG).show();
        }
    }
}
