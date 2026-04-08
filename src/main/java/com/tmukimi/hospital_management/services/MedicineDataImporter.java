package com.tmukimi.hospital_management.services;

import com.opencsv.CSVReader;
import com.tmukimi.hospital_management.entities.Medicine;
import com.tmukimi.hospital_management.entities.MedicineBrand;
import com.tmukimi.hospital_management.repositories.MedicineBrandRepository;
import com.tmukimi.hospital_management.repositories.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MedicineDataImporter {

    private final MedicineRepository medicineRepository;
    private final MedicineBrandRepository brandRepository;

    @Transactional
    public void importData() throws Exception {
        Map<String, Medicine> genericMap = new HashMap<>();

        System.out.println(">>> Importing Generics...");
        try (CSVReader reader = new CSVReader(new InputStreamReader(new ClassPathResource("data/generic.csv").getInputStream()))) {
            String[] line;
            reader.readNext();
            int gCount = 0;

            while ((line = reader.readNext()) != null) {
                if (line.length < 14) continue;

                String gName = line[1].trim();
                String description = cleanHtml(line[6]);
                String sideEffects = cleanHtml(line[13]);

                if (!genericMap.containsKey(gName.toLowerCase())) {
                    Medicine medicine = new Medicine();
                    medicine.setGenericName(gName);
                    medicine.setDescription(description);
                    medicine.setSideEffects(sideEffects);

                    medicine = medicineRepository.save(medicine);
                    genericMap.put(gName.toLowerCase(), medicine);
                    gCount++;
                }
            }
            System.out.println(">>> Total Generics Saved: " + gCount);
        }

        System.out.println(">>> Importing Brands...");
        try (CSVReader reader = new CSVReader(new InputStreamReader(new ClassPathResource("data/medicine.csv").getInputStream()))) {
            String[] line;
            reader.readNext();
            int bCount = 0;

            while ((line = reader.readNext()) != null) {
                if (line.length < 8) continue;

                String brandName = line[1].trim();
                String genericName = line[5].trim();
                String manufacturer = line[7].trim();

                Medicine associatedGeneric = genericMap.get(genericName.toLowerCase());

                if (associatedGeneric != null) {
                    MedicineBrand brand = new MedicineBrand();
                    brand.setBrandName(brandName);
                    brand.setCompanyName(manufacturer);
                    brand.setMedicine(associatedGeneric);

                    brandRepository.save(brand);
                    bCount++;
                }
                if (bCount % 5000 == 0) System.out.println("Imported " + bCount + " brands...");
            }
            System.out.println(">>> TOTAL BRAND IMPORTED: " + bCount);
        }
    }

    private String cleanHtml(String html) {
        if (html == null || html.isEmpty()) return "N/A";
        return html.replaceAll("<[^>]*>", "").replaceAll("&nbsp;", " ").trim();
    }
}