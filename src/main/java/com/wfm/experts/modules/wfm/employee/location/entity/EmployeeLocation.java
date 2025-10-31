package com.wfm.experts.modules.wfm.employee.location.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.locationtech.jts.geom.Point;
import java.time.OffsetDateTime;

@Entity
@Table(name = "employee_locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeLocation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "employee_id", nullable = false)
    private String employeeId;

    /**
     * Stores the GIS location point.
     * The column definition `geography(Point, 4326)` is specified in the Flyway migration.
     */
    @Column(name = "location", nullable = false, columnDefinition = "geography(Point, 4326)")
    private Point location;

    @Column(name = "timestamp", nullable = false)
    private OffsetDateTime timestamp;

    /**
     * Type of event (e.g., "PING", "IN", "OUT").
     */
    @Column(name = "punch_type", nullable = false, length = 20)
    private String punchType;
}