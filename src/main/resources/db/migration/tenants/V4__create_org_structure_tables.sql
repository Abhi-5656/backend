-- BUSINESS UNITS
CREATE TABLE business_units (
                                id BIGSERIAL PRIMARY KEY,
                                name VARCHAR(255) NOT NULL,
                                color VARCHAR(20),
                                effective_date DATE NOT NULL,
                                expiration_date DATE
);

-- JOB TITLES
CREATE TABLE job_titles (
                            id BIGSERIAL PRIMARY KEY,
                            job_title VARCHAR(255) NOT NULL,
                            short_name VARCHAR(100),
                            code VARCHAR(50),
                            sort_order INT,
                            effective_date DATE NOT NULL,
                            expiration_date DATE,
                            color VARCHAR(20),
                            no_expiration_date BOOLEAN DEFAULT FALSE
);

-- LOCATIONS
CREATE TABLE locations (
                           id BIGSERIAL PRIMARY KEY,
                           name VARCHAR(255) NOT NULL,
                           is_root BOOLEAN NOT NULL,
                           effective_date DATE NOT NULL,
                           expiration_date DATE,
                           color VARCHAR(20),

                           business_unit_id BIGINT NOT NULL,
                           parent_id BIGINT,

                           CONSTRAINT fk_location_business_unit FOREIGN KEY (business_unit_id) REFERENCES business_units(id),
                           CONSTRAINT fk_location_parent FOREIGN KEY (parent_id) REFERENCES locations(id) ON DELETE SET NULL
);

-- LOCATION → JOB TITLES MAPPING
CREATE TABLE location_job_titles (
                                     location_id BIGINT NOT NULL,
                                     job_title_id BIGINT NOT NULL,

                                     PRIMARY KEY (location_id, job_title_id),

                                     CONSTRAINT fk_location FOREIGN KEY (location_id) REFERENCES locations(id) ON DELETE CASCADE,
                                     CONSTRAINT fk_job_title FOREIGN KEY (job_title_id) REFERENCES job_titles(id) ON DELETE CASCADE
);
