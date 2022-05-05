package com.sebulli.fakturama.qrcode;

import com.sebulli.fakturama.model.Document;

/**
 * Service class for generating various QR codes 
 *
 */
public interface QRCodeService {

    byte[] createSwissCodeQR(Document document);

}
