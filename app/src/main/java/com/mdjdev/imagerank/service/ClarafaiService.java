package com.mdjdev.imagerank.service;

import android.util.Log;

import com.mdjdev.imagerank.Constants;

import java.io.File;
import java.util.List;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.api.ClarifaiResponse;
import clarifai2.api.request.model.PredictRequest;
import clarifai2.dto.input.ClarifaiImage;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.FocusModel;
import clarifai2.dto.model.output.ClarifaiOutput;
import clarifai2.dto.prediction.Concept;
import clarifai2.dto.prediction.Focus;

/**
 * Created by Guest on 4/9/18.
 */

public class ClarafaiService {

    public static void rateImages(String imagePath) {
        final ClarifaiClient client = new ClarifaiBuilder(Constants.CLARIFAI_TOKEN).buildSync();
        client.getDefaultModels().focusModel().predict()
                .withInputs(ClarifaiInput.forImage(ClarifaiImage.of("https://samples.clarifai.com/focus.jpg;")))
                .executeSync();
    }

}
