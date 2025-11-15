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
        return new Gson().toJson(Map.of("message", getMessage(), "status", code.toString()));
    }

    public static ResponseException fromJson(String json) {
        if (json == null || json.isEmpty()) {
            return new ResponseException(Code.ServerError, "Empty response from server");
        }
        Map<String, Object> map = new Gson().fromJson(json, HashMap.class);
        Code status = Code.ClientError;
        if (map.containsKey("status") && map.get("status") != null) {
            try {
                status = Code.valueOf(map.get("status").toString());
            } catch (IllegalArgumentException ignored) {
                status = Code.ServerError;
            }
        }
        String message = map.getOrDefault("message", "Unknown error").toString();

        return new ResponseException(status, message);
    }

    public static Code fromHttpStatusCode(int httpStatusCode) {
        if (httpStatusCode >= 400 && httpStatusCode < 500) {
            return Code.ClientError;
        }
        if (httpStatusCode >= 500) {
            return Code.ServerError;
        }
        return Code.ServerError;
    }
}