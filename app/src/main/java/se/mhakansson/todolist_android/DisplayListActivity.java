package se.mhakansson.todolist_android;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

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


public class DisplayListActivity extends ActionBarActivity {

    private RecyclerView mRecyclerView;
    private CustomRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private ArrayList<ListItem> mData = new ArrayList<>();

    // Id of the list currently viewed
    int listId;

    // Establish socket connection to server
    public static Socket socket;
    {
        try {
            socket = IO.socket(ListOverviewActivity.SERVER_ADDRESS);
        } catch (URISyntaxException e) {
            Log.d("error in socket url", "");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_list);

        Intent intent = getIntent();
        listId = intent.getIntExtra(ListOverviewActivity.LIST_ID, -1);

        // Download all current items in the current list
        new DownloadItems().execute(ListOverviewActivity.SERVER_ADDRESS + "/list/" + listId);

        // Initializing views.
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);

        // Setting the LayoutManager.
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Setting the adapter.
        mAdapter = new CustomRecyclerAdapter();
        mRecyclerView.setAdapter(mAdapter);

        // Set click listener for adding new items
        Button add_list_button = (Button) findViewById(R.id.add_item_button);
        add_list_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d("DisplayListActivity", "Click on add_item_button");

                JSONObject obj = new JSONObject();
                EditText textField = (EditText) findViewById(R.id.add_item_text_field);

                try {
                    String text = String.valueOf(textField.getText());
                    Log.d("DisplayListActivity", "Text in text field: " + text);
                    if (text != null) {
                        obj.put("url", "/item/create/" + listId + "/" + text);
                        DisplayListActivity.socket.emit("post", obj);

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
        Log.d("DisplayListActivity", "onPause called");
        socket.disconnect(); // Disconnect the socket
        socket.off(); // Unsubscribe from everything
        super.onPause();
    }

    @Override
    protected void onResume() {
        Log.d("DisplayListActivity", "onResume called");
        socket.on(Socket.EVENT_CONNECT, onConnect);
        socket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        socket.on(Socket.EVENT_CONNECT_ERROR, onEventConnectError);
        socket.on("itemAdded", onItemAdded);
        socket.on("updatedItem", onItemUpdated);
        socket.on("itemRemoved", onItemRemoved);
        socket.connect();
        super.onResume();
    }

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("DisplayActivity: ", "socket connected");

            // Subscribe to the currently viewed list by call to server API
            JSONObject obj = new JSONObject();
            try {
                obj.put("url", "/list/subscribe/" + listId);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            socket.emit("get", obj);
        }
    };

    private Emitter.Listener onItemAdded = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject obj = null;
            try {
                obj = new JSONObject(args[0].toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("DisplayActivity: ", "List Added: ");

            Log.d("Added item as json", obj.toString());

            try {
                final ListItem newListItem = new ListItem(obj.getInt("id"), obj.getInt("list"), obj.getString("text"), obj.getBoolean("finished"));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.addItem(newListItem);
                    }
                });

                Log.d("Item id: ", Integer.toString(obj.getInt("id")));
                Log.d("Text: ", obj.getString("text"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    };

    private Emitter.Listener onItemUpdated = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject obj = null;
            try {
                obj = new JSONObject(args[0].toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            final ListItem updatedItem = new ListItem(obj);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mAdapter.updateItem(updatedItem);
                }
            });

            // TODO: Change status of the checkbox
            Log.d("DisplayListActivity", "Item updated");
            Log.d("DisplayListActivity", obj.toString());
        }
    };

    private Emitter.Listener onItemRemoved = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            JSONObject obj = null;
            try {
                obj = new JSONObject(args[0].toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            try {
                final int idOfItemToRemove = obj.getInt("id");

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mAdapter.removeItem(idOfItemToRemove);
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
            Log.d("DisplayActivity: ", "socket disconnected");
        }
    };

    private Emitter.Listener onEventConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.d("DisplayActivity: ", "connection error");
        }
    };

    @Override
    protected void onDestroy() {
        Log.d("DisplayActivity", "onDestroy called");
        super.onDestroy();
        socket.disconnect(); // Disconnect the socket
        socket.off(); // Unsubscribe from everything
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
    class DownloadItems extends AsyncTask<String, String, JSONArray> {
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

        @Override
        protected void onPostExecute(JSONArray response) {
            super.onPostExecute(response);

            ArrayList<ListItem> lists = ListItem.fromJson(response);
            Log.d("DisplayListActivity json response", response.toString());
            mAdapter.updateList(lists);
        }
    }

}

