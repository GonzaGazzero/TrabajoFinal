package com.padelconnect.service;

import org.springframework.stereotype.Service;

@Service
public class HaversineService {

    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * Calcula la distancia en kilómetros entre dos puntos geográficos (latitud y longitud)
     * utilizando la fórmula matemática de Haversine.
     *
     * @param lat1 Latitud del punto 1
     * @param lon1 Longitud del punto 1
     * @param lat2 Latitud del punto 2
     * @param lon2 Longitud del punto 2
     * @return Distancia redondeada a un decimal (en km)
     */
    public Double calcularDistanciaKm(Double lat1, Double lon1, Double lat2, Double lon2) {
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return null;
        }

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.sin(dLon / 2) * Math.sin(dLon / 2) * Math.cos(radLat1) * Math.cos(radLat2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distancia = EARTH_RADIUS_KM * c;
        return Math.round(distancia * 10.0) / 10.0;
    }
}
