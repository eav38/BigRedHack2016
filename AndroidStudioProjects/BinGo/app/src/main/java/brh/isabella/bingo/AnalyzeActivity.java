//
// Copyright (c) Microsoft. All rights reserved.
// Licensed under the MIT license.
//
// Microsoft Cognitive Services (formerly Project Oxford): https://www.microsoft.com/cognitive-services
//
// Microsoft Cognitive Services (formerly Project Oxford) GitHub:
// https://github.com/Microsoft/Cognitive-Vision-Android
//
// Copyright (c) Microsoft Corporation
// All rights reserved.
//
// MIT License:
// Permission is hereby granted, free of charge, to any person obtaining
// a copy of this software and associated documentation files (the
// "Software"), to deal in the Software without restriction, including
// without limitation the rights to use, copy, modify, merge, publish,
// distribute, sublicense, and/or sell copies of the Software, and to
// permit persons to whom the Software is furnished to do so, subject to
// the following conditions:
//
// The above copyright notice and this permission notice shall be
// included in all copies or substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED ""AS IS"", WITHOUT WARRANTY OF ANY KIND,
// EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
// MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
// LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
// OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
// WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
package brh.isabella.bingo;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.microsoft.projectoxford.vision.VisionServiceClient;
import com.microsoft.projectoxford.vision.VisionServiceRestClient;
import com.microsoft.projectoxford.vision.contract.AnalysisResult;
import com.microsoft.projectoxford.vision.contract.Caption;
import com.microsoft.projectoxford.vision.contract.Category;
import com.microsoft.projectoxford.vision.contract.Tag;
import com.microsoft.projectoxford.vision.rest.VisionServiceException;
import brh.isabella.bingo.helper.ImageHelper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.ArrayList;
import java.util.Collections;

public class AnalyzeActivity extends Activity {

    public enum Recylable {
        PLASTIC, GLASS, CAN, PAPER, UNKNOWN
    }

    // Flag to indicate which task is to be performed.
    private static final int REQUEST_SELECT_IMAGE = 0;

    // The button to select an image
    private ImageButton mButtonSelectImage;

    // The URI of the image selected to detect.
    private Uri mImageUri;
    private Uri tempImage;

    // The image selected to detect.
    private Bitmap mBitmap;


    private CountDownLatch asyncCounter;

    private VisionServiceClient client;


    protected Boolean checkRecyclable(List<Category> categoryList, List<Tag> tagList) {

        for (Category cat : categoryList) {
            if (cat.name.equals("drink_can") && cat.score > 0.8) {
                return true;
            }
            if (cat.name.equals("drink_") && cat.score > .7) {
                return true;
            }
            if (cat.name.equals("text_menu") && cat.score > .6) {
                return true;
            }
        }

        for (Tag tag : tagList) {
            if (tag.name.equals("plastic") && tag.confidence > .25) {
                return true;
            }
            if (tag.name.equals("glass") && tag.confidence > .25) {
                return true;
            }
            if (tag.name.equals("bottle") && tag.confidence > .8) {
                return true;
            }
            if (tag.name.equals("beverage") && tag.confidence > .4) {
                return true;
            }
            if (tag.name.equals("can") && tag.confidence > .2) {
                return true;
            }
            if (tag.name.equals("cup") && tag.confidence > .9) {
                return true;
            }
            if (tag.name.equals("paper") && tag.confidence > .2) {
                return true;
            }
            if (tag.name.equals("document") && tag.confidence > .3) {
                return true;
            }
        }

        return false;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_camera);

        if (client == null) {
            client = new VisionServiceRestClient(getString(R.string.subscription_key));
        }

        mButtonSelectImage = (ImageButton) findViewById(R.id.selectImage);
        mButtonSelectImage.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                takePhoto();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_analyze, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    public void doAnalyze() {
        mButtonSelectImage.setEnabled(false);
        Toast.makeText(getApplicationContext(), "Analyzing Image...", Toast.LENGTH_LONG).show();
        try {
            //asyncCounter = new CountDownLatch(1);
            new doCategoryRequest().execute();
            //new doTagRequest().execute();
        } catch (Exception e) {
        }
    }

