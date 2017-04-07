package cz.xdx11.todo_list;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import com.orm.SugarContext;

import java.util.List;

import cz.xdx11.todo_list.R;

public class MainActivity extends AppCompatActivity {

    private static final int MENU_DELETE_ID = 0;
    private static final int MENU_UPDATE_ID = 1;
    private static final int MENU_DONE_ID = 2;
    private static final char POP_START = 's';
    private static final char POP_END = 'e';
    private static final char POP_TITLE = 't';
    private static final char POP_DESC = 'd';
    private String[] Countries;
    private Note noteDel;
    private ListView listView;
    private Switch switchDone;
    private ImageView imageStatus;
    private ImageButton order;
    private SharedPreferences sharedpreferences;
    private int switchNum;
    private int orderWay;
    private MenuItem menuItem_Sort;
    private static final String MyPREFERENCES = "myPreferences";
    public static final String FILTER_DONE = "filter_done";
    public static final String ORDER_NUM = "order_num";
    public static final String ORDER_WAY = "order_way";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {

            SugarContext.init(this);
            setContentView(R.layout.activity_main);

            sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
            // Always cast your custom Toolbar here, and set it as the ActionBar.
            Toolbar tb = (Toolbar) findViewById(R.id.toolbar_inc);
            switchDone = (Switch) findViewById(R.id.switch_done_filter);

            setSupportActionBar(tb);

            // Get the ActionBar here to configure the way it behaves.
            final ActionBar ab = getSupportActionBar();

            ab.setDisplayShowCustomEnabled(true); // enable overriding the default toolbar layout
            ab.setDisplayShowTitleEnabled(false); // disable the default title element here (for centered title)

            switchDone.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

                @Override
                public void onCheckedChanged(CompoundButton buttonView,
                                             boolean isChecked) {
                    SharedPreferences.Editor editor = sharedpreferences.edit();
                    editor.putString(FILTER_DONE, String.valueOf(switchDone.isChecked()));
                    editor.commit();
                    updateList();
                }
            });

            //Data.loadData();
            updateList();
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateList();
    }

    public void onAddNoteClicked(View v){
        Intent i = new Intent(this, AddActivity.class);
        startActivityForResult(i, 0);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        menuItem_Sort = menu.findItem(R.id.action_sort);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort:
                try {
                    orderBy();
                } catch (Exception e){
                    e.printStackTrace();
                }

                return true;
        }
        return true;
    }

    public void onCreateContextMenu (ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo){
        if (v.getId()==R.id.listNotes) {
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
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int index = info.position;
        View view = info.targetView;
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
        List<Note> notes = sortedList();
        listView = (ListView) findViewById(R.id.listNotes);
        NotesAdapter adapter = new NotesAdapter(this,R.layout.list_item_note,notes);
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

    private List<Note> sortedList(){
        String testSwitched = sharedpreferences.getString(FILTER_DONE, null);
        switchDone.setChecked(Boolean.parseBoolean(testSwitched));
        switchNum = sharedpreferences.getInt(ORDER_NUM,0);
        orderWay = sharedpreferences.getInt(ORDER_WAY,0);
        List<Note> notes;
        if(switchDone.isChecked()){
            //notes = Note.listAll(Note.class);
            if(orderWay==1){
                switch(switchNum){
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
                switch(switchNum){
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
            if(orderWay==1){
                switch(switchNum){
                    case 1: // START DATE
                        notes = Note.findWithQuery(Note.class, "Select * from note where is_done = '0' order by start_date asc");
                    case 2: // END DATE
                        notes = Note.findWithQuery(Note.class, "Select * from note where is_done = '0'  order by dead_date asc");
                    case 3: // TITLE
                        notes = Note.findWithQuery(Note.class, "Select * from note where is_done = '0'  order by title asc");
                    default:
                        notes = Note.listAll(Note.class);
                }
            } else {
                switch(switchNum){
                    case 1: // START DATE
                        notes = Note.findWithQuery(Note.class, "Select * from note where is_done = '0'  order by start_date desc");
                    case 2: // END DATE
                        notes = Note.findWithQuery(Note.class, "Select * from note where is_done = '0'  order by dead_date desc");
                    case 3: // TITLE
                        notes = Note.findWithQuery(Note.class, "Select * from note where is_done = '0'  order by title desc");
                    default:
                        notes = Note.listAll(Note.class);
                }
            }
        }
        return notes;
    }

    private AlertDialog AskOption(final long id)
    {
        AlertDialog myQuittingDialogBox =new AlertDialog.Builder(this)
                //set message, title, and icon
                .setTitle("Smazat")
                .setMessage("Opravdu si přejete smazat úkol?")
                .setIcon(android.R.drawable.ic_menu_delete)
                .setPositiveButton("Smazat", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int whichButton) {
                        Note note = Note.findById(Note.class, id);
                        if(note.delete()){
                            updateList();
                            Toast.makeText(getApplicationContext(), R.string.note_deleted, Toast.LENGTH_SHORT).show();
                        } else{
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
        return myQuittingDialogBox;

    }

    public void orderBy(){
        View menuItemView = findViewById(R.id.action_sort);
        PopupMenu popup = new PopupMenu(MainActivity.this, menuItemView);
        //Inflating the Popup using xml file
        popup.getMenuInflater().inflate(R.menu.order_menu1, popup.getMenu());

        //registering popup with OnMenuItemClickListener
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getNumericShortcut()){
                    case POP_START:
                        switchNum = 1;
                        Toast.makeText(MainActivity.this,"Seřazeno dle data přidání.",Toast.LENGTH_SHORT).show();
                        order();
                        return true;
                    case POP_END:
                        switchNum = 2;
                        Toast.makeText(MainActivity.this,"Seřazeno dle data terminu.",Toast.LENGTH_SHORT).show();
                        order();
                        return true;
                    case POP_TITLE:
                        switchNum = 3;
                        Toast.makeText(MainActivity.this,"Seřazeno dle názvu úkolů.",Toast.LENGTH_SHORT).show();
                        order();
                        return  true;
                    case POP_DESC:
                        if(orderWay==1){
                            orderWay=2;
                            item.setTitle("Sestupne");
                        } else {
                            orderWay=1;
                            item.setTitle("Vzestupne");

                        }
                        Toast.makeText(MainActivity.this,"Seřazeno opacne.",Toast.LENGTH_SHORT).show();
                        order();
                        return  true;
                    default:
                        switchNum = 1;
                        Toast.makeText(MainActivity.this,"item: " + item.getItemId() ,Toast.LENGTH_SHORT).show();
                        order();
                        return true;
                }
            }
        });
        popup.show();//showing popup menu
    }
    private void order(){
        SharedPreferences.Editor editor = sharedpreferences.edit();
        editor.putInt(ORDER_NUM, switchNum);
        editor.putInt(ORDER_WAY, orderWay);
        editor.apply();
        updateList();
    }
}
