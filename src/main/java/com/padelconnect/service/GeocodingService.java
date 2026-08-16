package com.padelconnect.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class GeocodingService {

    private final Map<String, double[]> cacheCoordenadas = new HashMap<>();
    private final RestTemplate restTemplate = new RestTemplate();

    public GeocodingService() {
        cacheCoordenadas.put("BELGRANO", new double[]{-34.5601, -58.4560});
        cacheCoordenadas.put("PALERMO", new double[]{-34.5711, -58.4233});
        cacheCoordenadas.put("CABALLITO", new double[]{-34.6186, -58.4411});
        cacheCoordenadas.put("RECOLETA", new double[]{-34.5875, -58.3974});
        cacheCoordenadas.put("SAN ISIDRO", new double[]{-34.4715, -58.5284});
        cacheCoordenadas.put("VICENTE LÓPEZ", new double[]{-34.5269, -58.4772});
        cacheCoordenadas.put("TIGRE", new double[]{-34.4260, -58.5796});
        cacheCoordenadas.put("RAMOS MEJÍA", new double[]{-34.6472, -58.5639});
        cacheCoordenadas.put("LOMAS DE ZAMORA", new double[]{-34.7588, -58.4022});
        cacheCoordenadas.put("LA PLATA", new double[]{-34.9214, -57.9545});
        cacheCoordenadas.put("MAR DEL PLATA", new double[]{-38.0055, -57.5426});
        cacheCoordenadas.put("ROSARIO", new double[]{-32.9468, -60.6393});
        cacheCoordenadas.put("CÓRDOBA", new double[]{-31.4201, -64.1888});
        cacheCoordenadas.put("MENDOZA", new double[]{-32.8895, -68.8458});
        cacheCoordenadas.put("MENDOZA CAPITAL", new double[]{-32.8895, -68.8458});
        cacheCoordenadas.put("MONTE BUEY", new double[]{-32.9156, -62.4549});
        cacheCoordenadas.put("VILLA MARÍA", new double[]{-32.4135, -63.2483});
        cacheCoordenadas.put("SAN MIGUEL DE TUCUMÁN", new double[]{-26.8083, -65.2176});
        cacheCoordenadas.put("SALTA", new double[]{-24.7821, -65.4232});
        cacheCoordenadas.put("BARILOCHE", new double[]{-41.1335, -71.3103});
        cacheCoordenadas.put("NEUQUÉN", new double[]{-38.9516, -68.0591});
    }

    public double[] obtenerCoordenadas(String nombreZona) {
        if (nombreZona == null || nombreZona.isBlank()) {
            return new double[]{-34.6037, -58.3816};
        }

        String key = nombreZona.toUpperCase().replaceAll("\\(.*\\)", "").trim();
        if (cacheCoordenadas.containsKey(key)) {
            return cacheCoordenadas.get(key);
        }

        String nombre = nombreZona;
        String provincia = "";

        Pattern pattern = Pattern.compile("^(.*?)\\s*\\((.*?)\\)$");
        Matcher matcher = pattern.matcher(nombreZona);
        if (matcher.find()) {
            nombre = matcher.group(1).trim();
            provincia = matcher.group(2).trim();
        }

        try {
            String url = "https://apis.datos.gob.ar/georef/api/localidades?nombre=" + URLEncoder.encode(nombre, StandardCharsets.UTF_8) + "&max=1";
            if (!provincia.isBlank() && !"CABA".equalsIgnoreCase(provincia)) {
                url += "&provincia=" + URLEncoder.encode(provincia, StandardCharsets.UTF_8);
            }

            String response = restTemplate.getForObject(url, String.class);
            if (response != null) {
                ObjectMapper mapper = new ObjectMapper();
                JsonNode root = mapper.readTree(response);
                JsonNode localidades = root.path("localidades");
                if (localidades.isArray() && localidades.size() > 0) {
                    JsonNode centroide = localidades.get(0).path("centroide");
                    double lat = centroide.path("lat").asDouble();
                    double lon = centroide.path("lon").asDouble();
                    double[] coords = new double[]{lat, lon};
                    cacheCoordenadas.put(key, coords);
                    return coords;
                }
            }
        } catch (Exception e) {
            // Fallback silencioso
        }

        return new double[]{-34.6037, -58.3816};
    }
}
