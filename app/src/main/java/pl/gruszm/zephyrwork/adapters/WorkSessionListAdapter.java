package pl.gruszm.zephyrwork.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

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
import pl.gruszm.zephyrwork.config.AppConfig;
import pl.gruszm.zephyrwork.viewholders.WorkSessionViewHolder;

public class WorkSessionListAdapter extends RecyclerView.Adapter<WorkSessionViewHolder>
{
    // Common
    private Activity activity;
    private Gson gson;
    private OkHttpClient okHttpClient;
    private SharedPreferences sharedPreferences;

    // List of work sessions
    private List<WorkSessionDTO> workSessionDTOs;

    public WorkSessionListAdapter(Activity activity)
    {
        this.activity = activity;

        gson = new Gson();
        okHttpClient = new OkHttpClient();
        sharedPreferences = activity.getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        workSessionDTOs = new ArrayList<>();

        retrieveWorkSessions();
    }

    private void retrieveWorkSessions()
    {
        Request request = new Request.Builder()
                .get()
                .url(AppConfig.BACKEND_BASE.concat("/worksessions/user/token"))
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

                    workSessionDTOs = gson.fromJson(response.body().string(), listOfWorkSessionDTOsType);

                    activity.runOnUiThread(() -> notifyDataSetChanged());

                    response.close();
                }
            }
        });
    }

    @NonNull
    @Override
    public WorkSessionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.work_session_item, parent, false);

        return new WorkSessionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull WorkSessionViewHolder holder, int position)
    {
        WorkSessionDTO workSessionDTO = workSessionDTOs.get(position);

        holder.setWorkSessionId(workSessionDTO.getId());
        holder.startingDateTv.setText(workSessionDTO.getStartTime());
        holder.endingDateTv.setText(workSessionDTO.getEndTime());
    }

    @Override
    public int getItemCount()
    {
        return workSessionDTOs.size();
    }
}
