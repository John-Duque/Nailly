package com.nailly;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class NaillyApplicationTests {

    @Test
    @DisplayName("Aplicação sobe com o perfil test")
    void contextLoads() {
    }

}
