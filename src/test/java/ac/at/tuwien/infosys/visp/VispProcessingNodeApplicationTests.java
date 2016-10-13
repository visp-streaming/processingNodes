package ac.at.tuwien.infosys.visp;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@TestPropertySource(locations="classpath:application.properties")
public class VispProcessingNodeApplicationTests {

	@Ignore
	@Test
	public void contextLoads() {
	}

}
