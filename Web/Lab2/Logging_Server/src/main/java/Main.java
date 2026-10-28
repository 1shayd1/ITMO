import com.fastcgi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class Main {
    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        final FCGIInterface fcgiInterface = new FCGIInterface();
        ArrayList<String> dots = new ArrayList<>();
        String port = System.getProperty("FCGI_PORT");
        log.info("Server is starting on port: " + port);
        while (fcgiInterface.FCGIaccept() >= 0) {
            long startTime = System.nanoTime();

            String traceId = FCGIInterface.request.params.getProperty("HTTP_X_REQUEST_ID");
            if (traceId == null || traceId.isBlank()) {
                traceId = UUID.randomUUID().toString();
            }
            MDC.put("traceId", traceId);

            try {
                String message;
                String status = "200 OK";
                String contentType = "text/plain; charset=utf-8";
                HashMap<String, String> querryData;
                String method = FCGIInterface.request.params.getProperty("REQUEST_METHOD");
                log.info(method + " method called");

                if (Objects.equals(method, "GET")) {
                    message = dots.toString();
                    contentType = "application/json; charset=utf-8";
                    printContent(status, message, contentType);
                    log.info(dots.size() + " records are sent");
                    log.info("Response is sent. Status: " + status);
                    continue;
                }

                if (Objects.equals(method, "DELETE")) {
                    int dotsSize = dots.size();
                    dots.clear();
                    message = "The set of dots is cleared";
                    printContent(status, message, contentType);
                    log.info(dotsSize + " records deleted");
                    log.info("Response is sent. Status: " + status + ". Message: " + message);
                    continue;
                }

                if (!Objects.equals(method, "POST")) {
                    message = "The only allowed methods are POST, GET and DELETE";
                    status = "405 Method Not Allowed";
                    printContent(status, message, contentType);
                    log.warn("The only allowed methods are POST, GET and DELETE");
                    log.info("Response is sent. Status: " + status + ". Message: " + message);
                    continue;
                }

                String length = FCGIInterface.request.params.getProperty("CONTENT_LENGTH");
                try {
                    int messageLength = Integer.parseInt(length);

                    byte[] buffer = System.in.readNBytes(messageLength);
                    message = new String(buffer, StandardCharsets.UTF_8);

                    querryData = parseBody(message);

                    if (!validate(querryData)) {
                        throw new IllegalArgumentException("x and y must ∈ [-5;5], r must ∈ {1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0}");
                    }

                    BigDecimal x = new BigDecimal(querryData.get("x"));
                    BigDecimal y = new BigDecimal(querryData.get("y"));
                    BigDecimal r = new BigDecimal(querryData.get("r"));
                    log.debug("{x=" + x.toEngineeringString() + " y=" + y.toEngineeringString() + " r=" + r.toEngineeringString() + "}");
                    boolean isHit = checkHit(querryData);
                    log.info("Is the target hit?: " + isHit);
                    long currentTime = System.currentTimeMillis();
                    long finishTime = System.nanoTime();
                    long workTime = finishTime - startTime;

                    String response = toJson(x, y, r, isHit, currentTime, workTime);
                    contentType = "application/json; charset=utf-8";

                    printContent(status, response, contentType);
                    log.info("Response is sent. Status: " + status + ". Message: " + message + ". Worktime: " + workTime);
                    dots.add(response);

                } catch (IOException e) {
                    message = "IO server error";
                    status = "500 Internal Server Error";
                    printContent(status, message, contentType);
                    log.error("IO server error", e);
                } catch (IllegalArgumentException | ArrayIndexOutOfBoundsException e) {
                    message = e.getMessage();
                    status = "400 Bad Request";
                    printContent(status, message, contentType);
                    log.warn("Status: " + status + ". Message: " + message);
                }
            } catch (Exception e){
                log.error("Server error", e);
                printContent("500 Internal Server Error", "Server Error", "text/plain; charset=utf-8");
            } finally {
               MDC.clear();
            }
        }
        log.error("FCGIaccept is failed, server stops. Mb port is busy");
    }

    public static void printContent(String status, String content, String contentType) {
        int length = content.getBytes(StandardCharsets.UTF_8).length + 2;
        System.out.print("HTTP/1.1 " + status + "\r\n");
        System.out.print("Content-Length: " + length + "\r\n");
        System.out.print("Content-Type: " + contentType + "\r\n");
        System.out.print("\r\n");
        System.out.print(content + "\r\n");
        System.out.flush();
    }

    static HashMap<String, String> parseBody(String body) {
        HashMap<String, String> map = new HashMap<>();
        String[] parts = body.split("&");
        for (String part: parts) {
            String[] newPart = part.split("=");
            if (newPart.length != 2){
                throw new ArrayIndexOutOfBoundsException("The content must consist of key and value: x&1 -> x is key, 1 is value");
            }
            String key= newPart[0];
            String value = URLDecoder.decode(newPart[1], StandardCharsets.UTF_8);
            map.put(key, value);
        }
        return map;
    }

    static boolean checkHit(HashMap<String, String> map) {
        BigDecimal x = new BigDecimal(map.get("x"));
        BigDecimal y = new BigDecimal(map.get("y"));
        BigDecimal r = new BigDecimal(map.get("r"));
        BigDecimal zero = BigDecimal.ZERO;
        BigDecimal two = new BigDecimal("2.0");

        //rectangle
        if (x.compareTo(zero) >= 0 && x.compareTo(r) <= 0 && y.compareTo(zero) >= 0 && (y.multiply(two)).compareTo(r) <= 0) {
            return true;
        }

        //triangle
        if (x.compareTo(zero) <= 0 && y.compareTo(zero) >= 0 && (y.multiply(two)).compareTo(x.add(r)) <= 0) {
            return true;
        }

        //sector
        if (x.compareTo(zero) <= 0 && y.compareTo(zero) <= 0 && x.multiply(x).add(y.multiply(y)).compareTo(r.multiply(r)) <= 0) {
            return true;
        }
        return false;
    }

    static boolean validate(HashMap<String, String> map) {
        if (!map.containsKey("x") || !map.containsKey("y") || !map.containsKey("r")) {
            throw new IllegalArgumentException("Some of x,y,r keys don't exist");
        }

        if (!map.get("x").matches("^-?(0|[1-9]\\d*)(\\.\\d{1,2})?$")){
            throw new IllegalArgumentException("x must be number, have 0-2 digits after dot and ∈ [-5;5]");
        }

        if (!map.get("y").matches("^-?(0|[1-9]\\d*)(\\.\\d{1,2})?$")){
            throw new IllegalArgumentException("y must be number, have 0-2 digits after dot and ∈ [-5;5]");
        }

        if (!map.get("r").matches("^(0|[1-9]\\d*)(\\.\\d)?$")){
            throw new IllegalArgumentException("r must be number, have 0-1 digit after dot and ∈ {1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0}");
        }


        List<BigDecimal> legalR = List.of(new BigDecimal("1.5"), new BigDecimal("2.0"),
                new BigDecimal("2.5"), new BigDecimal("3.0"), new BigDecimal("3.5"),
                new BigDecimal("4.0"), new BigDecimal("4.5"), new BigDecimal("5.0"));
        BigDecimal maxBound =  new BigDecimal("5");
        BigDecimal minBound =  new BigDecimal("-5");

        BigDecimal x = new BigDecimal(map.get("x"));
        BigDecimal y = new BigDecimal(map.get("y"));
        BigDecimal r = new BigDecimal(map.get("r"));
        return (y.compareTo(minBound) >= 0 && y.compareTo(maxBound) <= 0) &&
                (x.compareTo(minBound) >= 0 && x.compareTo(maxBound) <= 0) &&
                (legalR.stream().anyMatch(legal -> legal.compareTo(r) == 0));
    }

    static String toJson(BigDecimal x, BigDecimal y, BigDecimal r,
                         boolean isHit, long currentTime, long workTime) {
        return "{"
                + "\"x\":" + x.toPlainString() + ","
                + "\"y\":" + y.toPlainString() + ","
                + "\"r\":" + r.toPlainString() + ","
                + "\"isHit\":" + isHit + ","
                + "\"currentTime\":" + currentTime + ","
                + "\"workTime\":" + workTime
                + "}";
    }
}
