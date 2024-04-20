package tukano.client;

import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.impl.grpc.generated_java.BlobsGrpc;
import tukano.impl.grpc.generated_java.BlobsProtoBuf;

import java.net.URI;
import java.util.function.Supplier;

public class GrpcBlobsClient implements Blobs {
    private final BlobsGrpc.BlobsBlockingStub stub;

    public GrpcBlobsClient(URI serverURI) {
        var channel = ManagedChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort()).usePlaintext().build();
        stub = BlobsGrpc.newBlockingStub(channel);
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {
        return toJavaResult(() -> {
            stub.upload(BlobsProtoBuf.UploadArgs.newBuilder().setBlobId(blobId)
                    .setData(com.google.protobuf.ByteString.copyFrom(bytes))
                    .build());
            return null;
        });
    }


    @Override
    public Result<byte[]> download(String blobId) {
        return toJavaResult(() -> {
            stub.download(BlobsProtoBuf.DownloadArgs.newBuilder().setBlobId(blobId).build());
            return null;
        });
    }

    @Override
    public Result<Void> deleteBlob(String blobId) {
        return toJavaResult(() -> {
            stub.deleteBlob(BlobsProtoBuf.DeleteBlobArgs.newBuilder()
                    .setBlobId(blobId)
                    .build());
            return null;
        });
    }

    @Override
    public Result<Integer> getNumberBlobs() {
        return toJavaResult(() -> {
            var res = stub.getNumberBlobs(BlobsProtoBuf.GetNumberBlobsArgs.newBuilder().build());
            return res.getCount();
        });
    }

    static <T> Result<T> toJavaResult(Supplier<T> func) {
        try {
            return Result.ok(func.get());
        } catch (StatusRuntimeException sre) {
            if (sre.getStatus().getCode() == Status.Code.UNAVAILABLE || sre.getStatus().getCode() == Status.Code.DEADLINE_EXCEEDED)
                throw sre;
            return Result.error(statusToErrorCode(sre.getStatus()));
        }
    }

    static ErrorCode statusToErrorCode(Status status) {
        return switch (status.getCode()) {
            case OK -> ErrorCode.OK;
            case NOT_FOUND -> ErrorCode.NOT_FOUND;
            case ALREADY_EXISTS -> ErrorCode.CONFLICT;
            case PERMISSION_DENIED -> ErrorCode.FORBIDDEN;
            case INVALID_ARGUMENT -> ErrorCode.BAD_REQUEST;
            case UNIMPLEMENTED -> ErrorCode.NOT_IMPLEMENTED;
            default -> ErrorCode.INTERNAL_ERROR;
        };
    }
}
