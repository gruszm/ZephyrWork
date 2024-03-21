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

public class MainActivity extends AppCompatActivity
{
    private Button startWorkSessionBtn;
    private TextView startWorkSessionRequestResult;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startWorkSessionBtn = findViewById(R.id.start_work_session_btn);
        startWorkSessionRequestResult = findViewById(R.id.start_work_session_request_result);

        startWorkSessionBtn.setOnClickListener(this::startWorkSessionOnClickListener);
    }

    private void startWorkSessionOnClickListener(View view)
    {
        String jwt = "eyJhbGciOiJIUzUxMiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJVc2VyRGV0YWlscyIsInVzZXJFbWFpbCI6ImFhYUBhYWEuY29tIiwidXNlclJvbGVzIjpbXSwiaWF0IjoxNzEwOTczMzg1LCJleHAiOjE3MTA5NzY5ODV9.t8bXHQ08ScC_73b0prDGJ2UkAuZIDr8TtzX4wgh7TZyaNLuD0mmt3wU0VSn6i6349vt0hm8pQefx6SW4srLwxw";
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

                runOnUiThread(() ->
                {
                    try
                    {
                        startWorkSessionRequestResult.setText(response.body().string());
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                });
            }
        });
    }
}