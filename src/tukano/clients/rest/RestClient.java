package tukano.clients.rest;

import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import utils.Sleep;

import java.net.URI;
import java.util.function.Supplier;

public class RestClient {
    protected static final int MAX_RETRIES = 3;
    protected static final int RETRY_SLEEP = 1000;

    protected static final int READ_TIMEOUT = 10000;
    protected static final int CONNECT_TIMEOUT = 10000;

    final URI serverURI;
    final Client client;

    protected RestClient(URI serverURI) {
        this.serverURI = serverURI;
        ClientConfig config = new ClientConfig();
        config.property(ClientProperties.READ_TIMEOUT, READ_TIMEOUT);
        config.property(ClientProperties.CONNECT_TIMEOUT, CONNECT_TIMEOUT);
        this.client = ClientBuilder.newClient(config);
    }


    protected <T> Result<T> reTry(Supplier<Result<T>> func) {
        for (int i = 0; i < MAX_RETRIES; i++)
            try {
                return func.get();
            } catch (ProcessingException x) {
                Sleep.ms(RETRY_SLEEP);
            } catch (Exception x) {
                x.printStackTrace();
                return Result.error(ErrorCode.INTERNAL_ERROR);
            }
        return Result.error(ErrorCode.TIMEOUT);
    }

    protected <T> Result<T> toJavaResult(Response r, GenericType<T> entityType) {
        try {
            var status = r.getStatusInfo().toEnum();
            if (status == Status.OK && r.hasEntity())
                return Result.ok(r.readEntity(entityType));
            else if (status == Status.NO_CONTENT) return Result.ok();

            return Result.error(getErrorCodeFrom(status.getStatusCode()));
        } finally {
            r.close();
        }
    }

    protected <T> Result<T> toJavaResult(Response r, Class<T> entityType) {
        try {
            var status = r.getStatusInfo().toEnum();
            if (status == Status.OK && r.hasEntity())
                return Result.ok(r.readEntity(entityType));
            else if (status == Status.NO_CONTENT) return Result.ok();

            return Result.error(getErrorCodeFrom(status.getStatusCode()));
        } finally {
            r.close();
        }
    }

    public static ErrorCode getErrorCodeFrom(int status) {
        return switch (status) {
            case 200, 209 -> ErrorCode.OK;
            case 409 -> ErrorCode.CONFLICT;
            case 403 -> ErrorCode.FORBIDDEN;
            case 404 -> ErrorCode.NOT_FOUND;
            case 400 -> ErrorCode.BAD_REQUEST;
            case 500 -> ErrorCode.INTERNAL_ERROR;
            case 501 -> ErrorCode.NOT_IMPLEMENTED;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }
}