package naumen.daniilmezev.systemproject;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SystemprojectApplication {

	public static void main(String[] args) {
		SpringApplication.run(SystemprojectApplication.class, args);
	}

}
