package com.belat.fineract.portfolio.pdfgenerator.service.promissory.impl;

import com.belat.fineract.portfolio.pdfgenerator.api.PdfGeneratorConstants;
import com.belat.fineract.portfolio.pdfgenerator.service.promissory.FundPromissoryPdfGeneratorService;
import com.belat.fineract.portfolio.pdfgenerator.utils.NumberToWordEs;
import com.belat.fineract.portfolio.promissorynote.domain.PromissoryNote;
import com.belat.fineract.portfolio.promissorynote.domain.PromissoryNoteRepository;
import com.google.gson.JsonElement;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.Image;
import com.itextpdf.text.List;
import com.itextpdf.text.ListItem;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.core.exception.InvalidJsonException;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.address.data.AddressData;
import org.apache.fineract.portfolio.address.service.AddressReadPlatformService;
import org.apache.fineract.portfolio.client.data.ClientFamilyMembersData;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientIdentifier;
import org.apache.fineract.portfolio.client.domain.ClientIdentifierRepository;
import org.apache.fineract.portfolio.client.service.ClientFamilyMembersReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.service.LoanReadPlatformServiceCommon;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.portfolio.savings.service.SavingsAccountReadPlatformService;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;
import java.util.Collection;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FundPromissoryPdfGeneratorServiceImpl implements FundPromissoryPdfGeneratorService {

    private final PromissoryNoteRepository promissoryNoteRepository;
    private final FromJsonHelper fromApiJsonHelper;
    private final ClientIdentifierRepository clientIdentifierRepository;
    private final SavingsAccountRepositoryWrapper savingsAccountRepositoryWrapper;
    private final SavingsAccountReadPlatformService savingsAccountReadPlatformService;
    private final LoanReadPlatformServiceCommon loanReadPlatformServiceCommon;
    private final LoanRepository loanRepository;
    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final AddressReadPlatformService addressReadPlatformService;
    private final ClientFamilyMembersReadPlatformService familyMembersReadPlatformService;

    private java.util.List<PromissoryNote> promissoryNotesData;
    private SavingsAccount fundSavingsAccountData;
    private Client projectOwner;
    private ClientIdentifier projectOwnerDocument;
    private Loan loan;
    private Collection<ClientFamilyMembersData> legalRepresentatives;
    private Collection<ClientFamilyMembersData> guarantees;
    private DateTimeFormatter formatter;
    private String formattedDate;
    private Document document;

    @Override
    public String generateFundPromissoryNoteV1(String json) {
        if (StringUtils.isBlank(json)) {
            throw new InvalidJsonException();
        }
        JsonElement jsonElement = fromApiJsonHelper.parse(json);
        Long fundId = fromApiJsonHelper.extractLongNamed(PdfGeneratorConstants.fundIdParamName, jsonElement);

        if (fundId == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.fund.id.is.null", "Fund id is null");
        }

        this.promissoryNotesData = promissoryNoteRepository.retrieveByFundAccountId(fundId);

        if (this.promissoryNotesData.isEmpty()) {
            throw new GeneralPlatformDomainRuleException("error.msg.there.are.not.promissory.note.is.null", "Promissory note is null");
        }

        this.fundSavingsAccountData = savingsAccountRepositoryWrapper.findOneWithNotFoundDetection(fundId);

        if (fundSavingsAccountData == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.fund.not.found", "Fund with id ".concat(String.valueOf(fundId)).concat(" not found"));
        }

        PortfolioAccountData portfolioAccountData = savingsAccountReadPlatformService.retriveSavingsLinkedAssociation(fundSavingsAccountData.getId());

        Optional<Loan> temporallyLoan = loanRepository.findById(portfolioAccountData.getId()).stream().findFirst();

        if (temporallyLoan.isPresent()) {
            this.loan = temporallyLoan.get();
        } else {
            throw new GeneralPlatformDomainRuleException("error.msg.fund.association.not.found", "Fund association not found");
        }

        this.projectOwner = fundSavingsAccountData.getClient();

        this.projectOwnerDocument = clientIdentifierRepository.retrieveByClientId(this.projectOwner.getId());


        if (projectOwnerDocument == null) {
            throw new GeneralPlatformDomainRuleException("error.msg.client.does.not.have.client.identifier", "client does not have identifier");
        }

        this.legalRepresentatives = familyMembersReadPlatformService
                .getClientFamilyMembers(this.projectOwner.getId())
                .stream()
                .filter(family -> "Representante legal".equals(family.getRelationship()))
                .toList();

        if (this.legalRepresentatives.isEmpty()) {
            throw new GeneralPlatformDomainRuleException("error.msg.not.registered.legal.representative", "Not registered legal representative");
        }

        this.guarantees = familyMembersReadPlatformService
                .getClientFamilyMembers(this.projectOwner.getId())
                .stream()
                .filter(family -> "Aval".equals(family.getRelationship()))
                .toList();

        if (this.guarantees.isEmpty()) {
            throw new GeneralPlatformDomainRuleException("error.msg.not.registered.guarantees", "Not registered guarantees");
        }

        LocalDate date = DateUtils.getBusinessLocalDate();
        this.formatter = DateTimeFormatter.ofPattern("d 'de' MMMM 'de' yyyy", new Locale("es", "ES"));
        DateTimeFormatter formatterEndPage = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        this.formattedDate = date.format(this.formatter);

        this.document = new Document();

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PdfWriter writer = PdfWriter.getInstance(this.document, baos);

            Image logo = Image.getInstance(new URL("https://s3.us-east-2.amazonaws.com/fineract.dev.public/logo-doble-impacto-con-bajada.png"));

            writer.setPageEvent(new HeaderFooterEvent(logo, this.projectOwner.getDisplayName(), fundSavingsAccountData.getId(), fundSavingsAccountData.getAccountNumber(), date.format(formatterEndPage)));

            this.document.open();

            float leftMargin = 38f;
            float rightMargin = 38f;
            float topMargin = 72f;
            float bottomMargin = 50f;

            this.document.setMargins(leftMargin, rightMargin, topMargin, bottomMargin);

            Font titleFont = new Font(Font.FontFamily.HELVETICA, 18, Font.BOLD);
            Paragraph title = new Paragraph("PAGARÉ", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            this.document.add(title);

            Paragraph num = new Paragraph("Número: ".concat(String.valueOf(fundSavingsAccountData.getId())));
            num.setAlignment(Element.ALIGN_RIGHT);
            this.document.add(num);

            Paragraph cod = new Paragraph("Código: ".concat(fundSavingsAccountData.getAccountNumber()));
            cod.setAlignment(Element.ALIGN_RIGHT);
            this.document.add(cod);

            this.document.add(Chunk.NEWLINE);

            // Create first paragraphs
            createFirstParagraphs();

            // Create guarantee pages
            createGuaranteePages();

            // Create appendix pages
            createAppendixPages();

            this.document.close();

            byte[] pdfBytes = baos.toByteArray();
            return "{\"pdfBase64\":\"" + Base64.getEncoder().encodeToString(pdfBytes) + "\"}";

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


        Font normalFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);
        Font boldFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);

        paragraph.add(new Chunk(boldText, boldFont));

        paragraph.add(new Chunk(singleValue, normalFont));

        paragraph.setAlignment(Element.ALIGN_JUSTIFIED);

        return paragraph;

    }


    private PdfPTable createRepresentativeSignatures () {
        //This is if would there are more than 2 representatives to distribute spaces
        int maxColumnsPerRow = 2;

        int numColumns = Math.min(this.legalRepresentatives.size(), maxColumnsPerRow);

        PdfPTable signaturesTable = new PdfPTable(numColumns);
        signaturesTable.setWidthPercentage(100);
        signaturesTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);

        int count = 0;
        for (ClientFamilyMembersData item : this.legalRepresentatives) {

            PdfPCell cell = new PdfPCell(createSignatureParagraph(
                    item.getFullName(),
                    item.getDocumentNumber(),
                    this.projectOwner.getDisplayName()
            ));
            cell.setPaddingTop(40);
            cell.setBorder(Rectangle.NO_BORDER);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            signaturesTable.addCell(cell);
            count++;
        }

        int remainder = count % maxColumnsPerRow;
        if (remainder != 0) {
            for (int i = 0; i < maxColumnsPerRow - remainder; i++) {
                PdfPCell emptyCell = new PdfPCell(new Paragraph(""));
                emptyCell.setBorder(Rectangle.NO_BORDER);
                signaturesTable.addCell(emptyCell);
            }
        }
        return signaturesTable;
    }

    private static PdfPTable createSignatureParagraph(final String name, final String rut, final String debtor) {
        Font signatureFont = new Font(Font.FontFamily.HELVETICA, 12, Font.NORMAL);

        PdfPTable table = new PdfPTable(1);
        table.setWidthPercentage(50);

        PdfPCell lineCell = new PdfPCell(new Paragraph(new Chunk("_________________________", signatureFont)));
        lineCell.setBorder(Rectangle.NO_BORDER);
        lineCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(lineCell);

        PdfPCell nameCell = new PdfPCell(new Paragraph(name, signatureFont));
        nameCell.setBorder(Rectangle.NO_BORDER);
        nameCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(nameCell);

        PdfPCell rutCell = new PdfPCell(new Paragraph("CNI ".concat(rut), signatureFont));
        rutCell.setBorder(Rectangle.NO_BORDER);
        rutCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(rutCell);

        if (debtor != null) {
            PdfPCell debtorCell = new PdfPCell(new Paragraph("p.p. ".concat(debtor), signatureFont));
            debtorCell.setBorder(Rectangle.NO_BORDER);
            debtorCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(debtorCell);
        }

        return table;
    }

    private  void createGuaranteePages () throws DocumentException {
        this.document.newPage();

        Font guaranteeFontAppendix = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
        Paragraph guaranteeAppendix = new Paragraph("POR AVAL", guaranteeFontAppendix);
        guaranteeAppendix.setAlignment(Element.ALIGN_LEFT);
        this.document.add(guaranteeAppendix);

        this.document.add(Chunk.NEWLINE);
        this.document.add(Chunk.NEWLINE);

        this.document.add(createSingleParagraph("En lo sucesivo “El Avalista” se constituye en codeudor solidario del suscriptor o deudor antes " +
                "individualizado en favor de el(los) Acreedor(es) o de quien sus derechos represente, por todas y cada " +
                "una de las obligaciones señaladas precedentemente, por todo el tiempo que transcurriere hasta el " +
                "efectivo y completo pago de este documento, con expresa declaración de que:"));

        this.document.add(Chunk.NEWLINE);

        List wordList = new List(List.ORDERED, List.ALPHABETICAL);
        wordList.setLowercase(List.LOWERCASE);
        wordList.setPreSymbol("");
        wordList.setPostSymbol(". ");

        // Agregar elementos
        wordList.add(new ListItem("Acepta, desde luego los plazos, prórrogas y modificaciones que se otorguen expresa o tácitamente al " +
                "deudor, quedando subsistente la obligación solidaria, no obstante cualquier arreglo o convenio sobre " +
                "el modo, forma y condiciones de pago de la obligación."));
        wordList.add(new ListItem("Libera a el(los) Acreedor(es) de la obligación de protesto de este documento."));
        wordList.add(new ListItem("Su responsabilidad tendrá el carácter de indivisible para todos los efectos legales y en especial en " +
                "atención a lo dispuesto en los artículos 1.526 Nº 4 y 1.528 del Código Civil."));
        wordList.add(new ListItem("Su responsabilidad no se verá alterada en forma alguna por otras garantías que se hayan constituido o " +
                "que en adelante se constituyan para seguridad de la misma obligación, y que su responsabilidad " +
                "solidaria mantendrá su plena vigencia, aún cuando otra persona tome sobre sí la obligación avalada, " +
                "en cualquier forma, aún cuando dicha persona se haga cargo del activo y pasivo del deudor e " +
                "introduzca modificaciones a la sociedad deudora. El(los) Acreedor(es), y quien sus derechos " +
                "representen, quedan autorizados para modificar, sustituir, alzar o renunciar, en todo o parte de las " +
                "garantías que actualmente estén constituidas o que en el futuro se constituyan para caucionar las " +
                "obligaciones a que se refiere el presente pagaré."));
        wordList.add(new ListItem("Para todos los efectos de este aval, el avalista constituye domicilio en el lugar antes señalado por el " +
                "deudor principal y se somete a la jurisdicción de sus Tribunales Ordinarios de Justicia."));
        wordList.add(new ListItem("Autorización Ley 19.628: En caso de mora o simple retardo en el pago del total o de una de las cuotas " +
                "convenidas en este pagaré, de conformidad con lo dispuesto en el artículo 4° de la Ley N° 19.628, el " +
                "suscriptor autoriza al acreedor para comunicar dicha circunstancia a cualquier banco de datos que se " +
                "dedique al tratamiento de datos de carácter personal."));


        this.document.add(wordList);

        // Create guarantees info and signature space
        for (ClientFamilyMembersData familyMemberData : this.guarantees) {
            this.document.add(Chunk.NEWLINE);

            // Principal table
            PdfPTable parentTable = new PdfPTable(2);
            parentTable.setWidthPercentage(100);
            parentTable.setWidths(new float[]{60, 40}); // 60% text, 40% signature

            // Personal info
            PdfPTable leftTable = new PdfPTable(1);
            leftTable.setWidthPercentage(100);

            // Add cells without borders
            PdfPCell cell1 = new PdfPCell(createSingleParagraph("Nombre Avalista  :  ".concat(familyMemberData.getFullName())));
            cell1.setBorder(PdfPCell.NO_BORDER);
            leftTable.addCell(cell1);

            PdfPCell cell2 = new PdfPCell(createSingleParagraph("Dirección informativa : ".concat(familyMemberData.getAddress())));
            cell2.setBorder(PdfPCell.NO_BORDER);
            leftTable.addCell(cell2);

            PdfPCell cell3 = new PdfPCell(createSingleParagraph("CNI  :  ".concat(familyMemberData.getDocumentNumber())));
            cell3.setBorder(PdfPCell.NO_BORDER);
            leftTable.addCell(cell3);

            PdfPCell leftCell = new PdfPCell(leftTable);
            leftCell.setBorder(Rectangle.NO_BORDER);
            leftCell.setVerticalAlignment(Element.ALIGN_TOP);
            parentTable.addCell(leftCell);

            // Signature
            PdfPTable signatureTable = createSignatureParagraph(
                    familyMemberData.getFullName(),
                    familyMemberData.getDocumentNumber(),
                    null
            );

            signatureTable.setSpacingBefore(20);

            PdfPCell rightCell = new PdfPCell(signatureTable);
            rightCell.setBorder(Rectangle.NO_BORDER);
            rightCell.setVerticalAlignment(Element.ALIGN_TOP);
            parentTable.addCell(rightCell);

            this.document.add(parentTable);
        }
    }

    private void createFirstParagraphs () throws DocumentException {
        String investorsParagraph = "Debo y pagaré incondicionalmente a la orden de ";

        for (PromissoryNote promissoryNote : this.promissoryNotesData) {

            SavingsAccount investorSavingAccount = promissoryNote.getInvestorSavingsAccount();

            Client investor = investorSavingAccount.getClient();

            if (investor != null) {

                investorsParagraph = investorsParagraph.concat(promissoryNote.getInvestorSavingsAccount().getClient().getDisplayName()).concat(" cédula nacional de identidad ");

                ClientIdentifier investorDocument = clientIdentifierRepository.retrieveByClientId(investor.getId());

                if (investorDocument != null) {
                    investorsParagraph = investorsParagraph.concat(investorDocument.documentKey() + ", ");
                } else {
                    investorsParagraph = investorsParagraph.concat("(No registra), ");
                }
            }
        }

        this.document.add(createSingleParagraph(investorsParagraph.concat("en adelante el(los) “Acreedor(es)”, la suma de " +
                "$").concat(String.valueOf(fundSavingsAccountData.getMaxAllowedDepositLimit())).concat(
                ". - (".concat(NumberToWordEs.convert(fundSavingsAccountData.getMaxAllowedDepositLimit().setScale(2, RoundingMode.HALF_EVEN).longValue())).concat(" pesos), " +
                        "moneda legal, cantidad que he recibido en préstamo de el(los) Acreedor(es) a entera satisfacción."))));

        this.document.add(Chunk.NEWLINE);

        this.document.add(createBoldParagraph("Interés: ", "El capital adeudado devengará un interés del ".concat(
                String.valueOf(loanReadPlatformServiceCommon.getLoanAnnualInterestRate(this.loan.getId()).setScale(2,
                        RoundingMode.HALF_EVEN))).concat("% anual, vencido desde la fecha de " +
                "suscripción de este pagaré hasta su pago íntegro y efectivo.")));

        this.document.add(Chunk.NEWLINE);

        //interestCalculationParagraph
        this.document.add(createBoldParagraph("Cálculo de Intereses: ", "Las tasas mensuales de interés se refieren a meses de 30 días. Los intereses se " +
                "calcularán y pagarán por días corridos y sobre el total del capital que se esté adeudando hasta el día de " +
                "su pago efectivo."));

        this.document.add(Chunk.NEWLINE);

        //bodyText2
        this.document.add(createSingleParagraph("El capital total adeudado de $".concat(String.valueOf(fundSavingsAccountData.getMaxAllowedDepositLimit())).concat(".- ("
                .concat(NumberToWordEs.convert(fundSavingsAccountData.getMaxAllowedDepositLimit()
                        .setScale(2, RoundingMode.HALF_EVEN).longValue())).concat(" se pagará en las " +
                        "siguientes cuotas, que se devengarán en las siguientes fechas de vencimiento:"))));

        this.document.add(Chunk.NEWLINE);

        java.util.List<LoanRepaymentScheduleInstallment> list = loanRepositoryWrapper.getLoanRepaymentScheduleInstallments(this.loan.getId());

        List bulletList = new List(List.UNORDERED);

        for (int i = 1; i < list.size(); i++) {
            LoanRepaymentScheduleInstallment installment = list.get(i);
            LocalDate dueDate = installment.getDueDate();
            String formattedScheduleDate = dueDate.format(this.formatter);
            BigDecimal totalOutstanding = installment.getTotalOutstanding(MonetaryCurrency.fromCurrencyData(this.loan.getCurrency().toData())).getAmount();

            bulletList.add(new ListItem(formattedScheduleDate
                    .concat(", $" + totalOutstanding.setScale(2, RoundingMode.HALF_EVEN)).concat(
                            ".- (" + NumberToWordEs.convert(totalOutstanding.longValue()) + ")")));
        }

        this.document.add(bulletList);

        this.document.add(Chunk.NEWLINE);

        //accordingDateParagraph
        this.document.add(createBoldParagraph("Lugar y hora en que se efectuará el pago: ", "Todos los pagos de capital y/ o intereses se efectuarán" +
                "mediante depósito o transferencia a la cuenta bancaria Nº 1000-10-569, del Banco Estado, cuyo titular" +
                "es la empresa Doble Impacto SpA. Rut Nº 76.792.913-7, informándolo al correo electrónico" +
                "transacciones@dobleimpacto.cl, quien es la mandataria para el cobro. El pago debe efectuarse antes de" +
                "las 12:00 horas del día del vencimiento que corresponda."));

        this.document.add(Chunk.NEWLINE);

        //uninhabitedDaysParagraph
        this.document.add(createBoldParagraph("Días inhábiles, prórroga de pago: ", "Cualquier fecha para el pago de capital y/ o intereses que" +
                "corresponda a día inhábil prorrogará el plazo hasta el día hábil inmediatamente siguiente, debiendo el" +
                "respectivo pago incluir los intereses que correspondan a los días comprendidos en la prórroga."));

        this.document.add(Chunk.NEWLINE);

        //arrearsInterestParagraph
        this.document.add(createBoldParagraph("Intereses por retardo: ", "En caso de mora o simple retardo en el pago de todo o parte del capital e " +
                "intereses, se capitalizarán los intereses devengados y este pagaré devengará por todo el lapso de la " +
                "mora o retardo, el interés máximo convencional que la ley permite estipular para operaciones de crédito " +
                "de dinero en moneda nacional."));

        this.document.add(Chunk.NEWLINE);

        //bodyText3
        this.document.add(createSingleParagraph("Lo anterior es sin perjuicio de la facultad de el(los) Acreedor(es) de hacer exigible el total de lo " +
                "adeudado, en conformidad a lo estipulado en este pagaré."));

        this.document.add(Chunk.NEWLINE);

        //maturityAcelerationsParagraph
        this.document.add(createBoldParagraph("Aceleración del vencimiento: ", "El(los) Acreedor(es) tendrá(n) derecho a hacer inmediatamente exigible la " +
                "totalidad del crédito como si fuera de plazo vencido en los siguientes casos:"));

        this.document.add(Chunk.NEWLINE);

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

        this.document.add(wordList);

        this.document.add(Chunk.NEWLINE);

        //Indivisibility
        this.document.add(createBoldParagraph("Indivisibilidad: ", "Todas las obligaciones derivadas de este pagaré se considerarán indivisibles para el\n" +
                "suscriptor y/o sucesores, para todos los efectos legales."));

        this.document.add(Chunk.NEWLINE);

        //Prepays
        this.document.add(createBoldParagraph("Prepagos: ", "En caso de prepago total o parcial de la obligación, éste se distribuirá entre cada Acreedor a " +
                "prorrata del monto adeudado a cada uno. Adicionalmente, existirá una comisión de prepago en beneficio " +
                "de el(los) acreedor(es) según detalle adjunto, equivalente a un mes de intereses pactados calculados " +
                "sobre el capital que se prepaga, en cumplimiento del límite del artículo 10 de la ley 18.010 sobre " +
                "operaciones de crédito de dinero."));

        this.document.add(Chunk.NEWLINE);

        //Authorizations
        this.document.add(createBoldParagraph("Autorización Ley 19.628: ", "En caso de mora o simple retardo en el pago del total o de una de las cuotas " +
                "convenidas en este pagaré, de conformidad con lo dispuesto en el artículo 4° de la Ley N° 19.628, el " +
                "suscriptor autoriza al acreedor para comunicar dicha circunstancia a cualquier banco de datos que se " +
                "dedique al tratamiento de datos de carácter personal."));

        this.document.add(Chunk.NEWLINE);

        //Objection
        this.document.add(createBoldParagraph("Protesto: ", "Libero a el(los) Acreedor(es) de la obligación de protesto. En todo caso, en el evento de\n" +
                "protesto me obligo a pagar los gastos e impuestos que se devengaren con este motivo."));

        this.document.add(Chunk.NEWLINE);

        //Taxes, duties and expenses
        this.document.add(createBoldParagraph("Impuesto, derechos y gastos: ", "Cualquier impuesto, derechos o gastos que se devenguen con ocasión de " +
                "este pagaré, su modificación, prórroga, pago u otra circunstancia relativa a aquél o producida con " +
                "ocasión o motivo del mismo, será de cargo exclusivo del suscriptor."));

        this.document.add(Chunk.NEWLINE);

        this.document.add(createSingleParagraph("Exención Impuesto de Timbres y Estampillas: Este pagaré está exento del Impuestos de Timbre y Estampillas " +
                "conforme a lo prescrito en el Decreto Ley 3.475 Artículo 23° el cual señala en su numeral 10: “Sólo estarán " +
                "exentas de los impuestos que establece el presente decreto ley, las siguientes personas e instituciones: 10.- " +
                "Fundación Niño y Patria”."));

        this.document.add(Chunk.NEWLINE);

        this.document.add(createBoldParagraph("Domicilio y Jurisdicción: ", "Para todos los efectos legales derivados del presente pagaré, el deudor o " +
                "suscriptor constituye domicilio especial en la comuna y ciudad de Santiago y se somete a la Jurisdicción " +
                "de sus Tribunales Ordinarios de Justicia, domicilio que también será lugar hábil para las diligencias de " +
                "protesto en caso de practicarse."));

        this.document.add(Chunk.NEWLINE);

        this.document.add(createSingleParagraph("Razón social del suscriptor  :  ".concat(this.projectOwner.getDisplayName())));
        this.document.add(createSingleParagraph("Rut : ".concat(projectOwnerDocument.documentKey())));

        AddressData address = addressReadPlatformService.retrieveAllClientAddress(this.projectOwner.getId()).stream().findFirst().orElse(null);

        this.document.add(createSingleParagraph("Domicilio  :  ".concat(address != null ? address.getCustomAddress() : "")));

        this.document.add(createRepresentativeSignatures());


        this.document.add(Chunk.NEWLINE);
        this.document.add(Chunk.NEWLINE);
        this.document.add(Chunk.NEWLINE);
        this.document.add(Chunk.NEWLINE);

        this.document.add(createSingleParagraph("En Santiago de Chile a ".concat(this.formattedDate)));

    }

    private void createAppendixPages () throws DocumentException {
        this.document.newPage();

        Font titleFontAppendix = new Font(Font.FontFamily.HELVETICA, 14, Font.BOLD);
        Paragraph titleAppendix = new Paragraph("ANEXO", titleFontAppendix);
        titleAppendix.setAlignment(Element.ALIGN_CENTER);
        this.document.add(titleAppendix);

        this.document.add(Chunk.NEWLINE);

        this.document.add(createSingleParagraph("Relacionado con el pagaré suscrito el " + this.formattedDate + " por " +
                this.projectOwner.getDisplayName() + ", el monto de " +
                "capital $" + fundSavingsAccountData.getMaxAllowedDepositLimit() + ".- (" +
                NumberToWordEs.convert(fundSavingsAccountData.getMaxAllowedDepositLimit().longValue()) + " pesos), se desglosa de la siguiente manera:"));

        this.document.add(Chunk.NEWLINE);

        List investorsList = new List(List.UNORDERED);

        for (PromissoryNote promissoryNote : this.promissoryNotesData) {

            String text = "A la orden de ";

            SavingsAccount investorSavingAccount = promissoryNote.getInvestorSavingsAccount();

            Client investor = investorSavingAccount.getClient();

            if (investor != null) {

                text = text.concat(promissoryNote.getInvestorSavingsAccount().getClient().getDisplayName()).concat(
                        " cedula nacional de identidad ");

                ClientIdentifier investorDocument = clientIdentifierRepository.retrieveByClientId(investor.getId());

                if (investorDocument != null) {
                    text = text.concat(investorDocument.documentKey() + ", ");
                } else {
                    text = text.concat("(No registra), ");
                }
            } else {
                text = text.concat( "(no registra), ");
            }
            text = text.concat("o a quien sus derechos represente la suma de $" + promissoryNote.getInvestmentAmount() + ".- (" +
                    NumberToWordEs.convert(promissoryNote.getInvestmentAmount().longValue()) + " pesos) moneda de curso legal, por concepto de " +
                    "capital, que de dicha persona recibí a mi entera satisfacción;");

            investorsList.add(text);
        }

        this.document.add(investorsList);

//            this.document.add(createSingleParagraph("A la orden de " + investor.getDisplayName() + ", cedula nacional de identidad "
//                    + investorDocument.documentKey() + ", o a quién " +
//                    "sus derechos represente la suma de $" + promissoryNoteData.getInvestmentAmount() + ".- (" +
//                    NumberToWordEs.convert(promissoryNoteData.getInvestmentAmount().longValue()) + " pesos) moneda de curso legal, por concepto de " +
//                    "capital, que de dicha persona recibí a mi entera satisfacción;"));

        this.document.add(Chunk.NEWLINE);

        this.document.add(createSingleParagraph("Razón social del suscriptor  :  ".concat(this.projectOwner.getDisplayName())));
        this.document.add(createSingleParagraph("Rut : ".concat(projectOwnerDocument.documentKey())));
        this.document.add(createSingleParagraph("Domicilio  :  "));

        this.document.add(createRepresentativeSignatures());
    }

    static class HeaderFooterEvent extends PdfPageEventHelper {
        private final Image img;
        private final Font footerFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
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
                float marginLeft = 38f;
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
