package se.mhakansson.todolist_android;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ListOverviewRecyclerViewHolder extends RecyclerView.ViewHolder
        implements View.OnClickListener, View.OnLongClickListener {

    public TextView listName;
    public Button remove_button;

    private ClickListener clickListener;

    public ListOverviewRecyclerViewHolder(View listView) {
        super(listView);
        listName = (TextView) itemView.findViewById(R.id.list_name);
        remove_button = (Button) itemView.findViewById(R.id.remove_button);

        // We set listeners to the whole item view, but we could also
        // specify listeners for the different objects by changing 'itemView' to something else.
        itemView.setOnClickListener(this);
        itemView.setOnLongClickListener(this);
    }


    /* Interface for handling clicks - both normal and long ones. */
    public interface ClickListener {

        /**
         * Called when the view is clicked.
         *
         * @param v view that is clicked
         * @param position of the clicked item
         * @param isLongClick true if long click, false otherwise
         */
        public void onClick(View v, int position, boolean isLongClick);

    }

    /* Setter for listener. */
    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public void onClick(View v) {
        // If not long clicked, pass last variable as false.
        clickListener.onClick(v, getPosition(), false);
    }

    @Override
    public boolean onLongClick(View v) {
        // If long clicked, passed last variable as true.
        clickListener.onClick(v, getPosition(), true);
        return true;
    }

}
