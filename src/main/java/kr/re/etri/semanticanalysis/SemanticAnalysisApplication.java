package kr.re.etri.semanticanalysis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SemanticAnalysisApplication {

	public static void main(String[] args) {
		SpringApplication.run(SemanticAnalysisApplication.class, args);
	}
}
