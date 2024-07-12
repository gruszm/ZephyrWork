package pl.gruszm.zephyrwork.viewholders;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import pl.gruszm.zephyrwork.DTOs.UserDTO;
import pl.gruszm.zephyrwork.R;

public class SubordinateViewHolder extends RecyclerView.ViewHolder
{
    private TextView firstName, lastName, email;

    public SubordinateViewHolder(@NonNull View itemView)
    {
        super(itemView);

        firstName = itemView.findViewById(R.id.first_name);
        lastName = itemView.findViewById(R.id.last_name);
        email = itemView.findViewById(R.id.email);
    }

    public void setDataUsingUserDTO(UserDTO userDTO)
    {
        firstName.setText(userDTO.getFirstName());
        lastName.setText(userDTO.getLastName());
        email.setText(userDTO.getEmail());
    }
}
