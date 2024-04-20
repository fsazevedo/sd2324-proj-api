package tukano.servers.grpc;

import io.grpc.BindableService;
import io.grpc.ServerServiceDefinition;
import io.grpc.stub.StreamObserver;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Users;
import tukano.grpc.DataModelAdaptor;
import tukano.impl.grpc.generated_java.UsersGrpc;
import tukano.impl.grpc.generated_java.UsersProtoBuf;
import tukano.servers.java.JavaUsers;

import java.util.logging.Logger;


public class GrpcUsersServerStub implements UsersGrpc.AsyncService, BindableService {

    Users impl = new JavaUsers();

    private static Logger Log = Logger.getLogger(GrpcUsersServerStub.class.getName());


    @Override
    public ServerServiceDefinition bindService() {
        return UsersGrpc.bindService(this);
    }

    @Override
    public void createUser(UsersProtoBuf.CreateUserArgs request, StreamObserver<UsersProtoBuf.CreateUserResult> responseObserver) {
        var res = impl.createUser(DataModelAdaptor.GrpcUser_to_User(request.getUser()));
        if (res.isOK()) {
            responseObserver.onNext(UsersProtoBuf.CreateUserResult.newBuilder().setUserId(res.value()).build());
            responseObserver.onCompleted();
        } else
            responseObserver.onError(errorCodeToStatus(res.error()));
    }

    @Override
    public void getUser(UsersProtoBuf.GetUserArgs request, StreamObserver<UsersProtoBuf.GetUserResult> responseObserver) {
        var res = impl.getUser(request.getUserId(), request.getPassword());
        if (res.isOK()) {
            responseObserver.onNext(UsersProtoBuf.GetUserResult.newBuilder().setUser(DataModelAdaptor.User_to_GrpcUser(res.value())).build());
            responseObserver.onCompleted();
        } else
            responseObserver.onError(errorCodeToStatus(res.error()));
    }

    @Override
    public void updateUser(UsersProtoBuf.UpdateUserArgs request, StreamObserver<UsersProtoBuf.UpdateUserResult> responseObserver) {
        var res = impl.updateUser(request.getUserId(), request.getPassword(), DataModelAdaptor.GrpcUser_to_User(request.getUser()));
        if (res.isOK()) {
            responseObserver.onNext(UsersProtoBuf.UpdateUserResult.newBuilder().setUser(DataModelAdaptor.User_to_GrpcUser(res.value())).build());
            responseObserver.onCompleted();
        } else responseObserver.onError(errorCodeToStatus(res.error()));
    }

    @Override
    public void deleteUser(UsersProtoBuf.DeleteUserArgs request, StreamObserver<UsersProtoBuf.DeleteUserResult> responseObserver) {
        var res = impl.deleteUser(request.getUserId(), request.getPassword());
        if (res.isOK()) {
            responseObserver.onNext(UsersProtoBuf.DeleteUserResult.newBuilder()
                    .setUser(DataModelAdaptor.User_to_GrpcUser(res.value())).build());
            responseObserver.onCompleted();
        } else responseObserver.onError(errorCodeToStatus(res.error()));
    }

    @Override
    public void searchUsers(UsersProtoBuf.SearchUserArgs request, StreamObserver<UsersProtoBuf.GrpcUser> responseObserver) {
        var res = impl.searchUsers(request.getPattern());
        if (res.isOK()) {
            for (User user : res.value()) {
                responseObserver.onNext(DataModelAdaptor.User_to_GrpcUser(user));
            }
            responseObserver.onCompleted();
        } else {
            responseObserver.onError(errorCodeToStatus(res.error()));
        }
    }


    @Override
    public void userExists(UsersProtoBuf.UserExistsArgs request, StreamObserver<UsersProtoBuf.UserExistsResult> responseObserver) {
        var res = impl.userExists(request.getUserId());
        if (res.isOK()) {
            responseObserver.onNext(UsersProtoBuf.UserExistsResult.newBuilder().setUserId(res.value()).build());
            responseObserver.onCompleted();
        } else responseObserver.onError(errorCodeToStatus(res.error()));
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
