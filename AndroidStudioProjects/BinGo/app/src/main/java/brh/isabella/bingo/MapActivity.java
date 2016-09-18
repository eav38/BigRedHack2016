package brh.isabella.bingo;

import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;


import com.firebase.client.Firebase;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;



public class MapActivity extends FragmentActivity implements OnMapReadyCallback {

    public final static int INIT_BIN_SIZE = 1;
    public final static int SECOND_BIN_SIZE = 5;
    public final static int THIRD_BIN_SIZE = 12;
    public final static int FINAL_BIN_SIZE = 25;

    private GoogleMap mMap;
    public static LatLng userLoc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        ArrayList<Tuple<LatLng, String, Integer>> input = LoginScreen.input;
        Log.wtf("kek", "" + input.size());

        userLoc = LoginScreen.userLoc;

        CameraUpdate center = CameraUpdateFactory.newLatLng(userLoc);
        CameraUpdate zoom = CameraUpdateFactory.zoomTo(20);

        mMap.moveCamera(center);
        mMap.animateCamera(zoom);

        populateMap(input, userLoc);
    }

    public void populateMap(ArrayList<Tuple<LatLng, String, Integer>> input, LatLng curr) {
        for (Tuple t: input) {
            int path = determineMarkerDisplay((int) t.visits);
            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(path);
            mMap.addMarker(new MarkerOptions().position((LatLng) t.loc).title("Found By: " + t.name + "; " + t.visits + " ; " + distanceBetween(curr, (LatLng) t.loc) + " meters away").icon(icon));


        }
    }

    public int determineMarkerDisplay(int visits) {

        if (isBetween(visits, INIT_BIN_SIZE, SECOND_BIN_SIZE)) {
            return R.drawable.bin1;
        } else if (isBetween(visits, SECOND_BIN_SIZE, THIRD_BIN_SIZE)) {
            return R.drawable.bin2;
        } else if (isBetween(visits, THIRD_BIN_SIZE, FINAL_BIN_SIZE)) {
            return R.drawable.bin3;
        } else {
            return R.drawable.bin4;
        }
    }

    public float distanceBetween(LatLng a, LatLng b) {
        float[] result = new float[1];
        Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, result);
        return result[0];
    }

    public static boolean isBetween(int x, int lower, int upper) {
        return lower <= x && x < upper;
    }

    public void configureMap(LatLngBounds bounds) {
        CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, 0);
        mMap.moveCamera(cu);
    }
}