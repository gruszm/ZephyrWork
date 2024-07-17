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
import pl.gruszm.zephyrwork.DTOs.UserDTO;
import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.adapters.SubordinatesListAdapter;
import pl.gruszm.zephyrwork.callbacks.OnSubordinateDetailsClickCallback;
import pl.gruszm.zephyrwork.config.AppConfig;

public class SubordinatesListActivity extends AppCompatActivity implements OnSubordinateDetailsClickCallback
{
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subordinates_list);

        progressBar = findViewById(R.id.progress_bar);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        retrieveSubordinates();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        retrieveSubordinates();
    }

    private void retrieveSubordinates()
    {
        SharedPreferences sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        OkHttpClient okHttpClient = new OkHttpClient();
        Gson gson = new Gson();
        Request request = new Request.Builder()
                .url(AppConfig.BACKEND_BASE.concat("/users/subordinates"))
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
                if (response.code() == 200)
                {
                    Type userDTOListType = new TypeToken<ArrayList<UserDTO>>()
                    {
                    }.getType();
                    List<UserDTO> userDTOs = gson.fromJson(response.body().string(), userDTOListType);
                    SubordinatesListAdapter subordinatesListAdapter = new SubordinatesListAdapter(SubordinatesListActivity.this, userDTOs, SubordinatesListActivity.this);
                    runOnUiThread(() ->
                    {
                        progressBar.setVisibility(View.GONE);
                        recyclerView.setAdapter(subordinatesListAdapter);
                        recyclerView.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
    }

    @Override
    public void vanishSubordinatesList()
    {
        recyclerView.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
    }
}