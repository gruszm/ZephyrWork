package pl.gruszm.zephyrwork.activities;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

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
                runOnUiThread(() -> Toast.makeText(WorkSessionRouteActivity.this, "FAILURE", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                runOnUiThread(() -> Toast.makeText(WorkSessionRouteActivity.this, String.valueOf(response.code()), Toast.LENGTH_SHORT).show());

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

        // Create markers
        List<MarkerOptions> markersList = pointsOnMap.stream().map(p -> new MarkerOptions().position(p)).collect(Collectors.toList());

        // Create polyline
        PolylineOptions polylineOptions = new PolylineOptions()
                .width(5)
                .color(Color.RED);

        markersList.forEach(m -> polylineOptions.add(m.getPosition()));

        // Set the first marker's title
        markersList.get(0).title("START");

        // Only set the last marker's title, if there are 2 or more
        if (markersList.size() > 1)
        {
            markersList.get(markersList.size() - 1).title("END");
        }

        // Add markers to map
        markersList.forEach(m -> googleMap.addMarker(m));

        // Add polyline to map
        googleMap.addPolyline(polylineOptions);

        // Move the camera to the first point
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(pointsOnMap.get(0), 10));
    }
}
