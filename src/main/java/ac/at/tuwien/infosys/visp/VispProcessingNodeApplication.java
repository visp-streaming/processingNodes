package ac.at.tuwien.infosys.visp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
//public class VispProcessingNodeApplication extends SpringBootServletInitializer {
public class VispProcessingNodeApplication {

    /*
    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(VispProcessingNodeApplication.class);
    }
*/
    public static void main(String[] args) {
        SpringApplication.run(VispProcessingNodeApplication.class, args);
    }
}
