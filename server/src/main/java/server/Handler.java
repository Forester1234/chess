package server;

import com.google.gson.Gson;
import io.javalin.http.Context;
import org.jetbrains.annotations.NotNull;
import service.LogoutRequest;
import service.RegisterR.RegisterRequest;
import service.LoginR.LoginRequest;
import service.Service;

public class Handler {

    private final Service service;
    private final Gson gson = new Gson();

    public Handler(Service service) {
        this.service = service;
    }

    public void register(Context ctx) {
        try {
            RegisterRequest register = gson.fromJson(ctx.body(), RegisterRequest.class);
            var result = service.register(register);
            ctx.status(200).json(result);

        } catch (IllegalArgumentException e) {
            ctx.status(400).json(new ErrorMessage(e.getMessage()));

        } catch (IllegalStateException e) {
            ctx.status(403).json(new ErrorMessage(e.getMessage()));

        } catch (Exception e) {
            ctx.status(500).json(new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    public void login(Context ctx) {
        try {
            LoginRequest login = gson.fromJson(ctx.body(), LoginRequest.class);
            var result = service.login(login);
            ctx.status(200).json(result);
        } catch (IllegalArgumentException e) {
            ctx.status(400).json(new ErrorMessage(e.getMessage()));

        } catch (IllegalStateException e) {
            ctx.status(401).json(new ErrorMessage(e.getMessage()));

        } catch (Exception e) {
            ctx.status(500).json(new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    public void logout(Context ctx) {
        try {
            LogoutRequest logout = gson.fromJson(ctx.body(), LogoutRequest.class);
            service.logOut(logout);
            ctx.status(200).json(new Object());
        } catch (IllegalStateException e) {
            ctx.status(401).json(new ErrorMessage(e.getMessage()));

        } catch (Exception e) {
            ctx.status(500).json(new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    public void clearAll(Context ctx) {
        try {
            service.clearAll();
            ctx.status(200).json(new Object());
        } catch (Exception e) {
            ctx.status(500).json(new ErrorMessage("Error: " + e.getMessage()));
        }
    }

    private record ErrorMessage(String message) {}
}