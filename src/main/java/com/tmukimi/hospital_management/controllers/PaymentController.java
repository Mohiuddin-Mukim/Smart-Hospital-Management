package com.tmukimi.hospital_management.controllers;

import com.tmukimi.hospital_management.entities.Appointment;
import com.tmukimi.hospital_management.entities.Payment;
import com.tmukimi.hospital_management.entities.QueueToken;
import com.tmukimi.hospital_management.repositories.AppointmentRepository;
import com.tmukimi.hospital_management.repositories.PaymentRepository;
import com.tmukimi.hospital_management.repositories.QueueTokenRepository;
import com.tmukimi.hospital_management.services.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentRepository paymentRepository;
    private final PaymentService paymentService;
    private final AppointmentRepository appointmentRepository;
    private final QueueTokenRepository queueTokenRepository;

    /// SSLCommerz সাকসেস হলে এখানে POST রিকোয়েস্ট পাঠাবে
    @PostMapping(value = "/success", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<String> paymentSuccess(@RequestParam Map<String, String> formData) {
        // SSLCommerz থেকে 'tran_id' কি-তে আইডি আসে
        String tranId = formData.get("tran_id");
        String bankTranId = formData.get("bank_tran_id");

        Payment payment = paymentRepository.findByTransactionId(tranId)
                .orElseThrow(() -> new RuntimeException("Transaction not found in database: " + tranId));

        payment.setProviderReference(bankTranId);
        paymentService.finalizePayment(payment);    // By changing the status to SUCCESS and saving to db

        return ResponseEntity.ok("""
            <html>
                <body style="font-family: Arial; text-align: center; padding: 50px;">
                    <h1 style="color: green;">Payment Successful!</h1>
                    <p>Transaction ID: %s</p>
                    <p>Your appointment is now PENDING doctor approval.</p>
                    <a href="http://localhost:63342/hospital-management/static/frontend/patient-dashboard.html">Go to Dashboard</a>
                </body>
            </html>
            """.formatted(tranId));
    }



    /// SSLCommerz Fail হলে এখানে POST রিকোয়েস্ট আসবে
    @PostMapping("/fail")
    public ResponseEntity<String> paymentFail(@RequestParam Map<String, String> formData) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("<h1>Payment Failed!</h1><p>Reason: " + formData.get("error") + "</p>");
    }




    @PostMapping("/cancel")
    public ResponseEntity<String> paymentCancel() {
        return ResponseEntity.ok("<h1>Payment Cancelled by User</h1>");
    }




    @GetMapping("/{appointmentId}/receipt")
    public ResponseEntity<byte[]> downloadReceipt(
            @PathVariable Long appointmentId,
            Principal principal,
            HttpServletRequest request) {

        Appointment appt = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new RuntimeException("Appointment not found"));

        String currentUserEmail = principal.getName();
        boolean isOwner = appt.getPatient().getUser().getEmail().equals(currentUserEmail);  // checking are they same? logged in user and appointments patient

        if (!isOwner) {
            throw new RuntimeException("Security Alert: You are not authorized to download this receipt!");
        }

        Payment payment = paymentRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new RuntimeException("Payment record not found"));


        /// --------------------- generating PDF ----------------------
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }

        /// getting serial number from QueueToken table
        Integer serialNo = queueTokenRepository.findByAppointmentId(appointmentId)
                .map(QueueToken::getSerialNo)
                .orElse(null);

        byte[] pdfContent = paymentService.generateInvoicePdf(appt, payment, serialNo, clientIp);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=receipt_" + appointmentId + ".pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfContent);
    }
}