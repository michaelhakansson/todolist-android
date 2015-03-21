package se.mhakansson.todolist_android;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.widget.EditText;

import java.util.ArrayList;


public class DisplayListActivity extends ActionBarActivity {

    private RecyclerView mRecyclerView;
    private CustomRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private EditText mText;
    private ArrayList<ListItem> mData = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        ListItem listToAdd = new ListItem(1, "Texten", false);
        ListItem listToAdd2 = new ListItem(1, "Texten", false);
        ListItem listToAdd3 = new ListItem(1, "Texten", false);
        ListItem listToAdd4 = new ListItem(1, "Texten", false);
        ListItem listToAdd5 = new ListItem(1, "Texten", false);
        ListItem listToAdd6 = new ListItem(1, "Texten", false);

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


        // Update adapter
//        mAdapter.addItem(mData.size()-1, listToAdd);
        mAdapter.updateList(lst);


//        Intent intent = getIntent();
//        int listId = intent.getIntExtra(ListOverviewActivity.LIST_ID, -1);
//        TextView textView = new TextView(this);
//        textView.setText(Integer.toString(listId));
//        setContentView(textView);
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

