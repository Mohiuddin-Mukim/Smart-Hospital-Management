package com.tmukimi.hospital_management.services;

import com.itextpdf.barcodes.BarcodeQRCode;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.borders.Border;
import com.itextpdf.layout.borders.SolidBorder;
import com.itextpdf.layout.element.*;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.tmukimi.hospital_management.entities.Prescription;
import com.tmukimi.hospital_management.entities.PrescriptionMedicine;
import com.tmukimi.hospital_management.repositories.PrescriptionMedicineRepository;
import com.tmukimi.hospital_management.repositories.PrescriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PrescriptionPdfService {

    private final PrescriptionRepository prescriptionRepository;
    private final PrescriptionMedicineRepository pmRepository;

    public byte[] generatePrescriptionPdf(Long prescriptionId) {
        Prescription prescription = prescriptionRepository.findById(prescriptionId)
                .orElseThrow(() -> new RuntimeException("Prescription not found"));

        String loggedInUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        String patientEmail = prescription.getAppointment().getPatient().getUser().getEmail();
        boolean isDoctor = SecurityContextHolder.getContext().getAuthentication().getAuthorities()
                .stream().anyMatch(a -> a.getAuthority().equals("ROLE_DOCTOR"));

        if (!loggedInUserEmail.equals(patientEmail) && !isDoctor) {
            throw new AccessDeniedException("Unauthorized access to this prescription.");
        }

        List<PrescriptionMedicine> medicines = pmRepository.findByPrescriptionId(prescriptionId);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        try {
            PdfWriter writer = new PdfWriter(baos);
            PdfDocument pdf = new PdfDocument(writer);
            Document document = new Document(pdf);
            document.setMargins(20, 36, 36, 36);

            // --- HEADER SECTION ---
            Table headerTable = new Table(UnitValue.createPercentArray(new float[]{7, 3})).useAllAvailableWidth();

            Cell hospitalCell = new Cell().add(new Paragraph("T.MUKIMII HOSPITAL")
                            .setBold().setFontSize(24).setFontColor(new DeviceRgb(0, 102, 204)))
                    .add(new Paragraph("24/7 Specialized Medical Care & Diagnostic Center")
                            .setFontSize(9).setItalic().setFontColor(ColorConstants.GRAY))
                    .setBorder(Border.NO_BORDER);

            // --- DYNAMIC QR CODE FOR VERIFICATION ---
            // এখানে আপনার অরিজিনাল ডোমেইন নাম বসিয়ে দিন
            // PDF সার্ভিস ফাইলের ভেতরে এই অংশটি আপডেট করুন
            String verificationUrl = "http://localhost:8080/api/v1/public/verify/prescription/" + prescription.getVerificationToken();
            BarcodeQRCode qrCode = new BarcodeQRCode(verificationUrl);
            PdfFormXObject qrCodeObject = qrCode.createFormXObject(ColorConstants.BLACK, pdf);
            Image qrCodeImage = new Image(qrCodeObject).setWidth(60).setHorizontalAlignment(HorizontalAlignment.RIGHT);

            Cell qrCell = new Cell().add(qrCodeImage).setBorder(Border.NO_BORDER);

            headerTable.addCell(hospitalCell);
            headerTable.addCell(qrCell);
            document.add(headerTable);

            document.add(new LineSeparator(new com.itextpdf.kernel.pdf.canvas.draw.SolidLine(1f))
                    .setMarginTop(5).setFontColor(new DeviceRgb(0, 102, 204)));

            // --- DOCTOR & DATE INFO ---
            Table infoGrid = new Table(UnitValue.createPercentArray(new float[]{5, 5})).useAllAvailableWidth().setMarginTop(10);

            infoGrid.addCell(new Cell().add(new Paragraph("Dr. " + prescription.getAppointment().getDoctor().getName()).setBold().setFontSize(14))
                    .add(new Paragraph(prescription.getAppointment().getDoctor().getSpecialization()).setFontSize(10))
                    .setBorder(Border.NO_BORDER));

            infoGrid.addCell(new Cell().add(new Paragraph("Date: " + prescription.getCreatedAt().format(DateTimeFormatter.ofPattern("dd MMM, yyyy")))
                            .setTextAlignment(TextAlignment.RIGHT))
                    .add(new Paragraph("Prescription ID: #" + prescriptionId).setTextAlignment(TextAlignment.RIGHT).setFontSize(9))
                    .setBorder(Border.NO_BORDER));

            document.add(infoGrid);
            document.add(new Paragraph("\n"));

            // --- PATIENT VITALS BAR ---
            Table patientBar = new Table(UnitValue.createPercentArray(new float[]{3, 2, 2, 3})).useAllAvailableWidth();
            patientBar.setBackgroundColor(new DeviceRgb(245, 245, 245)).setPadding(5);

            patientBar.addCell(createVitalsCell("Patient: " + prescription.getAppointment().getPatient().getName(), true));
            patientBar.addCell(createVitalsCell("Age: " + (prescription.getAppointment().getPatient().getAge() != null ? prescription.getAppointment().getPatient().getAge() : "N/A"), false));
            patientBar.addCell(createVitalsCell("Weight: " + (prescription.getWeight() != null ? prescription.getWeight() : "-"), false));
            patientBar.addCell(createVitalsCell("BP: " + (prescription.getBloodPressure() != null ? prescription.getBloodPressure() : "-"), false));

            document.add(patientBar);

            // --- MAIN CONTENT (SIDEBAR + RX) ---
            Table contentTable = new Table(UnitValue.createPercentArray(new float[]{3, 7})).useAllAvailableWidth().setMarginTop(20);

            // Left Sidebar (Symptoms/Diagnosis)
            Cell sidebar = new Cell().setBorder(Border.NO_BORDER).setBorderRight(new SolidBorder(ColorConstants.LIGHT_GRAY, 0.5f)).setPaddingRight(10);
            sidebar.add(new Paragraph("Symptoms").setBold().setFontSize(11).setFontColor(ColorConstants.DARK_GRAY));
            sidebar.add(new Paragraph(prescription.getChiefComplaints() != null ? prescription.getChiefComplaints() : "N/A").setFontSize(9).setMarginBottom(10));

            sidebar.add(new Paragraph("Clinical Findings").setBold().setFontSize(11).setFontColor(ColorConstants.DARK_GRAY));
            sidebar.add(new Paragraph(prescription.getClinicalFindings() != null ? prescription.getClinicalFindings() : "None").setFontSize(9).setMarginBottom(10));

            sidebar.add(new Paragraph("Diagnosis").setBold().setFontSize(11).setFontColor(ColorConstants.DARK_GRAY));
            sidebar.add(new Paragraph(prescription.getDiagnosis() != null ? prescription.getDiagnosis() : "Under Observation").setFontSize(9));

            contentTable.addCell(sidebar);

            // Right Section (Rx/Medicines)
            Cell rxSection = new Cell().setBorder(Border.NO_BORDER).setPaddingLeft(15);
            rxSection.add(new Paragraph("Rx").setBold().setFontSize(28).setFontColor(new DeviceRgb(0, 102, 204)).setMarginBottom(10));

            int count = 1;
            for (PrescriptionMedicine pm : medicines) {
                Paragraph medTitle = new Paragraph(count + ". " + pm.getBrand().getBrandName().toUpperCase())
                        .setBold().setFontSize(12).setMarginBottom(0);
                rxSection.add(medTitle);

                Paragraph generic = new Paragraph(pm.getMedicine().getGenericName()).setFontSize(9).setItalic().setMarginBottom(2);
                rxSection.add(generic);

                Paragraph dose = new Paragraph(pm.getDosage() + " ---- " + pm.getDuration() + " (" + pm.getInstruction() + ")")
                        .setFontSize(10).setMarginBottom(10).setPaddingLeft(15);
                rxSection.add(dose);
                count++;
            }
            contentTable.addCell(rxSection);
            document.add(contentTable);

            // --- ADVICE ---
            document.add(new Paragraph("\nAdvice:").setBold().setUnderline().setMarginTop(20));
            document.add(new Paragraph(prescription.getAdvice() != null ? prescription.getAdvice() : "General rest and hydration."));

            // --- FOOTER & SIGNATURE ---
            Table footerTable = new Table(UnitValue.createPercentArray(new float[]{7, 3})).useAllAvailableWidth().setMarginTop(40);

            footerTable.addCell(new Cell().add(new Paragraph("Next Visit: " + (prescription.getNextVisitDate() != null ? prescription.getNextVisitDate() : "As needed"))
                    .setFontColor(ColorConstants.RED).setBold()).setBorder(Border.NO_BORDER));

            Cell sigCell = new Cell().add(new Paragraph("_________________")
                            .setTextAlignment(TextAlignment.CENTER))
                    .add(new Paragraph("Digital Signature")
                            .setFontSize(8).setItalic().setTextAlignment(TextAlignment.CENTER))
                    .setBorder(Border.NO_BORDER);
            footerTable.addCell(sigCell);

            document.add(footerTable);

            document.add(new Paragraph("\n\nGenerated by T.Mukimii HMS | " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
                    .setFontSize(7).setFontColor(ColorConstants.GRAY).setTextAlignment(TextAlignment.CENTER));

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error generating professional PDF: " + e.getMessage());
        }

        return baos.toByteArray();
    }

    private Cell createVitalsCell(String text, boolean isBold) {
        Paragraph p = new Paragraph(text).setFontSize(9);
        if (isBold) p.setBold();
        return new Cell().add(p).setBorder(Border.NO_BORDER).setPadding(5);
    }
}