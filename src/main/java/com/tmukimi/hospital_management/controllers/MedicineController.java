package com.tmukimi.hospital_management.controllers;

import com.tmukimi.hospital_management.repositories.MedicineBrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/medicines")
@RequiredArgsConstructor
public class MedicineController {

    private final MedicineBrandRepository brandRepository;

    @GetMapping("/search")
    public List<Map<String, Object>> searchMedicines(@RequestParam String query) {
        return brandRepository.searchByBrandName(query, PageRequest.of(0, 10))
                .stream()
                .map(brand -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("brandId", brand.getId());
                    map.put("brandName", brand.getBrandName());
                    map.put("genericName", brand.getMedicine().getGenericName());
                    map.put("company", brand.getCompanyName());
                    return map;
                }).collect(Collectors.toList());
    }
}