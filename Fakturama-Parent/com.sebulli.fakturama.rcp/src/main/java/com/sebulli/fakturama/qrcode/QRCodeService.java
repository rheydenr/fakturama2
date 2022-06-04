package com.sebulli.fakturama.qrcode;

import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.model.Invoice;

/**
 * Service class for generating various QR codes 
 *
 */
public interface QRCodeService {

    byte[] createSwissCodeQR(Invoice document);

    byte[] createGiroCode(Invoice document);

    byte[] createVCardQRCode(Document document);

    byte[] createEANCode(String productNumber);

}