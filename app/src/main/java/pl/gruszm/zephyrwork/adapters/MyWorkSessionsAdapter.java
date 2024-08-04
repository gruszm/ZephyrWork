package pl.gruszm.zephyrwork.adapters;

import static pl.gruszm.zephyrwork.config.AppConfig.CONNECTION_ERROR_STANDARD_MSG;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.gruszm.zephyrwork.DTOs.WorkSessionDTO;
import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.activities.LoginActivity;
import pl.gruszm.zephyrwork.callbacks.OnWorkSessionUpdateCallback;
import pl.gruszm.zephyrwork.config.AppConfig;
import pl.gruszm.zephyrwork.enums.RoleType;
import pl.gruszm.zephyrwork.enums.WorkSessionState;
import pl.gruszm.zephyrwork.viewholders.WorkSessionViewHolder;

public class MyWorkSessionsAdapter extends RecyclerView.Adapter<WorkSessionViewHolder> implements OnWorkSessionUpdateCallback
{
    // Common
    private Activity activity;
    private Gson gson;
    private OkHttpClient okHttpClient;
    private SharedPreferences sharedPreferences;
    private DateTimeFormatter formatter;
    private RoleType role;

    // List of Work Sessions
    private List<WorkSessionDTO> workSessionDTOs;
    private List<WorkSessionDTO> filteredWorkSessionDTOs;
    private WorkSessionState workSessionFilter;
    private LocalDateTime startingDateFilter;
    private LocalDateTime endingDateFilter;

    // Work Session State Display
    private Map<WorkSessionState, String> workSessionNames;
    private Map<WorkSessionState, Integer> workSessionColors;

