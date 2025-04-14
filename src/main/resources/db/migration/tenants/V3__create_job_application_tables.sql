-- Main table: job_applications
CREATE TABLE job_applications (
                                  id BIGSERIAL PRIMARY KEY,
                                  job_id BIGINT NOT NULL,

    -- Personal Info
                                  first_name VARCHAR(100) NOT NULL,
                                  last_name VARCHAR(100) NOT NULL,
                                  email VARCHAR(150) NOT NULL,
                                  phone VARCHAR(20),

    -- Address
                                  address_line1 VARCHAR(255),
                                  address_line2 VARCHAR(255),
                                  city VARCHAR(100),
                                  state VARCHAR(100),
                                  pincode VARCHAR(20),

    -- Experience
                                  total_experience INT,
                                  current_company VARCHAR(150),
                                  current_ctc VARCHAR(50),
                                  expected_ctc VARCHAR(50),
                                  notice_period VARCHAR(50),

    -- Education
                                  degree VARCHAR(150),
                                  specialization VARCHAR(150),
                                  university VARCHAR(150),
                                  passing_year INT,

    -- Resume
                                  resume_url TEXT,

    -- Metadata
                                  applied_date DATE DEFAULT CURRENT_DATE
);

-- Subtable: job_application_skills
CREATE TABLE job_application_skills (
                                        application_id BIGINT NOT NULL,
                                        skill VARCHAR(100) NOT NULL,
                                        badge_color VARCHAR(20),
                                        CONSTRAINT fk_application_skill FOREIGN KEY (application_id) REFERENCES job_applications(id) ON DELETE CASCADE
);

-- Subtable: job_application_certifications
CREATE TABLE job_application_certifications (
                                                application_id BIGINT NOT NULL,
                                                certification VARCHAR(150) NOT NULL,
                                                badge_color VARCHAR(20),
                                                CONSTRAINT fk_application_cert FOREIGN KEY (application_id) REFERENCES job_applications(id) ON DELETE CASCADE
);
