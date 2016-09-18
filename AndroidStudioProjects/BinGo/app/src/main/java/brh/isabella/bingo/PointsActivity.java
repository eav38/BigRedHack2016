package brh.isabella.bingo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
/**
 * Created by wisabella on 9/17/16.
 */
public class PointsActivity extends AppCompatActivity{
    private static Integer images[] = {R.drawable.baby, R.drawable.seedling, R.drawable.branch,
            R.drawable.mini, R.drawable.little, R.drawable.teenager, R.drawable.tree,
            R.drawable.mediumtree, R.drawable.largetree};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_points);

        setCurrentImage();
        TextView score = (TextView)findViewById(R.id.textView2);
        score.setText("Score: " + (LoginScreen.score));
    }

    private void setCurrentImage() {

        final ImageView imageView = (ImageView) findViewById(R.id.imageView3);
        int currImage;
        int points = LoginScreen.score;
        if (points < 20) {
            currImage = 0;
        }
        else if (points < 40) {
            currImage = 1;
        }
        else if (points < 60) {
            currImage = 2;
        }
        else if (points < 80) {
            currImage = 3;
        }
        else if (points < 100) {
            currImage = 4;
        }
        else if (points < 120) {
            currImage = 5;
        }
        else if (points < 140) {
            currImage = 6;
        }
        else if (points < 160) {
            currImage = 7;
        }
        else {
            currImage = 8;
        }

        imageView.setImageResource(images[currImage]);
        imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

    }

}
