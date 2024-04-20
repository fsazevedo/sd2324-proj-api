package tukano.client.grpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import utils.Sleep;

import java.util.function.Supplier;

public class GrpcClient {
    protected static final int MAX_RETRIES = 3;
    protected static final int RETRY_SLEEP = 1000;

    final ManagedChannel channel;
    final String target;

    protected GrpcClient(String target) {
        this.target = target;
        this.channel = ManagedChannelBuilder.forTarget(target).usePlaintext().build();
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

    protected <T> Result<T> reTry(Supplier<Result<T>> func) {
        for (int i = 0; i < MAX_RETRIES; i++) {
            try {
                return func.get();
            } catch (StatusRuntimeException e) {
                Sleep.ms(RETRY_SLEEP);
            } catch (Exception e) {
                e.printStackTrace();
                return Result.error(ErrorCode.INTERNAL_ERROR);
            }
        }
        return Result.error(ErrorCode.TIMEOUT);
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

    public void shutdown() {
        channel.shutdown(); // Don't forget to shutdown the channel properly
    }
}
