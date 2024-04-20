package tukano.client;

import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import tukano.api.Short;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.grpc.DataModelAdaptor;
import tukano.impl.grpc.generated_java.ShortsGrpc;
import tukano.impl.grpc.generated_java.ShortsProtoBuf;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class GrpcShortsClient implements Shorts {

    private final ShortsGrpc.ShortsBlockingStub stub;

    public GrpcShortsClient(URI serverURI) {
        var channel = ManagedChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort()).usePlaintext().build();
        stub = ShortsGrpc.newBlockingStub(channel);
    }

    @Override
    public Result<Short> createShort(String userId, String password) {
        return toJavaResult(() -> {
            var res = stub.createShort(ShortsProtoBuf.CreateShortArgs.newBuilder()
                    .setUserId(userId).setPassword(password)
                    .build());
            return DataModelAdaptor.GrpcShort_to_Short(res.getValue());
        });
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        return toJavaResult(() -> {
            stub.deleteShort(ShortsProtoBuf.DeleteShortArgs.newBuilder()
                    .setShortId(shortId).setPassword(password)
                    .build());
            return null;
        });
    }

    @Override
    public Result<Short> getShort(String shortId) {
        return toJavaResult(() -> {
            var res = stub.getShort(ShortsProtoBuf.GetShortArgs.newBuilder()
                    .setShortId(shortId)
                    .build());
            return DataModelAdaptor.GrpcShort_to_Short(res.getValue());
        });
    }

    @Override
    public Result<List<String>> getShorts(String userId) {
        return toJavaResult(() -> {
            var res = stub.getShorts(ShortsProtoBuf.GetShortsArgs.newBuilder().setUserId(userId).build());
            List<String> shortsList = new ArrayList<>();
            res.getShortIdList().forEach(shortsList::add);
            return shortsList;
        });
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        return toJavaResult(() -> {
            stub.follow(ShortsProtoBuf.FollowArgs.newBuilder()
                    .setUserId1(userId1).setUserId2(userId2).setIsFollowing(isFollowing).setPassword(password)
                    .build());
            return null;
        });
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        return toJavaResult(() -> {
            var res = stub.followers(ShortsProtoBuf.FollowersArgs.newBuilder().setUserId(userId).setPassword(password).build());
            List<String> followersList = new ArrayList<>();
            followersList.addAll(res.getUserIdList());
            return followersList;
        });
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        return toJavaResult(() -> {
            stub.like(ShortsProtoBuf.LikeArgs.newBuilder()
                    .setShortId(shortId).setUserId(userId).setIsLiked(isLiked).setPassword(password)
                    .build());
            return null;
        });
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        return toJavaResult(() -> {
            var res = stub.likes(ShortsProtoBuf.LikesArgs.newBuilder().setShortId(shortId).setPassword(password).build());
            List<String> likesList = new ArrayList<>();
            likesList.addAll(res.getUserIdList());
            return likesList;
        });
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        return toJavaResult(() -> {
            var res = stub.getFeed(ShortsProtoBuf.GetFeedArgs.newBuilder().setUserId(userId).setPassword(password).build());
            List<String> feedList = new ArrayList<>();
            feedList.addAll(res.getShortIdList()); // Assuming the response has a list of Short IDs
            return feedList;
        });
    }

    @Override
    public Result<Void> burnDownUser(String userId, String password) {
        return toJavaResult(() -> {
            stub.burnDownUser(ShortsProtoBuf.BurnDownUserArgs.newBuilder()
                    .setUserId(userId).setPassword(password)
                    .build());
            return null;
        });
    }

    static <T> Result<T> toJavaResult(Supplier<T> func) {
        try {
            return Result.ok(func.get());
        } catch (StatusRuntimeException sre) {
            var code = sre.getStatus().getCode();
            if (code == Status.Code.UNAVAILABLE || code == Status.Code.DEADLINE_EXCEEDED)
                throw sre;
            return Result.error(statusToErrorCode(sre.getStatus()));
        }
    }

    static Result.ErrorCode statusToErrorCode(Status status) {
        return switch (status.getCode()) {
            case OK -> Result.ErrorCode.OK;
            case NOT_FOUND -> Result.ErrorCode.NOT_FOUND;
            case ALREADY_EXISTS -> Result.ErrorCode.CONFLICT;
            case PERMISSION_DENIED -> Result.ErrorCode.FORBIDDEN;
            case INVALID_ARGUMENT -> Result.ErrorCode.BAD_REQUEST;
            case UNIMPLEMENTED -> Result.ErrorCode.NOT_IMPLEMENTED;
            default -> Result.ErrorCode.INTERNAL_ERROR;
        };
    }

}
