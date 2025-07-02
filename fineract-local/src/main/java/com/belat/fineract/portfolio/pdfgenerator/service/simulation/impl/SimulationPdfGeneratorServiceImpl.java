package com.belat.fineract.portfolio.pdfgenerator.service.simulation.impl;

import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProject;
import com.belat.fineract.portfolio.investmentproject.domain.InvestmentProjectRepository;
import com.belat.fineract.portfolio.investmentproject.domain.commission.AdditionalExpenses;
import com.belat.fineract.portfolio.investmentproject.domain.commission.AdditionalExpensesRepository;
import com.belat.fineract.portfolio.investmentproject.domain.statushistory.StatusHistoryProject;
import com.belat.fineract.portfolio.investmentproject.domain.statushistory.StatusHistoryProjectEnum;
import com.belat.fineract.portfolio.investmentproject.domain.statushistory.StatusHistoryProjectRepository;
import com.belat.fineract.portfolio.pdfgenerator.api.PdfGeneratorConstants;
import com.belat.fineract.portfolio.pdfgenerator.service.simulation.SimulationPdfGeneratorService;
import com.google.gson.JsonElement;
import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.client.domain.ClientIdentifier;
import org.apache.fineract.portfolio.client.domain.ClientIdentifierRepository;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SimulationPdfGeneratorServiceImpl implements SimulationPdfGeneratorService {

    private final FromJsonHelper fromApiJsonHelper;
    private final ClientIdentifierRepository clientIdentifierRepository;
    private final InvestmentProjectRepository investmentProjectRepository;
    private final StatusHistoryProjectRepository statusHistoryProjectRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final AdditionalExpensesRepository additionalExpensesRepository;

    static class Watermark extends PdfPageEventHelper {
        private final String watermarkText;
        private final Font FONT;

        public Watermark(String watermarkText) throws Exception {
            this.watermarkText = watermarkText;
            this.FONT = new Font(Font.FontFamily.HELVETICA, 50, Font.NORMAL, new BaseColor(200, 200, 200));
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte canvas = writer.getDirectContent(); // Use getDirectContent() for overlay
            Phrase watermark = new Phrase(watermarkText, FONT);
            ColumnText.showTextAligned(
                    canvas,
                    Element.ALIGN_CENTER,
                    watermark,
                    (document.getPageSize().getWidth()) / 2,
                    (document.getPageSize().getHeight()) / 2,
                    45 // angle
            );
        }
    }

    @Override
    public String generateSimulationV1(String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        JsonElement jsonElement = fromApiJsonHelper.parse(json);

        Long projectId = fromApiJsonHelper.extractLongNamed(PdfGeneratorConstants.projectIdParamName, jsonElement);

        if (projectId == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.loan.id.is.null", "Loan id is null");
        }

        InvestmentProject investmentProject = investmentProjectRepository.retrieveOneByProjectId(projectId);

        if (investmentProject == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.investment.project.is.null", "Investment project is null");
        }

        Loan loan = investmentProject.getLoan();

        List<LoanRepaymentScheduleInstallment> list = loanRepositoryWrapper.getLoanRepaymentScheduleInstallments(loan.getId());

        LocalDateTime dateTime = DateUtils.getLocalDateTimeOfSystem();
        DateTimeFormatter formatterEndPage = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

        Document document = new Document();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            StatusHistoryProject lastStatus = statusHistoryProjectRepository.getLastStatusByInvestmentProjectId(investmentProject.getId());
            if (lastStatus != null && StatusHistoryProjectEnum.DRAFT.getCode().trim().equals(lastStatus.getStatusValue().getLabel().trim())) {
                writer.setPageEvent(new Watermark(StatusHistoryProjectEnum.DRAFT.getCode().trim()));
            }

            Image logo = Image.getInstance(new URL("https://s3.us-east-2.amazonaws.com/fineract.dev.public/logo-doble-impacto-con-bajada.png"));
            logo.scaleToFit(150, 50);

            // Evento para header/footer
            writer.setPageEvent(new HeaderFooterEvent(logo, "Fecha de emisión ".concat(dateTime.format(formatterEndPage))));

            float leftMargin = 38f;
            float rightMargin = 38f;
            float topMargin = 110f;
            float bottomMargin = 50f;

            document.setMargins(leftMargin, rightMargin, topMargin, bottomMargin);

            document.open();

            BaseColor purpleBackground = new BaseColor(0x51, 0x28, 0x5F);
            BaseColor whiteText = BaseColor.WHITE;

            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, purpleBackground);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, purpleBackground);

            document.add(createSimulationTableInfo(list, investmentProject, purpleBackground, whiteText));

            // Párrafo 1
            Paragraph p1 = new Paragraph();
            p1.setSpacingBefore(20f);
            p1.setSpacingAfter(10f);
            p1.setAlignment(Element.ALIGN_JUSTIFIED);
            Chunk bullet1 = new Chunk("- ", boldFont);
            Chunk text1 = new Chunk("La entrega de esta simulación no implica que el financiamiento se encuentre aprobado. La aprobación y sus condiciones están sujetas a la evaluación de impacto y riesgo crédito por parte de Doble Impacto. Las condiciones estipuladas tienen una validez de 7 (siete) días.", boldFont);
            p1.add(bullet1);
            p1.add(text1);

            // Párrafo 2
            Paragraph p2 = new Paragraph();
            p2.setSpacingAfter(20f);
            p2.setAlignment(Element.ALIGN_JUSTIFIED);
            Chunk bullet2 = new Chunk("- ", normalFont);
            Chunk text2 = new Chunk("Costo Total de financiamiento incluye asesoría de estructura financiera, impuestos, gastos notariales, intereses y otros gastos (tasaciones, inscripciones, seguros, otros).", normalFont);
            p2.add(bullet2);
            p2.add(text2);

            // Agregar al documento
            document.add(p1);
            document.add(p2);

            document.add(createInstallmentsTable(list, loan, purpleBackground, whiteText));

            document.close();

            byte[] pdfBytes = baos.toByteArray();

            return "{\"pdfBase64\":\"" + Base64.getEncoder().encodeToString(pdfBytes) + "\"}";

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String formatNumberWithThousandsSeparatorAndTwoDecimals(double number) {
        return String.format("%.2f", number).replace(".", ",").replaceAll("\\B(?=(\\d{3})+(?!\\d))", ".");
    }

    private PdfPTable createSimulationTableInfo (List<LoanRepaymentScheduleInstallment> list, InvestmentProject investmentProject, BaseColor purpleBackground, BaseColor whiteText) {

        ClientIdentifier projectOwnerDocument = clientIdentifierRepository.retrieveByClientId(investmentProject.getOwner().getId());

        if (projectOwnerDocument == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.client.does.not.have.client.identifier", "El cliente no tiene documento de identidad registrado");
        }

        List<AdditionalExpenses> additionalExpenses = additionalExpensesRepository.findByInvestmentProjectId(investmentProject.getId());

        BigDecimal aefCommission = BigDecimal.ZERO;
        BigDecimal aefCommissionIva = BigDecimal.ZERO;
        BigDecimal iteCommission = BigDecimal.ZERO;
        BigDecimal otherExpenses = BigDecimal.ZERO;
        BigDecimal notarialExpenses = BigDecimal.ZERO;
        BigDecimal aefTax = BigDecimal.ZERO;

        for (AdditionalExpenses item : additionalExpenses){
            String codeValue = item.getCommissionType().getLabel();

            switch (codeValue) {
                case "AEF":
                    aefCommission = aefCommission.add(item.getTotal());
                    aefTax = item.getVat();
                    break;
                case "IVA-AEF":
                    aefCommissionIva = aefCommissionIva.add(item.getTotal());
                    break;
                case "ITE":
                    iteCommission = iteCommission.add(item.getTotal());
                    break;
                case "OTROS GASTOS":
                    otherExpenses = otherExpenses.add(item.getTotal());
                    break;
                case "MONTO FACTURA":
                    notarialExpenses = notarialExpenses.add(item.getTotal());
                    break;
            }
        }

        BigDecimal total = aefCommission.add(aefCommissionIva.add(iteCommission.add(otherExpenses.add(
                notarialExpenses.add(investmentProject.getLoan().getNetDisbursalAmount())))));

        BigDecimal totalOperation = aefCommission.add(aefCommissionIva.add(iteCommission.add(otherExpenses.add(
                notarialExpenses))));

        // Tabla con 4 columnas
        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(95);
        table.setSpacingBefore(20f);

        // Encabezado general que abarca las 4 columnas
        PdfPCell headerGeneral = new PdfPCell(new Paragraph("Simulación Financiamiento", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, whiteText)));
        headerGeneral.setColspan(2);
        headerGeneral.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerGeneral.setBackgroundColor(purpleBackground);
        headerGeneral.setPadding(10f);
        headerGeneral.setBorderColor(BaseColor.WHITE);
        table.addCell(headerGeneral);

        BaseColor rowColor1 = new BaseColor(0xEB, 0xE6, 0xED);
        BaseColor rowColor2 = new BaseColor(0xD3, 0xCC, 0xD9);


        Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10, purpleBackground);

        double[] doubleArray = new double[list.size() + 1];

        doubleArray[0] = investmentProject.getLoan().getNetDisbursalAmount().doubleValue() * -1;

        for (int i = 1; i < list.size(); i++) {
            doubleArray[i] = list.get(i).getTotalPrincipalAndInterest(investmentProject.getLoan().getCurrency()).getAmount().doubleValue();
        }

        // Simular filas con dos columnas (cada celda usa colspan 2)
        String[][] rows = {
                {"Nombre Cliente", investmentProject.getLoan().getClient().getDisplayName()},
                {"RUT", projectOwnerDocument.documentKey()},
                {"Monto Financiamiento", "$" + formatNumberWithThousandsSeparatorAndTwoDecimals(total.setScale(2, RoundingMode.HALF_EVEN).doubleValue())},
                {"Asesoría Estructura Financiera (1) (AEF Banca Ética)", "$" + formatNumberWithThousandsSeparatorAndTwoDecimals(aefCommission.longValue())},
                {"IVA Asesoría Estructura Financiera (2) (AEF Banca Ética)", "$" + formatNumberWithThousandsSeparatorAndTwoDecimals(aefCommissionIva.longValue())},
                {"Impuesto Timbres y Estampillas (3)", "$" + formatNumberWithThousandsSeparatorAndTwoDecimals(iteCommission.longValue())},
                {"Gastos Notariales (4)", "$" + formatNumberWithThousandsSeparatorAndTwoDecimals(notarialExpenses.longValue())},
                {"Otros Gastos (5)", "$" + formatNumberWithThousandsSeparatorAndTwoDecimals(otherExpenses.longValue())},
                {"Gasto Total Operación (1)+(2)+(3)+(4)+(5)", "$" + formatNumberWithThousandsSeparatorAndTwoDecimals(totalOperation.longValue())},
                {"Monto a Entregar Cliente", "$" + formatNumberWithThousandsSeparatorAndTwoDecimals(investmentProject.getLoan().getNetDisbursalAmount().longValue())},
                {"CAE", MathUtil.calculateAnnualIRR(doubleArray) + "%"},
                {"Tasa AEF Banca Ética (anual)", aefTax.setScale(2, RoundingMode.HALF_EVEN) + "%"},
                {"Tasa Retorno Inversionista (anual)", investmentProject.getRate().setScale(2, RoundingMode.HALF_EVEN) + "%"},
                {"Plazo (meses)", String.valueOf(investmentProject.getLoan().getTermFrequency())},
                {"Amortización", "Libre"},
                {"Costo Total Financiamiento", ""},
        };

        for (int i = 0; i < rows.length; i++) {
            BaseColor bgColor;
            if (i == 2 || i == 9 || i == 15) {
                bgColor = rowColor2;
            } else {
                bgColor = rowColor1;
            }

            PdfPCell labelCell = new PdfPCell(new Paragraph(rows[i][0], dataFont));
            labelCell.setColspan(1);
            labelCell.setBackgroundColor(bgColor);
            labelCell.setBorderColor(BaseColor.WHITE);
            labelCell.setPadding(5f);
            table.addCell(labelCell);

            PdfPCell valueCell = new PdfPCell(new Paragraph(rows[i][1], dataFont));
            valueCell.setColspan(2);
            valueCell.setBackgroundColor(bgColor);
            valueCell.setBorderColor(BaseColor.WHITE);
            valueCell.setPadding(5f);
            valueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(valueCell);
        }

        return table;
    }

    private static PdfPTable createInstallmentsTable (List<LoanRepaymentScheduleInstallment> list, Loan loan, BaseColor purpleBackground, BaseColor whiteText ) {
        // Tabla con 4 columnas
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(95);
        table.setSpacingBefore(20f);

        // Encabezado general que abarca las 4 columnas
        PdfPCell headerGeneral = new PdfPCell(new Paragraph("Estructura de Cuotas (" + loan.getCurrencyCode() + ")", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, whiteText)));
        headerGeneral.setColspan(4);
        headerGeneral.setHorizontalAlignment(Element.ALIGN_CENTER);
        headerGeneral.setBackgroundColor(purpleBackground);
        headerGeneral.setPadding(10f);
        headerGeneral.setBorderColor(BaseColor.WHITE);
        table.addCell(headerGeneral);

        // Encabezados de columna
        PdfPCell header1 = new PdfPCell(new Paragraph("Cuota", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, whiteText)));
        header1.setBackgroundColor(purpleBackground);
        header1.setHorizontalAlignment(Element.ALIGN_CENTER);
        header1.setPadding(5f);
        header1.setBorderColor(BaseColor.WHITE);
        table.addCell(header1);

        PdfPCell header2 = new PdfPCell(new Paragraph("Amortización", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, whiteText)));
        header2.setBackgroundColor(purpleBackground);
        header2.setHorizontalAlignment(Element.ALIGN_CENTER);
        header2.setPadding(5f);
        header2.setBorderColor(BaseColor.WHITE);
        table.addCell(header2);

        PdfPCell header3 = new PdfPCell(new Paragraph("Interés", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, whiteText)));
        header3.setBackgroundColor(purpleBackground);
        header3.setHorizontalAlignment(Element.ALIGN_CENTER);
        header3.setPadding(5f);
        header3.setBorderColor(BaseColor.WHITE);
        table.addCell(header3);

        PdfPCell header4 = new PdfPCell(new Paragraph("Valor Cuota", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, whiteText)));
        header4.setBackgroundColor(purpleBackground);
        header4.setHorizontalAlignment(Element.ALIGN_CENTER);
        header4.setPadding(5f);
        header4.setBorderColor(BaseColor.WHITE);
        table.addCell(header4);

        // Aquí se indica que las primeras 2 filas (el header general y el de columnas) se repiten en cada página
        table.setHeaderRows(2);

        BaseColor rowColor1 = new BaseColor(0xEB, 0xE6, 0xED);
        BaseColor rowColor2 = new BaseColor(0xD3, 0xCC, 0xD9);

        // Filas de datos
        for (int i = 0; i < list.size(); i++) {
            LoanRepaymentScheduleInstallment item = list.get(i);
            BaseColor backgroundColor = (i % 2 == 0) ? rowColor1 : rowColor2;

            Font dataFont = FontFactory.getFont(FontFactory.HELVETICA, 10, purpleBackground);

            PdfPCell cell1 = new PdfPCell(new Paragraph(String.valueOf(item.getInstallmentNumber()), dataFont));
            cell1.setPadding(5f);
            cell1.setBorderColor(BaseColor.WHITE);
            cell1.setBackgroundColor(backgroundColor);
            cell1.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell1);

            BigDecimal principal = item.getPrincipal();
            PdfPCell cell2 = new PdfPCell(new Paragraph("$" + (principal != null ? formatNumberWithThousandsSeparatorAndTwoDecimals(principal.setScale(2, RoundingMode.HALF_EVEN).doubleValue()) : "0"), dataFont));
            cell2.setPadding(5f);
            cell2.setBorderColor(BaseColor.WHITE);
            cell2.setBackgroundColor(backgroundColor);
            cell2.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell2);

            BigDecimal interest = item.getInterestCharged();
            PdfPCell cell3 = new PdfPCell(new Paragraph("$" + (interest != null ? formatNumberWithThousandsSeparatorAndTwoDecimals(interest.setScale(2, RoundingMode.HALF_EVEN).doubleValue()) : "0"), dataFont));
            cell3.setPadding(5f);
            cell3.setBorderColor(BaseColor.WHITE);
            cell3.setBackgroundColor(backgroundColor);
            cell3.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell3);

            BigDecimal totalOutstanding = item.getTotalOutstanding(MonetaryCurrency.fromCurrencyData(loan.getCurrency().toData())).getAmount();
            PdfPCell cell4 = new PdfPCell(new Paragraph("$" + formatNumberWithThousandsSeparatorAndTwoDecimals(totalOutstanding.setScale(2, RoundingMode.HALF_EVEN).doubleValue()), dataFont));
            cell4.setPadding(5f);
            cell4.setBorderColor(BaseColor.WHITE);
            cell4.setBackgroundColor(backgroundColor);
            cell4.setHorizontalAlignment(Element.ALIGN_RIGHT);
            table.addCell(cell4);
        }
        return table;
    }

    private static class HeaderFooterEvent extends PdfPageEventHelper {
        private final Image logo;
        private final String footerText;
        private PdfTemplate totalPageTemplate;
        private BaseFont baseFont;

        public HeaderFooterEvent(Image logo, String footerText) {
            this.logo = logo;
            this.footerText = footerText;
        }

        @Override
        public void onOpenDocument(PdfWriter writer, Document document) {
            totalPageTemplate = writer.getDirectContent().createTemplate(50, 50);
            try {
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.WINANSI, BaseFont.NOT_EMBEDDED);
            } catch (Exception e) {
                throw new RuntimeException("Error initializing base font", e);
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();
            int pageNumber = writer.getPageNumber();
            Rectangle pageSize = document.getPageSize();

            if (logo != null) {
                try {
                    float imageWidth = logo.getScaledWidth();
                    float x = (document.right() + document.left() - imageWidth) / 2;
                    float y = document.getPageSize().getTop() - 60; // Ajuste vertical fijo

                    logo.setAbsolutePosition(x, y);
                    cb.addImage(logo, false);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase(footerText, FontFactory.getFont(FontFactory.HELVETICA, 8, BaseColor.GRAY)),
                    document.right(), document.bottom() - 20, 0);

            // Page number: "1/X"
            String text = pageNumber + "/";
            float textBase = pageSize.getBottom() + 30;
            float textSize = baseFont.getWidthPoint(text, 9);
            float x = (pageSize.getLeft() + pageSize.getRight()) / 2;

            cb.beginText();
            cb.setFontAndSize(baseFont, 9);
            cb.setTextMatrix(x - textSize / 2, textBase);
            cb.showText(text);
            cb.endText();

            // Reserve space for total page count
            cb.addTemplate(totalPageTemplate, x - textSize / 2 + baseFont.getWidthPoint(text, 9), textBase);
        }

        @Override
        public void onCloseDocument(PdfWriter writer, Document document) {
            int totalPageCount = writer.getPageNumber() - 1;
            String totalText = String.valueOf(totalPageCount);
            totalPageTemplate.beginText();
            totalPageTemplate.setFontAndSize(baseFont, 9);
            totalPageTemplate.setTextMatrix(0, 0);
            totalPageTemplate.showText(totalText);
            totalPageTemplate.endText();
        }
    }
}
