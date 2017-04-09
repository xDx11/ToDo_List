package cz.xdx11.todo_list;

import android.graphics.drawable.Drawable;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Prdík on 09.04.2017.
 */

public class MonthCallendarData {
    private Map<Date, Drawable> map;
    private Map<Date, Integer> mapText;

    public MonthCallendarData(Map<Date, Drawable> map, Map<Date, Integer> mapText){
        this.map=map;
        this.mapText=mapText;
    }

    public Map<Date, Drawable> getMap() {
        return map;
    }

    public void setMap(Map<Date, Drawable> map) {
        this.map = map;
    }

    public Map<Date, Integer> getMapText() {
        return mapText;
    }

    public void setMapText(Map<Date, Integer> mapText) {
        this.mapText = mapText;
    }
}
