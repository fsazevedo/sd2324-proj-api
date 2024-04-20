package tukano.clients.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.MediaType;
import tukano.api.Follow;
import tukano.api.Liked;
import tukano.api.Short;
import tukano.api.java.Result;
import tukano.api.java.Shorts;
import tukano.api.rest.RestShorts;
import tukano.api.rest.RestUsers;

import java.net.URI;
import java.util.List;

public class RestShortsClient extends RestClient implements Shorts {

    private final WebTarget target;

    public RestShortsClient(URI serverURI) {
        super(serverURI);
        this.target = client.target(serverURI).path(RestShorts.PATH);
    }

    @Override
    public Result<Short> createShort(String userId, String password) {
        return super.reTry(() -> clt_createShort(userId, password));
    }

    private Result<Short> clt_createShort(String userId, String password) {
        return super.toJavaResult(target.request().post(Entity.entity(Shorts.class, MediaType.APPLICATION_JSON)), Short.class);
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {
        return super.reTry(() -> clt_deleteShort(shortId, password));
    }

    private Result<Void> clt_deleteShort(String shortId, String password) {
        return super.toJavaResult(target.path(shortId).queryParam(RestUsers.PWD, password).request().delete(), Void.class);
    }

    @Override
    public Result<Short> getShort(String shortId) {
        return super.reTry(() -> clt_etShort(shortId));
    }

    private Result<Short> clt_etShort(String shortId) {
        return super.toJavaResult(target.path(shortId).request().accept(MediaType.APPLICATION_JSON).get(), Short.class);
    }

    @Override
    public Result<List<String>> getShorts(String userId) {
        return super.reTry(() -> clt_getShorts(userId));
    }

    private Result<List<String>> clt_getShorts(String userId) {
        return super.toJavaResult(target.path(userId).path("shorts").request(MediaType.APPLICATION_JSON).get(), new GenericType<>() {
        });
    }


    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        return super.reTry(() -> clt_follow(userId1, userId2, isFollowing, password));
    }

    private Result<Void> clt_follow(String userId1, String userId2, boolean isFollowing, String password) {
        return super.toJavaResult(target.path(userId1).path(userId2).path("followers")
                .queryParam("pwd", password)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(Follow.class, MediaType.APPLICATION_JSON)), Void.class);
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        return super.reTry(() -> clt_followers(userId, password));
    }

    private Result<List<String>> clt_followers(String userId, String password) {
        return super.toJavaResult(target.path(userId).path("followers")
                .queryParam("pwd", password)
                .request(MediaType.APPLICATION_JSON)
                .get(), new GenericType<>() {
        });
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {
        return super.reTry(() -> clt_like(shortId, userId, isLiked, password));
    }

    private Result<Void> clt_like(String shortId, String userId, boolean isLiked, String password) {
        return super.toJavaResult(target.path(shortId).path(userId).path("likes")
                .queryParam("pwd", password)
                .request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(Liked.class, MediaType.APPLICATION_JSON)), Void.class);
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        return super.reTry(() -> clt_likes(shortId, password));

    }

    private Result<List<String>> clt_likes(String shortId, String password) {
        return super.toJavaResult(target.path(shortId).path("likes")
                .queryParam("pwd", password)
                .request(MediaType.APPLICATION_JSON)
                .get(), new GenericType<>() {
        });
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        return super.reTry(() -> clt_getFeed(userId, password));
    }

    private Result<List<String>> clt_getFeed(String userId, String password) {
        return super.toJavaResult(target.path(userId).path("feed")
                .queryParam("pwd", password)
                .request(MediaType.APPLICATION_JSON)
                .get(), new GenericType<>() {
        });
    }

    @Override
    public Result<Void> burnDownUser(String userId, String password) {
        return super.reTry(() -> clt_burnDownUser(userId, password));
    }

    private Result<Void> clt_burnDownUser(String userId, String password) {
        return super.toJavaResult(target.path("user").path(userId).path("all")
                .queryParam("pwd", password).request().delete(), Void.class);
    }
}
