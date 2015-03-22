package se.mhakansson.todolist_android;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;

import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;
import java.util.ArrayList;


public class DisplayListActivity extends ActionBarActivity {

    private RecyclerView mRecyclerView;
    private CustomRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private EditText mText;
    private ArrayList<ListItem> mData = new ArrayList<>();

    // Id of the list currently viewed
    int listId;

    // Establish socket connection to server
    private Socket socket;
    {
        try {
            socket = IO.socket(ListOverviewActivity.SERVER_ADDRESS);
        } catch (URISyntaxException e) {
            Log.d("error in socket url", "");
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Intent intent = getIntent();
        listId = intent.getIntExtra(ListOverviewActivity.LIST_ID, -1);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_list);

        // Initializing views.
        mText = (EditText) findViewById(R.id.textEt);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler);

        // Setting the LayoutManager.
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // Setting the adapter.
        mAdapter = new CustomRecyclerAdapter();
        mRecyclerView.setAdapter(mAdapter);

        // Add data locally to the list.
        ListItem listToAdd = new ListItem(1, "Texten2", false);
        ListItem listToAdd2 = new ListItem(1, "Texten3", false);
        ListItem listToAdd3 = new ListItem(1, "Texten1", false);
        ListItem listToAdd4 = new ListItem(1, "Texten4", false);
        ListItem listToAdd5 = new ListItem(1, "Texten6", false);
        ListItem listToAdd6 = new ListItem(1, "Texten5", false);
        ListItem listToAdd7 = new ListItem(1, "Texten7", false);

        ArrayList<ListItem> lst = new ArrayList<ListItem>();
        lst.add(listToAdd);
        lst.add(listToAdd2);
        lst.add(listToAdd3);
        lst.add(listToAdd4);
        lst.add(listToAdd5);
        lst.add(listToAdd6);

        mData.add(listToAdd);
        mData.add(listToAdd2);
        mData.add(listToAdd3);
        mData.add(listToAdd4);
        mData.add(listToAdd5);
        mData.add(listToAdd6);


        mAdapter.updateList(lst);
        mAdapter.addItem(listToAdd7);


//        TextView textView = new TextView(this);
//        textView.setText(Integer.toString(listId));
//        setContentView(textView);
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
        // Todo: updatedItem
        // Todo: itemRemoved
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
                final ListItem newListItem = new ListItem(obj.getInt("id"), obj.getString("text"), obj.getBoolean("finished"));
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

}

