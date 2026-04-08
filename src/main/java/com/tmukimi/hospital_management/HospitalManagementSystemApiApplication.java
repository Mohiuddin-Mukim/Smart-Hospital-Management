package com.tmukimi.hospital_management;

import com.tmukimi.hospital_management.repositories.PatientRepository;
import com.tmukimi.hospital_management.services.MedicineDataImporter;
import com.tmukimi.hospital_management.services.PatientService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class HospitalManagementSystemApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(HospitalManagementSystemApiApplication.class, args);
    }
@Bean
public CommandLineRunner initDatabase(PatientService patientService, MedicineDataImporter importer) {
    return args -> {
        System.out.println(">>> STARTING SYSTEM INITIALIZATION <<<");
    };
    }
}