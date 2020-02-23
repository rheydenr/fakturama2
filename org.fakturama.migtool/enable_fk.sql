ALTER TABLE `fkt_category` ADD CONSTRAINT FK_FKT_CATEGORY_ABSTRACTCATEGORY_PARENT FOREIGN KEY (`ABSTRACTCATEGORY_PARENT`) REFERENCES `fkt_category` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_confirmation` ADD CONSTRAINT FK_FKT_CONFIRMATION_CONFIRMATION_PARENT_ID FOREIGN KEY (`CONFIRMATION_PARENT_ID`) REFERENCES `fkt_document` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_contact` ADD CONSTRAINT FK_FKT_CONTACT_FK_ADDRESS FOREIGN KEY (`FK_ADDRESS`) REFERENCES `fkt_address` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_contact` ADD CONSTRAINT FK_FKT_CONTACT_FK_ALTERNATECONTACT FOREIGN KEY (`FK_ALTERNATECONTACT`) REFERENCES `fkt_contact` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_contact` ADD CONSTRAINT FK_FKT_CONTACT_FK_BANKACCOUNT FOREIGN KEY (`FK_BANKACCOUNT`) REFERENCES `fkt_bankaccount` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_contact` ADD CONSTRAINT FK_FKT_CONTACT_FK_CATEGORY FOREIGN KEY (`FK_CATEGORY`) REFERENCES `fkt_category` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_contact` ADD CONSTRAINT FK_FKT_CONTACT_FK_PAYMENT FOREIGN KEY (`FK_PAYMENT`) REFERENCES `fkt_payment` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_credit` ADD CONSTRAINT FK_FKT_CREDIT_CREDIT_PARENT_ID FOREIGN KEY (`CREDIT_PARENT_ID`) REFERENCES `fkt_document` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_delivery` ADD CONSTRAINT FK_FKT_DELIVERY_DELIVERY_PARENT_ID FOREIGN KEY (`DELIVERY_PARENT_ID`) REFERENCES `fkt_document` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_document` ADD CONSTRAINT FK_FKT_DOCUMENT_DOCUMENT_INVOICEREFERENCE FOREIGN KEY (`DOCUMENT_INVOICEREFERENCE`) REFERENCES `fkt_document` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_document` ADD CONSTRAINT FK_FKT_DOCUMENT_FK_CONTACT FOREIGN KEY (`FK_CONTACT`) REFERENCES `fkt_contact` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_document` ADD CONSTRAINT FK_FKT_DOCUMENT_FK_DELIVERYCONTACT FOREIGN KEY (`FK_DELIVERYCONTACT`) REFERENCES `fkt_contact` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_document` ADD CONSTRAINT FK_FKT_DOCUMENT_FK_INDIVIDUALINFO FOREIGN KEY (`FK_INDIVIDUALINFO`) REFERENCES `fkt_individualdocumentinfo` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_document` ADD CONSTRAINT FK_FKT_DOCUMENT_FK_NOVATREF FOREIGN KEY (`FK_NOVATREF`) REFERENCES `fkt_vat` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_document` ADD CONSTRAINT FK_FKT_DOCUMENT_FK_PAYMENT FOREIGN KEY (`FK_PAYMENT`) REFERENCES `fkt_payment` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_document` ADD CONSTRAINT FK_FKT_DOCUMENT_FK_SHIPPING FOREIGN KEY (`FK_SHIPPING`) REFERENCES `fkt_shipping` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_document` ADD CONSTRAINT FK_FKT_DOCUMENT_FK_SRCDOCUMENT FOREIGN KEY (`FK_SRCDOCUMENT`) REFERENCES `fkt_document` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_documentitem` ADD CONSTRAINT FK_FKT_DOCUMENTITEM_FK_DOCUMENT FOREIGN KEY (`FK_DOCUMENT`) REFERENCES `fkt_document` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_documentitem` ADD CONSTRAINT FK_FKT_DOCUMENTITEM_FK_PRODUCT FOREIGN KEY (`FK_PRODUCT`) REFERENCES `fkt_product` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_documentitem` ADD CONSTRAINT FK_FKT_DOCUMENTITEM_FK_VAT FOREIGN KEY (`FK_VAT`) REFERENCES `fkt_vat` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_dunning` ADD CONSTRAINT FK_FKT_DUNNING_DUNNING_PARENT_ID FOREIGN KEY (`DUNNING_PARENT_ID`) REFERENCES `fkt_document` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_invoice` ADD CONSTRAINT FK_FKT_INVOICE_INVOICE_PARENT_ID FOREIGN KEY (`INVOICE_PARENT_ID`) REFERENCES `fkt_document` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_itemaccounttype` ADD CONSTRAINT FK_FKT_ITEMACCOUNTTYPE_ITEMACCOUNTTYPE_CATEGORY FOREIGN KEY (`ITEMACCOUNTTYPE_CATEGORY`) REFERENCES `fkt_category` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_letter` ADD CONSTRAINT FK_FKT_LETTER_LETTER_PARENT_ID FOREIGN KEY (`LETTER_PARENT_ID`) REFERENCES `fkt_document` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_offer` ADD CONSTRAINT FK_FKT_OFFER_OFFER_PARENT_ID FOREIGN KEY (`OFFER_PARENT_ID`) REFERENCES `fkt_document` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_order` ADD CONSTRAINT FK_FKT_ORDER_ORDER_PARENT_ID FOREIGN KEY (`ORDER_PARENT_ID`) REFERENCES `fkt_document` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_payment` ADD CONSTRAINT FK_FKT_PAYMENT_FK_CATEGORY FOREIGN KEY (`FK_CATEGORY`) REFERENCES `fkt_category` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_product` ADD CONSTRAINT FK_FKT_PRODUCT_FK_CATEGORY FOREIGN KEY (`FK_CATEGORY`) REFERENCES `fkt_category` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_product` ADD CONSTRAINT FK_FKT_PRODUCT_FK_VAT FOREIGN KEY (`FK_VAT`) REFERENCES `fkt_vat` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_productblockprice` ADD CONSTRAINT FK_FKT_PRODUCTBLOCKPRICE_FK_PRODUCT FOREIGN KEY (`FK_PRODUCT`) REFERENCES `fkt_product` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_productoptions` ADD CONSTRAINT FK_FKT_PRODUCTOPTIONS_FK_PRODUCT FOREIGN KEY (`FK_PRODUCT`) REFERENCES `fkt_product` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_proforma` ADD CONSTRAINT FK_FKT_PROFORMA_PROFORMA_PARENT_ID FOREIGN KEY (`PROFORMA_PARENT_ID`) REFERENCES `fkt_document` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_role` ADD CONSTRAINT FK_FKT_ROLE_FK_USER FOREIGN KEY (`FK_USER`) REFERENCES `fkt_user` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_shipping` ADD CONSTRAINT FK_FKT_SHIPPING_FK_CATEGORY FOREIGN KEY (`FK_CATEGORY`) REFERENCES `fkt_category` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_shipping` ADD CONSTRAINT FK_FKT_SHIPPING_FK_VAT FOREIGN KEY (`FK_VAT`) REFERENCES `fkt_vat` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_textmodule` ADD CONSTRAINT FK_FKT_TEXTMODULE_FK_CATEGORY FOREIGN KEY (`FK_CATEGORY`) REFERENCES `fkt_category` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_user` ADD CONSTRAINT FK_FKT_USER_FK_TENANT FOREIGN KEY (`FK_TENANT`) REFERENCES `fkt_tenant` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_vat` ADD CONSTRAINT FK_FKT_VAT_FK_CATEGORY FOREIGN KEY (`FK_CATEGORY`) REFERENCES `fkt_category` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_voucheritems` ADD CONSTRAINT FK_FKT_VOUCHERITEMS_FK_ACCOUNTTYPE FOREIGN KEY (`FK_ACCOUNTTYPE`) REFERENCES `fkt_itemaccounttype` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_voucheritems` ADD CONSTRAINT FK_FKT_VOUCHERITEMS_FK_VAT FOREIGN KEY (`FK_VAT`) REFERENCES `fkt_vat` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_voucheritems` ADD CONSTRAINT FK_FKT_VOUCHERITEMS_FK_VOUCHER FOREIGN KEY (`FK_VOUCHER`) REFERENCES `fkt_vouchers` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_vouchers` ADD CONSTRAINT FK_FKT_VOUCHERS_FK_CATEGORY FOREIGN KEY (`FK_CATEGORY`) REFERENCES `fkt_category` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `fkt_webshopstatemapping` ADD CONSTRAINT FK_FKT_WEBSHOPSTATEMAPPING_FK_WEBSHOP FOREIGN KEY (`FK_WEBSHOP`) REFERENCES `fkt_webshop` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;