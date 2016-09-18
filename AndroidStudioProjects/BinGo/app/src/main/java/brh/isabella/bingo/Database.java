package brh.isabella.bingo;

import com.firebase.client.Firebase;

import android.content.Context;
import android.location.Location;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jatin1 on 9/17/16.
 */
public class Database {
    public static Firebase db;
    public static ArrayList<Tuple<LatLng, String, Integer>> markers = new ArrayList<>();
    public static final int NEW_BIN_SCORE = 1;
    public static final int VERIFY_BIN_SCORE = 2;
    public static final int GET_BIN_VERIFIED_SCORE = 3;


    public static void markBin(final LatLng position, final String user, final Context c, final boolean isDumping) {
        //check if position is occupied
        db.child("Root").child("Bins").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean found = false;
                for (DataSnapshot elem : snapshot.getChildren()) {
                    Log.wtf("kek", "" + elem);
                    if (in5M(s2l("" + elem.getKey()), position)) {
                        int visits = Integer.parseInt("" + elem.child("Visits").getValue());
                        Log.wtf("kek", "FOUND MATCHING KEY SO INCREMENTING COUNT");
                        found = true;
                        if (isDumping) {
                            if (LoginScreen.numRecycled > 0) {
                                db.child("Root").child("Bins").child(elem.getKey()).child("Visits").setValue(visits + 1);
                                //points for YOU verifying a bin
                                int yourWinnings = VERIFY_BIN_SCORE + LoginScreen.numRecycled * 2;
                                updateScore(user, yourWinnings);
                                //points for THEM getting verified
                                updateScore("" + elem.child("Name").getValue(), GET_BIN_VERIFIED_SCORE);
                                Toast.makeText(c, "Successfully recycled to " + elem.child("Name").getValue() + "'s bin! That's " + yourWinnings + " points!", Toast.LENGTH_SHORT).show();
                                LoginScreen.numRecycled = 0;
                                MainActivity.inventory.setText("Inventory: " + LoginScreen.numRecycled);
                            } else {
                                Toast.makeText(c, "You don't have anything in your inventory!", Toast.LENGTH_SHORT).show();
                            }
                        }
                        break;
                    }
                }

                if (!found && !isDumping) {
                    String key = l2s(position);
                    Toast.makeText(c, "Successfully Tagged! +1", Toast.LENGTH_SHORT).show();

                    Log.wtf("kek", "DID NOT MATCH SO ADDING NEW USER MARKER");
                    db.child("Root").child("Bins").child(key).child("Name").setValue(user);
                    db.child("Root").child("Bins").child(key).child("Visits").setValue(1);
                    updateScore(user, NEW_BIN_SCORE);
                }

                if (!found && isDumping) {
                    Toast.makeText(c, "Sorry, there is no valid recycling bin near you!", Toast.LENGTH_SHORT).show();
                }

                if (found && !isDumping) {
                    Toast.makeText(c, "There is already a recycling bin nearby!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.wtf("HELLO", "" + firebaseError.getMessage());
            }
        });
    }

    public static void populateMarkers() {
        db.child("Root").child("Bins").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {

                for (DataSnapshot elem : snapshot.getChildren()) {
                    markers.add(new Tuple<LatLng, String, Integer>(s2l(elem.getKey()), "" + elem.child("Name").getValue(), Integer.parseInt("" + elem.child("Visits").getValue())));
                }
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });
    }

    public static void updateScore(final String user, final int inc) {
        db.child("Root").child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                int currScore = 0;
                int newScore = 0;
                for (DataSnapshot elem: snapshot.getChildren()) {
                    if (elem.getKey().equals(user)) {
                        currScore = Integer.parseInt("" + elem.child("score").getValue());
                        Log.wtf("kek", "" + currScore);
                        newScore = currScore + inc;
                        break;
                    }
                }
                Map<String, Object> toUpdate = new HashMap<>();
                toUpdate.put("score", newScore);
                db.child("Root").child("Users").child(user).updateChildren(toUpdate);
                Log.wtf("kek", "" + newScore);
                if (user.equals(LoginScreen.user)) {
                    LoginScreen.score = newScore;
                }
            }

            @Override
            public void onCancelled(FirebaseError error) {
            }
        });
    }

    public static String registerUser(final String user, final String pass) {
        db.child("Root").child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean found = false;
                for (DataSnapshot elem : snapshot.getChildren()) {
                    Log.wtf("kek", "" + elem);
                    if (elem.getKey().equals(user)) {
                        Log.wtf("kek", "FOUND MATCHING USER SO CAN'T CREATE ACCOUNT");
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    Log.wtf("kek", "FIRST ACCOUNT WITH THIS USER NAME - CREATE NEW USER");
                    Log.wtf("kek", "DID NOT MATCH SO ADDING NEW USER");
                    Map<String, Object> toUpdate = new HashMap<>();
                    toUpdate.put("password", pass);
                    toUpdate.put("score", 0);
                    db.child("Root").child("Users").child(user).updateChildren(toUpdate);
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.wtf("HELLO", "" + firebaseError.getMessage());
            }
        });
        return user;
    }

    public static String login(final String user, final String pass) {
        db.child("Root").child("Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                boolean found = false;
                for (DataSnapshot elem : snapshot.getChildren()) {
                    Log.wtf("kek", "" + elem);
                    if (elem.getKey().equals(user)) {
                        Log.wtf("kek", "FOUND MATCHING USER - validating password");
                        if (("" + elem.child("password").getValue()).equals(pass)) {
                            Log.wtf("kek", "ENTERED THE CORRECT PASSWORD!");
                            found = true;
                            break;
                        }
                    }
                }

                if (!found) {
                    Log.wtf("kek", "DID NOT FIND A MATCH - INVALID USER/PASS");
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.wtf("HELLO", "" + firebaseError.getMessage());
            }
        });
        //check if username is taken, else
        return user;
    }


    public static boolean in5M(LatLng a, LatLng b) {
        float[] result = new float[1];
        Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, result);

        return result[0] < 5;
    }

    public static String l2s (LatLng l) {
        String lat = "" + l.latitude;
        lat = lat.replace('.', ',');
        String lng = "" + l.longitude;
        lng = lng.replace('.', ',');
        return lat + " " + lng;
    }

    public static LatLng s2l (String s) {
        s = s.replace(',', '.');
        String[] latlong = s.split(" ");
        double latitude = Double.parseDouble(latlong[0]);
        double longitude = Double.parseDouble(latlong[1]);
        return new LatLng(latitude, longitude);
    }

}

