package nagp.directservice.requests;




import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.jms.annotation.EnableJms;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
@EnableDiscoveryClient
@EnableJms
public class RequestsApplication {

	public static void main(String[] args) {
		SpringApplication.run(RequestsApplication.class, args);
	}
	
	@Bean(name = "restTemplate")
	RestTemplate restTemplate() {
		return new RestTemplate();
	}
}
