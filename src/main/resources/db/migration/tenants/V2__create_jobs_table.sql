CREATE TABLE jobs (
                      id BIGSERIAL PRIMARY KEY,
                      title VARCHAR(255) NOT NULL,
                      position VARCHAR(255) NOT NULL,
                      description TEXT,
                      openings INT NOT NULL,
                      annual_salary NUMERIC(10,2),
                      employment_type VARCHAR(20) NOT NULL,
                      experience_level VARCHAR(20) NOT NULL,
                      years_of_experience INT,
                      status VARCHAR(20) NOT NULL,
                      created_by VARCHAR(255) NOT NULL,
                      created_date DATE NOT NULL DEFAULT CURRENT_DATE,
                      expiry_date DATE
);
