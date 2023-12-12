package org.arhyth.fetcher;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Fetcher {
    private static final Logger logger = LoggerFactory.getLogger("ConsoleJSONLogger");

    private static final String holdSQL = "INSERT INTO %s (timestamp, base) VALUES (%d, '%s') RETURNING id";
    private static final String rateSQL = "UPDATE %s SET pairs = '%s'::jsonb WHERE id = %d";

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
             Statement statement = connection.createStatement();) {
            long now = Instant.now().truncatedTo(ChronoUnit.HOURS).getEpochSecond();
            String sql = String.format(holdSQL, "rates", now, "USD");
            statement.execute(sql, Statement.RETURN_GENERATED_KEYS);
            ResultSet rs = statement.getGeneratedKeys();
            rs.next();
            int rateID = rs.getInt("id");

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                .GET()
                .uri(URI.create(String.format("https://openexchangerates.org/api/latest.json?app_id=%s", apiKey)))
                .build();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            if (response.statusCode() != 200) {
                logger.atError()
                    .addKeyValue("http_status", response.statusCode())
                    .log("Request failed");
                System.exit(1);
            }
            ObjectMapper mapper = new ObjectMapper();
            Rate rate = mapper.readValue(response.body(), Rate.class);
            logger.atInfo()
                .addKeyValue("id", rateID)
                .addKeyValue("ts", now)
                .addKeyValue("base", rate.base)
                .log("Fetch rate successful");
            try (Statement update = connection.createStatement()) {
                mapper = new ObjectMapper();
                byte[] bits = mapper.writeValueAsBytes(rate.pairs);
                update.execute(String.format(rateSQL, "rates", new String(bits, StandardCharsets.UTF_8), rateID));
                logger.atInfo()
                    .addKeyValue("id", rateID)
                    .addKeyValue("ts", now)
                    .addKeyValue("base", rate.base)
                    .log("Rate pairs updated");
            }
        } catch (SQLException|IOException|InterruptedException e) {
            e.printStackTrace();
        }
    }
}
