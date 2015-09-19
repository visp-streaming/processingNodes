package ac.at.tuwien.infosys.visp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.web.SpringBootServletInitializer;

@SpringBootApplication
public class VispProcessingNodeApplication extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder appication) {
        return appication.sources(VispProcessingNodeApplication.class);
    }

    public static void main(String[] args) {
        SpringApplication.run(VispProcessingNodeApplication.class, args);
    }
}
