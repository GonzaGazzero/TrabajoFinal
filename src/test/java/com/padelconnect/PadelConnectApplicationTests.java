package com.padelconnect;

import com.padelconnect.service.HaversineService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class PadelConnectApplicationTests {

    @Autowired
    private HaversineService haversineService;

    @Test
    void contextLoads() {
        assertNotNull(haversineService);
    }

    @Test
    void testCalculoHaversine() {
        // Coordenadas Obelisco CABA: -34.6037, -58.3816
        // Coordenadas Club Belgrano CABA: -34.5601, -58.4560
        double distancia = haversineService.calcularDistanciaKm(-34.6037, -58.3816, -34.5601, -58.4560);
        
        // La distancia aproximada entre estos dos puntos es ~8.3 km
        assertEquals(8.3, distancia, 0.5);
    }
}
