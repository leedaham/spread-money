package me.study.spreadmoney;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@SpringBootApplication
public class SpreadMoneyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpreadMoneyApplication.class, args);
	}

}
