package pl.gruszm.zephyrwork.activities;

import static pl.gruszm.zephyrwork.enums.RoleType.EMPLOYEE;

import android.os.Bundle;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.adapters.MyWorkSessionsAdapter;
import pl.gruszm.zephyrwork.enums.RoleType;

public class MyWorkSessionsActivity extends AppCompatActivity
{
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private RoleType role;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_work_sessions);

        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();

            role = RoleType.valueOf(extras.getString("role", EMPLOYEE.name()));
        }

        recyclerView = findViewById(R.id.recycler_view);
        progressBar = findViewById(R.id.progress_bar);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyWorkSessionsAdapter(this, progressBar, role));
    }
}