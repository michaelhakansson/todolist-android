package se.mhakansson.todolist_android;

import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class CustomRecyclerAdapter
        extends RecyclerView.Adapter<RecyclerViewHolder> {

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
    public void onBindViewHolder(RecyclerViewHolder viewHolder, int position) {
        viewHolder.text.setText(mArrayOfListItems.get(position).text);

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
        Log.d("CustomRecyclerAdapter updateList data in parameter", data.toString());
        mArrayOfListItems = data;
        sortList();
        notifyDataSetChanged();
    }

    public void addItem(ListItem item) {
        Log.d("CustomRecyclerAdapter: ", "first in addItem");
        int position = getIndexToInsertInto(item);
        mArrayOfListItems.add(position, item);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        mArrayOfListItems.remove(position);
        notifyItemRemoved(position);
    }
}
