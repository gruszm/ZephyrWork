package pl.gruszm.zephyrwork.activities;

import static pl.gruszm.zephyrwork.config.AppConfig.CONNECTION_ERROR_STANDARD_MSG;
import static pl.gruszm.zephyrwork.enums.RoleType.EMPLOYEE;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.gruszm.zephyrwork.DTOs.WorkSessionDTO;
import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.adapters.WorkSessionsUnderReviewAdapter;
import pl.gruszm.zephyrwork.config.AppConfig;
import pl.gruszm.zephyrwork.enums.RoleType;
import pl.gruszm.zephyrwork.enums.WorkSessionState;

public class WorkSessionsUnderReviewActivity extends AppCompatActivity
{
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private RoleType role;

    private Gson gson;
    private OkHttpClient okHttpClient;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_employees_work_sessions);

        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();

            role = RoleType.valueOf(extras.getString("role", EMPLOYEE.name()));
        }

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
                runOnUiThread(() ->
                {
                    Toast.makeText(WorkSessionsUnderReviewActivity.this, CONNECTION_ERROR_STANDARD_MSG, Toast.LENGTH_SHORT).show();

                    finish();
                });
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
                    List<WorkSessionDTO> workSessionDTOsUnderReview = workSessionDTOs.stream().filter(ws -> ws.getWorkSessionState().equals(WorkSessionState.UNDER_REVIEW)).collect(Collectors.toList());

                    runOnUiThread(() ->
                    {
                        recyclerView.setAdapter(new WorkSessionsUnderReviewAdapter(WorkSessionsUnderReviewActivity.this, workSessionDTOsUnderReview, role));
                        progressBar.setVisibility(View.GONE);
                    });

                    response.close();
                }
                else if (response.code() == 401) // Unauthorized, the token is invalid or missing
                {
                    // Show error message and redirect to Login activity
                    runOnUiThread(() ->
                    {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(WorkSessionsUnderReviewActivity.this);

                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Authorization error. Please log in and try again.");
                        alertDialogBuilder.setPositiveButton("OK", (dialogInterface, i) ->
                        {
                            Intent intent = new Intent(WorkSessionsUnderReviewActivity.this, LoginActivity.class);

                            dialogInterface.dismiss();
                            finish();
                            startActivity(intent);
                        });
                        alertDialogBuilder.create().show();
                    });
                }
            }
        });
    }
}