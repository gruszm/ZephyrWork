package pl.gruszm.zephyrwork.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import pl.gruszm.zephyrwork.DTOs.UserDTO;
import pl.gruszm.zephyrwork.R;

public class EmployeeDetailsActivity extends AppCompatActivity
{
    private UserDTO userDTO;
    private TextView employeeIdTv, email, firstName, lastName, role;
    private EditText intervalEt;
    private Button saveIntervalBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employee_details);

        employeeIdTv = findViewById(R.id.employee_id);
        email = findViewById(R.id.employee_email);
        firstName = findViewById(R.id.employee_first_name);
        lastName = findViewById(R.id.employee_last_name);
        role = findViewById(R.id.employee_role);
        saveIntervalBtn = findViewById(R.id.save_interval_button);
        intervalEt = findViewById(R.id.employee_location_registration_interval);

        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();

            userDTO = new Gson().fromJson(extras.getString("UserDTO"), UserDTO.class);

            employeeIdTv.setText(String.valueOf(userDTO.getId()));
            email.setText(userDTO.getEmail());
            firstName.setText(userDTO.getFirstName());
            lastName.setText(userDTO.getLastName());
            role.setText(userDTO.getRoleName());
            intervalEt.setText(String.valueOf(userDTO.getLocationRegistrationInterval()));
        }
    }
}