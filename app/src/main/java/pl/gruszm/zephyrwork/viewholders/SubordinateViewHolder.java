package pl.gruszm.zephyrwork.viewholders;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;

import pl.gruszm.zephyrwork.DTOs.UserDTO;
import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.activities.EmployeeDetailsActivity;
import pl.gruszm.zephyrwork.callbacks.OnSubordinateDetailsClickCallback;

public class SubordinateViewHolder extends RecyclerView.ViewHolder
{
    private UserDTO userDTO;
    private Activity activity;
    private TextView firstName, lastName, email;
    private ImageButton details;
    private OnSubordinateDetailsClickCallback onSubordinateDetailsClickCallback;
    private String supervisorRole, supervisorFirstAndLastName, supervisorEmail;

    public SubordinateViewHolder(@NonNull View itemView)
    {
        super(itemView);

        firstName = itemView.findViewById(R.id.first_name);
        lastName = itemView.findViewById(R.id.last_name);
        email = itemView.findViewById(R.id.email);
        details = itemView.findViewById(R.id.details_icon);

        details.setOnClickListener(this::detailsOnClickListener);
    }

    private void detailsOnClickListener(View view)
    {
        Gson gson = new Gson();
        Intent intent = new Intent(activity, EmployeeDetailsActivity.class);
        intent.putExtra("UserDTO", gson.toJson(userDTO));
        intent.putExtra("supervisor_role", supervisorRole);
        intent.putExtra("supervisor_first_and_last_name", supervisorFirstAndLastName);
        intent.putExtra("supervisor_email", supervisorEmail);

        onSubordinateDetailsClickCallback.vanishSubordinatesList();
        activity.startActivity(intent);
    }

    public void setNavigationData(String supervisorRole, String supervisorFirstAndLastName, String supervisorEmail)
    {
        this.supervisorRole = supervisorRole;
        this.supervisorFirstAndLastName = supervisorFirstAndLastName;
        this.supervisorEmail = supervisorEmail;
    }

    public void setActivity(Activity activity)
    {
        this.activity = activity;
    }

    public void setDataUsingUserDTO(UserDTO userDTO)
    {
        this.userDTO = userDTO;

        firstName.setText(userDTO.getFirstName());
        lastName.setText(userDTO.getLastName());
        email.setText(userDTO.getEmail());
    }

    public void setOnSubordinateDetailsClickCallback(OnSubordinateDetailsClickCallback onSubordinateDetailsClickCallback)
    {
        this.onSubordinateDetailsClickCallback = onSubordinateDetailsClickCallback;
    }
}
