package com.mdjdev.imagerank.service;

import javax.security.auth.callback.Callback;

import clarifai2.api.ClarifaiBuilder;
import clarifai2.api.ClarifaiClient;
import clarifai2.dto.input.ClarifaiImage;
import clarifai2.dto.input.ClarifaiInput;
import clarifai2.dto.model.Model;
import clarifai2.dto.prediction.Concept;
import clarifai2.exception.ClarifaiException;

/**
 * Created by Guest on 4/9/18.
 */

public class ClarafaiService {

    public static void rateImages(Callback callback, String imagePath) {
        final ClarifaiClient client = new ClarifaiBuilder("apiKey").buildSync();
        client.getDefaultModels().focusModel().predict()
                .withInputs(ClarifaiInput.forImage(ClarifaiImage.of(imagePath)))
                .executeSync()
                .get();

    }
}
