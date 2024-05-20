package pl.gruszm.zephyrwork.activities;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.adapters.WorkSessionListAdapter;

public class MyWorkSessionsActivity extends AppCompatActivity
{
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_work_sessions);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new WorkSessionListAdapter(this));
    }
}