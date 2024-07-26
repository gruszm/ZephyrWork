package pl.gruszm.zephyrwork.adapters;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import pl.gruszm.zephyrwork.DTOs.UserDTO;
import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.callbacks.OnSubordinateDetailsClickCallback;
import pl.gruszm.zephyrwork.viewholders.SubordinateViewHolder;

public class SubordinatesListAdapter extends RecyclerView.Adapter<SubordinateViewHolder>
{
    private Activity activity;
    private List<UserDTO> userDTOs;
    private List<UserDTO> displayedUserDTOs;
    private OnSubordinateDetailsClickCallback onSubordinateDetailsClickCallback;

    public SubordinatesListAdapter(Activity activity, List<UserDTO> userDTOs, OnSubordinateDetailsClickCallback onSubordinateDetailsClickCallback)
    {
        this.activity = activity;
        this.userDTOs = userDTOs;
        this.displayedUserDTOs = userDTOs;
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
        holder.setDataUsingUserDTO(displayedUserDTOs.get(position));
        holder.setActivity(this.activity);
        holder.setOnSubordinateDetailsClickCallback(onSubordinateDetailsClickCallback);
    }

    @Override
    public int getItemCount()
    {
        return displayedUserDTOs.size();
    }

    public void searchForEmployee(String searchPhrase)
    {
        if (searchPhrase.isEmpty())
        {
            displayedUserDTOs = userDTOs;
        }

        String[] searchKeyWords = searchPhrase.split(" ");
        HashMap<UserDTO, Integer> hashMap = new HashMap<>();

        userDTOs.forEach(dto ->
        {
            AtomicInteger count = new AtomicInteger(0);

            Arrays.stream(searchKeyWords).forEach(kw ->
            {
                String firstNameAndLastName = dto.getFirstName() + " " + dto.getLastName();

                if (firstNameAndLastName.contains(kw))
                {
                    count.incrementAndGet();
                }
            });

            hashMap.put(dto, count.get());
        });

        displayedUserDTOs = hashMap.entrySet().stream().sorted((o1, o2) ->
                {
                    if (o1.getValue() < o2.getValue())
                    {
                        return -1;
                    }
                    else if (o1.getValue() > o2.getValue())
                    {
                        return 1;
                    }
                    else
                    {
                        return 0;
                    }
                })
                .collect(Collectors.toList()).stream().map(entry -> entry.getKey())
                .collect(Collectors.toList());
    }
}
