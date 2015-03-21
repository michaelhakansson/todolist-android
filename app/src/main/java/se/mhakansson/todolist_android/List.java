package se.mhakansson.todolist_android;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class List {
    public int id;
    public String name;

    public List(int id, String name) {
        this.id = id;
        this.name = name;
    }

    // Constructor to convert JSON object into a Java class instance
    public List(JSONObject object){
        try {
            this.id = object.getInt("id");
            this.name = object.getString("name");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    // Factory method to convert an array of JSON objects into a list of objects
    // User.fromJson(jsonArray);
    public static ArrayList<List> fromJson(JSONArray jsonObjects) {
        ArrayList<List> lists = new ArrayList<List>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                lists.add(new List(jsonObjects.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return lists;
    }
}