    public void takePhoto() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (intent.resolveActivity(getPackageManager()) != null) {

            // Save the photo taken to a temporary file.
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            try {
                File file = File.createTempFile("IMG_", ".jpg", storageDir);
                tempImage = Uri.fromFile(file);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, tempImage);
                startActivityForResult(intent, REQUEST_SELECT_IMAGE);
                Log.i("takePhoto", "Photo taken");
            } catch (IOException e) {
                Log.wtf("kek", e.getMessage());
            }
        }
    }

    // Called when image selection is done.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d("AnalyzeActivity", "onActivityResult");
        switch (requestCode) {
            case REQUEST_SELECT_IMAGE:
                Log.i("REQUEST_SELECT_IMAGE", String.valueOf(resultCode));
                if (resultCode == RESULT_OK) {
                    Log.i("ResultOK", "InResultCode");
                    // If image is selected successfully, set the image URI and bitmap.
                    if (data == null || data.getData() == null) {
                        mImageUri = tempImage;
                    } else {
                        mImageUri = data.getData();
                    }

                    mBitmap = ImageHelper.loadSizeLimitedBitmapFromUri(
                            mImageUri, getContentResolver());
                    if (mBitmap != null) {
                        // Show the image on screen.
                        ImageView imageView = (ImageView) findViewById(R.id.selectedImage);
                        imageView.setImageBitmap(mBitmap);

                        // Add detection log.
                        Log.d("AnalyzeActivity", "Image: " + mImageUri + " resized to " + mBitmap.getWidth()
                                + "x" + mBitmap.getHeight());

                        doAnalyze();
                    }
                }
                break;
            default:
                break;
        }
    }


    private String categoryProcess() throws VisionServiceException, IOException {
        Gson gson = new Gson();
        String[] features = {"Categories", "Tags"};
        String[] details = {};

        // Put the image into an input stream for detection.
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, output);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(output.toByteArray());

        AnalysisResult v = this.client.analyzeImage(inputStream, features, details);

        String result = gson.toJson(v);
        Log.d("result", result);
        inputStream.close();
        output.close();

        return result;
    }


    private class doCategoryRequest extends AsyncTask<String, String, String> {
        // Store error message
        private Exception e = null;

        public doCategoryRequest() {
        }

        @Override
        protected String doInBackground(String... args) {
            try {
                return categoryProcess();
            } catch (Exception e) {
                this.e = e;    // Store error
            }

            return null;
        }

        @Override
        protected void onPostExecute(String data) {
            super.onPostExecute(data);
            // Display based on error existence

            ArrayList<Category> categoryList = new ArrayList<Category>();
            ArrayList<Tag> tagList = new ArrayList<Tag>();

            if (e != null) {
                this.e = null;
            } else {
                Gson gson = new Gson();
                AnalysisResult result = gson.fromJson(data, AnalysisResult.class);
                if (result.categories != null) {
                    for (Category category : result.categories) {
                        Log.wtf("Analysis", "Category: " + category.name + ", score: " + category.score);
                        categoryList.add(category);
                    }
                }
                if (result.tags != null) {
                    for (Tag tag : result.tags) {
                        Log.wtf("Analysis", "Tag: " + tag.name + ", conf: " + tag.confidence + "\n");
                        tagList.add(tag);
                    }
                }
            }

            mButtonSelectImage.setEnabled(true);

            Boolean isRecyclable = checkRecyclable(categoryList, tagList);
            if (isRecyclable) {
                Toast.makeText(getApplicationContext(), "Recyclable item added to inventory!", Toast.LENGTH_SHORT).show();
                LoginScreen.numRecycled++;
                MainActivity.inventory.setText("Inventory: " + LoginScreen.numRecycled);

            } else {
                Toast.makeText(getApplicationContext(), "It doesn't look like this item is recyclable. Please try again!", Toast.LENGTH_SHORT).show();
            }
        }
    }
}