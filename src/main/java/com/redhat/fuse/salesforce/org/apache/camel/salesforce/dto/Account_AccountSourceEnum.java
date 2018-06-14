/*
 * Salesforce DTO generated by camel-salesforce-maven-plugin
 * Generated on: Thu Jun 14 00:24:57 EDT 2018
 */
package com.redhat.fuse.salesforce.org.apache.camel.salesforce.dto;

import javax.annotation.Generated;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Salesforce Enumeration DTO for picklist AccountSource
 */
@Generated("org.apache.camel.maven.CamelSalesforceMojo")
public enum Account_AccountSourceEnum {

    // Other
    OTHER("Other"),
    // Partner Referral
    PARTNER_REFERRAL("Partner Referral"),
    // Phone Inquiry
    PHONE_INQUIRY("Phone Inquiry"),
    // Purchased List
    PURCHASED_LIST("Purchased List"),
    // Web
    WEB("Web");

    final String value;

    private Account_AccountSourceEnum(String value) {
        this.value = value;
    }

    @JsonValue
    public String value() {
        return this.value;
    }

    @JsonCreator
    public static Account_AccountSourceEnum fromValue(String value) {
        for (Account_AccountSourceEnum e : Account_AccountSourceEnum.values()) {
            if (e.value.equals(value)) {
                return e;
            }
        }
        throw new IllegalArgumentException(value);
    }

}
