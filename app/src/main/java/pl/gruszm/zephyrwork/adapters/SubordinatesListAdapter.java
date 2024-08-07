package pl.gruszm.zephyrwork.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import pl.gruszm.zephyrwork.DTOs.UserDTO;
import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.callbacks.OnSubordinateDetailsClickCallback;
import pl.gruszm.zephyrwork.viewholders.SubordinateViewHolder;

public class SubordinatesListAdapter extends RecyclerView.Adapter<SubordinateViewHolder>
{
    private Activity activity;
    private List<UserDTO> userDTOs;
    private OnSubordinateDetailsClickCallback onSubordinateDetailsClickCallback;

    public SubordinatesListAdapter(Activity activity, List<UserDTO> userDTOs, OnSubordinateDetailsClickCallback onSubordinateDetailsClickCallback)
    {
        this.activity = activity;
        this.userDTOs = userDTOs;
        this.onSubordinateDetailsClickCallback = onSubordinateDetailsClickCallback;
    }

    @NonNull
    @Override
    public SubordinateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.subordinate_item, parent, false);

        return new SubordinateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubordinateViewHolder holder, int position)
    {
        holder.setDataUsingUserDTO(userDTOs.get(position));
        holder.setActivity(this.activity);
        holder.setOnSubordinateDetailsClickCallback(onSubordinateDetailsClickCallback);
    }

    @Override
    public int getItemCount()
    {
        return userDTOs.size();
    }
}
