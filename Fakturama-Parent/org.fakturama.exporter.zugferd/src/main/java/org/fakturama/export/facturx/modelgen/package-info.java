@XmlSchema(namespace = "urn:factur-x:pdfa:CrossIndustryDocument:invoice:1p0",  
    xmlns = {   
//        @XmlNs(namespaceURI = "http://www.w3.org/2001/XMLSchema", prefix = "xs"),  
        @XmlNs(namespaceURI = "urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100", prefix = "ram"),  
        @XmlNs(namespaceURI = "urn:un:unece:uncefact:data:standard:QualifiedDataType:100", prefix = "qdt"),  
        @XmlNs(namespaceURI = "urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100", prefix = "rsm"),  
        @XmlNs(namespaceURI = "urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100", prefix = "udt")  
    },  
    elementFormDefault = javax.xml.bind.annotation.XmlNsForm.UNQUALIFIED)  
  
/*
<xs:schema xmlns:rsm="urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100"
    xmlns:xs="http://www.w3.org/2001/XMLSchema"
    xmlns:qdt="urn:un:unece:uncefact:data:standard:QualifiedDataType:100"
    xmlns:ram="urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100"
    xmlns:udt="urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100"
    targetNamespace="urn:un:unece:uncefact:data:standard:CrossIndustryInvoice:100"
    elementFormDefault="qualified">
  <xs:import namespace="urn:un:unece:uncefact:data:standard:QualifiedDataType:100" schemaLocation="FACTUR-X_EN16931_urn_un_unece_uncefact_data_standard_QualifiedDataType_100.xsd"/>
  <xs:import namespace="urn:un:unece:uncefact:data:standard:ReusableAggregateBusinessInformationEntity:100" schemaLocation="FACTUR-X_EN16931_urn_un_unece_uncefact_data_standard_ReusableAggregateBusinessInformationEntity_100.xsd"/>
  <xs:import namespace="urn:un:unece:uncefact:data:standard:UnqualifiedDataType:100" schemaLocation="FACTUR-X_EN16931_urn_un_unece_uncefact_data_standard_UnqualifiedDataType_100.xsd"/>
  <!-- definition slightly changed so that xjb could annotate it with XmlRootelement -->
  <xs:element name="CrossIndustryInvoice">
      <xs:complexType>
        <xs:sequence>
          <xs:element name="ExchangedDocumentContext" type="ram:ExchangedDocumentContextType"/>
          <xs:element name="ExchangedDocument" type="ram:ExchangedDocumentType"/>
          <xs:element name="SupplyChainTradeTransaction" type="ram:SupplyChainTradeTransactionType"/>
        </xs:sequence>
      </xs:complexType>
  </xs:element>
</xs:schema>
 */


package org.fakturama.export.facturx.modelgen;  
  
import javax.xml.bind.annotation.XmlNs;  
import javax.xml.bind.annotation.XmlSchema; 