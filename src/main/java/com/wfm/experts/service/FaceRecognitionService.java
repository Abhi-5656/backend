package com.wfm.experts.service;

import ai.djl.MalformedModelException;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.translate.TranslateException;

import java.io.IOException;
import java.util.List;

/**
 * Interface for the face recognition system.
 * Defines the contract for generating face embeddings and finding matches.
 */
public interface FaceRecognitionService {

    /**
     * Generates a unique facial embedding (a float array) from a Base64 encoded image.
     *
     * @param base64Image The Base64 string of the image.
     * @return A float array representing the facial features.
     * @throws IOException if there's an error reading the image data.
     * @throws TranslateException if the ML model fails to process the image.
     * @throws IllegalStateException if no face or multiple faces are detected.
     */
    float[] getFaceEmbedding(String base64Image) throws IOException, TranslateException, ModelNotFoundException, MalformedModelException;

    /**
     * Compares a new face embedding against a list of existing embeddings to find the best match.
     *
     * @param newEmbedding The embedding of the face to be recognized.
     * @param existingEmbeddings A list of embeddings from the database.
     * @param threshold The maximum allowed distance for a match (e.g., 0.8).
     * @return The index of the best match in the list, or null if no match is found within the threshold.
     */
    String findBestMatch(float[] newEmbedding, List<float[]> existingEmbeddings, double threshold);
}