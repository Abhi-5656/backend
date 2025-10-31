package com.wfm.experts.modules.wfm.employee.location.producer;

import com.wfm.experts.modules.wfm.employee.location.dto.EmployeeLocationDTO;

/**
 * Interface for publishing employee location updates to RabbitMQ.
 */
public interface LocationTrackingProducer {

    /**
     * Sends a location update to the message broker.
     *
     * @param locationDTO The DTO containing employee ID, coordinates, and timestamp.
     */
    void sendLocationUpdate(EmployeeLocationDTO locationDTO);
}