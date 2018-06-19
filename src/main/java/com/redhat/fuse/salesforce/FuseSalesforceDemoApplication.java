package com.redhat.fuse.salesforce;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class FuseSalesforceDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(FuseSalesforceDemoApplication.class, args);
    }

    @Bean
    public RouteBuilder salesforceRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("direct:createAccount").routeId("createAccount")
                        .to("salesforce:createSObject?SObjectName=Account");

                from("direct:updateAccount").routeId("updateAccount")
                        .to("salesforce:updateSObject?SObjectName=Account");

                from( "direct:getAccount").routeId("getAccount")
                        .to("salesforce:getSObject?SObjectName=Account&sObjectFields=Id,Name,BillingAddress,AnnualRevenue");

                from( "direct:deleteAccount").routeId("deleteAccount")
                        .to("salesforce:deleteSObject?SObjectName=Account");

                from("salesforce:AccountChanges?notifyForFields=ALL").routeId("accountNotifications")
                        .to("seda:accountNotifications");
            }
        };
    }
}
