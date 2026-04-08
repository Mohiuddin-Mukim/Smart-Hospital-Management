package com.tmukimi.hospital_management.services;

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.tmukimi.hospital_management.entities.Appointment;
import com.tmukimi.hospital_management.entities.Payment;
import com.tmukimi.hospital_management.entities.QueueToken;
import com.tmukimi.hospital_management.repositories.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * বুকিংয়ের সময় পেমেন্ট ইনিশিয়েট করার মেথড।
     * এখানে SSLCommerz থেকে আসা Transaction ID ব্যবহার করা হবে।
     */
    public Payment initiateTokenPayment(Appointment appt, BigDecimal amount, String tranId) {
        Payment payment = Payment.builder()
                .appointment(appt)
                .patient(appt.getPatient())
                .amount(amount)
                .invoiceNo("INV-" + System.currentTimeMillis())
                .status("INITIATED")
                .transactionId(tranId)
                .paymentMethod("SSLCOMMERZ")
                .createdAt(LocalDateTime.now())
                .build();

        return paymentRepository.save(payment);
    }

    /// পেমেন্ট সফল হলে স্ট্যাটাস আপডেট করার মেথড
    public void finalizePayment(Payment payment) {
        payment.setStatus("SUCCESS");
        paymentRepository.save(payment);
    }


    public byte[] generateInvoicePdf(Appointment appt, Payment payment, Integer serialNo, String userIp) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter writer = new PdfWriter(out);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);

            document.add(new Paragraph("T.MUKIMII HOSPITAL MANAGEMENT").setBold().setFontSize(20).setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
            document.add(new Paragraph("Official Payment Receipt").setItalic().setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
            document.add(new Paragraph("also Appointment Receipt & Token").setItalic().setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER));
            document.add(new Paragraph("\n"));

            document.add(new Paragraph("Invoice No: " + payment.getInvoiceNo()));
            document.add(new Paragraph("Status: " + payment.getStatus().toUpperCase()).setBold());
            document.add(new Paragraph("-------------------------------------------------------------------------------------------------------------------"));


            document.add(new Paragraph("Patient Information:").setBold());
            document.add(new Paragraph("Patient: " + appt.getPatient().getName() + " | Contact: " + appt.getPatient().getPhone()));

            document.add(new Paragraph("Doctor Information:").setBold());
            document.add(new Paragraph("Doctor: " + appt.getDoctor().getName() + " (" + appt.getDoctor().getSpecialization() + ")"));
            document.add(new Paragraph("Date: " + appt.getDate() + " | Scheduled Time Slot: " + appt.getTime()));


            document.add(new Paragraph("\nReason for Visit").setBold());
            document.add(new Paragraph((appt.getReason() != null ? appt.getReason() : "N/A")));

            if ("BOOKED".equalsIgnoreCase(appt.getStatus().name()) && serialNo != null) {
                document.add(new Paragraph("\nTOKEN SERIAL NUMBER")
                        .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER).setFontSize(12));
                document.add(new Paragraph("#" + serialNo)
                        .setBold().setFontSize(40) // সিরিয়ালটি বড় করে দেখানো
                        .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER)
                        .setFontColor(com.itextpdf.kernel.colors.ColorConstants.BLUE));
                document.add(new Paragraph("Please arrive 10 minutes before your slot: " + appt.getTime())
                        .setItalic().setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER).setFontSize(9));
            }

            document.add(new Paragraph("-------------------------------------------------------------------------------------------------------------------"));

            document.add(new Paragraph("Paid Amount: BDT " + payment.getAmount() + " (" + payment.getPaymentMethod() + ")").setBold());
            document.add(new Paragraph("Transaction ID: " + payment.getTransactionId()).setBold());

            if ("REFUNDED".equalsIgnoreCase(payment.getStatus())) {
                document.add(new Paragraph("Refund Date: " + payment.getRefundedAt()).setFontColor(com.itextpdf.kernel.colors.ColorConstants.RED));
            }

            document.add(new Paragraph("\n\n-------------------------------------------------------------------------------------------------------------------"));

            String footerText = String.format(
                    "Issued: %s | Source IP: %s | Txn: %s",
                    LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
                    userIp,
                    payment.getTransactionId().substring(0, 8).toUpperCase() // ট্রানজেকশন আইডির প্রথম ৮ অক্ষর
            );

            Paragraph footer = new Paragraph(footerText)
                    .setFontSize(8)
                    .setFontColor(com.itextpdf.kernel.colors.ColorConstants.GRAY)
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.CENTER);
            document.add(footer);

            String authCode = "AUTH-" + appt.getId() + "-" + payment.getAmount().intValue();
            document.add(new Paragraph("Verification Code: " + authCode)
                    .setFontSize(7).setItalic()
                    .setTextAlignment(com.itextpdf.layout.properties.TextAlignment.RIGHT));

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("PDF Generation Failed: " + e.getMessage());
        }
        return out.toByteArray();
    }
}