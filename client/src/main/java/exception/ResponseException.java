package exception;

import com.google.gson.Gson;

import java.util.HashMap;
import java.util.Map;

public class ResponseException extends Exception {

    public enum Code {
        ServerError,
        ClientError,
    }

    final private Code code;

    public ResponseException(Code code, String message) {
        super(message);
        this.code = code;
    }

    public String toJson() {
        return new Gson().toJson(Map.of("message", getMessage(), "status", code));
    }

    public static ResponseException fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return new ResponseException(Code.ServerError, "Empty response from server");
        }
        Map<String, Object> map = new Gson().fromJson(json, HashMap.class);
        Code status = Code.ServerError;
        if (map.containsKey("status") && map.get("status") != null) {
            try {
                status = Code.valueOf(map.get("status").toString());
            } catch (IllegalArgumentException e) {
                status = Code.ServerError;
            }
        }
        String message = "Unknown error";
        if (map.containsKey("message") && map.get("message") != null) {
            message = map.get("message").toString();
        }
        return new ResponseException(status, message);
    }

    public static Code fromHttpStatusCode(int httpStatusCode) {
        return switch (httpStatusCode) {
            case 500 -> Code.ServerError;
            case 400 -> Code.ClientError;
            default -> throw new IllegalArgumentException("Unknown HTTP status code: " + httpStatusCode);
        };
    }
}