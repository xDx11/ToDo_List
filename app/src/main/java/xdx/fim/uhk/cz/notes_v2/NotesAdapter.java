package xdx.fim.uhk.cz.notes_v2;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by xDx on 25.2.2016.
 */
public class NotesAdapter extends ArrayAdapter<Note> {


    public NotesAdapter(Context context, int resource, List<Note> objects) {
        super(context, resource, objects);
    }

    public View getView(int position, View convertView, ViewGroup group){

        if (convertView == null) {
            LayoutInflater inflator = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflator.inflate(R.layout.list_item_note, null);
        }

        Note note = getItem(position);

        TextView textView = (TextView) convertView.findViewById(R.id.textView);
        ImageView image = (ImageView) convertView.findViewById(R.id.imageView);
        TextView textDate = (TextView) convertView.findViewById(R.id.textViewDateItem);
        TextView textStartDate = (TextView) convertView.findViewById(R.id.textViewStartDateItem);
        RelativeLayout relativeLayout = (RelativeLayout) convertView.findViewById(R.id.rel_layout_item);
        ImageView imageStatus = (ImageView) convertView.findViewById(R.id.imageViewStatus);

        textView.setText(note.getTitle());
        if(note.isImportant()){
            image.setVisibility(View.VISIBLE);
        } else {
            image.setVisibility(View.INVISIBLE);
        }

        String date = Utils.getFormattedDate(Utils.DEFAULT, new Date(note.getDeadDate()));

        SimpleDateFormat newFormat = new SimpleDateFormat("dd/MM/yyyy");
        String date2 = newFormat.format(note.getStartDate()*1000L);
        textDate.setText(date2);
        textStartDate.setText(date);

        if(note.isDone()){
            textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            imageStatus.setImageResource(R.drawable.green_status);

            //ListView list = (ListView) convertView.findViewById(R.id.listNotes);
            //list.setSelector(R.drawable.list_selector_done);
            //relativeLayout.setBackgroundResource(R.color.lightGreen);
            convertView.setBackgroundResource(R.drawable.list_selector_done);
        } else {
            textView.setPaintFlags(textView.getPaintFlags() & (~ Paint.STRIKE_THRU_TEXT_FLAG));

            imageStatus.setImageResource(R.drawable.red_status);
            convertView.setBackgroundResource(R.drawable.list_selector);

            //relativeLayout.setBackgroundResource(R.color.lightRed);
            //ListView list = (ListView) convertView.findViewById(R.id.listNotes);
            //list.setSelector(R.drawable.list_selector);
        }

        return convertView;
    }
}
