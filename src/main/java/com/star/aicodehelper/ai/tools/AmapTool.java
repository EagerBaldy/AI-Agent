package com.star.aicodehelper.ai.tools;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
@Slf4j
public class AmapTool {

    @Value("${amap.key}")
    private String apiKey;

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public AmapTool() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Get weather information for a specific city
     * @param city City name (e.g., "北京市", "Shanghai")
     * @return Weather description
     */
    public String getWeather(String city) {
        try {
            // 1. Get Adcode from city name
            String adcode = getAdcode(city);
            if (adcode == null) {
                return "Sorry, I couldn't find the location: " + city;
            }

            // 2. Get Weather from Adcode
            return getWeatherInfo(adcode, city);

        } catch (Exception e) {
            log.error("Failed to get weather for city: {}", city, e);
            return "Sorry, an error occurred while checking the weather.";
        }
    }

    private String getAdcode(String address) {
        try {
            // URL Encode the address to handle Chinese characters and special symbols
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = String.format("https://restapi.amap.com/v3/geocode/geo?address=%s&key=%s", 
                    encodedAddress, apiKey);
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());

            if ("1".equals(root.path("status").asText()) && root.path("geocodes").size() > 0) {
                return root.path("geocodes").get(0).path("adcode").asText();
            }
        } catch (Exception e) {
            log.error("Geocoding failed for address: {}", address, e);
        }
        return null;
    }

    private String getWeatherInfo(String adcode, String cityName) {
        try {
            String url = String.format("https://restapi.amap.com/v3/weather/weatherInfo?city=%s&key=%s&extensions=all",
                    adcode, apiKey);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            JsonNode root = objectMapper.readTree(response.body());

            if ("1".equals(root.path("status").asText()) && root.path("forecasts").size() > 0) {
                JsonNode forecast = root.path("forecasts").get(0);
                JsonNode casts = forecast.path("casts");
                StringBuilder sb = new StringBuilder();
                sb.append(String.format("Weather forecast for %s:\n", cityName));

                for (JsonNode cast : casts) {
                    String date = cast.path("date").asText();
                    String dayWeather = cast.path("dayweather").asText();
                    String nightWeather = cast.path("nightweather").asText();
                    String dayTemp = cast.path("daytemp").asText();
                    String nightTemp = cast.path("nighttemp").asText();
                    String dayWind = cast.path("daywind").asText();
                    String dayPower = cast.path("daypower").asText();

                    sb.append(String.format("- %s: Day %s %s°C, Night %s %s°C, Wind: %s (%s level)\n",
                            date, dayWeather, dayTemp, nightWeather, nightTemp, dayWind, dayPower));
                }
                return sb.toString();
            }
        } catch (Exception e) {
            log.error("Weather info failed for adcode: {}", adcode, e);
        }
        return "Weather data not available for " + cityName;
    }
}
