package pl.gruszm.zephyrwork.activities;

import static pl.gruszm.zephyrwork.config.AppConfig.CONNECTION_ERROR_STANDARD_MSG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

public class UserProfileActivity extends AppCompatActivity
{
    private OkHttpClient okHttpClient;
    private Gson gson;
    private SharedPreferences sharedPreferences;
    private TextView firstName, lastName, email, role, supervisorFirstName, supervisorLastName, supervisorEmail;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        okHttpClient = new OkHttpClient();
        gson = new Gson();
        sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);

        firstName = findViewById(R.id.user_profile_first_name_edit);
        lastName = findViewById(R.id.user_profile_last_name_edit);
        email = findViewById(R.id.user_profile_email_edit);
        role = findViewById(R.id.user_profile_role_edit);
        supervisorFirstName = findViewById(R.id.user_profile_supervisor_first_name_edit);
        supervisorLastName = findViewById(R.id.user_profile_supervisor_last_name_edit);
        supervisorEmail = findViewById(R.id.user_profile_supervisor_email_edit);

        retrieveUserData();
    }

    private void logoutOnClickListener(View view)
    {
        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Intent intent = new Intent(this, LoginActivity.class);

        editor.remove("Auth");
        editor.apply();

        finish();
        startActivity(intent);
    }

    private void retrieveUserData()
    {
        String jwt = sharedPreferences.getString("Auth", "");

        Request userDataRequest = new Request.Builder()
                .url(AppConfig.BACKEND_BASE.concat("/users/token"))
                .header("Auth", jwt)
                .build();

        okHttpClient.newCall(userDataRequest).enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                runOnUiThread(() ->
                {
                    Toast.makeText(UserProfileActivity.this, CONNECTION_ERROR_STANDARD_MSG, Toast.LENGTH_SHORT).show();

                    finish();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                if (response.isSuccessful())
                {
                    UserDTO userDTO = gson.fromJson(response.body().string(), UserDTO.class);

                    runOnUiThread(() ->
                    {
                        firstName.setText(userDTO.getFirstName());
                        lastName.setText(userDTO.getLastName());
                        email.setText(userDTO.getEmail());
                        role.setText(userDTO.getRoleName());
                    });

                    retrieveSupervisorData();

                    response.close();
                }
                else if (response.code() == 401) // Unauthorized, the token is invalid or missing
                {
                    runOnUiThread(() ->
                    {
                        Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);

                        // Show error message and redirect to Login activity
                        Toast.makeText(UserProfileActivity.this, "Authorization error. Please log in and try again.", Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(intent);
                    });
                }
            }
        });
    }

    private void retrieveSupervisorData()
    {
        String jwt = sharedPreferences.getString("Auth", "");

        Request supervisorDataRequest = new Request.Builder()
                .url(AppConfig.BACKEND_BASE.concat("/users/supervisor/token"))
                .header("Auth", jwt)
                .build();

        okHttpClient.newCall(supervisorDataRequest).enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                runOnUiThread(() ->
                {
                    Toast.makeText(UserProfileActivity.this, CONNECTION_ERROR_STANDARD_MSG, Toast.LENGTH_SHORT).show();

                    finish();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                if (response.isSuccessful())
                {
                    UserDTO supervisorDto = gson.fromJson(response.body().string(), UserDTO.class);

                    runOnUiThread(() ->
                    {
                        supervisorFirstName.setText(supervisorDto.getFirstName());
                        supervisorLastName.setText(supervisorDto.getLastName());
                        supervisorEmail.setText(supervisorDto.getEmail());
                    });

                    response.close();
                }
                else if (response.code() == 401) // Unauthorized, the token is invalid or missing
                {
                    runOnUiThread(() ->
                    {
                        Intent intent = new Intent(UserProfileActivity.this, LoginActivity.class);

                        // Show error message and redirect to Login activity
                        Toast.makeText(UserProfileActivity.this, "Authorization error. Please log in and try again.", Toast.LENGTH_SHORT).show();
                        finish();
                        startActivity(intent);
                    });
                }
                else if (response.code() == 404) // User does not have a supervisor
                {
                    runOnUiThread(() ->
                    {
                        supervisorFirstName.setText("NOT ASSIGNED");
                        supervisorLastName.setText("NOT ASSIGNED");
                        supervisorEmail.setText("NOT ASSIGNED");
                    });
                }
            }
        });
    }
}