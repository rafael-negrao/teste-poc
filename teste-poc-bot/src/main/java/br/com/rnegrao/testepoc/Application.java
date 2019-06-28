package br.com.rnegrao.testepoc;

import com.pengrad.telegrambot.TelegramBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableScheduling
@EnableFeignClients
@SpringBootApplication
public class Application {

	@Value("${telegram.token}")
	private String token;

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Bean
	public TelegramBot get() {
		return new TelegramBot.Builder(token).build();

	}

}
