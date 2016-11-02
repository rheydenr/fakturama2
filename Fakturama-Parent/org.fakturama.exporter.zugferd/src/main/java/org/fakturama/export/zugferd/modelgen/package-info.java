@XmlSchema(namespace = "urn:ferd:CrossIndustryDocument:invoice:1p0",  
    xmlns = {   
//        @XmlNs(namespaceURI = "http://www.w3.org/2001/XMLSchema", prefix = "xs"),  
        @XmlNs(namespaceURI = "urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:12", prefix = "ram"),  
        @XmlNs(namespaceURI = "urn:un:unece:uncefact:data:standard:QualifiedDataType:12", prefix = "qdt"),  
        @XmlNs(namespaceURI = "urn:ferd:CrossIndustryDocument:invoice:1p0", prefix = "rsm"),  
        @XmlNs(namespaceURI = "urn:un:unece:uncefact:data:standard:UnqualifiedDataType:15", prefix = "udt")  
    },  
    elementFormDefault = javax.xml.bind.annotation.XmlNsForm.UNQUALIFIED)  
  
/*
<xs:schema xmlns:rsm="urn:ferd:CrossIndustryDocument:invoice:1p0" 
xmlns:xs="http://www.w3.org/2001/XMLSchema" 
xmlns:qdt="urn:un:unece:uncefact:data:standard:QualifiedDataType:12" 
xmlns:ram="urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:12" 
xmlns:udt="urn:un:unece:uncefact:data:standard:UnqualifiedDataType:15" 
targetNamespace="urn:ferd:CrossIndustryDocument:invoice:1p0" elementFormDefault="qualified">
	<xs:import namespace="urn:un:unece:uncefact:data:standard:QualifiedDataType:12" schemaLocation="ZUGFeRD_1p0_urn_un_unece_uncefact_data_standard_QualifiedDataType_12.xsd"/>
	<xs:import namespace="urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:12" schemaLocation="ZUGFeRD_1p0_urn_un_unece_uncefact_data_standard_ReusableAggregateBusinessInformationEntity_12.xsd"/>
	<xs:import namespace="urn:un:unece:uncefact:data:standard:UnqualifiedDataType:15" schemaLocation="ZUGFeRD_1p0_urn_un_unece_uncefact_data_standard_UnqualifiedDataType_15.xsd"/>
	<xs:element name="CrossIndustryDocument" type="rsm:CrossIndustryDocumentType"/>
	<xs:complexType name="CrossIndustryDocumentType">
		<xs:sequence>
			<xs:element name="SpecifiedExchangedDocumentContext" type="ram:ExchangedDocumentContextType"/>
			<xs:element name="HeaderExchangedDocument" type="ram:ExchangedDocumentType"/>
			<xs:element name="SpecifiedSupplyChainTradeTransaction" type="ram:SupplyChainTradeTransactionType"/>
		</xs:sequence>
	</xs:complexType>
</xs:schema>
 */


package org.fakturama.export.zugferd.modelgen;  
  
import javax.xml.bind.annotation.XmlNs;  
import javax.xml.bind.annotation.XmlSchema; 