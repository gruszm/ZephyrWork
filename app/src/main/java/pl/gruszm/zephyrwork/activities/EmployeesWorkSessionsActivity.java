package pl.gruszm.zephyrwork.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;

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
import pl.gruszm.zephyrwork.DTOs.WorkSessionDTO;
import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.adapters.EmployeesWorkSessionsAdapter;
import pl.gruszm.zephyrwork.config.AppConfig;

public class EmployeesWorkSessionsActivity extends AppCompatActivity
{
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private Gson gson;
    private OkHttpClient okHttpClient;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employees_work_sessions);

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);

        gson = new Gson();
        okHttpClient = new OkHttpClient();
        sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        retrieveWorkSessions();
    }

    private void retrieveWorkSessions()
    {
        Request request = new Request.Builder()
                .url(AppConfig.BACKEND_BASE.concat("/worksessions/by/supervisor"))
                .get()
                .header("Auth", sharedPreferences.getString("Auth", ""))
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
                    Type listOfWorkSessionDTOsType = new TypeToken<ArrayList<WorkSessionDTO>>()
                    {
                    }.getType();

                    List<WorkSessionDTO> workSessionDTOs = gson.fromJson(response.body().string(), listOfWorkSessionDTOsType);

                    runOnUiThread(() ->
                    {
                        recyclerView.setAdapter(new EmployeesWorkSessionsAdapter(EmployeesWorkSessionsActivity.this, workSessionDTOs));
                        progressBar.setVisibility(View.GONE);
                    });

                    response.close();
                }
            }
        });
    }
}