CREATE TABLE doctor_profile_request (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        doctor_id BIGINT NOT NULL,
                                        name VARCHAR(255),
                                        specialization VARCHAR(255),
                                        degree VARCHAR(255),
                                        designation VARCHAR(255),
                                        phone VARCHAR(20),
                                        room_no VARCHAR(50),
                                        consultation_fee DECIMAL(19, 2),
                                        about_doctor TEXT,
                                        profile_picture_url VARCHAR(255),
                                        experience_years INT,
                                        requested_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                                        processed BOOLEAN DEFAULT FALSE
);