package pl.gruszm.zephyrwork.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.gruszm.zephyrwork.DTOs.WorkSessionDTO;
import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.config.AppConfig;
import pl.gruszm.zephyrwork.enums.WorkSessionState;
import pl.gruszm.zephyrwork.viewholders.WorkSessionViewHolder;

public class WorkSessionListAdapter extends RecyclerView.Adapter<WorkSessionViewHolder>
{
    // Common
    private Activity activity;
    private Gson gson;
    private OkHttpClient okHttpClient;
    private SharedPreferences sharedPreferences;
    private DateTimeFormatter formatter;

    // List of work sessions
    private List<WorkSessionDTO> workSessionDTOs;

    // Work Session State Display
    private Map<WorkSessionState, String> workSessionNames;
    private Map<WorkSessionState, Integer> workSessionColors;

    public WorkSessionListAdapter(Activity activity, ProgressBar progressBar)
    {
        this.activity = activity;

        // Work Session State Display
        workSessionNames = new HashMap<>();
        workSessionColors = new HashMap<>();

        workSessionNames.put(WorkSessionState.IN_PROGRESS, "IN PROGRESS");
        workSessionNames.put(WorkSessionState.UNDER_REVIEW, "UNDER REVIEW");
        workSessionNames.put(WorkSessionState.APPROVED, "APPROVED");
        workSessionNames.put(WorkSessionState.RETURNED, "RETURNED");
        workSessionNames.put(WorkSessionState.CANCELLED, "CANCELLED");

        workSessionColors.put(WorkSessionState.IN_PROGRESS, R.color.yellow); // Yellow
        workSessionColors.put(WorkSessionState.UNDER_REVIEW, R.color.blue); // Blue
        workSessionColors.put(WorkSessionState.APPROVED, R.color.green); // Green
        workSessionColors.put(WorkSessionState.RETURNED, R.color.magenta); // Magenta
        workSessionColors.put(WorkSessionState.CANCELLED, R.color.red); // Red

        // Common
        gson = new Gson();
        okHttpClient = new OkHttpClient();
        sharedPreferences = activity.getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, Context.MODE_PRIVATE);
        workSessionDTOs = new ArrayList<>();
        formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

        // Retrieve Work Sessions
        retrieveWorkSessions(progressBar);
    }

    private void retrieveWorkSessions(ProgressBar progressBar)
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

                    activity.runOnUiThread(() ->
                    {
                        notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    });

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

        String startTime = "Start: " + LocalDateTime.parse(workSessionDTO.getStartTime()).format(formatter).toString();
        String endTime = "End: " + LocalDateTime.parse(workSessionDTO.getEndTime()).format(formatter).toString();

        holder.setWorkSessionId(workSessionDTO.getId());
        holder.firstNameAndLastNameTv.setText(workSessionDTO.getEmployeeName());
        holder.startingDateTv.setText(startTime);
        holder.endingDateTv.setText(endTime);
        holder.state.setText(workSessionNames.get(workSessionDTO.getWorkSessionState()));
        holder.state.setTextColor(ContextCompat.getColor(activity, workSessionColors.get(workSessionDTO.getWorkSessionState())));
    }

    @Override
    public int getItemCount()
    {
        return workSessionDTOs.size();
    }
}
