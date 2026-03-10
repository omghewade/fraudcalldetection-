package com.Teleco.FraudCall;

import com.Teleco.FraudCall.model.CDRRecord;
import com.Teleco.FraudCall.model.CallStore;
import com.Teleco.FraudCall.model.GeneratorConfig;
import com.Teleco.FraudCall.service.CDRGeneratorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class FraudCallApplicationTests {

	@Autowired
	private CDRGeneratorService generatorService;

	@Test
	void contextLoads() {
	}

	@Test
	void testCDRRecordInitialization() {
		CDRRecord record = CDRRecord.createNew();
		assertThat(record.getRecordType()).isEqualTo("MO");
		assertThat(record.getSystemIdentity()).isEqualTo("d0");
		assertThat(record.getCallingIMSI()).isNotNull();
		assertThat(record.getCalledIMSI()).isNotNull();
	}

	@Test
	void testCDRRecordSetData() {
		CDRRecord record = CDRRecord.createNew();
		record.setData("CallingNum", "1234567890");
		record.setData("CalledNum", "0987654321");
		record.setData("CallPeriod", "120");

		assertThat(record.getCallingNum()).isEqualTo("1234567890");
		assertThat(record.getCalledNum()).isEqualTo("0987654321");
		assertThat(record.getCallPeriod()).isEqualTo(120);
	}

	@Test
	void testCallStore() {
		CallStore store = new CallStore(100);
		assertThat(store.getCallNos()).hasSize(100);
		assertThat(store.getSwitchCountries()).hasSize(5);
		assertThat(store.getRandomCallNo()).isNotNull();
		assertThat(store.getRandomSwitch()).isNotNull();
	}

	@Test
	void testGeneratorConfig() {
		GeneratorConfig config = new GeneratorConfig(100, 0.2f, 1);
		assertThat(config.getCdrPerFile()).isEqualTo(100);
		assertThat(config.getCallBackPercent()).isEqualTo(0.2f);
		assertThat(config.getDurationHours()).isEqualTo(1);
		assertThat(config.toString()).contains("100");
	}

	@Test
	void testServiceGenerateRecords() {
		List<CDRRecord> records = generatorService.generateRecords(5);
		assertThat(records).hasSize(5);
		for (CDRRecord record : records) {
			assertThat(record.getCallingIMSI()).isNotNull();
			assertThat(record.getSwitchNum()).isNotNull();
		}
	}
}
