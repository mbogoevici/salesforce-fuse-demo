package com.redhat.fuse.salesforce;

import com.redhat.fuse.salesforce.org.apache.camel.salesforce.dto.Account;
import org.apache.camel.component.salesforce.SalesforceComponent;
import org.apache.camel.component.salesforce.SalesforceLoginConfig;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SalesforceConfiguration {


    @Bean
    @ConfigurationProperties("salesforce")
    public SalesforceComponent salesforce() {
        SalesforceComponent salesforceComponent = new SalesforceComponent();
        salesforceComponent.setPackages(Account.class.getPackage().getName());
        return salesforceComponent;
    }
}