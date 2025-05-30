package com.belat.fineract.portfolio.pdfgenerator.service.promissory.impl;

import com.belat.fineract.portfolio.pdfgenerator.api.PdfGeneratorConstants;
import com.belat.fineract.portfolio.pdfgenerator.service.promissory.FundPromissoryPdfGeneratorService;
import com.belat.fineract.portfolio.pdfgenerator.utils.NumberToWordEs;
import com.belat.fineract.portfolio.promissorynote.domain.PromissoryNote;
import com.belat.fineract.portfolio.promissorynote.domain.PromissoryNoteRepository;
import com.google.gson.JsonElement;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.List;
import com.lowagie.text.ListItem;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.ColumnText;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientIdentifier;
import org.apache.fineract.portfolio.client.domain.ClientIdentifierRepository;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformServiceCommon;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Locale;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FundPromissoryPdfGeneratorServiceImpl implements FundPromissoryPdfGeneratorService {

    private final PromissoryNoteRepository promissoryNoteRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final ClientIdentifierRepository clientIdentifierRepository;
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final LoanReadPlatformServiceCommon loanReadPlatformServiceCommon;
    private final LoanRepository loanRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;

    @Override
    public String generateFundPromissoryNoteV1(String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        JsonElement jsonElement = fromApiJsonHelper.parse(json);
        Long promissoryId = fromApiJsonHelper.extractLongNamed(PdfGeneratorConstants.promissoryIdParamName, jsonElement);

        if (promissoryId == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.promissory.id.is.null", "Promissory id is null");
        }

        PromissoryNote promissoryNoteData = promissoryNoteRepository.retrieveOneByPromissoryNoteId(promissoryId);

        if (promissoryNoteData == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.promissory.note.is.null", "Promissory note is null");
        }

        SavingsAccount fundSavingsAccountData = promissoryNoteData.getFundSavingsAccount();

        PortfolioAccountData portfolioAccountData = savingsAccountReadPlatformService.retriveSavingsLinkedAssociation(fundSavingsAccountData.getId());

        Optional<Loan> temporallyLoan = loanRepository.findById(portfolioAccountData.getId()).stream().findFirst();

        Loan loan = null;

        if (temporallyLoan.isPresent()) {
            loan = temporallyLoan.get();
        } else {
            throw new GeneralPlatformDomainRuleException("error.msg.fund.association.not.found", "Fund association not found");
        }

        SavingsAccount investorSavingAccount = promissoryNoteData.getInvestorSavingsAccount();

        Client investor = investorSavingAccount.getClient();

        Client projectOwner = fundSavingsAccountData.getClient();

        ClientIdentifier investorDocument = clientIdentifierRepository.retrieveByClientId(investor.getId());

        if (investorDocument == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.client.does.not.have.client.identifier", "El cliente no tiene documento de identidad registrado");
        }

        ClientIdentifier projectOwnerDocument = clientIdentifierRepository.retrieveByClientId(projectOwner.getId());

        if (projectOwnerDocument == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.client.does.not.have.client.identifier", "El cliente no tiene documento de identidad registrado");
        }

        LocalDate date = DateUtils.getBusinessLocalDate();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        DateTimeFormatter formatterEndPage = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String formattedDate = date.format(formatter);

        Document document = new Document();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(document, baos);

            Image logo = Image.getInstance(new URL("https://s3.us-east-2.amazonaws.com/fineract.dev.public/logo-doble-impacto-con-bajada.png"));

            writer.setPageEvent(new HeaderFooterEvent(logo, projectOwner.getDisplayName(), promissoryNoteData.getId(), promissoryNoteData.getPromissoryNoteNumber(), date.format(formatterEndPage)));

            document.open();

            float leftMargin = 38f;
            float rightMargin = 38f;
            float topMargin = 72f;
            float bottomMargin = 50f;

            document.setMargins(leftMargin, rightMargin, topMargin, bottomMargin);

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("PAGARÉ", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);

            Paragraph num = new Paragraph("Número: ".concat(String.valueOf(promissoryNoteData.getId())));
            num.setAlignment(Element.ALIGN_RIGHT);
            document.add(num);

            Paragraph cod = new Paragraph("Código: ".concat(promissoryNoteData.getPromissoryNoteNumber()));
            cod.setAlignment(Element.ALIGN_RIGHT);
            document.add(cod);

            document.add(Chunk.NEWLINE);

            document.add(createSingleParagraph("Debo y pagaré incondicionalmente a la orden de ".concat(investor.getDisplayName()).concat(" cédula " +
                    ("nacional de identidad ".concat(investorDocument.documentKey()).concat(" en adelante el(los) “Acreedor(es)”, la suma de " +
                            "$").concat(String.valueOf(promissoryNoteData.getInvestmentAmount())).concat(
                            ". - (".concat(NumberToWordEs.convert(promissoryNoteData.getInvestmentAmount().setScale(2, RoundingMode.HALF_EVEN).longValue())).concat(" pesos), " +
                                    "moneda legal, cantidad que he recibido en préstamo de el(los) Acreedor(es) a entera satisfacción."))))));

            document.add(Chunk.NEWLINE);

            document.add(createBoldParagraph("Interés: ", "El capital adeudado devengará un interés del ".concat(
                    String.valueOf(loanReadPlatformServiceCommon.getLoanAnnualInterestRate(loan.getId()).setScale(2,
                            RoundingMode.HALF_EVEN))).concat("% anual, vencido desde la fecha de " +
                    "suscripción de este pagaré hasta su pago íntegro y efectivo.")));

            document.add(Chunk.NEWLINE);

            //interestCalculationParagraph
            document.add(createBoldParagraph("Cálculo de Intereses: ", "Las tasas mensuales de interés se refieren a meses de 30 días. Los intereses se " +
                    "calcularán y pagarán por días corridos y sobre el total del capital que se esté adeudando hasta el día de " +
                    "su pago efectivo."));

            document.add(Chunk.NEWLINE);

            //bodyText2
            document.add(createSingleParagraph("El capital total adeudado de $".concat(String.valueOf(promissoryNoteData.getInvestmentAmount())).concat(".- ("
                    .concat(NumberToWordEs.convert(promissoryNoteData.getInvestmentAmount()
                            .setScale(2, RoundingMode.HALF_EVEN).longValue())).concat(" se pagará en las " +
                            "siguientes cuotas, que se devengarán en las siguientes fechas de vencimiento:"))));

            document.add(Chunk.NEWLINE);

            java.util.List<LoanRepaymentScheduleInstallment> list = loanRepositoryWrapper.getLoanRepaymentScheduleInstallments(loan.getId());

            List bulletList = new List(List.UNORDERED);

            for (int i = 1; i < list.size(); i++) {
                LoanRepaymentScheduleInstallment installment = list.get(i);
                LocalDate dueDate = installment.getDueDate();
                formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
                formattedDate = dueDate.format(formatter);
                BigDecimal totalOutstanding = installment.getTotalOutstanding(MonetaryCurrency.fromCurrencyData(loan.getCurrency().toData())).getAmount();

                bulletList.add(new ListItem(formattedDate
                        .concat(", $" + totalOutstanding.setScale(2, RoundingMode.HALF_EVEN)).concat(
                                ".- (" + NumberToWordEs.convert(totalOutstanding.longValue()) + ")")));
            }

            document.add(bulletList);

            document.add(Chunk.NEWLINE);

            //accordingDateParagraph
            document.add(createBoldParagraph("Lugar y hora en que se efectuará el pago: ", "Todos los pagos de capital y/ o intereses se efectuarán" +
                    "mediante depósito o transferencia a la cuenta bancaria Nº 1000-10-569, del Banco Estado, cuyo titular" +
                    "es la empresa Doble Impacto SpA. Rut Nº 76.792.913-7, informándolo al correo electrónico" +
                    "transacciones@dobleimpacto.cl, quien es la mandataria para el cobro. El pago debe efectuarse antes de" +
                    "las 12:00 horas del día del vencimiento que corresponda."));

            document.add(Chunk.NEWLINE);

            //uninhabitedDaysParagraph
            document.add(createBoldParagraph("Días inhábiles, prórroga de pago: ", "Cualquier fecha para el pago de capital y/ o intereses que" +
                    "corresponda a día inhábil prorrogará el plazo hasta el día hábil inmediatamente siguiente, debiendo el" +
                    "respectivo pago incluir los intereses que correspondan a los días comprendidos en la prórroga."));

            document.add(Chunk.NEWLINE);

            //arrearsInterestParagraph
            document.add(createBoldParagraph("Intereses por retardo: ", "En caso de mora o simple retardo en el pago de todo o parte del capital e " +
                    "intereses, se capitalizarán los intereses devengados y este pagaré devengará por todo el lapso de la " +
                    "mora o retardo, el interés máximo convencional que la ley permite estipular para operaciones de crédito " +
                    "de dinero en moneda nacional."));

            document.add(Chunk.NEWLINE);

            //bodyText3
            document.add(createSingleParagraph("Lo anterior es sin perjuicio de la facultad de el(los) Acreedor(es) de hacer exigible el total de lo " +
                    "adeudado, en conformidad a lo estipulado en este pagaré."));

            document.add(Chunk.NEWLINE);

            //maturityAcelerationsParagraph
            document.add(createBoldParagraph("Aceleración del vencimiento: ", "El(los) Acreedor(es) tendrá(n) derecho a hacer inmediatamente exigible la " +
                    "totalidad del crédito como si fuera de plazo vencido en los siguientes casos:"));

            document.add(Chunk.NEWLINE);

            // Create word lists a., b., c., etc.
            List wordList = new List(List.ORDERED, List.ALPHABETICAL);
            wordList.setLowercase(List.LOWERCASE);
            wordList.setPreSymbol("");
            wordList.setPostSymbol(". ");

            // Agregar elementos
            wordList.add(new ListItem("Si el suscriptor u otro obligado al pago del presente pagaré cayera en insolvencia, entendiéndose para " +
                    "todos los efectos que existe notoria insolvencia de su parte si cesara en el pago de cualquiera " +
                    "obligación que hubiere contraído o contraiga en el futuro para con el(los) Acreedor(es), Doble Impacto " +
                    "SpA o para con cualquiera otra persona; si el mismo o uno más de sus acreedores solicitan su quiebra " +
                    "o formulan proposiciones de convenio extrajudicial o judicial; si por la vía de medidas prejudiciales o " +
                    "precautorias se obtienen en su contra secuestros, retenciones, prohibiciones de celebrar actos o " +
                    "contratos respecto de cualquiera de sus bienes o el nombramiento de interventores; si se trabare " +
                    "embargo respecto de cualquiera de sus bienes o si ocurriere otro hecho que también ponga en " +
                    "evidencia una notoria insolvencia de su parte;"));
            wordList.add(new ListItem("Si el suscriptor no destina los fondos al objeto informado a Doble Impacto SpA; o"));
            wordList.add(new ListItem("Si el suscriptor no da cumplimiento a las condiciones sobre las cuales fue aprobado este crédito."));

            document.add(wordList);

            document.add(Chunk.NEWLINE);

            //Indivisibility
            document.add(createBoldParagraph("Indivisibilidad: ", "Todas las obligaciones derivadas de este pagaré se considerarán indivisibles para el\n" +
                    "suscriptor y/o sucesores, para todos los efectos legales."));

            document.add(Chunk.NEWLINE);

            //Prepays
            document.add(createBoldParagraph("Prepagos: ", "En caso de prepago total o parcial de la obligación, éste se distribuirá entre cada Acreedor a " +
                    "prorrata del monto adeudado a cada uno. Adicionalmente, existirá una comisión de prepago en beneficio " +
                    "de el(los) acreedor(es) según detalle adjunto, equivalente a un mes de intereses pactados calculados " +
                    "sobre el capital que se prepaga, en cumplimiento del límite del artículo 10 de la ley 18.010 sobre " +
                    "operaciones de crédito de dinero."));

            document.add(Chunk.NEWLINE);

            //Authorizations
            document.add(createBoldParagraph("Autorización Ley 19.628: ", "En caso de mora o simple retardo en el pago del total o de una de las cuotas " +
                    "convenidas en este pagaré, de conformidad con lo dispuesto en el artículo 4° de la Ley N° 19.628, el " +
                    "suscriptor autoriza al acreedor para comunicar dicha circunstancia a cualquier banco de datos que se " +
                    "dedique al tratamiento de datos de carácter personal."));

            document.add(Chunk.NEWLINE);

            //Objection
            document.add(createBoldParagraph("Protesto: ", "Libero a el(los) Acreedor(es) de la obligación de protesto. En todo caso, en el evento de\n" +
                    "protesto me obligo a pagar los gastos e impuestos que se devengaren con este motivo."));

            document.add(Chunk.NEWLINE);

            //Taxes, duties and expenses
            document.add(createBoldParagraph("Impuesto, derechos y gastos: ", "Cualquier impuesto, derechos o gastos que se devenguen con ocasión de " +
                    "este pagaré, su modificación, prórroga, pago u otra circunstancia relativa a aquél o producida con " +
                    "ocasión o motivo del mismo, será de cargo exclusivo del suscriptor."));

            document.add(Chunk.NEWLINE);

            document.add(createSingleParagraph("Exención Impuesto de Timbres y Estampillas: Este pagaré está exento del Impuestos de Timbre y Estampillas " +
                    "conforme a lo prescrito en el Decreto Ley 3.475 Artículo 23° el cual señala en su numeral 10: “Sólo estarán " +
                    "exentas de los impuestos que establece el presente decreto ley, las siguientes personas e instituciones: 10.- " +
                    "Fundación Niño y Patria”."));

            document.add(Chunk.NEWLINE);

            document.add(createBoldParagraph("Domicilio y Jurisdicción: ", "Para todos los efectos legales derivados del presente pagaré, el deudor o " +
                    "suscriptor constituye domicilio especial en la comuna y ciudad de Santiago y se somete a la Jurisdicción " +
                    "de sus Tribunales Ordinarios de Justicia, domicilio que también será lugar hábil para las diligencias de " +
                    "protesto en caso de practicarse."));

            document.add(Chunk.NEWLINE);

            document.add(createSingleParagraph("Razón social del suscriptor  :  ".concat(projectOwner.getDisplayName())));
            document.add(createSingleParagraph("Rut : ".concat(projectOwnerDocument.documentKey())));
            document.add(createSingleParagraph("Domicilio  :  Valenzuela Castillo 1520, 7500700 Providencia, Región Metropolitana, Chile"));

            document.add(createSignatureParagraph());

            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);
            document.add(Chunk.NEWLINE);

            document.add(createSingleParagraph("En Santiago de Chile a ".concat(formattedDate)));

            document.newPage();

            Font titleFontAppendix = new Font(Font.HELVETICA, 14, Font.BOLD);
            Paragraph titleAppendix = new Paragraph("ANEXO", titleFontAppendix);
            titleAppendix.setAlignment(Element.ALIGN_CENTER);
            document.add(titleAppendix);

            document.add(Chunk.NEWLINE);

            document.add(createSingleParagraph("Relacionado con el pagaré suscrito el " + formattedDate + " por " +
                            investor.getDisplayName() + ", el monto de " +
                    "capital $" + promissoryNoteData.getInvestmentAmount() + ".- (" +
                    NumberToWordEs.convert(promissoryNoteData.getInvestmentAmount().longValue()) + " pesos), se desglosa de la siguiente manera:"));

            document.add(Chunk.NEWLINE);

            document.add(createSingleParagraph("A la orden de " + investor.getDisplayName() + ", cedula nacional de identidad "
                    + investorDocument.documentKey() + ", o a quién " +
                    "sus derechos represente la suma de $" + promissoryNoteData.getInvestmentAmount() + ".- (" +
                    NumberToWordEs.convert(promissoryNoteData.getInvestmentAmount().longValue()) + " pesos) moneda de curso legal, por concepto de " +
                    "capital, que de dicha persona recibí a mi entera satisfacción;"));

            document.add(Chunk.NEWLINE);

            document.add(createSingleParagraph("Razón social del suscriptor  :  ".concat(projectOwner.getDisplayName())));
            document.add(createSingleParagraph("Rut : ".concat(projectOwnerDocument.documentKey())));
            document.add(createSingleParagraph("Domicilio  :  Valenzuela Castillo 1520, 7500700 Providencia, Región Metropolitana, Chile"));

            document.add(createSignatureParagraph());

            document.close();

            byte[] pdfBytes = baos.toByteArray();
            return Base64.getEncoder().encodeToString(pdfBytes);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private static Paragraph createSingleParagraph (String text) {
        Paragraph bodyParagraph = new Paragraph(text);
        bodyParagraph.setAlignment(Element.ALIGN_JUSTIFIED);
        return bodyParagraph;
    }

    private static Paragraph createBoldParagraph (String boldText, String singleValue) {
        Paragraph paragraph = new Paragraph();


        Font normalFont = new Font(Font.HELVETICA, 12, Font.NORMAL);
        Font boldFont = new Font(Font.HELVETICA, 12, Font.BOLD);

        paragraph.add(new Chunk(boldText, boldFont));

        paragraph.add(new Chunk(singleValue, normalFont));

        paragraph.setAlignment(Element.ALIGN_JUSTIFIED);

        return paragraph;

    }

    private static Paragraph createSignatureParagraph () {
        Paragraph signatureParagraph = new Paragraph();
        Font signatureFont = new Font(Font.HELVETICA, 12, Font.NORMAL);
        signatureParagraph.setFont(signatureFont);
        signatureParagraph.setSpacingBefore(50f);


        String leftSignature = "      _________________________";
        String rightSignature = "_________________________";

        String spaces = "                                        ";

        signatureParagraph.add(leftSignature + spaces + rightSignature);
        return signatureParagraph;
    }

    static class HeaderFooterEvent extends PdfPageEventHelper {
        private final Image img;
        private final Font footerFont = new Font(Font.HELVETICA, 9, Font.NORMAL);
        private final String projectOwner;
        private final Long promissoryNoteId;
        private final String promissoryNoteCode;
        private final String dateString;

        public HeaderFooterEvent(Image img, String projectOwner, Long promissoryNoteId, String promissoryNoteCode, String dateString) {
            this.img = img;
            this.projectOwner = projectOwner;
            this.promissoryNoteId = promissoryNoteId;
            this.promissoryNoteCode = promissoryNoteCode;
            this.dateString = dateString;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document doc) {
            try {
                Rectangle pageSize = doc.getPageSize();
                float marginLeft = 10f;
                float marginTop = 10f;

                float maxWidth = 150f;
                if (img.getScaledWidth() > maxWidth) {
                    img.scaleToFit(maxWidth, img.getScaledHeight());
                }

                img.setAbsolutePosition(marginLeft, pageSize.getTop() - img.getScaledHeight() - marginTop);
                PdfContentByte canvas = writer.getDirectContent();
                canvas.addImage(img);

                String footerText = "Página " + writer.getPageNumber() + " - Continuación de Pagaré Nº" + this.promissoryNoteId +
                        " - " + this.promissoryNoteCode + ", suscrito por " + this.projectOwner + " con fecha " + dateString;
                Phrase footer = new Phrase(footerText, footerFont);

                ColumnText.showTextAligned(canvas, Element.ALIGN_CENTER, footer,
                        (doc.right() + doc.left()) / 2, doc.bottom() - 10, 0);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
