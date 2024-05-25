package pl.gruszm.zephyrwork.activities;

import static pl.gruszm.zephyrwork.config.AppConfig.CONNECTION_ERROR_STANDARD_MSG;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.maps.android.clustering.ClusterItem;
import com.google.maps.android.clustering.ClusterManager;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.List;
import java.util.stream.Collectors;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import pl.gruszm.zephyrwork.DTOs.LocationDTO;
import pl.gruszm.zephyrwork.R;
import pl.gruszm.zephyrwork.config.AppConfig;

public class WorkSessionRouteActivity extends AppCompatActivity implements OnMapReadyCallback
{
    private int workSessionId;
    private Gson gson;
    private OkHttpClient okHttpClient;
    private SharedPreferences sharedPreferences;
    private List<LatLng> pointsOnMap;
    private SupportMapFragment supportMapFragment;
    private ClusterManager<ClusterMarker> clusterManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_session_route);

        if (savedInstanceState == null)
        {
            Bundle extras = getIntent().getExtras();

            workSessionId = extras.getInt("workSessionId");
        }

        gson = new Gson();
        okHttpClient = new OkHttpClient();
        sharedPreferences = getSharedPreferences(AppConfig.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
        supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        retrieveLocations();
    }

    private void retrieveLocations()
    {
        Request request = new Request.Builder()
                .url(AppConfig.BACKEND_BASE.concat("/locations/worksession/").concat(String.valueOf(workSessionId)))
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
                    Toast.makeText(WorkSessionRouteActivity.this, CONNECTION_ERROR_STANDARD_MSG, Toast.LENGTH_SHORT).show();

                    finish();
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                if (response.isSuccessful())
                {
                    Type locationsListType = new TypeToken<List<LocationDTO>>()
                    {
                    }.getType();

                    List<LocationDTO> locationDTOs = gson.fromJson(response.body().string(), locationsListType);
                    pointsOnMap = locationDTOs.stream().map(loc -> new LatLng(loc.getLatitude(), loc.getLongitude())).collect(Collectors.toList());

                    runOnUiThread(() -> supportMapFragment.getMapAsync(WorkSessionRouteActivity.this));

                    response.close();
                }
                else if (response.code() == 401) // Unauthorized, the token is invalid or missing
                {
                    // Show error message and redirect to Login activity
                    runOnUiThread(() ->
                    {
                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(WorkSessionRouteActivity.this);

                        alertDialogBuilder.setTitle("Error");
                        alertDialogBuilder.setMessage("Authorization error. Please log in and try again.");
                        alertDialogBuilder.setPositiveButton("OK", (dialogInterface, i) ->
                        {
                            Intent intent = new Intent(WorkSessionRouteActivity.this, LoginActivity.class);

                            dialogInterface.dismiss();
                            finish();
                            startActivity(intent);
                        });
                        alertDialogBuilder.create().show();
                    });
                }
                else
                {
                    runOnUiThread(() -> Toast.makeText(WorkSessionRouteActivity.this, "Error while creating the route. Try again later.", Toast.LENGTH_SHORT).show());
                }
            }
        });
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap)
    {
        // If there are no points, then do not proceed with the route
        if (pointsOnMap.isEmpty())
        {
            return;
        }

        // Initialize the cluster manager
        clusterManager = new ClusterManager<>(this, googleMap);
        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);

        // Add items to the cluster manager
        List<ClusterMarker> clusterMarkers = pointsOnMap.stream().map(point -> new ClusterMarker(point.latitude, point.longitude)).collect(Collectors.toList());
        clusterMarkers.get(0).setTitle("START");

        // Only set the last marker's title, if there are at least 2 markers
        if (clusterMarkers.size() > 1)
        {
            clusterMarkers.get(clusterMarkers.size() - 1).setTitle("END");
        }

        clusterManager.addItems(clusterMarkers);
        clusterManager.cluster();

        // Create polyline
        PolylineOptions polylineOptions = new PolylineOptions()
                .width(5)
                .color(Color.RED);

        pointsOnMap.forEach(point -> polylineOptions.add(point));

        // Add polyline to map
        googleMap.addPolyline(polylineOptions);

        // Move the camera to the first point
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pointsOnMap.get(0), 11));
    }

    private static class ClusterMarker implements ClusterItem
    {
        private LatLng position;
        private String title;
        private String snippet;

        public ClusterMarker(double latitude, double longitude)
        {
            this.position = new LatLng(latitude, longitude);
        }

        public void setPosition(LatLng position)
        {
            this.position = position;
        }

        public void setTitle(String title)
        {
            this.title = title;
        }

        public void setSnippet(String snippet)
        {
            this.snippet = snippet;
        }

        @NonNull
        @Override
        public LatLng getPosition()
        {
            return position;
        }

        @Nullable
        @Override
        public String getTitle()
        {
            return title;
        }

        @Nullable
        @Override
        public String getSnippet()
        {
            return snippet;
        }

        @Nullable
        @Override
        public Float getZIndex()
        {
            return null;
        }
    }
}
