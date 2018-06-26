package com.redhat.fuse.salesforce;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.Processor;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.rest.RestBindingMode;
import org.apache.camel.salesforce.dto.QueryRecordsAccount;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

@SpringBootApplication
public class FuseSalesforceDemoApplication {

    private List<Notification> notifications = new CopyOnWriteArrayList<>();

    public static void main(String[] args) {
        SpringApplication.run(FuseSalesforceDemoApplication.class, args);
    }

    @Bean
    public RouteBuilder salesforceRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() throws Exception {

                // Camel routes for integrating with Salesforce
                from("direct:createAccount").routeId("createAccount")
                        .to("salesforce:createSObject?SObjectName=Account");

                from("direct:updateAccount").routeId("updateAccount")
                        .to("salesforce:updateSObject?SObjectName=Account");

                from("direct:getAccount").routeId("getAccount")
                        .to("salesforce:getSObject?SObjectName=Account&sObjectFields=Id,Name,TickerSymbol,Type,Active__c,AccountNumber,AnnualRevenue");

                from("direct:deleteAccount").routeId("deleteAccount")
                        .to("salesforce:deleteSObject?SObjectName=Account");

                from("direct:getAccounts").routeId("getAccounts")
                        .to("salesforce:query?sObjectQuery=SELECT Id,Name,TickerSymbol,Type,Active__c,AccountNumber,AnnualRevenue from Account&sObjectClass=" + QueryRecordsAccount.class.getName());

                from("salesforce:AcctUpdate?notifyForFields=ALL").routeId("accountNotifications")
                        .to("log:salesforce?showHeaders=true")
                        .to("seda:accountNotifications");

                // Capture and store incoming notifications
                from("seda:accountNotifications").process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                        Message in = exchange.getIn();
                        notifications.add(new Notification((String) in.getHeader("CamelSalesforceEventType"),
                                (String) in.getHeader("CamelSalesforceCreatedDate"),
                                in.getBody(Map.class)));

                        exchange.setOut(null);
                    }
                });

                // REST endpoints for interacting with Camel routes
                restConfiguration()
                        .component("servlet")
                        .bindingMode(RestBindingMode.auto);

                rest("/accounts")
                        .get("/").route().transform().simple("").to("direct:getAccounts").setBody().simple("${body.records}").endRest()

                        .get("/{id}").route().transform().simple("${headers['id']}").to("direct:getAccount").endRest()

                        .post("/").to("direct:createAccount")

                        .put("/{id}").route().setHeader("sObjectId").simple("${headers['id']}").to("direct:updateAccount").endRest()

                        .delete("/{id}").route().transform().simple("${headers['id']}").to("direct:deleteAccount").endRest();

                rest("/notifications")
                        .get().route().setBody().constant(notifications).endRest();
            }
        };
    }
}
