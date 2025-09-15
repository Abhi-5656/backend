package com.wfm.experts.service.impl;

import ai.djl.Application;
import ai.djl.MalformedModelException;
import ai.djl.inference.Predictor;
import ai.djl.modality.cv.Image;
import ai.djl.modality.cv.ImageFactory;
import ai.djl.modality.cv.output.BoundingBox;
import ai.djl.modality.cv.output.DetectedObjects;
import ai.djl.modality.cv.output.Rectangle;
import ai.djl.repository.zoo.Criteria;
import ai.djl.repository.zoo.ModelNotFoundException;
import ai.djl.repository.zoo.ZooModel;
import ai.djl.translate.TranslateException;
import com.wfm.experts.service.FaceRecognitionService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FaceRecognitionServiceImpl implements FaceRecognitionService {

    private final Criteria<Image, DetectedObjects> faceDetectionCriteria =
            Criteria.builder()
                    .optApplication(Application.CV.OBJECT_DETECTION)
                    .setTypes(Image.class, DetectedObjects.class)
                    .optFilter("size", "320")
                    .optFilter("backbone", "mobilenet_v2")
                    .optEngine("PyTorch")
                    .optArgument("translator", "face_detection") // Key change: Specify face detection
                    .build();

    private final Criteria<Image, float[]> faceEmbeddingCriteria =
            Criteria.builder()
                    .optApplication(Application.CV.IMAGE_CLASSIFICATION)
                    .setTypes(Image.class, float[].class)
                    .optFilter("backbone", "arcface")
                    .optEngine("PyTorch")
                    .build();


    @Override
    public float[] getFaceEmbedding(String base64Image) throws IOException, TranslateException, ModelNotFoundException, MalformedModelException {
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        Image image = ImageFactory.getInstance().fromInputStream(new ByteArrayInputStream(imageBytes));

        try (ZooModel<Image, DetectedObjects> detectionModel = faceDetectionCriteria.loadModel();
             Predictor<Image, DetectedObjects> detector = detectionModel.newPredictor();
             ZooModel<Image, float[]> embeddingModel = faceEmbeddingCriteria.loadModel();
             Predictor<Image, float[]> embedder = embeddingModel.newPredictor()) {

            DetectedObjects detectedObjects = detector.predict(image);
            List<DetectedObjects.DetectedObject> faces = detectedObjects.items();

            if (faces.isEmpty()) {
                throw new IllegalStateException("No face detected in the image.");
            }
            if (faces.size() > 1) {
                throw new IllegalStateException("Multiple faces detected in the image. Please provide an image with a single face.");
            }

            DetectedObjects.DetectedObject face = faces.get(0);
            Image croppedFace = getSubImage(image, face.getBoundingBox());
            return embedder.predict(croppedFace);
        }
    }

    @Override
    public String findBestMatch(float[] newEmbedding, List<float[]> existingEmbeddings, double threshold) {
        String bestMatchIndex = null;
        double minDistance = Double.MAX_VALUE;

        for (int i = 0; i < existingEmbeddings.size(); i++) {
            if (existingEmbeddings.get(i) == null) continue;

            double distance = cosineDistance(newEmbedding, existingEmbeddings.get(i));
            if (distance < minDistance) {
                minDistance = distance;
            }
        }

        if (minDistance < threshold) {
            for (int i = 0; i < existingEmbeddings.size(); i++) {
                if (existingEmbeddings.get(i) != null && cosineDistance(newEmbedding, existingEmbeddings.get(i)) == minDistance) {
                    bestMatchIndex = String.valueOf(i);
                    break;
                }
            }
        }

        return bestMatchIndex;
    }

    private double cosineDistance(float[] vectorA, float[] vectorB) {
        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < vectorA.length; i++) {
            dotProduct += vectorA[i] * vectorB[i];
            normA += Math.pow(vectorA[i], 2);
            normB += Math.pow(vectorB[i], 2);
        }
        return 1 - (dotProduct / (Math.sqrt(normA) * Math.sqrt(normB)));
    }

    private Image getSubImage(Image img, BoundingBox box) {
        Rectangle rect = box.getBounds();
        int width = img.getWidth();
        int height = img.getHeight();
        int[] recovered =
                new int[] {
                        (int) (rect.getX() * width),
                        (int) (rect.getY() * height),
                        (int) (rect.getWidth() * width),
                        (int) (rect.getHeight() * height)
                };
        return img.getSubImage(recovered[0], recovered[1], recovered[2], recovered[3]);
    }
}