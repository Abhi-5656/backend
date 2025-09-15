package com.wfm.experts.controller;

import com.wfm.experts.service.FaceRecognitionService;
import com.wfm.experts.tenant.common.employees.entity.EmployeeProfileRegistration;
import com.wfm.experts.tenant.common.employees.repository.EmployeeProfileRegistrationRepository;
import com.wfm.experts.util.EmbeddingConverter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/face-recognition")
public class FaceRecognitionController {

    private final FaceRecognitionService faceRecognitionService;
    private final EmployeeProfileRegistrationRepository registrationRepository;

    public FaceRecognitionController(FaceRecognitionService faceRecognitionService, EmployeeProfileRegistrationRepository registrationRepository) {
        this.faceRecognitionService = faceRecognitionService;
        this.registrationRepository = registrationRepository;
    }

    @PostMapping("/recognize")
    public ResponseEntity<?> recognizeFace(@RequestBody Map<String, String> payload) {
        String base64Image = payload.get("image");
        if (base64Image == null || base64Image.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Image data is required."));
        }

        try {
            float[] newEmbedding = faceRecognitionService.getFaceEmbedding(base64Image);

            List<EmployeeProfileRegistration> allRegistrations = registrationRepository.findAll();
            List<float[]> existingEmbeddings = allRegistrations.stream()
                    .map(reg -> EmbeddingConverter.toFloatArray(reg.getFaceEmbedding()))
                    .collect(Collectors.toList());

            // You can adjust this threshold (lower is stricter, higher is more lenient)
            String bestMatchIndexStr = faceRecognitionService.findBestMatch(newEmbedding, existingEmbeddings, 0.8);

            if (bestMatchIndexStr != null) {
                int bestMatchIndex = Integer.parseInt(bestMatchIndexStr);
                String employeeId = allRegistrations.get(bestMatchIndex).getEmployeeId();
                return ResponseEntity.ok(Map.of("employeeId", employeeId));
            } else {
                return ResponseEntity.status(404).body(Map.of("error", "No matching employee found."));
            }

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}