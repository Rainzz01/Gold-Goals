package com.example.goalandgoals.Helper;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.example.goalandgoals.Adapter.ToDoAdapter;
import com.example.goalandgoals.Model.ToDoModel;
import com.example.goalandgoals.R;

public class RecyclerItemTouchHelper extends ItemTouchHelper.SimpleCallback {
    private static final String TAG = "RecyclerItemTouchHelper";
    private ToDoAdapter adapter;
    private int lastSwipedPosition = -1;

    public RecyclerItemTouchHelper(ToDoAdapter adapter) {
        super(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT);
        this.adapter = adapter;
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
        return false;
    }

    @Override
    public int getSwipeDirs(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        int position = viewHolder.getAdapterPosition();
        ToDoModel item = getTaskAtPosition(position);
        if (item != null && item.getStatus() == 1) {
            // Disable swipe for completed tasks
            return 0;
        }
        return super.getSwipeDirs(recyclerView, viewHolder);
    }

    @Override
    public void onSwiped(@NonNull final RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        lastSwipedPosition = position;
        Log.d(TAG, "onSwiped: Position=" + position + ", Direction=" + (direction == ItemTouchHelper.LEFT ? "LEFT" : "RIGHT"));

        if (direction == ItemTouchHelper.LEFT) {
            AlertDialog.Builder builder = new AlertDialog.Builder(adapter.getContext());
            builder.setTitle("Delete Task");
            builder.setMessage("Are you sure you want to delete this Task?");
            builder.setPositiveButton("Confirm",
                    (dialog, which) -> adapter.deleteItem(position));
            builder.setNegativeButton(android.R.string.cancel,
                    (dialog, which) -> {
                        adapter.notifyItemChanged(position);
                        lastSwipedPosition = -1; // Reset after cancel
                    });
            AlertDialog dialog = builder.create();
            dialog.show();
        } else {
            // Clear swipe state before navigating to edit
            clearView(adapter.getRecyclerView(), viewHolder);
            adapter.editItem(position);
        }
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

        Drawable icon;
        ColorDrawable background;

        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;

        if (dX > 0) { // Swiping to the right
            icon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.ic_baseline_edit);
            background = new ColorDrawable(Color.YELLOW);
        } else { // Swiping to the left
            icon = ContextCompat.getDrawable(adapter.getContext(), R.drawable.ic_baseline_delete);
            background = new ColorDrawable(Color.RED);
        }

        if (icon == null) {
            Log.e(TAG, "Icon is null");
            return;
        }

        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();

        if (dX > 0) { // Swiping to the right
            int iconLeft = itemView.getLeft() + iconMargin;
            int iconRight = itemView.getLeft() + iconMargin + icon.getIntrinsicWidth();
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getLeft(), itemView.getTop(),
                    itemView.getLeft() + ((int) dX) + backgroundCornerOffset, itemView.getBottom());
        } else if (dX < 0) { // Swiping to the left
            int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
            int iconRight = itemView.getRight() - iconMargin;
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);

            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else { // View is unSwiped
            background.setBounds(0, 0, 0, 0);
        }

        background.draw(c);
        icon.draw(c);
    }

    private ToDoModel getTaskAtPosition(int position) {
        try {
            // Use reflection to access private method getTaskAtPosition
            java.lang.reflect.Method method = ToDoAdapter.class.getDeclaredMethod("getTaskAtPosition", int.class);
            method.setAccessible(true);
            return (ToDoModel) method.invoke(adapter, position);
        } catch (Exception e) {
            Log.e(TAG, "Failed to get task at position: " + e.getMessage());
            return null;
        }
    }

    public void resetSwipeState(RecyclerView recyclerView) {
        if (lastSwipedPosition != -1) {
            Log.d(TAG, "resetSwipeState: Resetting swipe for position=" + lastSwipedPosition);
            adapter.notifyItemChanged(lastSwipedPosition);
            lastSwipedPosition = -1;
        }
    }

    private RecyclerView getRecyclerView() {
        RecyclerView recyclerView = adapter.getRecyclerView();
        if (recyclerView == null) {
            Log.w(TAG, "getRecyclerView: RecyclerView is null, adapter not attached");
        }
        return recyclerView;
    }
}