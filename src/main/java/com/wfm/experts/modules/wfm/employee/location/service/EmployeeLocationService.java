package com.wfm.experts.modules.wfm.employee.location.service;

import com.wfm.experts.modules.wfm.employee.location.dto.EmployeeLocationDTO;

/**
 * Service interface for handling business logic related to employee location tracking.
 */
public interface EmployeeLocationService {

    /**
     * Receives a location update from the controller, validates it,
     * and publishes it to the message queue for asynchronous processing.
     *
     * @param locationDTO DTO from the mobile API.
     */
    void trackLocation(EmployeeLocationDTO locationDTO);

    /**
     * Saves the processed location data (from the message queue) to the database.
     * This method is intended to be called by the RabbitMQ consumer.
     *
     * @param locationDTO The DTO received from the queue.
     */
    void saveLocation(EmployeeLocationDTO locationDTO);
}