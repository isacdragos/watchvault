package com.watchvault.watchvaultbackendspringboot;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
		"spring.datasource.url=jdbc:h2:mem:watchvault_context_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.jpa.defer-datasource-initialization=true",
		"spring.sql.init.mode=always",
		"server.ssl.enabled=false"
})
class WatchvaultBackendSpringbootApplicationTests {

	@Test
	void contextLoads() {
	}

}
