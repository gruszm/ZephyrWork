package pl.gruszm.zephyrwork;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class WorkSessionActivity extends AppCompatActivity
{
    private Button startWorkSessionBtn, finishWorkSessionBtn;
    private TextView workSessionResponse;
    private String jwt;

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

        jwt = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJVc2VyRGV0YWlscyIsInVzZXJFbWFpbCI6ImFhYUBhYWEuY29tIiwidXNlclJvbGVzIjpbXSwiaWF0IjoxNzExMDQzNjI2LCJleHAiOjE3MTEwNDcyMjZ9.hHpI12ZOwQM3yOQlXCSw3vU-b1CFlM8q9JaRTlpZXo7iLYeQ5v8FcAae4xN1elTk7Bd7xjSCRYUEa6Rd83oPLA";
    }

    private void startWorkSessionOnClickListener(View view)
    {
        OkHttpClient okHttpClient = new OkHttpClient();

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
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
            {
                System.out.println("RESPONSE: " + response.code());
                final String responseBody;

                try
                {
                    responseBody = response.body().string();
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }

                runOnUiThread(() -> workSessionResponse.setText(responseBody));

                response.close();
            }
        });
    }

    private void finishWorkSessionOnClickListener(View view)
    {
        OkHttpClient okHttpClient = new OkHttpClient();

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
                e.printStackTrace();
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response)
            {
                System.out.println("RESPONSE: " + response.code());
                final String responseBody;

                try
                {
                    responseBody = response.body().string();
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }

                runOnUiThread(() -> workSessionResponse.setText(responseBody));

                response.close();
            }
        });
    }
}