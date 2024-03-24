package pl.gruszm.zephyrwork;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import pl.gruszm.zephyrwork.config.AppConfig;

public class WorkSessionActivity extends AppCompatActivity
{
    private Button startWorkSessionBtn, finishWorkSessionBtn;
    private TextView workSessionResponse;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.work_session);

        startWorkSessionBtn = findViewById(R.id.start_work_session_btn);
        finishWorkSessionBtn = findViewById(R.id.finish_work_session_btn);
        workSessionResponse = findViewById(R.id.work_session_response);

        startWorkSessionBtn.setOnClickListener(this::startWorkSessionOnClickListener);
        finishWorkSessionBtn.setOnClickListener(this::finishWorkSessionOnClickListener);
    }

    private void startWorkSessionOnClickListener(View view)
    {
        OkHttpClient okHttpClient = new OkHttpClient();
        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        String jwt = sharedPreferences.getString("Auth", "");

        Request request = new Request.Builder()
                .url("http://192.168.0.100:8080/api/worksessions/start")
                .header("Auth", jwt)
                .post(RequestBody.create("", null))
                .build();

        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                runOnUiThread(() -> Toast.makeText(WorkSessionActivity.this, "Connection error. Please check your internet connection and try again.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                System.out.println("RESPONSE: " + response.code());

                if (response.isSuccessful())
                {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> workSessionResponse.setText(responseBody));
                }
                else if (response.code() == 401) // Unauthorized, the token is invalid or missing
                {
                    runOnUiThread(() ->
                    {
                        Intent intent = new Intent(WorkSessionActivity.this, LoginActivity.class);

                        startActivity(intent);
                        finish();
                    });
                }
                else if (response.code() == 400) // Bad Request, the user already has an active Work Session
                {
                    runOnUiThread(() -> Toast.makeText(WorkSessionActivity.this, "You already have an active Work Session at the moment.", Toast.LENGTH_SHORT).show());
                }

                response.close();
            }
        });
    }

    private void finishWorkSessionOnClickListener(View view)
    {
        OkHttpClient okHttpClient = new OkHttpClient();
        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        String jwt = sharedPreferences.getString("Auth", "");

        Request request = new Request.Builder()
                .url("http://192.168.0.100:8080/api/worksessions/stop")
                .header("Auth", jwt)
                .post(RequestBody.create("", null))
                .build();

        Call call = okHttpClient.newCall(request);

        call.enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                runOnUiThread(() -> Toast.makeText(WorkSessionActivity.this, "Connection error. Please check your internet connection and try again.", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                System.out.println("RESPONSE: " + response.code());

                if (response.isSuccessful())
                {
                    String responseBody = response.body().string();
                    runOnUiThread(() -> workSessionResponse.setText(responseBody));
                }
                else if (response.code() == 401) // Unauthorized, the token is invalid or missing
                {
                    runOnUiThread(() ->
                    {
                        Intent intent = new Intent(WorkSessionActivity.this, LoginActivity.class);

                        startActivity(intent);
                        finish();
                    });
                }
                else if (response.code() == 400) // Bad Request, the user does not have an active Work Session
                {
                    runOnUiThread(() -> Toast.makeText(WorkSessionActivity.this, "You do not have an active Work Session at the moment.", Toast.LENGTH_SHORT).show());
                }

                response.close();
            }
        });
    }
}