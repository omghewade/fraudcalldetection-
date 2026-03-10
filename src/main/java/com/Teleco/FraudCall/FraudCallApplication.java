package com.Teleco.FraudCall;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import com.Teleco.FraudCall.config.TelcoGeneratorProperties;

@SpringBootApplication
@EnableConfigurationProperties(TelcoGeneratorProperties.class)
public class FraudCallApplication {

	public static void main(String[] args) {
		SpringApplication.run(FraudCallApplication.class, args);
	}

}
