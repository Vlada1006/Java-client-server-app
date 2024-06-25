package org.example;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONObject;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.Base64;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// Відсутність негативних значень в БД прописані при її створенні

public class HttpStoreServer {
    public HttpStoreServer() throws SQLException {
    }

    public static void main(String[] args) throws Exception {

        String[][] login_data = new String[2][2];
        login_data[0] = new String[]{"admin", "admin"};
        login_data[1] = new String[]{"user_login", "user_password"};

        String Jwt_header = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";

        MessageDigest md = MessageDigest.getInstance("MD5");

        for (int i = 0; i < login_data.length; i++) {
            for (int j = 0; j < login_data[i].length; j++) {
                byte[] hashBytes = md.digest(login_data[i][j].getBytes());
                StringBuilder sb = new StringBuilder();
                for (byte b : hashBytes) {
                    sb.append(String.format("%02x", b));
                }
                login_data[i][j] = sb.toString();
            }
        }

        HttpServer server = HttpServer.create();
        server.bind(new InetSocketAddress(63623), 0);

        HttpContext login_context = server.createContext("/login", new LoginHandler(login_data, Jwt_header));
        HttpContext put_context = server.createContext("/api/good", new GoodHandler());
        //HttpContext id_context = server.createContext("/api/good/", new GoodIDHandler());

        server.setExecutor(null);
        server.start();

    }

    static DatabaseProcessor database;

    static {
        try {
            database = new DatabaseProcessor("jdbc:mysql://localhost:3306/mydatabase", "AE1165et");
        } catch (SQLException e) {
            System.out.println("ERROR");
            throw new RuntimeException(e);
        }
    }

    private static String secret_key = "Very-very string key";

    private static String base64Encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    private static String base64Decode(String encodedString) {
        return new String(Base64.getUrlDecoder().decode(encodedString));
    }

    private static String hmacSha256(String data, String password) {
        try {
            byte[] hash = password.getBytes(StandardCharsets.UTF_8);
            Mac sha256Hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(hash, "HmacSHA256");
            sha256Hmac.init(secretKey);
            byte[] signedBytes = sha256Hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return base64Encode(signedBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private static boolean checkJwt(String body, String password){
        Pattern pattern = Pattern.compile("JWT=\".+?\"");
        Matcher matcher = pattern.matcher(body);
        String JWT = "";
        if(matcher.find()){
            JWT = matcher.group();
            JWT = JWT.substring(5, JWT.length()-1);
        }
        else{
            return false;
        }
        StringBuilder builder = new StringBuilder();
        String[] parts = JWT.split("\\.");
        builder.append(parts[0]).append(".").append(parts[1]);
        return Objects.equals(hmacSha256(builder.toString(), password), parts[2]);
    }

    private static Json_POJO readJson(String body) {
        Pattern pattern = Pattern.compile("good_json=\\{.+\\}");
        Matcher matcher = pattern.matcher(body);
        String json_string = "";
        if(matcher.find()){
            json_string = matcher.group().substring(10);
            System.out.println(json_string);
        }
        else{
            return null;
        }
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(json_string, Json_POJO.class);
        }
        catch (JsonProcessingException e){
            return null;
        }
    }

    static class GoodIDHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            exchange.sendResponseHeaders(666, -1);
        }
    }

    static class GoodHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            Pattern pattern_id = Pattern.compile("/api/good/\\d+");
            Matcher matcher_id = pattern_id.matcher(exchange.getRequestURI().toString());
            String good_id = "";
            if(matcher_id.find()){
                good_id = matcher_id.group().substring(10);
            }

            System.out.println(good_id);

            InputStream body_stream = exchange.getRequestBody();
            String body = URLDecoder.decode(new String(body_stream.readAllBytes(), StandardCharsets.UTF_8), StandardCharsets.UTF_8);

            Json_POJO json = new Json_POJO();

            if (!Objects.equals(exchange.getRequestMethod(), "GET") && !Objects.equals(exchange.getRequestMethod(), "DELETE")){     // Скіпаємо перевірку, бо в
                // GET та DELETE немає json
                if ((json = readJson(body)) == null){
                    exchange.sendResponseHeaders(409, -1);
                    throw new IOException();
                }

            }
            if(!checkJwt(body, secret_key)){
                exchange.sendResponseHeaders(401, -1);
                throw new IOException();
            }

