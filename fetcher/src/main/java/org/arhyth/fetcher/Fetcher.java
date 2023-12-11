package org.arhyth.fetcher;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Properties;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.message.MapMessage;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Fetcher {
    private static final Logger logger = LogManager.getLogger("ConsoleJSONLogger");

    private static final String holdSQL = "INSERT INTO %s (timestamp, base) VALUES (%d, '%s') RETURNING id";

    public static void main(String[] args) {
        Map<String, String> env = System.getenv();
        // OpenExchangeRates configuration
        String apiKey = env.get("OXR_API_KEY");
        if (apiKey == null) {
            System.out.println("Environment variable `OXR_API_KEY` is not set");
            System.exit(1);
        }
        // Database connection parameters
        String dbHost, dbPort, dbName, dbUsr, dbPwd, dbUrl;
        dbHost = env.get("DB_HOST");
        dbUsr = env.get("DB_USER");
        dbPwd = env.get("DB_PWD");
        dbPort = env.containsKey("DB_PORT") ? env.get("DB_PORT") : "5432";
        dbName = env.containsKey("DB_NAME") ? env.get("DB_NAME") : "openexchangerates";
        if (dbHost == null || dbUsr == null || dbPwd == null) {
            System.out.println("Check environment variables `DB_HOST`, `DB_USER`, `DB_PWD` are properly set");
            System.exit(1);
        }
        dbUrl = String.format("jdbc:postgresql://%s:%s/%s", dbHost, dbPort, dbName);

        try (Connection connection = DriverManager.getConnection(dbUrl, dbUsr, dbPwd);
             Statement statement = connection.createStatement()) {
            long now = Instant.now().truncatedTo(ChronoUnit.HOURS).getEpochSecond();
            String sql = String.format(holdSQL, "rates", now, "USD");

            logger.info(new MapMessage()
                .with("currency", "USD")
                .with("ts", now));
            statement.execute(sql, Statement.RETURN_GENERATED_KEYS);

            // statement.close();
            // statement = connection.createStatement();
            // double rid = statement.getGeneratedKeys().getDouble("id");
            ResultSet rs = statement.getGeneratedKeys();
            rs.next();
            int rid = rs.getInt("id");
            logger.fatal(new MapMessage()
                .with("id", rid)
                .with("currency", "USD")
                .with("ts", now));
            System.exit(1);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(String.format("https://openexchangerates.org/api/latest.json?app_id=%s", apiKey)))
                .build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            ObjectMapper mapper = new ObjectMapper();
            if (response.statusCode() != 200) {
                logger.error(String.format("Request failed with status code: %d", response.statusCode()));
                System.exit(1);
            }
            Rate rate = mapper.readValue(response.body(), Rate.class);
            logger.error(rate);
        } catch (SQLException|IOException|InterruptedException e) {
            e.printStackTrace();
        }
    }
}
