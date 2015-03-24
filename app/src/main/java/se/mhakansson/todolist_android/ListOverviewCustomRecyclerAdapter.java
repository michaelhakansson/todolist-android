package se.mhakansson.todolist_android;

import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class ListOverviewCustomRecyclerAdapter
        extends RecyclerView.Adapter<ListOverviewRecyclerViewHolder> {

    private static String TAG = "ListOverviewCustomRecyclerAdapter";

    private ArrayList<List> mArrayOfLists = new ArrayList<List>();

    public ListOverviewCustomRecyclerAdapter() {
        // Pass context or other static stuff that will be needed.
    }

    @Override
    public ListOverviewRecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View listView = inflater.inflate(R.layout.item_list, viewGroup, false);
        return new ListOverviewRecyclerViewHolder(listView);
    }

    @Override
    public void onBindViewHolder(final ListOverviewRecyclerViewHolder viewHolder, final int position) {
        viewHolder.listName.setText(mArrayOfLists.get(position).name);

        final int id = mArrayOfLists.get(position).id;

        viewHolder.setClickListener(new ListOverviewRecyclerViewHolder.ClickListener() {
            @Override
            public void onClick(View v, int pos, boolean isLongClick) {
                if (isLongClick) {
                    // View v at position pos is long-clicked.
                    Log.d("long Click", "list text: " + mArrayOfLists.get(pos).name);
                } else {
                    // View v at position pos is clicked.
                    Log.d("short Click", "list text: " + mArrayOfLists.get(pos).name);

                    // Make intent and send to the DisplayListActivity
                    Intent intent = new Intent(v.getContext(), DisplayListActivity.class);
                    intent.putExtra(ListOverviewActivity.LIST_ID, id);
                    Log.d("Click on item", Integer.toString(id));
                    v.getContext().startActivity(intent);
                }
            }
        });


        viewHolder.remove_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "CLICK ON BUTTON " + Integer.toString(position));

                JSONObject obj = new JSONObject();

                try {
                    obj.put("url", "/list/remove/" + id);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                ListOverviewActivity.socket.emit("put", obj);
            }
        });

    }

    @Override
    public int getItemCount() {
        return mArrayOfLists.size();
    }

    public void sortList() {
        Collections.sort(mArrayOfLists, new Comparator<List>() {
            public int compare(List lhs, List rhs) {
                return lhs.name.compareTo(rhs.name);
            }
        });
    }

    public int getIndexToInsertInto(List item) {
        int index = 0;

        if (getItemCount() > 0) {
            for (index = 0; index < mArrayOfLists.size(); index++) {
                if (mArrayOfLists.get(index).name.compareToIgnoreCase(item.name) > 0) {
                    // Found item is larger than item to insert ==> insert before this item
                    break;
                }
            }
        }
        return index;
    }

    public void updateList(ArrayList<List> data) {
        Log.d(TAG, "updateList data in parameter " + data.toString());
        mArrayOfLists = data;
        sortList();
        notifyDataSetChanged();
    }

    public void addList(List item) {
        Log.d(TAG, "first in addItem");
        int position = getIndexToInsertInto(item);
        mArrayOfLists.add(position, item);
        notifyItemInserted(position);
    }


    public void removeList(int id) {
        int indexToRemove = getListIndexById(id);
        if (indexToRemove != -1) {
            mArrayOfLists.remove(indexToRemove);
            notifyItemRemoved(indexToRemove);
        }
    }

    private int getListIndexById(int id) {
        int index = -1;
        int size = mArrayOfLists.size();
        for (int i = 0; i < size; ++i) {
            if (mArrayOfLists.get(i).id == id) {
                index = i;
                break;
            }
        }
        return index;
    }
}
