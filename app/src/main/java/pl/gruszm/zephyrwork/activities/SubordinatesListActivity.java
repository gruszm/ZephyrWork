package pl.gruszm.zephyrwork.activities;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.navigation.NavigationView;
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
import pl.gruszm.zephyrwork.navigation.MyOnNavigationItemSelectedListener;

public class SubordinatesListActivity extends AppCompatActivity implements OnSubordinateDetailsClickCallback
{
    private ProgressBar progressBar;
    private RecyclerView recyclerView;

    // Layout
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle toggle;
    private Toolbar toolbar;
    private NavigationView navigationView;

    // Navigation Header Views
    private TextView navFirstNameAndLastName, navEmail;
    private String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subordinates_list);

        progressBar = findViewById(R.id.progress_bar);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Layout
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        toolbar = findViewById(R.id.toolbar);
        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        // Configure navigation
        setSupportActionBar(toolbar);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        // Navigation Header Views
        navFirstNameAndLastName = navigationView.getHeaderView(0).findViewById(R.id.nav_header_name);
        navEmail = navigationView.getHeaderView(0).findViewById(R.id.nav_header_email);

        // Toolbar and navigation handling
        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();
            userRole = extras.getString("user_role");
            navFirstNameAndLastName.setText(extras.getString("nav_first_and_last_name", ""));
            navEmail.setText(extras.getString("email", ""));
        }

        MyOnNavigationItemSelectedListener itemSelectedListener = new MyOnNavigationItemSelectedListener(
                this,
                userRole,
                navFirstNameAndLastName.getText().toString(),
                navEmail.getText().toString(),
                drawerLayout
        );
        toolbar.setNavigationOnClickListener(this::navigationOnClickListener);
        navigationView.setNavigationItemSelectedListener(itemSelectedListener);

        retrieveSubordinates();
    }

    private void navigationOnClickListener(View view)
    {
        if (drawerLayout.isDrawerOpen(GravityCompat.START))
        {
            drawerLayout.closeDrawer(GravityCompat.START);
        }
        else
        {
            drawerLayout.openDrawer(GravityCompat.START);
        }
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
                    subordinatesListAdapter.setNavigationData(userRole, navFirstNameAndLastName.getText().toString(), navEmail.getText().toString());
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