    public MyWorkSessionsAdapter(Activity activity, ProgressBar progressBar, RoleType role)
    {
        this.activity = activity;
        this.role = role;

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
        filteredWorkSessionDTOs = new ArrayList<>();
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
                activity.runOnUiThread(() ->
                {
                    Toast.makeText(activity, CONNECTION_ERROR_STANDARD_MSG, Toast.LENGTH_SHORT).show();

                    activity.finish();
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

                    workSessionDTOs = gson.fromJson(response.body().string(), listOfWorkSessionDTOsType);
                    filteredWorkSessionDTOs = workSessionDTOs;

                    activity.runOnUiThread(() ->
                    {
                        notifyDataSetChanged();
                        progressBar.setVisibility(View.GONE);
                    });

                    response.close();
                }
                else if (response.code() == 401) // Unauthorized, the token is invalid or missing
                {
                    // Show error message and redirect to Login activity
                    activity.runOnUiThread(() ->
                    {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(activity);

                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Authorization error. Please log in and try again.");
                        alertDialogBuilder.setPositiveButton("OK", (dialogInterface, i) ->
                        {
                            Intent intent = new Intent(activity, LoginActivity.class);

                            dialogInterface.dismiss();
                            activity.finish();
                            activity.startActivity(intent);
                        });
                        alertDialogBuilder.create().show();
                    });
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
        WorkSessionDTO workSessionDTO = filteredWorkSessionDTOs.get(position);

        String startTime = "Start: " + LocalDateTime.parse(workSessionDTO.getStartTime()).plusHours(2).format(formatter);
        String endTime = "";

        if (workSessionDTO.getEndTime() != null)
        {
            endTime = "End: " + LocalDateTime.parse(workSessionDTO.getEndTime()).plusHours(2).format(formatter);
        }

        holder.setWorkSessionState(workSessionDTO.getWorkSessionState());
        holder.setActivityAndSharedPreferences(activity);
        holder.setWorkSessionId(workSessionDTO.getId());
        holder.setOnWorkSessionUpdateCallback(this);
        holder.firstNameAndLastNameTv.setText(workSessionDTO.getEmployeeName());
        holder.startingDateTv.setText(startTime);
        holder.endingDateTv.setText(endTime);
        holder.setNotesFromSupervisor(workSessionDTO.getNotesFromSupervisor());
        holder.setNotesFromEmployee(workSessionDTO.getNotesFromEmployee());
        holder.state.setText(workSessionNames.get(workSessionDTO.getWorkSessionState()));
        holder.state.setTextColor(ContextCompat.getColor(activity, workSessionColors.get(workSessionDTO.getWorkSessionState())));
    }

    @Override
    public int getItemCount()
    {
        return filteredWorkSessionDTOs.size();
    }

    @Override
    public void removeWorkSession(int workSessionId)
    {
        List<WorkSessionDTO> workSessionToRemove = filteredWorkSessionDTOs.stream().filter(ws -> (ws.getId() == workSessionId)).collect(Collectors.toList());

        if (workSessionToRemove.size() == 0)
        {
            return;
        }

        int positionToRemove = filteredWorkSessionDTOs.indexOf(workSessionToRemove.get(0));

        WorkSessionDTO workSessionRemoved = filteredWorkSessionDTOs.remove(positionToRemove);
        workSessionDTOs.remove(workSessionRemoved);
        activity.runOnUiThread(() -> notifyItemRemoved(positionToRemove));
    }

    @Override
    public void updateWorkSessionState(int workSessionId, WorkSessionState workSessionState)
    {
        List<WorkSessionDTO> workSessionToUpdate = filteredWorkSessionDTOs.stream().filter(ws -> (ws.getId() == workSessionId)).collect(Collectors.toList());

        if (workSessionToUpdate.size() == 0)
        {
            return;
        }

        int positionToUpdate = filteredWorkSessionDTOs.indexOf(workSessionToUpdate.get(0));

        workSessionToUpdate.get(0).setWorkSessionState(workSessionState);
        activity.runOnUiThread(() -> notifyItemChanged(positionToUpdate));

        applyFilters();
    }

    @Override
    public void updateNotesFromEmployee(int workSessionId, String notesFromEmployee)
    {
        List<WorkSessionDTO> workSessionToUpdate = filteredWorkSessionDTOs.stream().filter(ws -> (ws.getId() == workSessionId)).collect(Collectors.toList());

        if (workSessionToUpdate.size() == 0)
        {
            return;
        }

        int positionToUpdate = filteredWorkSessionDTOs.indexOf(workSessionToUpdate.get(0));

        workSessionToUpdate.get(0).setNotesFromEmployee(notesFromEmployee);
        activity.runOnUiThread(() -> notifyItemChanged(positionToUpdate));
    }

    @Override
    public void updateNotesFromSupervisor(int workSessionId, String notesFromSupervisor)
    {
        List<WorkSessionDTO> workSessionToUpdate = filteredWorkSessionDTOs.stream().filter(ws -> (ws.getId() == workSessionId)).collect(Collectors.toList());

        if (workSessionToUpdate.size() == 0)
        {
            return;
        }

        int positionToUpdate = filteredWorkSessionDTOs.indexOf(workSessionToUpdate.get(0));

        workSessionToUpdate.get(0).setNotesFromSupervisor(notesFromSupervisor);
        workSessionToUpdate.get(0).setNotesFromEmployee(null);
        activity.runOnUiThread(() -> notifyItemChanged(positionToUpdate));
    }

    public void setWorkSessionFilter(WorkSessionState workSessionFilter)
    {
        this.workSessionFilter = workSessionFilter;

        applyFilters();
    }

    public void setStartingDateFilter(int year, int month, int dayOfMonth)
    {
        LocalDateTime startingDateFilter = LocalDateTime.of(year, month + 1, dayOfMonth, 0, 0);
        this.startingDateFilter = startingDateFilter;

        applyFilters();
    }

    public void setEndingDateFilter(int year, int month, int dayOfMonth)
    {
        LocalDateTime endingDateFilter = LocalDateTime.of(year, month + 1, dayOfMonth, 23, 59);
        this.endingDateFilter = endingDateFilter;

        applyFilters();
    }

    private void applyFilters()
    {
        Stream<WorkSessionDTO> stream = workSessionDTOs.stream();

        stream = (this.workSessionFilter != null) ? (stream.filter(ws -> ws.getWorkSessionState().equals(this.workSessionFilter))) : stream;
        stream = (this.startingDateFilter != null) ? (stream.filter(ws -> LocalDateTime.parse(ws.getStartTime()).isAfter(this.startingDateFilter))) : stream;
        stream = (this.endingDateFilter != null) ? (stream.filter(ws -> LocalDateTime.parse(ws.getStartTime()).isBefore(this.endingDateFilter))) : stream;

        filteredWorkSessionDTOs = stream.collect(Collectors.toList());

        activity.runOnUiThread(() -> notifyDataSetChanged());
    }

    public void clearDatesFilter()
    {
        startingDateFilter = null;
        endingDateFilter = null;

        applyFilters();
    }
}
