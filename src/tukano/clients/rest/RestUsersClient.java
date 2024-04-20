package tukano.clients.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Users;
import tukano.api.rest.RestUsers;

import java.net.URI;
import java.util.List;

public class RestUsersClient extends RestClient implements Users {

    final WebTarget target;

    public RestUsersClient(URI serverURI) {
        super(serverURI);
        target = client.target(serverURI).path(RestUsers.PATH);
    }

    @Override
    public Result<String> createUser(User user) {
        return super.reTry(() -> clt_createUser(user));
    }

    private Result<String> clt_createUser(User user) {
        return super.toJavaResult(target.request()
                .post(Entity.entity(user, MediaType.APPLICATION_JSON)), String.class);
    }

    @Override
    public Result<User> getUser(String name, String pwd) {
        return super.reTry(() -> clt_getUser(name, pwd));
    }

    private Result<User> clt_getUser(String name, String pwd) {
        return super.toJavaResult(target.path(name)
                .queryParam(RestUsers.PWD, pwd).request()
                .accept(MediaType.APPLICATION_JSON)
                .get(), User.class);
    }


    @Override
    public Result<User> updateUser(String userId, String password, User user) {
        return super.reTry(() -> clt_updateUser(userId, password, user));
    }

    private Result<User> clt_updateUser(String userId, String password, User user) {
        return super.toJavaResult(target.path(userId).queryParam(RestUsers.PWD, password).request().put(Entity.entity(user, MediaType.APPLICATION_JSON)), User.class);
    }


    @Override
    public Result<User> deleteUser(String userId, String password) {
        return super.reTry(() -> clt_deleteUser(userId, password));
    }

    private Result<User> clt_deleteUser(String userId, String password) {
        return super.toJavaResult(target.path(userId).queryParam(RestUsers.PWD, password).request().delete(), User.class);
    }


    @Override
    public Result<List<User>> searchUsers(String pattern) {
        return super.reTry(() -> clt_searchUsers(pattern));
    }

    private Result<List<User>> clt_searchUsers(String pattern) {
        return super.toJavaResult(target.path("search").queryParam("pattern", pattern).request().accept(MediaType.APPLICATION_JSON).get(), new GenericType<>() {
        });
    }

    @Override
    public Result<String> userExists(String userId) {
        return super.reTry(() -> clt_userExists(userId));
    }

    private Result<String> clt_userExists(String userId) {
        return super.toJavaResult(target.path("exists").path(userId).queryParam("userId", userId).request().get(), String.class);
    }

}
