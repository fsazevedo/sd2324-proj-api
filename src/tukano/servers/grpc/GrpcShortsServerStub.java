package tukano.servers.grpc;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.grpc.DataModelAdaptor;
import tukano.impl.grpc.generated_java.ShortsGrpc;
import tukano.impl.grpc.generated_java.ShortsProtoBuf;
import tukano.servers.java.JavaShorts;

public class GrpcShortsServerStub implements ShortsGrpc.AsyncService, BindableService {

    Shorts impl = new JavaShorts();

    @Override
    public ServerServiceDefinition bindService() {
        return ShortsGrpc.bindService(this);
    }

    @Override
    public void createShort(ShortsProtoBuf.CreateShortArgs request, StreamObserver<ShortsProtoBuf.CreateShortResult> responseObserver) {
        var res = impl.createShort(request.getUserId(), request.getPassword());
        if (res.isOK()) {
            responseObserver.onNext(ShortsProtoBuf.CreateShortResult.newBuilder().mergeValue(DataModelAdaptor.Short_to_GrpcShort(res.value())).build());
            responseObserver.onCompleted();
        } else
            responseObserver.onError(errorCodeToStatus(res.error()));
    }

    @Override
    public void deleteShort(ShortsProtoBuf.DeleteShortArgs request, StreamObserver<ShortsProtoBuf.DeleteShortResult> responseObserver) {
        var res = impl.deleteShort(request.getShortId(), request.getPassword());
        if (res.isOK()) {
            responseObserver.onNext(ShortsProtoBuf.DeleteShortResult.newBuilder().build());
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(errorCodeToStatus(res.error()));
        }
    }

    @Override
    public void getShort(ShortsProtoBuf.GetShortArgs request, StreamObserver<ShortsProtoBuf.GetShortResult> responseObserver) {
        var res = impl.getShort(request.getShortId());
        if (res.isOK()) {
            responseObserver.onNext(ShortsProtoBuf.GetShortResult.newBuilder().setValue(DataModelAdaptor.Short_to_GrpcShort(res.value())).build());
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(errorCodeToStatus(res.error()));
        }
    }

    @Override
    public void getShorts(ShortsProtoBuf.GetShortsArgs request, StreamObserver<ShortsProtoBuf.GetShortsResult> responseObserver) {
        var res = impl.getShorts(request.getUserId());
        if (res.isOK()) {
            var shortsList = res.value();
            responseObserver.onNext(ShortsProtoBuf.GetShortsResult.newBuilder().addAllShortId(shortsList).build());
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(errorCodeToStatus(res.error()));
        }
    }

    @Override
    public void follow(ShortsProtoBuf.FollowArgs request, StreamObserver<ShortsProtoBuf.FollowResult> responseObserver) {
        var res = impl.follow(request.getUserId1(), request.getUserId2(), request.getIsFollowing(), request.getPassword());
        if (res.isOK()) {
            responseObserver.onNext(null);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(errorCodeToStatus(res.error()));
        }
    }

    @Override
    public void followers(ShortsProtoBuf.FollowersArgs request, StreamObserver<ShortsProtoBuf.FollowersResult> responseObserver) {
        var res = impl.followers(request.getUserId(), request.getPassword());
        if (res.isOK()) {
            responseObserver.onNext(ShortsProtoBuf.FollowersResult.newBuilder().addAllUserId(res.value()).build());
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(errorCodeToStatus(res.error()));
        }
    }

    @Override
    public void like(ShortsProtoBuf.LikeArgs request, StreamObserver<ShortsProtoBuf.LikeResult> responseObserver) {
        var res = impl.like(request.getShortId(), request.getUserId(), request.getIsLiked(), request.getPassword());
        if (res.isOK()) {
            responseObserver.onNext(null);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(errorCodeToStatus(res.error()));
        }
    }

    @Override
    public void likes(ShortsProtoBuf.LikesArgs request, StreamObserver<ShortsProtoBuf.LikesResult> responseObserver) {
        var res = impl.likes(request.getShortId(), request.getPassword());
        if (res.isOK()) {
            ShortsProtoBuf.LikesResult result = ShortsProtoBuf.LikesResult.newBuilder()
                    .addAllUserId(res.value())
                    .build();
            responseObserver.onNext(result);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(errorCodeToStatus(res.error()));
        }
    }

    @Override
    public void getFeed(ShortsProtoBuf.GetFeedArgs request, StreamObserver<ShortsProtoBuf.GetFeedResult> responseObserver) {
        var res = impl.getFeed(request.getUserId(), request.getPassword());
        if (res.isOK()) {
            ShortsProtoBuf.GetFeedResult result = ShortsProtoBuf.GetFeedResult.newBuilder()
                    .addAllShortId(res.value())
                    .build();
            responseObserver.onNext(result);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(errorCodeToStatus(res.error()));
        }
    }

    public void burnDownUser(ShortsProtoBuf.BurnDownUserArgs request, StreamObserver<ShortsProtoBuf.BurnDownUserResult> responseObserver) {
        var res = impl.getFeed(request.getUserId(), request.getPassword());
        if (res.isOK()) {
            responseObserver.onNext(null);
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(errorCodeToStatus(res.error()));
        }
    }

    protected static Throwable errorCodeToStatus(Result.ErrorCode error) {
        var status = switch (error) {
            case NOT_FOUND -> io.grpc.Status.NOT_FOUND;
            case CONFLICT -> io.grpc.Status.ALREADY_EXISTS;
            case FORBIDDEN -> io.grpc.Status.PERMISSION_DENIED;
            case NOT_IMPLEMENTED -> io.grpc.Status.UNIMPLEMENTED;
            case BAD_REQUEST -> io.grpc.Status.INVALID_ARGUMENT;
            default -> io.grpc.Status.INTERNAL;
        };

        return status.asException();
    }
}
