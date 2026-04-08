package com.tmukimi.hospital_management.services;

import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


@Service
@RequiredArgsConstructor
public class SSLCommerzService {

    private final RestTemplate restTemplate;

    @Value("${sslcommerz.api.base_url}")
    private String baseUrl;

    @Value("${sslcommerz.api.store_id}")
    private String storeId;

    @Value("${sslcommerz.api.store_password}")
    private String storePassword;

    public String initiatePayment(double amount, String tranId, String patientName, String patientEmail) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("store_id", storeId);
        map.add("store_passwd", storePassword);
        map.add("total_amount", String.valueOf(amount));
        map.add("currency", "BDT");
        map.add("tran_id", tranId);
        map.add("success_url", "http://localhost:8080/api/v1/payments/success");
        map.add("fail_url", "http://localhost:8080/api/v1/payments/fail");
        map.add("cancel_url", "http://localhost:8080/api/v1/payments/cancel");
        map.add("cus_name", patientName);
        map.add("cus_email", patientEmail);
        map.add("cus_phone", "01700000000");
        map.add("cus_add1", "Dhaka");
        map.add("cus_city", "Dhaka");
        map.add("cus_country", "Bangladesh");
        map.add("shipping_method", "NO");
        map.add("product_name", "Appointment_Token");
        map.add("product_category", "Health");
        map.add("product_profile", "general");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(baseUrl, request, String.class);
            JSONObject json = new JSONObject(response.getBody());

            System.out.println("SSL Response: " + response.getBody());

            if ("SUCCESS".equals(json.getString("status"))) {
                return json.getString("GatewayPageURL");
            }
            throw new RuntimeException("SSLCommerz Error: " + json.optString("failedreason"));
        } catch (Exception e) {
            throw new RuntimeException("Payment initiation failed: " + e.getMessage());
        }
    }




    public boolean initiateSSLCommerzRefund(String bankTranId, BigDecimal amount, String remark) {

        String refundUrl = "https://sandbox.sslcommerz.com/validator/api/merchantTransIDvalidationAPI.php";

        String finalUrl = String.format(
                "%s?v=1&format=json&store_id=%s&store_passwd=%s&bank_tran_id=%s&refund_amount=%s&refund_remark=%s",
                refundUrl,
                storeId,
                storePassword,
                bankTranId,
                amount.toString(),
                URLEncoder.encode(remark, StandardCharsets.UTF_8)
        );

        try {
            System.out.println("Initiating V4 Refund via: " + finalUrl);

            ResponseEntity<String> response = restTemplate.getForEntity(finalUrl, String.class);
            System.out.println("SSL Refund Response Body: " + response.getBody());

            JSONObject json = new JSONObject(response.getBody());

            String apiConnect = json.optString("APIConnect");
            String status = json.optString("status");

            if ("DONE".equalsIgnoreCase(apiConnect) && "success".equalsIgnoreCase(status)) {
                return true;
            } else {
                System.err.println("SSL Refund Failed: " + json.optString("failedreason"));
                return false;
            }
        } catch (Exception e) {
            System.err.println("Refund Exception: " + e.getMessage());
            return false;
        }
    }
}