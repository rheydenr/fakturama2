ALTER TABLE `FKT_ADDRESS` ADD CONSTRAINT FK_FKT_ADDRESS_CONTACT_ADDRESSES FOREIGN KEY (`CONTACT_ADDRESSES`) REFERENCES `FKT_CONTACT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_ADDRESS_CONTACTTYPES` ADD CONSTRAINT FK_FKT_ADDRESS_CONTACTTYPES_ADDRESS_CONTACTTYPES FOREIGN KEY (`ADDRESS_CONTACTTYPES`) REFERENCES `FKT_ADDRESS` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_CATEGORY` ADD CONSTRAINT FK_FKT_CATEGORY_FK_PARENT_CATEGORY FOREIGN KEY (`FK_PARENT_CATEGORY`) REFERENCES `FKT_CATEGORY` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_CONFIRMATION` ADD CONSTRAINT FK_FKT_CONFIRMATION_CONFIRMATION_PARENT_ID FOREIGN KEY (`CONFIRMATION_PARENT_ID`) REFERENCES `FKT_DOCUMENT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_CONTACT` ADD CONSTRAINT FK_FKT_CONTACT_FK_BANKACCOUNT FOREIGN KEY (`FK_BANKACCOUNT`) REFERENCES `FKT_BANKACCOUNT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_CONTACT` ADD CONSTRAINT FK_FKT_CONTACT_FK_CATEGORY FOREIGN KEY (`FK_CATEGORY`) REFERENCES `FKT_CATEGORY` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_CONTACT` ADD CONSTRAINT FK_FKT_CONTACT_FK_PAYMENT FOREIGN KEY (`FK_PAYMENT`) REFERENCES `FKT_PAYMENT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_CREDIT` ADD CONSTRAINT FK_FKT_CREDIT_CREDIT_PARENT_ID FOREIGN KEY (`CREDIT_PARENT_ID`) REFERENCES `FKT_DOCUMENT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_DELIVERY` ADD CONSTRAINT FK_FKT_DELIVERY_DELIVERY_PARENT_ID FOREIGN KEY (`DELIVERY_PARENT_ID`) REFERENCES `FKT_DOCUMENT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_DOCUMENT` ADD CONSTRAINT FK_FKT_DOCUMENT_DOCUMENT_INVOICEREFERENCE FOREIGN KEY (`DOCUMENT_INVOICEREFERENCE`) REFERENCES `FKT_DOCUMENT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_DOCUMENT` ADD CONSTRAINT FK_FKT_DOCUMENT_FK_INDIVIDUALINFO FOREIGN KEY (`FK_INDIVIDUALINFO`) REFERENCES `FKT_INDIVIDUALDOCUMENTINFO` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_DOCUMENT` ADD CONSTRAINT FK_FKT_DOCUMENT_FK_NOVATREF FOREIGN KEY (`FK_NOVATREF`) REFERENCES `FKT_VAT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_DOCUMENT` ADD CONSTRAINT FK_FKT_DOCUMENT_FK_PAYMENT FOREIGN KEY (`FK_PAYMENT`) REFERENCES `FKT_PAYMENT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_DOCUMENT` ADD CONSTRAINT FK_FKT_DOCUMENT_FK_SHIPPING FOREIGN KEY (`FK_SHIPPING`) REFERENCES `FKT_SHIPPING` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_DOCUMENT` ADD CONSTRAINT FK_FKT_DOCUMENT_FK_SRCDOCUMENT FOREIGN KEY (`FK_SRCDOCUMENT`) REFERENCES `FKT_DOCUMENT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_DOCUMENTITEM` ADD CONSTRAINT FK_FKT_DOCUMENTITEM_FK_DOCUMENT FOREIGN KEY (`FK_DOCUMENT`) REFERENCES `FKT_DOCUMENT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_DOCUMENTITEM` ADD CONSTRAINT FK_FKT_DOCUMENTITEM_FK_PRODUCT FOREIGN KEY (`FK_PRODUCT`) REFERENCES `FKT_PRODUCT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_DOCUMENTITEM` ADD CONSTRAINT FK_FKT_DOCUMENTITEM_FK_VAT FOREIGN KEY (`FK_VAT`) REFERENCES `FKT_VAT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_DOCUMENTRECEIVER` ADD CONSTRAINT FK_FKT_DOCUMENTRECEIVER_FK_DOCUMENT FOREIGN KEY (`FK_DOCUMENT`) REFERENCES `FKT_DOCUMENT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_DUNNING` ADD CONSTRAINT FK_FKT_DUNNING_DUNNING_PARENT_ID FOREIGN KEY (`DUNNING_PARENT_ID`) REFERENCES `FKT_DOCUMENT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_INVOICE` ADD CONSTRAINT FK_FKT_INVOICE_INVOICE_PARENT_ID FOREIGN KEY (`INVOICE_PARENT_ID`) REFERENCES `FKT_DOCUMENT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_ITEMACCOUNTTYPE` ADD CONSTRAINT FK_FKT_ITEMACCOUNTTYPE_FK_CATEGORY FOREIGN KEY (`FK_CATEGORY`) REFERENCES `FKT_CATEGORY` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_LETTER` ADD CONSTRAINT FK_FKT_LETTER_LETTER_PARENT_ID FOREIGN KEY (`LETTER_PARENT_ID`) REFERENCES `FKT_DOCUMENT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_OFFER` ADD CONSTRAINT FK_FKT_OFFER_OFFER_PARENT_ID FOREIGN KEY (`OFFER_PARENT_ID`) REFERENCES `FKT_DOCUMENT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_ORDER` ADD CONSTRAINT FK_FKT_ORDER_ORDER_PARENT_ID FOREIGN KEY (`ORDER_PARENT_ID`) REFERENCES `FKT_DOCUMENT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_PAYMENT` ADD CONSTRAINT FK_FKT_PAYMENT_FK_CATEGORY FOREIGN KEY (`FK_CATEGORY`) REFERENCES `FKT_CATEGORY` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_PRODUCT` ADD CONSTRAINT FK_FKT_PRODUCT_FK_CATEGORY FOREIGN KEY (`FK_CATEGORY`) REFERENCES `FKT_CATEGORY` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_PRODUCT` ADD CONSTRAINT FK_FKT_PRODUCT_FK_VAT FOREIGN KEY (`FK_VAT`) REFERENCES `FKT_VAT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_PRODUCTBLOCKPRICE` ADD CONSTRAINT FK_FKT_PRODUCTBLOCKPRICE_FK_PRODUCT FOREIGN KEY (`FK_PRODUCT`) REFERENCES `FKT_PRODUCT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_PRODUCTOPTIONS` ADD CONSTRAINT FK_FKT_PRODUCTOPTIONS_FK_PRODUCT FOREIGN KEY (`FK_PRODUCT`) REFERENCES `FKT_PRODUCT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_PROFORMA` ADD CONSTRAINT FK_FKT_PROFORMA_PROFORMA_PARENT_ID FOREIGN KEY (`PROFORMA_PARENT_ID`) REFERENCES `FKT_DOCUMENT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_ROLE` ADD CONSTRAINT FK_FKT_ROLE_FK_USER FOREIGN KEY (`FK_USER`) REFERENCES `FKT_USER` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_SHIPPING` ADD CONSTRAINT FK_FKT_SHIPPING_FK_CATEGORY FOREIGN KEY (`FK_CATEGORY`) REFERENCES `FKT_CATEGORY` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_SHIPPING` ADD CONSTRAINT FK_FKT_SHIPPING_FK_VAT FOREIGN KEY (`FK_VAT`) REFERENCES `FKT_VAT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_TEXTMODULE` ADD CONSTRAINT FK_FKT_TEXTMODULE_FK_CATEGORY FOREIGN KEY (`FK_CATEGORY`) REFERENCES `FKT_CATEGORY` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_USER` ADD CONSTRAINT FK_FKT_USER_FK_TENANT FOREIGN KEY (`FK_TENANT`) REFERENCES `FKT_TENANT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_VAT` ADD CONSTRAINT FK_FKT_VAT_FK_CATEGORY FOREIGN KEY (`FK_CATEGORY`) REFERENCES `FKT_CATEGORY` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_VOUCHERITEMS` ADD CONSTRAINT FK_FKT_VOUCHERITEMS_FK_ACCOUNTTYPE FOREIGN KEY (`FK_ACCOUNTTYPE`) REFERENCES `FKT_ITEMACCOUNTTYPE` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_VOUCHERITEMS` ADD CONSTRAINT FK_FKT_VOUCHERITEMS_FK_VAT FOREIGN KEY (`FK_VAT`) REFERENCES `FKT_VAT` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_VOUCHERITEMS` ADD CONSTRAINT FK_FKT_VOUCHERITEMS_FK_VOUCHER FOREIGN KEY (`FK_VOUCHER`) REFERENCES `FKT_VOUCHERS` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_VOUCHERS` ADD CONSTRAINT FK_FKT_VOUCHERS_FK_CATEGORY FOREIGN KEY (`FK_CATEGORY`) REFERENCES `FKT_CATEGORY` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;
ALTER TABLE `FKT_WEBSHOPSTATEMAPPING` ADD CONSTRAINT FK_FKT_WEBSHOPSTATEMAPPING_FK_WEBSHOP FOREIGN KEY (`FK_WEBSHOP`) REFERENCES `FKT_WEBSHOP` (`ID`) ON DELETE NO ACTION ON UPDATE NO ACTION;