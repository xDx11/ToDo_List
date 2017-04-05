package xdx.fim.uhk.cz.notes_v2;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.Toast;

import java.util.ArrayList;

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
    private static final String MyPREFERENCES = "myPreferences";
    public static final String FILTER_DONE = "filter_done";
    public static final String ORDER_NUM = "order_num";
    public static final String ORDER_WAY = "order_way";

    private boolean getBoolean(int columnIndex, Cursor cursor){
        if (cursor.isNull(columnIndex) || cursor.getShort(columnIndex) == 0) {
            return false;
        } else {
            return true;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);


        // Always cast your custom Toolbar here, and set it as the ActionBar.
        Toolbar tb = (Toolbar) findViewById(R.id.toolbar_inc);
        switchDone = (Switch) findViewById(R.id.switch_done_filter);
        order = (ImageButton) findViewById(R.id.btn_orderby);

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


        //super.onCreateContextMenu(menu, v, menuInfo);



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
                Notes_DB notes_db = new Notes_DB(this);
                boolean success = notes_db.updateNoteDone(noteDel.getId(), noteDel.isDone());
                notes_db.close();
                updateList();
                return success;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void deleteNote(long id){
        AlertDialog diaBox = AskOption(id);
        diaBox.show();
    }

    private void editNote(int id){
        Intent i = new Intent(this, EditActivity.class);
        i.putExtra("id", id);
        startActivityForResult(i, 0);
    }

    private void updateList(){

        Context ctx = getApplicationContext();
        Notes_DB notes_db = new Notes_DB(ctx);
        ArrayList<Note> notes = new ArrayList<>();

        String testSwitched = sharedpreferences.getString(FILTER_DONE, null);
        switchDone.setChecked(Boolean.parseBoolean(testSwitched));
        switchNum = sharedpreferences.getInt(ORDER_NUM,0);
        orderWay = sharedpreferences.getInt(ORDER_WAY,0);
        Cursor c = notes_db.getNotes(switchNum, orderWay);
        if(switchDone.isChecked()){
            if (c != null) {
                while(c.moveToNext()) {
                    notes.add(new Note(c.getInt(0),c.getString(1),c.getString(2),getBoolean(3,c), c.getLong(4), c.getLong(5), getBoolean(6,c)));
                }
                c.close();
            }
        }
        else {
            if (c != null) {
                while(c.moveToNext()) {
                    if(!getBoolean(6,c)){
                        notes.add(new Note(c.getInt(0),c.getString(1),c.getString(2),getBoolean(3,c), c.getLong(4), c.getLong(5), getBoolean(6,c)));
                    }
                }
                c.close();
            }
        }


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
        for(int i = 0; i<notes.size();i++){
            System.out.println(notes.get(i).getId() + " " + notes.get(i).getTitle());
        }
        registerForContextMenu(listView);
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
                        Notes_DB notes = new Notes_DB(getApplicationContext());
                        if(notes.deleteNote(id)){
                            updateList();
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

    public void orderBy(View v){

        PopupMenu popup = new PopupMenu(MainActivity.this, order);
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
                        Toast.makeText(MainActivity.this,"Seřazeno dle data dokončení.",Toast.LENGTH_SHORT).show();
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
        editor.commit();
        updateList();
    }
}
