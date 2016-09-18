package brh.isabella.bingo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.firebase.client.Firebase;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class LoginScreen extends AppCompatActivity {

    public static String user;
    public static String pass;
    public static int score = 0;
    public static int numRecycled = 0;
    public static ArrayList<Tuple<LatLng, String, Integer>> input;
    public static LatLng userLoc = new LatLng(42.443961, -76.501881);


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_screen);
        Firebase.setAndroidContext(this);
        Database.db = new Firebase("https://bingo-6286b.firebaseio.com/");
        Database.populateMarkers();
        input = Database.markers;
    }



    /** Start MainActivity */
    public void logIn(View view) {
        EditText usr = (EditText)findViewById(R.id.username);
        EditText pswd = (EditText)findViewById(R.id.password);

        if (usr != null) {
            user = usr.getText().toString();
        }
        if (pswd != null) {
            pass = pswd.getText().toString();
        }
        Database.login(user, pass);
        Database.updateScore(user, 0);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void register(View view) {
        EditText usr = (EditText)findViewById(R.id.username);
        EditText pswd = (EditText)findViewById(R.id.password);

        if (usr != null) {
            user = usr.getText().toString();
        }
        if (pswd != null) {
            pass = pswd.getText().toString();
        }
        Database.registerUser(user, pass);
        Database.updateScore(user, 0);
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
