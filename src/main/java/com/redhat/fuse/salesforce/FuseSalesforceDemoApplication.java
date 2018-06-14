package com.redhat.fuse.salesforce;

import com.redhat.fuse.salesforce.org.apache.camel.salesforce.dto.Account;
import org.apache.camel.CamelContext;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.salesforce.api.dto.CreateSObjectResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;

import java.util.Date;

@SpringBootApplication
public class FuseSalesforceDemoApplication implements CommandLineRunner {

    @Autowired
    CamelContext camelContext;

    @Autowired
    ConfigurableApplicationContext configurableApplicationContext;

    @Value("${salesforceDemo.deleteAccount:false}")
    private boolean deleteAccount;

    public static void main(String[] args) throws Exception {
        ConfigurableApplicationContext configurableApplicationContext = SpringApplication.run(FuseSalesforceDemoApplication.class, args);
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
                        .log("${header['CamelSalesforceEventType']}: ${body}");
            }
        };
    }

    @Override
    public void run(String... strings) throws Exception {
        ProducerTemplate producerTemplate = camelContext.createProducerTemplate();

        // Create a new Account
        Account account = new Account();
        account.setName("NewAccount " + new Date().toString()); // generate a unique name account
        CreateSObjectResult createSObjectResult = producerTemplate.requestBody("direct:createAccount", account, CreateSObjectResult.class);
        String id = createSObjectResult.getId();

        // Sleep between creation and update
        // Sending the update too fast might result in a single 'create' event combining both create and update data
        Thread.sleep(5_000);
        Account updatedAccount = new Account();
        updatedAccount.setId(id);
        updatedAccount.setAnnualRevenue(1000.0);
        producerTemplate.requestBody("direct:updateAccount", updatedAccount);

        // Get account
        Thread.sleep(3_000);
        Account retrievedAccount = producerTemplate.requestBody("direct:getAccount", id, Account.class);
        System.out.println(retrievedAccount.getDescription());

        if (deleteAccount) {
            producerTemplate.requestBody("direct:deleteAccount", id, CreateSObjectResult.class);
        }

        // sleep and allow the application to process
        Thread.sleep(3_000);
        configurableApplicationContext.stop();

    }
}
