package com.wfm.experts.modules.wfm.employee.location.service.impl;

import com.wfm.experts.modules.wfm.employee.location.dto.EmployeeLocationDTO;
import com.wfm.experts.modules.wfm.employee.location.entity.EmployeeLocation;
import com.wfm.experts.modules.wfm.employee.location.producer.LocationTrackingProducer;
import com.wfm.experts.modules.wfm.employee.location.repository.EmployeeLocationRepository;
import com.wfm.experts.modules.wfm.employee.location.service.EmployeeLocationService;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class EmployeeLocationServiceImpl implements EmployeeLocationService {

    private static final Logger logger = LoggerFactory.getLogger(EmployeeLocationServiceImpl.class);

    private final LocationTrackingProducer locationProducer;
    private final EmployeeLocationRepository locationRepository;

    // GeometryFactory with SRID 4326 (WGS 84)
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

    /**
     * API-facing method. Publishes to RabbitMQ.
     */
    @Override
    public void trackLocation(EmployeeLocationDTO locationDTO) {
        // Validation is handled by @Valid in controller, but we can add business validation here.
        // The employeeId is set by the controller from the JWT, so it's trusted.

        logger.debug("Publishing location for employee: {}", locationDTO.getEmployeeId());
        locationProducer.sendLocationUpdate(locationDTO);
    }

    /**
     * Consumer-facing method. Saves to Database.
     */
    @Override
    @Transactional
    public void saveLocation(EmployeeLocationDTO locationDTO) {
        try {
            // Convert latitude/longitude to a JTS Point object
            // IMPORTANT: PostGIS stores (Longitude, Latitude)
            Coordinate coordinate = new Coordinate(locationDTO.getLongitude(), locationDTO.getLatitude());
            Point point = geometryFactory.createPoint(coordinate);

            EmployeeLocation location = EmployeeLocation.builder()
                    .employeeId(locationDTO.getEmployeeId())
                    .location(point)
                    .timestamp(locationDTO.getTimestamp().atOffset(OffsetDateTime.now().getOffset())) // Convert Instant to OffsetDateTime
                    .punchType(locationDTO.getPunchType())
                    .build();

            locationRepository.save(location);
            logger.debug("Successfully saved location for employee: {}", locationDTO.getEmployeeId());
        } catch (Exception e) {
            logger.error("Failed to save location for employee: {}. Error: {}",
                    locationDTO.getEmployeeId(), e.getMessage(), e);
            // This exception will be caught by the consumer, which should reject the message.
            throw new RuntimeException("Failed to save location data", e);
        }
    }
}