package pl.gruszm.zephyrwork;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.gruszm.zephyrwork.DTOs.UserDTO;
import pl.gruszm.zephyrwork.enums.RoleType;

public class RegisterNewEmployeeActivity extends AppCompatActivity
{
    private OkHttpClient okHttpClient;
    private Gson gson;
    private EditText email, repeatEmail, firstName, lastName, password, repeatPassword;
    private Spinner roleSpinner, supervisorSpinner;
    private Button registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_new_employee);

        okHttpClient = new OkHttpClient();
        gson = new Gson();

        email = findViewById(R.id.registration_email_et);
        repeatEmail = findViewById(R.id.registration_repeat_email_et);
        firstName = findViewById(R.id.registration_first_name_et);
        lastName = findViewById(R.id.registration_last_name_et);
        password = findViewById(R.id.registration_password_et);
        repeatPassword = findViewById(R.id.registration_repeat_password_et);
        roleSpinner = findViewById(R.id.registration_role_spinner);
        supervisorSpinner = findViewById(R.id.registration_supervisor_spinner);
        registerBtn = findViewById(R.id.registration_register_btn);

        populateSpinners();
    }

    private void populateSpinners()
    {
        ArrayAdapter<RoleType> roleSpinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                RoleType.values()
        );

        roleSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        roleSpinner.setAdapter(roleSpinnerAdapter);

        Request request = new Request.Builder()
                .get()
                .url("https://zephyrwork.onrender.com/api/users/supervisors")
                .build();

        Call call = okHttpClient.newCall(request);

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
                    Type listType = new TypeToken<ArrayList<UserDTO>>()
                    {
                    }.getType();
                    List<UserDTO> supervisorsDTOs = gson.fromJson(response.body().string(), listType);
                    ArrayAdapter<UserDTO> supervisorSpinnerAdapter;

                    supervisorSpinnerAdapter = new ArrayAdapter<>(
                            RegisterNewEmployeeActivity.this,
                            android.R.layout.simple_spinner_item,
                            supervisorsDTOs
                    );

                    supervisorSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    runOnUiThread(() -> supervisorSpinner.setAdapter(supervisorSpinnerAdapter));

                    response.close();
                }
            }
        });
    }
}