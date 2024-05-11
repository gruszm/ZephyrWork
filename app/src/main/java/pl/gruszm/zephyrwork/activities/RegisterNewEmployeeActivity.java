package pl.gruszm.zephyrwork.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.config.AppConfig;
import pl.gruszm.zephyrwork.enums.RoleType;

public class RegisterNewEmployeeActivity extends AppCompatActivity
{
    private static final int MINIMUM_PASSWORD_LENGTH = 6;

    private OkHttpClient okHttpClient;
    private Gson gson;
    private EditText email, repeatEmail, firstName, lastName, password, repeatPassword;
    private Spinner roleSpinner, supervisorSpinner;
    private Button registerBtn;
    private String userRole;
    private RoleType[] rolesChoice;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_new_employee);

        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            userRole = extras.getString("user_role");

            // Set the choice of roles to those lower than user's role
            if (userRole.equals(RoleType.CEO.name()))
            {
                rolesChoice = new RoleType[]{RoleType.EMPLOYEE, RoleType.MANAGER};
            }
            else
            {
                rolesChoice = new RoleType[]{RoleType.EMPLOYEE};
            }
        }

        // Common
        okHttpClient = new OkHttpClient();
        gson = new Gson();

        // Views
        email = findViewById(R.id.registration_email_et);
        repeatEmail = findViewById(R.id.registration_repeat_email_et);
        firstName = findViewById(R.id.registration_first_name_et);
        lastName = findViewById(R.id.registration_last_name_et);
        password = findViewById(R.id.registration_password_et);
        repeatPassword = findViewById(R.id.registration_repeat_password_et);
        roleSpinner = findViewById(R.id.registration_role_spinner);
        supervisorSpinner = findViewById(R.id.registration_supervisor_spinner);
        registerBtn = findViewById(R.id.registration_register_btn);

        // OnClickListeners
        registerBtn.setOnClickListener(this::registerOnClickListener);

        // Populate the spinners with roles and supervisors
        populateSpinners();
    }

    private void registerOnClickListener(View view)
    {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        alertDialogBuilder
                .setTitle("Error")
                .setPositiveButton("OK", (dialogInterface, i) -> dialogInterface.dismiss());

        // Check, if all fields are filled
        if (email.getText().toString().isEmpty()
                || repeatEmail.getText().toString().isEmpty()
                || firstName.getText().toString().isEmpty()
                || lastName.getText().toString().isEmpty()
                || password.getText().toString().isEmpty()
                || repeatPassword.getText().toString().isEmpty())
        {
            alertDialogBuilder.setMessage("All fields must be filled.");
            alertDialogBuilder.create().show();

            return;
        }

        // Check, if the 'repeat' fields are the same as the original ones

        if (!email.getText().toString().equals(repeatEmail.getText().toString()))
        {
            alertDialogBuilder.setMessage("The email and repeat email fields are not the same.");
            alertDialogBuilder.create().show();

            return;
        }

        if (!password.getText().toString().equals(repeatPassword.getText().toString()))
        {
            alertDialogBuilder.setMessage("The password and repeat password fields are not the same.");
            alertDialogBuilder.create().show();

            return;
        }

        if (password.getText().toString().length() < MINIMUM_PASSWORD_LENGTH)
        {
            alertDialogBuilder.setMessage("The password must be at least 6 characters long.");
            alertDialogBuilder.create().show();

            return;
        }

        // TODO: Request to backend to register a new employee
    }

    private void populateSpinners()
    {
        ArrayAdapter<RoleType> roleSpinnerAdapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                rolesChoice
        );

        roleSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        roleSpinner.setAdapter(roleSpinnerAdapter);

        Request request = new Request.Builder()
                .get()
                .url(AppConfig.BACKEND_BASE.concat("/users/supervisors"))
                .build();

        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                Toast.makeText(RegisterNewEmployeeActivity.this, "Connection error. Please check your internet connection and try again.", Toast.LENGTH_SHORT).show();

                finish();
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

                    supervisorSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_expandable_list_item_1);

                    runOnUiThread(() -> supervisorSpinner.setAdapter(supervisorSpinnerAdapter));

                    response.close();
                }
            }
        });
    }
}