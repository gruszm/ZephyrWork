package pl.gruszm.zephyrwork;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.gson.Gson;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.gruszm.zephyrwork.api.LoginRequest;
import pl.gruszm.zephyrwork.config.AppConfig;

public class LoginActivity extends AppCompatActivity
{
    private EditText email, password;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = findViewById(R.id.email_edit_text);
        password = findViewById(R.id.password_edit_text);
        submitButton = findViewById(R.id.login_submit_btn);

        submitButton.setOnClickListener(this::submitOnClickListener);

        validateTokenAndSwitchActivity();
    }

    private void validateTokenAndSwitchActivity()
    {
        OkHttpClient okHttpClient = new OkHttpClient();
        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        String jwt = sharedPreferences.getString("Auth", null);

        if (jwt != null)
        {
            Request request = new Request.Builder()
                    .url("http://192.168.0.100:8080/api/auth/validate")
                    .get()
                    .addHeader("Auth", jwt)
                    .build();
            Call call = okHttpClient.newCall(request);

            call.enqueue(new Callback()
            {
                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e)
                {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Connection error. Please check your internet connection and try again.", Toast.LENGTH_SHORT).show());
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
                {
                    if (response.isSuccessful())
                    {
                        runOnUiThread(() ->
                        {
                            Intent intent = new Intent(LoginActivity.this, WorkSessionActivity.class);

                            startActivity(intent);
                            finish();
                        });
                    }

                    response.close();
                }
            });
        }
    }

    private void submitOnClickListener(View view)
    {
        OkHttpClient okHttpClient = new OkHttpClient();
        Gson gson = new Gson();
        MediaType mediaTypeJson = MediaType.get("application/json");
        LoginRequest loginRequest = new LoginRequest(email.getText().toString(), password.getText().toString());
        String loginRequestJson = gson.toJson(loginRequest);
        RequestBody requestBody = RequestBody.create(loginRequestJson, mediaTypeJson);
        Request request = new Request.Builder()
                .url("http://192.168.0.100:8080/api/auth/login")
                .post(requestBody)
                .build();
        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Connection error. Please check your internet connection and try again.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                if (response.isSuccessful())
                {
                    String jwt = response.body().string();
                    SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
                    SharedPreferences.Editor editor = sharedPreferences.edit();

                    editor.putString("Auth", jwt);
                    editor.apply();

                    runOnUiThread(() ->
                    {
                        Intent intent = new Intent(LoginActivity.this, WorkSessionActivity.class);

                        startActivity(intent);
                        finish();
                    });
                }
                else
                {
                    runOnUiThread(() -> Toast.makeText(LoginActivity.this, "Invalid email or password. Please try again.", Toast.LENGTH_SHORT).show());
                }

                response.close();
            }
        });
    }
}