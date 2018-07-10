package pl.legol.logrest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import sun.rmi.log.ReliableLog;

import javax.validation.Valid;
import java.nio.file.*;
import java.util.Random;

import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

@EnableScheduling
@SpringBootApplication
public class LogrestApplication {

	private static final Logger log = LoggerFactory.getLogger(LogrestApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(LogrestApplication.class, args);
	}

	@Autowired
	private LogController logController;

	@Scheduled(fixedRate = 1000)
	public void generateLogs() {
		log.info("Generated log - random {}", new Random().nextLong());
	}

	@Bean
	public CommandLineRunner logFileWatcher() {
		return args -> new LogFileWatcher("logs/Logrest.log", 1000, logController).run();
	}
}
