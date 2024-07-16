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
    //    private int employeeId, interval;
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

//        retrieveEmployeeData();
    }

//    private void retrieveEmployeeData()
//    {
//        OkHttpClient okHttpClient = new OkHttpClient();
//        Gson gson = new Gson();
//        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
//        Request request = new Request.Builder()
//                .get()
//                .url(AppConfig.BACKEND_BASE.concat("/api/users/id/").concat(String.valueOf(employeeId)))
//                .header("Auth", sharedPreferences.getString("Auth", ""))
//                .build();
//        Call call = okHttpClient.newCall(request);
//
//        call.enqueue(new Callback()
//        {
//            @Override
//            public void onFailure(@NonNull Call call, @NonNull IOException e)
//            {
//
//            }
//
//            @Override
//            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
//            {
//                if (response.isSuccessful())
//                {
//                    UserDTO userDTO = gson.fromJson(response.body().string(), UserDTO.class);
//
//                    runOnUiThread(() ->
//                    {
//                        employeeIdTv.setText(String.valueOf(userDTO.getId()));
//                        email.setText(userDTO.getEmail());
//                        firstName.setText(userDTO.getFirstName());
//                        lastName.setText(userDTO.getLastName());
//                        role.setText(userDTO.getRoleName());
//                        intervalEt.setText(String.valueOf(userDTO.getLocationRegistrationInterval()));
//                    });
//
//                    response.close();
//                }
//            }
//        });
//    }
}