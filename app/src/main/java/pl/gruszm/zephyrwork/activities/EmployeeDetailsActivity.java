package pl.gruszm.zephyrwork.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.gruszm.zephyrwork.DTOs.UserDTO;
import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.config.AppConfig;

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

        saveIntervalBtn.setOnClickListener(this::saveIntervalOnClickListener);
    }

    private void saveIntervalOnClickListener(View view)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        Request request = new Request.Builder()
                .get()
                .url(AppConfig.BACKEND_BASE
                        .concat("/users/subordinates/interval/")
                        .concat(String.valueOf(userDTO.getId()))
                        .concat("/")
                        .concat(intervalEt.getText().toString()))
                .header("Auth", sharedPreferences.getString("Auth", ""))
                .build();
        Call call = new OkHttpClient().newCall(request);

        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {

            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                if (response.isSuccessful())
                {
                    runOnUiThread(() -> Toast.makeText(EmployeeDetailsActivity.this, "Saved", Toast.LENGTH_SHORT).show());
                }
                else
                {
                    runOnUiThread(() -> Toast.makeText(EmployeeDetailsActivity.this, String.valueOf(response.code()), Toast.LENGTH_SHORT).show());
                }
            }
        });
    }
}