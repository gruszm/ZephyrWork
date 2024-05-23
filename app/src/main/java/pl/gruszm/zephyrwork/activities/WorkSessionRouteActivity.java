package pl.gruszm.zephyrwork.activities;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import pl.gruszm.zephyrwork.R;

public class WorkSessionRouteActivity extends AppCompatActivity implements OnMapReadyCallback
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_work_session_route);

        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);

        if (supportMapFragment != null)
        {
            supportMapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap)
    {
        // Points
        LatLng point1 = new LatLng(37.7749, -122.4194);
        LatLng point2 = new LatLng(34.0522, -118.2437);
        LatLng point3 = new LatLng(36.1699, -115.1398);

        // Markers for points
        googleMap.addMarker(new MarkerOptions().position(point1).title("Point 1"));
        googleMap.addMarker(new MarkerOptions().position(point2).title("Point 2"));
        googleMap.addMarker(new MarkerOptions().position(point3).title("Point 3"));

        // Create polyline
        PolylineOptions polylineOptions = new PolylineOptions()
                .add(point1)
                .add(point2)
                .add(point3)
                .width(5)
                .color(Color.RED);

        // Add polyline to map
        googleMap.addPolyline(polylineOptions);

        // Move the camera to the first point
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point1, 10));
    }
}
