package com.triage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

// This annotation tells Spring: "This is a Spring Boot app. Scan this folder and all subfolders for my Controllers, Services, and Repositories."
@SpringBootApplication
public class TicketTriageApplication {

	public static void main(String[] args) {
		// This line is the entry point. It starts the embedded web server (Tomcat), 
		// connects to the database, and loads all your Spring Beans (Dependency Injection).
		SpringApplication.run(TicketTriageApplication.class, args);
	}

}