            if (Objects.equals(exchange.getRequestMethod(), "GET")){
                System.out.println(json.getGood_id());
                json.setGood_id(Integer.parseInt(good_id));
                try {
                    String info = database.run_query(json, 0);
                    byte[] bytes = info.getBytes();
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(201, bytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                } catch (SQLException e) {
                    System.out.println(e);
                    exchange.sendResponseHeaders(404, -1);
                    throw new RuntimeException(e);
                }
            }

            else if (Objects.equals(exchange.getRequestMethod(), "PUT")){
                try {
                    String info = database.run_query(json, 1);
                    System.out.println(info);
                    byte[] bytes = info.getBytes();
                    exchange.sendResponseHeaders(200, bytes.length);
                    OutputStream os = exchange.getResponseBody();
                    os.write(bytes);
                    os.close();
                } catch (SQLException e) {
                    System.out.println(e);
                    exchange.sendResponseHeaders(409, -1);
                    throw new RuntimeException(e);
                }
            }


            else if (Objects.equals(exchange.getRequestMethod(), "POST")){
                json.setGood_id(Integer.parseInt(good_id));
                System.out.println(json.getGood_id());
                System.out.println(json.getName());
                System.out.println(json.getAmount());
                System.out.println(json.getPrice());
                try {
                    database.run_query(json, 2);
                }
                catch (SQLIntegrityConstraintViolationException e) {
                    System.out.println(e);
                    exchange.sendResponseHeaders(404, -1);
                    throw new RuntimeException(e);
                }
                catch (SQLException e) {
                    System.out.println(e);
                    exchange.sendResponseHeaders(409, -1);
                    throw new RuntimeException(e);
                }

            }

            else if (Objects.equals(exchange.getRequestMethod(), "DELETE")){
                json.setGood_id(Integer.parseInt(good_id));
                try {
                    database.run_query(json, 3);
                } catch (SQLException e) {
                    System.out.println(e);
                    throw new RuntimeException(e);
                }
            }

            exchange.sendResponseHeaders(666, -1);
        }
    }

    static class LoginHandler implements HttpHandler {

        private static String JWT_HEADER = null;
        private final String[][] login_data;

        LoginHandler(String[][] login_data, String Jwt_header) {
            this.login_data = login_data;
            JWT_HEADER = Jwt_header;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {

            StringBuilder builder = new StringBuilder();

            String params = exchange.getRequestURI().getQuery();

            if (!Objects.equals(exchange.getRequestMethod(), "POST")){
                exchange.sendResponseHeaders(401, -1);
                throw new IOException();
            }

            Pattern login_pattern = Pattern.compile("login=.+&");
            Pattern password_pattern = Pattern.compile("password=.+");

            Matcher login_matcher = login_pattern.matcher(params);
            Matcher password_matcher = password_pattern.matcher(params);

            if (!login_matcher.find()){
                exchange.sendResponseHeaders(401, -1);
                throw new IOException();
            }

            if (!password_matcher.find()){
                exchange.sendResponseHeaders(401, -1);
                throw new IOException();
            }

            String login = login_matcher.group();
            login = login.substring(6, login.length() - 1);
            String password = password_matcher.group();
            password = password.substring(9);

            MessageDigest md;
            try {
                md = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            boolean passed = false;
            for (int i = 0; i < login_data.length; i++){
                if (login.equals(login_data[i][0]) && password.equals(login_data[i][1])){
                    passed = true;
                    break;
                }
            }

            if(!passed){
                exchange.sendResponseHeaders(401, -1);
                throw new IOException();
            }

            builder.append(base64Encode(JWT_HEADER.getBytes())).append(".");

            JSONObject user_data = new JSONObject();
            user_data.put("login", login);
            user_data.put("password", password);

            builder.append(base64Encode(user_data.toString().getBytes()));

            String signature = hmacSha256(builder.toString(), secret_key);

            builder.append(".").append(signature);

            byte[] bytes = builder.toString().getBytes();
            exchange.sendResponseHeaders(200, bytes.length);

            OutputStream os = exchange.getResponseBody();
            os.write(bytes);
            os.close();
        }
    }
}



