package com.mdjdev.imagerank.ui;

import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.mdjdev.imagerank.Constants;
import com.mdjdev.imagerank.R;
import com.mdjdev.imagerank.service.ClarafaiService;

import org.json.JSONArray;
import org.json.JSONObject;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.dto.input.ClarifaiImage;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.ConceptModel;
import clarifai2.dto.model.FocusModel;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import clarifai2.dto.prediction.Focus;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
@Bind(R.id.rankButton) Button mRankButton;
@Bind(R.id.ivPreview) ImageView mIvPreview;
InputStream inputStream = null;
String filePath = null;
public static final int PICK_IMAGE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mRankButton.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {

        if (v == mRankButton) {
            startActivityForResult(new Intent(Intent.ACTION_PICK).setType("image/*"), PICK_IMAGE);

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


        new AsyncTask<Void, Void, ClarifaiResponse<List<ClarifaiOutput<Focus>>>>() {
            @Override protected ClarifaiResponse<List<ClarifaiOutput<Focus>>> doInBackground(Void... params) {
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
                Log.d("RESULTS", results.get(0).data().toString());
                Bitmap decodedBitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
                mIvPreview.setImageBitmap(decodedBitmap);

            }
        }.execute();
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
