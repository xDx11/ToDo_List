package cz.xdx11.todo_list;

import com.orm.SugarRecord;
import com.orm.dsl.Table;

/**
 * Created by xDx on 25.2.2016.
 */

public class Note extends SugarRecord{
    private long id;
    private String title;
    private String description;
    private boolean isImportant;
    private long deadDate;
    private long startDate;
    private boolean isDone;


    public Note(){

    }

    public Note(String title, String description, boolean isImportant, long startDate, long deadDate, boolean isDone) {
        this.title = title;
        this.description = description;
        this.isImportant = isImportant;
        this.startDate = startDate;
        this.deadDate = deadDate;
        this.isDone = isDone;
    }

    /*
    public Note(long id, String title, String description, boolean isImportant) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isImportant = isImportant;
    }

    public Note(long id, String title, String description, boolean isImportant, long deadDate, long startDate, boolean isDone) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.isImportant = isImportant;
        this.deadDate = deadDate;
        this.startDate = startDate;
        this.isDone = isDone;
    }
    */


    public Long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isImportant() {
        return isImportant;
    }

    public void setIsImportant(boolean isImportant) {
        this.isImportant = isImportant;
    }

    public long getDeadDate() {
        return deadDate;
    }

    public void setDeadDate(long deadDate) {
        this.deadDate = deadDate;
    }

    public long getStartDate() {
        return startDate;
    }

    public void setStartDate(long startDate) {
        this.startDate = startDate;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    @Override
    public String toString() {
        return "Note{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", description='" + description + '\'' +
                ", isImportant=" + isImportant +
                ", deadDate=" + deadDate +
                ", startDate=" + startDate +
                ", isDone=" + isDone +
                '}';
    }
}
