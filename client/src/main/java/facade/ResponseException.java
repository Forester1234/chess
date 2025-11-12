package facade;

public class ResponseException extends RuntimeException {
    public static Object Code;

    public ResponseException(Object o, String message) {
        super(message);
    }

    public static Object fromHttpStatusCode(int status) {
    }

    public static ResponseException fromJson(String body) {
    }
}
