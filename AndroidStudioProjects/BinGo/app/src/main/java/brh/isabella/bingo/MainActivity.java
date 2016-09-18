package brh.isabella.bingo;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static TextView inventory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton next = (ImageButton) findViewById(R.id.imageButton);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent myIntent = new Intent(view.getContext(), PointsActivity.class);
                startActivityForResult(myIntent, 0);
            }
        });

        inventory = (TextView) findViewById(R.id.textView);
    }

    /** Start ItemCamera */
    public void itemCamera(View view) {
        Intent intent = new Intent(this, AnalyzeActivity.class);
        startActivity(intent);
    }

    /** Start BinCamera */
    public void tagBin(final View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                this);
        alertDialogBuilder.setTitle("Bin It!");
        alertDialogBuilder
                .setCancelable(true)
                .setPositiveButton("Tag New Bin!",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        Database.markBin(LoginScreen.userLoc, LoginScreen.user, getApplicationContext(), false);
                        dialog.cancel();
                    }
                })
                .setNegativeButton("Recycle inventory here!",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // do nothing
                        Database.markBin(LoginScreen.userLoc, LoginScreen.user, getApplicationContext(), true);
                        dialog.cancel();
                    }
                });
        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /** Start MapActivity */
    public void viewMap(View view) {
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    public void viewMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
