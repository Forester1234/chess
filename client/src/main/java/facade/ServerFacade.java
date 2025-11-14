package facade;

import com.google.gson.Gson;
import exception.ResponseException;

import service.joinr.JoinRequest;
import service.joinr.JoinResult;
import service.loginr.LoginRequest;
import service.loginr.LoginResult;
import service.registerr.RegisterRequest;
import service.registerr.RegisterResult;

import java.net.*;
import java.net.http.*;

public class ServerFacade {

    private final Gson gson = new Gson();
    private final HttpClient client = HttpClient.newHttpClient();
    private final String serverUrl;

    public ServerFacade(int port) {
        this.serverUrl = "http://localhost:" + port;
    }

    // ------------------------------------------------------------------

    public RegisterResult register(RegisterRequest req) throws ResponseException {
        return http("POST",
                "/user",
                req,
                RegisterResult.class,
                null
        );
    }

    public LoginResult login(LoginRequest req) throws ResponseException {
        return http(
                "POST",
                "/session",
                req,
                LoginResult.class,
                null
        );
    }

    public JoinResult join(JoinRequest req) throws ResponseException {
        return http(
                "PUT",
                "/game",
                req,
                JoinResult.class,
                req.authToken()
        );
    }

    // -------------helper functions-------------------------------------

    private <T> T http(String method, String path, Object body, Class<T> responseType, String authToken)
            throws ResponseException {
        HttpRequest request = buildRequest(method, path, body, authToken);
        HttpResponse<String> response = sendRequest(request);
        return handleResponse(response, responseType);
    }

    private HttpRequest buildRequest(String method, String path, Object body, String authToken) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null) {
            builder.setHeader("Content-Type", "application/json");
        }
        if (authToken != null) {
            builder.setHeader("Authorization", authToken);
        }
        return builder.build();
    }

    private HttpRequest.BodyPublisher makeRequestBody(Object request) {
        return (request != null)
                ? HttpRequest.BodyPublishers.ofString(gson.toJson(request))
                : HttpRequest.BodyPublishers.noBody();
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return client.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (Exception ex) {
            throw new ResponseException(ResponseException.Code.ServerError, ex.getMessage());
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        int status = response.statusCode();
        if (!isSuccessful(status)) {
            String errorJson = response.body();
            if (errorJson != null && !errorJson.isEmpty()) {
                throw ResponseException.fromJson(errorJson);
            }
            throw new ResponseException(
                    ResponseException.fromHttpStatusCode(status),
                    "Unexpected error: " + status
            );
        }
        if (responseClass == null) return null;
        return gson.fromJson(response.body(), responseClass);
    }

    private boolean isSuccessful(int status) {
        return status >= 200 && status < 300;
    }
}
