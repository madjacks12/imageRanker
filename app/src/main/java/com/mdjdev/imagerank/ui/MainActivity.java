package com.mdjdev.imagerank.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.mdjdev.imagerank.Constants;
import com.mdjdev.imagerank.R;
import com.mdjdev.imagerank.service.ClarafaiService;

import org.json.JSONObject;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.api.request.model.PredictRequest;
import clarifai2.dto.input.ClarifaiImage;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.FocusModel;
import clarifai2.dto.model.output.ClarifaiOutput;


import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.NumberFormat;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import clarifai2.dto.prediction.Focus;
import clarifai2.dto.prediction.Prediction;

import static java.lang.String.valueOf;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
@Bind(R.id.focusButton) Button mFocusButton;
@Bind(R.id.portraitButton) Button mPortraitButton;
@Bind(R.id.landscapeButton) Button mLandscapeButton;
@Bind(R.id.ivPreview) ImageView mIvPreview;
@Bind(R.id.focusScore) TextView mFocusScore;
InputStream inputStream = null;
String filePath = null;
public static final int PICK_IMAGE = 100;
String selectedButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mFocusButton.setOnClickListener(this);
        mPortraitButton.setOnClickListener(this);
        mLandscapeButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (v == mFocusButton) {
            startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), PICK_IMAGE);
            selectedButton = "focus";
        }
        if (v == mLandscapeButton) {
            startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), PICK_IMAGE);
            selectedButton = "landscape";
        }
        if (v == mPortraitButton) {
            startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), PICK_IMAGE);
            selectedButton = "portrait";
        }
    }


