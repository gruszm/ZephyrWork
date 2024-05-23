package pl.gruszm.zephyrwork.viewholders;

import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.enums.RoleType;

public class WorkSessionViewHolder extends RecyclerView.ViewHolder
{
    private Context context;
    private RoleType userRole;
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

        detailsBtn.setOnClickListener(this::detailsOnClickListener);
    }

    private void detailsOnClickListener(View view)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder
                .append(firstNameAndLastNameTv.getText())
                .append("\n")
                .append(startingDateTv.getText())
                .append("\n")
                .append(endingDateTv.getText());

        alertDialogBuilder.setTitle("Work Session Details");
        alertDialogBuilder.setMessage(stringBuilder.toString());

        alertDialogBuilder.setNeutralButton("VERIFY ON MAP", (DialogInterface var1, int var2) ->
        {
            Toast.makeText(context, "VERIFY ON MAP", Toast.LENGTH_SHORT).show();
        });

        if (!userRole.equals(RoleType.EMPLOYEE))
        {
            alertDialogBuilder.setNegativeButton("RETURN", (DialogInterface var1, int var2) ->
            {
                Toast.makeText(context, "RETURN", Toast.LENGTH_SHORT).show();
            });
            alertDialogBuilder.setPositiveButton("ACCEPT", (DialogInterface var1, int var2) ->
            {
                Toast.makeText(context, "ACCEPT", Toast.LENGTH_SHORT).show();
            });
        }

        alertDialogBuilder.create().show();
    }

    public void setUserRole(RoleType userRole)
    {
        this.userRole = userRole;
    }

    public void setContext(Context context)
    {
        this.context = context;
    }

    public void setWorkSessionId(int workSessionId)
    {
        this.workSessionId = workSessionId;
    }
}
