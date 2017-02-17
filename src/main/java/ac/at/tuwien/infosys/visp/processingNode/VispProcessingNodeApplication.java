package ac.at.tuwien.infosys.visp.processingNode;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@SpringBootApplication
public class VispProcessingNodeApplication {

    @Autowired
    Receiver receiver;

    public static void main(String[] args) {
        SpringApplication.run(VispProcessingNodeApplication.class, args);
    }
}