//    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (data.getClipData() != null) {
//            ClipData mClipData = data.getClipData();
//            ArrayList<Uri> mArrayUri = new ArrayList<Uri>();
//            ArrayList<Bitmap>mBitmapsSelected = new ArrayList<Bitmap>();
//            for (int i = 0; i < mClipData.getItemCount(); i++) {
//                ClipData.Item item = mClipData.getItemAt(i);
//                Uri uri = item.getUri();
//                mArrayUri.add(uri);
//                try {
//                    inputStream = getContentResolver().openInputStream(uri);
//                    File photoFile = createTemporalFileFrom(inputStream);
//
//                    filePath = photoFile.getPath();
//                    getRankings(filePath);
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }

    public void getRankings(String imagePath) {
        final ClarafaiService clarafaiService = new ClarafaiService();
        clarafaiService.rateImages(imagePath);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK) {
            return;
        }
        switch(requestCode) {
            case PICK_IMAGE:
                final byte[] imageBytes = retrieveSelectedImage(this, data);
                if (imageBytes != null) {
                    onImagePicked(imageBytes);
                }
                break;
        }
    }

    private void onImagePicked(@NonNull final byte[] imageBytes) {

        if (selectedButton == "focus") {
            new AsyncTask<Void, Void, ClarifaiResponse<List<ClarifaiOutput<Focus>>>>() {
                @Override
                protected ClarifaiResponse<List<ClarifaiOutput<Focus>>> doInBackground(Void... params) {
                    // The default Clarifai model that identifies concepts in images
                    final ClarifaiClient client = new ClarifaiBuilder(Constants.CLARIFAI_TOKEN).buildSync();
                    FocusModel focusModel = client.getDefaultModels().focusModel();
                    // Use this model to predict, with the image that the user just selected as the input
                    return focusModel.predict()
                            .withInputs(ClarifaiInput.forImage(ClarifaiImage.of(imageBytes)))
                            .executeSync();
                }

                protected void onPostExecute(ClarifaiResponse<List<ClarifaiOutput<Focus>>> response) {
                    if (!response.isSuccessful()) {
                    }
                    final List<ClarifaiOutput<Focus>> results = response.get();
                    double value = results.get(0).data().get(0).value() * 100;
                    NumberFormat numberFormat = NumberFormat.getNumberInstance();
                    numberFormat.setMaximumFractionDigits(2);

                    Log.d("RESULTS", valueOf(value));

                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    mIvPreview.setImageBitmap(decodedBitmap);
                    mFocusScore.setText("Focus score: " + numberFormat.format(value));
                }
            }.execute();
        }
        if (selectedButton == "portrait") {
            new AsyncTask<Void, Void, ClarifaiResponse<List<ClarifaiOutput<Prediction>>>>() {
                @Override
                protected ClarifaiResponse<List<ClarifaiOutput<Prediction>>> doInBackground(Void... params) {
                    // The default Clarifai model that identifies concepts in images
                    final ClarifaiClient client = new ClarifaiBuilder(Constants.CLARIFAI_TOKEN).buildSync();
                    PredictRequest<Prediction> portraitModel = client.predict("de9bd05cfdbf4534af151beb2a5d0953");
                    // Use this model to predict, with the image that the user just selected as the input
                    return portraitModel.withInputs().withInputs(ClarifaiInput.forImage(ClarifaiImage.of(imageBytes)))
                            .executeSync();
                }

                protected void onPostExecute(ClarifaiResponse<List<ClarifaiOutput<Prediction>>> response) {
                    if (!response.isSuccessful()) {
                    }
                    final List<ClarifaiOutput<Prediction>> results = response.get();
                    Float value = results.get(0).data().get(0).asConcept().value() * 100;
                    String qualityName = results.get(0).data().get(0).asConcept().name();

                    NumberFormat numberFormat = NumberFormat.getNumberInstance();
                    numberFormat.setMaximumFractionDigits(2);

                    String qualityRating = null;
                    if (value > 75 && qualityName.contains("high")) {
                        qualityRating = "Excellent Quality";
                    }
                    else if (qualityName.contains("high") && value > 50 && value < 76) {
                        qualityRating = "Good Quality";
                    }
                    else if (qualityName.contains("high") && value > 25 && value < 51) {
                        qualityRating = "OK Quality";
                    }

                    else if (qualityName.contains("low") && value > 75) {
                        qualityRating = "Terrible Quality";
                    }
                    else if (qualityName.contains("low") && value > 50 && value < 76) {
                        qualityRating = "Poor Quality";
                    }
                    else if (qualityName.contains("low") && value > 25 && value < 51) {
                        qualityRating = "Mediocre Quality";
                    }
                    else {
                        qualityRating = "Could not determine quality";
                    }

                    Log.d("RESULTS", valueOf(value));
                    Log.d("2nd", qualityRating);

                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    mIvPreview.setImageBitmap(decodedBitmap);
                    mFocusScore.setText(qualityRating);
                }
            }.execute();
        }
        if (selectedButton == "landscape") {
            new AsyncTask<Void, Void, ClarifaiResponse<List<ClarifaiOutput<Prediction>>>>() {
                @Override
                protected ClarifaiResponse<List<ClarifaiOutput<Prediction>>> doInBackground(Void... params) {
                    // The default Clarifai model that identifies concepts in images
                    final ClarifaiClient client = new ClarifaiBuilder(Constants.CLARIFAI_TOKEN).buildSync();
                    PredictRequest<Prediction> portraitModel = client.predict("bec14810deb94c40a05f1f0eb3c91403");
                    // Use this model to predict, with the image that the user just selected as the input
                    return portraitModel.withInputs().withInputs(ClarifaiInput.forImage(ClarifaiImage.of(imageBytes)))
                            .executeSync();
                }

                protected void onPostExecute(ClarifaiResponse<List<ClarifaiOutput<Prediction>>> response) {
                    if (!response.isSuccessful()) {
                    }
                    final List<ClarifaiOutput<Prediction>> results = response.get();
                    Float value = results.get(0).data().get(0).asConcept().value() * 100;
                    String qualityName = results.get(0).data().get(0).asConcept().name();

                    NumberFormat numberFormat = NumberFormat.getNumberInstance();
                    numberFormat.setMaximumFractionDigits(2);

                    String qualityRating = null;
                    if (value > 75 && qualityName.contains("high")) {
                        qualityRating = "Excellent Quality";
                    }
                    else if (qualityName.contains("high") && value > 50 && value < 76) {
                        qualityRating = "Good Quality";
                    }
                    else if (qualityName.contains("high") && value > 25 && value < 51) {
                        qualityRating = "OK Quality";
                    }

                    else if (qualityName.contains("low") && value > 75) {
                        qualityRating = "Terrible Quality";
                    }
                    else if (qualityName.contains("low") && value > 50 && value < 76) {
                        qualityRating = "Poor Quality";
                    }
                    else if (qualityName.contains("low") && value > 25 && value < 51) {
                        qualityRating = "Mediocre Quality";
                    }
                    else {
                        qualityRating = "Could not determine quality";
                    }

                    Log.d("RESULTS", valueOf(value));
                    Log.d("2nd", qualityRating);

                    Bitmap decodedBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                    mIvPreview.setImageBitmap(decodedBitmap);
                    mFocusScore.setText(qualityRating);
                }
            }.execute();
        }
    }


    public static byte[] retrieveSelectedImage(@NonNull Context context, @NonNull Intent data) {
        InputStream inStream = null;
        Bitmap bitmap = null;
        try {
            inStream = context.getContentResolver().openInputStream(data.getData());
            bitmap = BitmapFactory.decodeStream(inStream);
            final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream);
            return outStream.toByteArray();
        } catch (FileNotFoundException e) {
            return null;
        } finally {
            if (inStream != null) {
                try {
                    inStream.close();
                } catch (IOException ignored) {
                }
            }
            if (bitmap != null) {
                bitmap.recycle();

            }
        }
    }



}
