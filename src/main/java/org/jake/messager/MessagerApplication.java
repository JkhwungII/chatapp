package org.jake.messager;

import org.jake.messager.message.Message;
import org.jake.messager.message.MessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.time.LocalDateTime;
import java.util.List;

@SpringBootApplication
public class MessagerApplication {

	private static final Logger logger
			= LoggerFactory.getLogger(MessagerApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(MessagerApplication.class, args);
	}

}
