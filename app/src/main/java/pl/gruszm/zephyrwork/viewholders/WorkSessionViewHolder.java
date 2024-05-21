package pl.gruszm.zephyrwork.viewholders;

import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import pl.gruszm.zephyrwork.R;

public class WorkSessionViewHolder extends RecyclerView.ViewHolder
{
    private int workSessionId;
    public TextView firstNameAndLastNameTv, startingDateTv, endingDateTv, state;
    private Button detailsBtn;

    public WorkSessionViewHolder(@NonNull View itemView)
    {
        super(itemView);

        firstNameAndLastNameTv = itemView.findViewById(R.id.first_name_and_last_name);
        startingDateTv = itemView.findViewById(R.id.starting_date);
        endingDateTv = itemView.findViewById(R.id.ending_date);
        detailsBtn = itemView.findViewById(R.id.details);
        state = itemView.findViewById(R.id.state);
    }

    public void setWorkSessionId(int workSessionId)
    {
        this.workSessionId = workSessionId;
    }
}
