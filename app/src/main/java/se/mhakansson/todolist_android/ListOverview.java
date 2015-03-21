package se.mhakansson.todolist_android;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

public class ListOverview extends ActionBarActivity {

    private Socket socket;
    private ListsAdapter adapter;
    // Establish socket connection to server
    {
        try {
            socket = IO.socket("http://192.168.0.104:1337");
        } catch (URISyntaxException e) {
            Log.d("error in socket url", "");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_overview);

        // Download all current lists and add to the view
        new DownloadLists().execute("http://192.168.0.104:1337/list");


        // Construct the data source
        ArrayList<List> arrayOfLists = new ArrayList<List>();
        // Create the adapter to convert the array to views
        adapter = new ListsAdapter(this, arrayOfLists);
        // Attach the adapter to a ListView
        ListView listView = (ListView) findViewById(R.id.list_container);
        listView.setAdapter(adapter);

        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.on(Socket.EVENT_CONNECT_ERROR, onEventConnectError);
        socket.on("listAdded", onListAdded);
        socket.connect();
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("MainActivity: ", "socket connected");

            // Subscribe to lists by call to server API
            JSONObject obj = new JSONObject();
            try {
                obj.put("url", "/lists/subscribe");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("get", obj);
        }
    };

    private Emitter.Listener onListAdded = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject obj = null;
            try {
                obj = new JSONObject(args[0].toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("MainActivity: ", "List Added: ");

            try {
                final List newList = new List(obj.getInt("id"), obj.getString("name"));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        adapter.add(newList);
                    }
                });

                Log.d("List id: ", Integer.toString(obj.getInt("id")));
                Log.d("List name: ", obj.getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }



        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("MainActivity: ", "socket disconnected");
        }
    };

    private Emitter.Listener onEventConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("MainActivity: ", "connection error");
        }
    };

    @Override
    protected void onDestroy() {
        Log.d("OnDestroy", "Called");
        super.onDestroy();
        socket.disconnect(); // Disconnect the socket
        socket.off(); // Unsubscribe from everything
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_list_overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // Downloads all lists asynchronous and loads them into the view
    class DownloadLists extends AsyncTask<String, String, JSONArray> {
        @Override
        protected JSONArray doInBackground(String... uri) {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response;
            String responseString = null;
            JSONArray responseJson = null;
            try {
                response = httpclient.execute(new HttpGet(uri[0]));
                StatusLine statusLine = response.getStatusLine();
                if(statusLine.getStatusCode() == HttpStatus.SC_OK){
                    ByteArrayOutputStream out = new ByteArrayOutputStream();
                    response.getEntity().writeTo(out);
                    responseString = out.toString();
                    out.close();
                } else{
                    //Closes the connection.
                    response.getEntity().getContent().close();
                    throw new IOException(statusLine.getReasonPhrase());
                }
            } catch (ClientProtocolException e) {
                //TODO Handle problems..
            } catch (IOException e) {
                //TODO Handle problems..
            }
            try {
                responseJson = new JSONArray(responseString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return responseJson;
        }

        @TargetApi(Build.VERSION_CODES.HONEYCOMB)
        @Override
        protected void onPostExecute(JSONArray response) {
            super.onPostExecute(response);
            JSONObject row = null;

            ArrayList<List> lists = List.fromJson(response);
            adapter.addAll(lists);
        }
    }

    public static class List {
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

    public class ListsAdapter extends ArrayAdapter<List> {
        public ListsAdapter(Context context, ArrayList<List> lists) {
            super(context, 0, lists);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // Get the data item for this position
            final List list = getItem(position);

            // Check if an existing view is being reused, otherwise inflate the view
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list, parent, false);
            }

            // Lookup view for data population
            TextView name = (TextView) convertView.findViewById(R.id.listName);

            // Populate the data into the template view using the data object
            name.setText(list.name);
            convertView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Click on item", Integer.toString(list.id));
                }
            });
            // Return the completed view to render on screen
            return convertView;
        }
    }

}
