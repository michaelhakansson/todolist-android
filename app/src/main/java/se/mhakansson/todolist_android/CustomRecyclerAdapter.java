package se.mhakansson.todolist_android;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CustomRecyclerAdapter
        extends RecyclerView.Adapter<RecyclerViewHolder> {

    private static String TAG = "CustomRecyclerAdapter";

    private ArrayList<ListItem> mArrayOfListItems = new ArrayList<ListItem>();

    public CustomRecyclerAdapter() {
        // Pass context or other static stuff that will be needed.
    }

    @Override
    public RecyclerViewHolder onCreateViewHolder(ViewGroup viewGroup, int position) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.list_item, viewGroup, false);
        return new RecyclerViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final RecyclerViewHolder viewHolder, final int position) {
        viewHolder.text.setText(mArrayOfListItems.get(position).text);
        viewHolder.checkbox.setChecked(mArrayOfListItems.get(position).finished);

        final int id = mArrayOfListItems.get(position).id;
        final int listId = mArrayOfListItems.get(position).listId;

        viewHolder.setClickListener(new RecyclerViewHolder.ClickListener() {
            @Override
            public void onClick(View v, int pos, boolean isLongClick) {
                if (isLongClick) {
                    // View v at position pos is long-clicked.
                    Log.d("long Click", "list text: " + mArrayOfListItems.get(pos).text);
                } else {
                    // View v at position pos is clicked.
                    Log.d("short Click", "list text: " + mArrayOfListItems.get(pos).text);
                }
            }
        });

        viewHolder.checkbox.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "CLICK ON CHECKBOX " + Integer.toString(position));
                boolean isChecked = viewHolder.checkbox.isChecked();

                JSONObject obj = new JSONObject();

                try {
                    obj.put("url", "/item/update/" +
                            id + "/" +
                            isChecked + "/" +
                            listId);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                DisplayListActivity.socket.emit("put", obj);
            }
        });

        viewHolder.remove_button.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Log.d(TAG, "CLICK ON BUTTON " + Integer.toString(position));

                JSONObject obj = new JSONObject();

                try {
                    obj.put("url", "/item/remove/" + id);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                DisplayListActivity.socket.emit("put", obj);
            }
        });

        viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            /*
             * This can be used if something is to happen every time a checkbox is changed.
             * This is called every time the check box is changed, no matter if it is by
             * clicking the checkbox or by updating the checkbox via the adapter.
             */

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Log.d("CustomRecyclerAdapter checkbox changed", Integer.toString(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return mArrayOfListItems.size();
    }

    public void sortList() {
        Collections.sort(mArrayOfListItems, new Comparator<ListItem>() {
            public int compare(ListItem lhs, ListItem rhs) {
                return lhs.text.compareTo(rhs.text);
            }
        });
    }

    public int getIndexToInsertInto(ListItem item) {
        int index = 0;

        if (getItemCount() > 0) {
            for (index = 0; index < mArrayOfListItems.size(); index++) {
                if (mArrayOfListItems.get(index).text.compareToIgnoreCase(item.text) > 0) {
                    // Found item is larger than item to insert ==> insert before this item
                    break;
                }
            }
        }
        return index;
    }

    public void updateList(ArrayList<ListItem> data) {
        Log.d(TAG, "updateList data in parameter " + data.toString());
        mArrayOfListItems = data;
        sortList();
        notifyDataSetChanged();
    }

    public void addItem(ListItem item) {
        Log.d(TAG, "first in addItem");
        int position = getIndexToInsertInto(item);
        mArrayOfListItems.add(position, item);
        notifyItemInserted(position);
    }

    public void updateItem(ListItem item) {
        Log.d(TAG, "first in addItem");
        int position = getItemIndexById(item.id);
        mArrayOfListItems.get(position).finished = item.finished;
        notifyItemChanged(position);
    }

    public void removeItem(int id) {
        int indexToRemove = getItemIndexById(id);
        if (indexToRemove != -1) {
            mArrayOfListItems.remove(indexToRemove);
            notifyItemRemoved(indexToRemove);
        }
    }

    private int getItemIndexById(int id) {
        int index = -1;
        int size = mArrayOfListItems.size();
        for (int i = 0; i < size; ++i) {
            if (mArrayOfListItems.get(i).id == id) {
                index = i;
                break;
            }
        }
        return index;
    }
}
