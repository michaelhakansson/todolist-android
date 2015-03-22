package se.mhakansson.todolist_android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ListItem {
    public int id;
    public int listId;
    public String text;
    public boolean finished;

    public ListItem(int id, int listId, String name, boolean finished) {
        this.id = id;
        this.listId = listId;
        this.text = name;
        this.finished = finished;
    }

    // Constructor to convert JSON object into a Java class instance
    public ListItem(JSONObject object){
        try {
            this.id = object.getInt("id");
            this.listId = object.getInt("list");
            this.text = object.getString("text");
            this.finished = object.getBoolean("finished");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Factory method to convert an array of JSON objects into a list of objects
    // User.fromJson(jsonArray);
    public static ArrayList<ListItem> fromJson(JSONArray jsonObjects) {
        ArrayList<ListItem> lists = new ArrayList<ListItem>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                lists.add(new ListItem(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return lists;
    }
}
