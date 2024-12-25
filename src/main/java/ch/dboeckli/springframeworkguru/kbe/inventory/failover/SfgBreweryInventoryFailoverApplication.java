package ch.dboeckli.springframeworkguru.kbe.inventory.failover;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jms.artemis.ArtemisAutoConfiguration;

@Slf4j
@SpringBootApplication(exclude = ArtemisAutoConfiguration.class)
public class SfgBreweryInventoryFailoverApplication {

    public static void main(String[] args) {
        log.info("Starting Spring 6 Template Application...");
        SpringApplication.run(SfgBreweryInventoryFailoverApplication.class, args);
    }

}
