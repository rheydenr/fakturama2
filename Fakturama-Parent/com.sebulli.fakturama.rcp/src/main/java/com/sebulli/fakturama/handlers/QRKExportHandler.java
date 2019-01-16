
package com.sebulli.fakturama.handlers;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.core.di.annotations.CanExecute;
import org.eclipse.e4.core.di.annotations.Execute;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.nls.Translation;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.persistence.jaxb.JAXBContextFactory;
import org.eclipse.persistence.jaxb.MarshallerProperties;
import org.eclipse.swt.widgets.Shell;

import com.sebulli.fakturama.i18n.Messages;
import com.sebulli.fakturama.log.ILogger;
import com.sebulli.fakturama.misc.INumberFormatterService;
import com.sebulli.fakturama.model.Document;
import com.sebulli.fakturama.parts.DocumentEditor;

import at.ckvsoft.qrk.QRKPaymentTypes;
import at.ckvsoft.qrk.type.ObjectFactory;
import at.ckvsoft.qrk.type.Qrkvoucher;
import at.ckvsoft.qrk.type.Receipt2Bon;

public class QRKExportHandler {

	@Inject
	@Translation
	protected Messages msg;

	@Inject
	protected IEclipseContext context;

	@Inject
	@Optional
	private IPreferenceStore preferences;

	@Inject
	private ILogger log;
	
	@Inject
	private INumberFormatterService numberFormatter;

	@Execute
	public void execute(Shell shell, EPartService partService) {
		MPart activePart = partService.getActivePart();
		if(activePart != null && activePart.getObject() != null
				&& (activePart.getObject() instanceof DocumentEditor)
				&& ((DocumentEditor) activePart.getObject()).getDocument().getBillingType().isINVOICE()) {
            JAXBContext jc;
			try {
				Map<String, Object> properties = new HashMap<>();
				properties.put(MarshallerProperties.MEDIA_TYPE, "application/json");
				jc = JAXBContextFactory.createContext(new Class[]{Receipt2Bon.class, ObjectFactory.class}, properties);
				
	            ObjectFactory qrkObjectFactory = new ObjectFactory();
				Document document = ((DocumentEditor) activePart.getObject()).getDocument();
				Receipt2Bon qrkVouchers = qrkObjectFactory.createReceipt2Bon();
				Qrkvoucher singleVoucher = qrkObjectFactory.createQrkvoucher()
						.withCustomerText(document.getCustomerRef())
						.withGross(numberFormatter.doubleToFormattedQuantity(document.getTotalValue()))
						.withReceiptNum(document.getName());
				
				if(document.getPayment() != null) {
					java.util.Optional<QRKPaymentTypes> paymentMatch = Arrays.stream(QRKPaymentTypes.values()).filter(payment -> document.getPayment().getName().equalsIgnoreCase(payment.getName())).findFirst();
					if(paymentMatch.isPresent()) {
						singleVoucher.setPayedBy(Integer.toString(paymentMatch.get().getId()));
					} else {
						logAndShowError(shell, "No payment type matches for document " + document.getName() + ".");
						return;
					}
				} else {
					logAndShowError(shell, "Document "+ document.getName() + " has no payment type!");
					return;
				}
				
				qrkVouchers.getR2B().add(singleVoucher);

	            Marshaller marshaller = jc.createMarshaller();
	            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
	            marshaller.marshal(qrkVouchers, System.out);
			} catch (JAXBException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	private void logAndShowError(Shell shell, String message) {
		log.error(message);
		MessageDialog.openError(shell, msg.dialogMessageboxTitleError, message);
	}

	@CanExecute
	public boolean canExecute(EPartService partService) {
		MPart activePart = partService.getActivePart();
		return activePart != null && activePart.getObject() != null
				&& (activePart.getObject() instanceof DocumentEditor)
				&& ((DocumentEditor) activePart.getObject()).getDocument().getBillingType().isINVOICE();
	}

}