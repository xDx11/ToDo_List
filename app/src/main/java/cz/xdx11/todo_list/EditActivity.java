package cz.xdx11.todo_list;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
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

import cz.xdx11.todo_list.R;

public class EditActivity extends AppCompatActivity {

    private long id;
    private TextView dateView;
    private ImageView setDateBtn;
    private long dateUpd = 0;
    private DatePickerDialog datePickDiag;
    private MenuItem menuItem_Save;
    private Note note;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        Toolbar tb = (Toolbar) findViewById(R.id.toolbar_inc);
        setSupportActionBar(tb);
        final ActionBar ab = getSupportActionBar();
        if(ab!=null)
            ab.setDisplayHomeAsUpEnabled(true);

        id = getIntent().getLongExtra("id", 0);
        note = Note.findById(Note.class, id);

        dateView = (TextView) findViewById(R.id.get_date_time_edit);
        dateUpd = note.getDeadDate();
        String date = Utils.getFormattedDate(Utils.DEFAULT, new Date(dateUpd));
        dateView.setText(date);

        setDateBtn = (ImageView) findViewById(R.id.btn_set_date_edit);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_menu, menu);
        menuItem_Save = menu.findItem(R.id.action_add);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_add:
                onUpdateNote();
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

    public void onUpdateNote(){

        String title = ((EditText)findViewById(R.id.title_edit)).getText().toString();
        String text = ((EditText)findViewById(R.id.textDesc_edit)).getText().toString();
        boolean important2 = ((CheckBox)findViewById(R.id.chb_important_edit)).isChecked();
        boolean done2 = ((CheckBox)findViewById(R.id.ch_done_edit)).isChecked();

        //validate
        EditText titleText = (EditText) findViewById(R.id.title_edit);
        if(TextUtils.isEmpty(title)) {
            titleText.setError("Prázdná hodnota není povolena!");
            return;
        }

        note.setTitle(title);
        note.setDescription(text);
        note.setDone(done2);
        note.setIsImportant(important2);
        note.setDeadDate(dateUpd);

        long success = note.save();
        if (success > -1) {
            Intent i = new Intent(this, MainActivity.class);
            finish();
            startActivity(i);
        }else{
            Toast.makeText(this, R.string.note_not_added, Toast.LENGTH_LONG).show();
        }
    }

}
