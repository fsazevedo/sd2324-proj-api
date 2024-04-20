package tukano.client;


import io.grpc.ManagedChannelBuilder;
import io.grpc.Status;
import io.grpc.Status.Code;
import io.grpc.StatusRuntimeException;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.java.Users;
import tukano.grpc.DataModelAdaptor;
import tukano.impl.grpc.generated_java.UsersGrpc;
import tukano.impl.grpc.generated_java.UsersProtoBuf.*;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;


public class GrpcUsersClient implements Users {
    final UsersGrpc.UsersBlockingStub stub;

    public GrpcUsersClient(URI serverURI) {
        var channel = ManagedChannelBuilder.forAddress(serverURI.getHost(), serverURI.getPort()).usePlaintext().build();
        stub = UsersGrpc.newBlockingStub(channel);
    }

    @Override
    public Result<String> createUser(User user) {
        return toJavaResult(() -> {
            var res = stub.createUser(CreateUserArgs.newBuilder()
                    .setUser(DataModelAdaptor.User_to_GrpcUser(user))
                    .build());
            return res.getUserId();
        });
    }

    @Override
    public Result<User> getUser(String userId, String pwd) {
        return toJavaResult(() -> {
            var res = stub.getUser(GetUserArgs.newBuilder().setUserId(userId).setPassword(pwd).build());
            return DataModelAdaptor.GrpcUser_to_User(res.getUser());
        });
    }

    @Override
    public Result<User> updateUser(String userId, String pwd, User user) {
        return toJavaResult(() -> {
            var res = stub.updateUser(UpdateUserArgs.newBuilder().setUserId(userId).setPassword(pwd).setUser(DataModelAdaptor.User_to_GrpcUser(user)).build());
            return DataModelAdaptor.GrpcUser_to_User(res.getUser());
        });
    }

    @Override
    public Result<User> deleteUser(String userId, String pwd) {
        return toJavaResult(() -> {
            var res = stub.deleteUser(DeleteUserArgs.newBuilder().setUserId(userId).setPassword(pwd).build());
            return DataModelAdaptor.GrpcUser_to_User(res.getUser());
        });
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        return toJavaResult(() -> {
            var res = stub.searchUsers(SearchUserArgs.newBuilder().setPattern(pattern).build());
            List<User> userList = new ArrayList<>();
            while (res.hasNext()) {
                userList.add(DataModelAdaptor.GrpcUser_to_User(res.next()));
            }
            return userList;
        });
    }

    @Override
    public Result<String> userExists(String userId) {
        return toJavaResult(() -> {
            var res = stub.userExists(UserExistsArgs.newBuilder().setUserId(userId).build());
            return res.getUserId();
        });
    }

    static <T> Result<T> toJavaResult(Supplier<T> func) {
        try {
            return Result.ok(func.get());
        } catch (StatusRuntimeException sre) {
            var code = sre.getStatus().getCode();
            if (code == Code.UNAVAILABLE || code == Code.DEADLINE_EXCEEDED)
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
