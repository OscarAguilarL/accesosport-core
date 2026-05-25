package com.accesosport;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Requires running PostgreSQL + Cloudinary — use docker compose up -d before enabling")
class AccesosportApplicationTests {

	@Test
	void contextLoads() {
	}

}
