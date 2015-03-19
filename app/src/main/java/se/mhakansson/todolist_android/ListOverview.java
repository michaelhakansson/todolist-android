package se.mhakansson.todolist_android;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.nkzawa.emitter.Emitter;
import com.github.nkzawa.socketio.client.IO;
import com.github.nkzawa.socketio.client.Socket;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URISyntaxException;

public class ListOverview extends ActionBarActivity {

    private Socket socket;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_overview);

        String[] array = new String[]{"Apple", "Google"};
        ViewGroup listContainer = (ViewGroup) findViewById(R.id.list_container);

        for (String anArray : array) {
            TextView text = new TextView(this);
            final String textToSet = anArray;
            text.setText(anArray);
            text.setTextSize(20);
            text.setClickable(true);

            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("Click", textToSet);
                }
            });

            listContainer.addView(text);

            //CheckBox checkBox = new CheckBox(this);
            //checkBox.setText(array[i]);
            //checkboxContainer.addView(checkBox);
        }


        try {
            socket = IO.socket("http://192.168.0.104:1337");
        } catch (URISyntaxException e) {
            Log.d("error in socket url", "");
        }

        socket.on(Socket.EVENT_CONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.d("MainActivity: ", "socket connected");

                JSONObject obj = new JSONObject();
                try {
                    obj.put("url", "/lists/subscribe");
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                socket.emit("get", obj);
            }

        }).on("listAdded", new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.d("MainActivity: ", "List Added: ");
                    Log.d("MainActivity: ", args[0].toString());
            }

        }).on(Socket.EVENT_DISCONNECT, new Emitter.Listener() {

            @Override
            public void call(Object... args) {
                Log.d("MainActivity: ", "socket disconnected");
            }

        }).on(Socket.EVENT_CONNECT_ERROR, new Emitter.Listener() {
            @Override
            public void call(Object... args) {
                Log.d("MainActivity: ", "connection error");
            }
        });
        socket.connect();
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

}
