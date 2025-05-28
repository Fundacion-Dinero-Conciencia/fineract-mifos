package com.belat.fineract.portfolio.pdfgenerator.service;

public interface PdfGeneratorWritePlatformService {

    String generateFundMandateV1(String json);

    String generateRetailMandateV1(String json);

}
