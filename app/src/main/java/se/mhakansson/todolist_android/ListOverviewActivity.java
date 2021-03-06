package se.mhakansson.todolist_android;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
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
import java.util.Collections;
import java.util.Comparator;

public class ListOverviewActivity extends ActionBarActivity {
    public final static String SERVER_ADDRESS = "http://192.168.1.103:1337";
    public final static String LIST_ID = "se.mhakansson.todolist_android.LIST_ID";


    private RecyclerView mRecyclerView;
    private ListOverviewCustomRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    // Establish socket connection to server
    public static Socket socket;
    {
        try {
            socket = IO.socket(SERVER_ADDRESS);
        } catch (URISyntaxException e) {
            Log.d("error in socket url", "");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_overview);

        // Download all current lists and add to the view
        new DownloadLists().execute(SERVER_ADDRESS + "/list");

        // Initializing views.
        mRecyclerView = (RecyclerView) findViewById(R.id.list_overview_recycler);

        // Setting the LayoutManager.
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Setting the adapter.
        mAdapter = new ListOverviewCustomRecyclerAdapter();
        mRecyclerView.setAdapter(mAdapter);

        // Set click listener for adding new items
        Button add_list_button = (Button) findViewById(R.id.add_list_button);
        add_list_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("ListOverviewActivity", "Click on add_list_button");

                JSONObject obj = new JSONObject();
                EditText textField = (EditText) findViewById(R.id.add_list_text_field);

                try {
                    String listName = String.valueOf(textField.getText());
                    Log.d("DisplayListActivity", "Text in text field: " + listName);
                    if (listName != null) {
                        obj.put("url", "/list/create/" + listName);
                        socket.emit("post", obj);

                        // Clear text field and remove focus from keyboard
                        textField.setText("");
                        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(textField.getWindowToken(), 0);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
        });

    }

    @Override
    protected void onPause() {
        Log.d("ListOverviewActivity", "onPause called");
        socket.disconnect(); // Disconnect the socket
        socket.off(); // Unsubscribe from everything
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d("ListOverviewActivity", "onResume called");

        // Listen to events
        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.on(Socket.EVENT_CONNECT_ERROR, onEventConnectError);
        socket.on("listAdded", onListAdded);
        socket.on("listRemoved", onListRemoved);
        socket.connect();

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        Log.d("ListOverviewActivity", "onDestroy called");
        super.onDestroy();
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("ListOverviewActivity", "socket connected");

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
            Log.d("ListOverviewActivity", "List Added: ");

            try {
                final List newList = new List(obj.getInt("id"), obj.getString("name"));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.addList(newList);
                    }
                });

                Log.d("List id: ", Integer.toString(obj.getInt("id")));
                Log.d("List name: ", obj.getString("name"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onListRemoved = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject obj = null;
            try {
                obj = new JSONObject(args[0].toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("ListOverviewActivity", "List Removed " + obj.toString());

            try {
                final int idOfListToRemove = obj.getInt("id");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.removeList(idOfListToRemove);
                    }
                });

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("ListOverviewActivity", "socket disconnected");
        }
    };

    private Emitter.Listener onEventConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("ListOverviewActivity", "connection error");
        }
    };


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

            ArrayList<List> lists = List.fromJson(response);
            mAdapter.updateList(lists);
        }
    }

//    public class ListsAdapter extends ArrayAdapter<List> {
//        public ListsAdapter(Context context, ArrayList<List> lists) {
//            super(context, 0, lists);
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            // Get the data item for this position
//            final List list = getItem(position);
//
//            // Check if an existing view is being reused, otherwise inflate the view
//            if (convertView == null) {
//                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list, parent, false);
//            }
//
//            // Lookup view for data population
//            TextView name = (TextView) convertView.findViewById(R.id.listName);
//
//            // Populate the data into the template view using the data object
//            name.setText(list.name);
//            convertView.setOnClickListener(new View.OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    // Make intent and send to the DisplayListActivity
//                    Intent intent = new Intent(view.getContext(), DisplayListActivity.class);
//                    intent.putExtra(LIST_ID, list.id);
//                    Log.d("Click on item", Integer.toString(list.id));
//                    startActivity(intent);
//                }
//            });
//            // Return the completed view to render on screen
//            return convertView;
//        }
//
//        // Override notification on changed data in order to sort before notifying
//        @Override
//        public void notifyDataSetChanged() {
//            Collections.sort(arrayOfLists, new Comparator<List>() {
//                public int compare(List lhs, List rhs) {
//                    return lhs.name.compareTo(rhs.name);
//                }
//            });
//
//            super.notifyDataSetChanged();
//        }
//    }

}

