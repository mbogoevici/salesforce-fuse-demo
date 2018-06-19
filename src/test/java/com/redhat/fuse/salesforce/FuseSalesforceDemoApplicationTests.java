package com.redhat.fuse.salesforce;

import org.apache.camel.CamelContext;
import org.apache.camel.ConsumerTemplate;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.salesforce.api.dto.CreateSObjectResult;
import org.apache.camel.salesforce.dto.Account;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;

@RunWith(SpringRunner.class)
@SpringBootTest
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FuseSalesforceDemoApplicationTests {

	private ProducerTemplate producerTemplate;

	private ConsumerTemplate consumerTemplate;

	private static String accountId;

	private static String accountName;

	@Autowired
	public void setCamelContext(CamelContext camelContext) {
		this.producerTemplate = camelContext.createProducerTemplate();
		this.consumerTemplate = camelContext.createConsumerTemplate();
	}

	@BeforeClass
	public static void setUp() {
		accountId = null;
		accountName = "TestAccount " + new Date().toString();
	}

	@Test
	public void testCreate() {

		// Create a new Account
		Account account = new Account();
		account.setName(accountName); // generate a unique accountName account
		CreateSObjectResult result =
				producerTemplate.requestBody("direct:createAccount", account,
						CreateSObjectResult.class);

		// test that object creation has succeeded
		assertThat(result.getErrors()).isEmpty();
		assertThat(result.getId()).isNotNull();
		accountId = result.getId();

		Exchange receivedEvent = consumerTemplate.receive("seda:accountNotifications", 5000);
		assertThat(receivedEvent.getIn().getHeaders()).contains(entry("CamelSalesforceEventType", "created"));
		assertThat(receivedEvent.getIn().getBody(Map.class)).contains(entry("Id", accountId));
		assertThat(receivedEvent.getIn().getBody(Map.class)).contains(entry("Name", accountName));
	}

	@Test
	public void testModifyAndGet() {

		Account retrievedAccount = producerTemplate.requestBody("direct:getAccount", accountId, Account.class);
		assertThat(retrievedAccount).hasFieldOrPropertyWithValue("Id", accountId);
		assertThat(retrievedAccount).hasFieldOrPropertyWithValue("Name", accountName);
		assertThat(retrievedAccount).hasFieldOrPropertyWithValue("AnnualRevenue", null);

		Account accountToUpdate = new Account();
		accountToUpdate.setId(accountId);
		accountToUpdate.setAnnualRevenue(1000.0);
		producerTemplate.sendBody("direct:updateAccount", accountToUpdate);

		Exchange receivedEvent = consumerTemplate.receive("seda:accountNotifications", 5000);
		assertThat(receivedEvent.getIn().getHeaders()).contains(entry("CamelSalesforceEventType", "updated"));
		assertThat(receivedEvent.getIn().getBody(Map.class)).contains(entry("Id", accountId));
		assertThat(receivedEvent.getIn().getBody(Map.class)).contains(entry("Name", accountName));
		assertThat(receivedEvent.getIn().getBody(Map.class)).contains(entry("AnnualRevenue", 1000.0));

		retrievedAccount = producerTemplate.requestBody("direct:getAccount", accountId, Account.class);
		assertThat(retrievedAccount).hasFieldOrPropertyWithValue("Id", accountId);
		assertThat(retrievedAccount).hasFieldOrPropertyWithValue("Name", accountName);
		assertThat(retrievedAccount).hasFieldOrPropertyWithValue("AnnualRevenue", 1000.0);

	}

	@Test
	public void testRemove() {

		producerTemplate.sendBody("direct:deleteAccount", accountId);

		Exchange receivedEvent = consumerTemplate.receive("seda:accountNotifications", 5000);
		assertThat(receivedEvent.getIn().getHeaders()).contains(entry("CamelSalesforceEventType", "deleted"));
		assertThat(receivedEvent.getIn().getBody(Map.class)).contains(entry("Id", accountId));
	}


}
