package ac.at.tuwien.infosys.visp;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = VispProcessingNodeApplication.class)
public class RedisTest {

    @Autowired
    private StringRedisTemplate template;

    @Test
    public void testRedis() {
        HashOperations<String, String, String> ops = this.template.opsForHash();

        String key = "101";

        if (!this.template.hasKey(key)) {
            ops.put(key, "start", new DateTime().toString());
        }
        System.out.println("Found key " + key + ", value=" + ops.get(key, "start"));
    }



}
