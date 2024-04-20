package tukano.servers.java;

import tukano.api.Follow;
import tukano.api.Liked;
import tukano.api.Short;
import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.java.Shorts;
import tukano.client.Clients;
import tukano.persistence.Hibernate;

import java.net.URI;
import java.util.*;
import java.util.logging.Logger;

public class JavaShorts implements Shorts {
    private static Logger Log = Logger.getLogger(JavaShorts.class.getName());
    private final Hibernate session = Hibernate.getInstance();

    @Override
    public Result<Short> createShort(String userId, String password) {
        if (emptyStringToNull(userId) == null || emptyStringToNull(password) == null) {
            Log.info("User ID and password cannot be null or empty.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            List<URI> uris = Clients.blobsClient.getAllClients();
            int temp = Integer.MAX_VALUE;
            int index = 0;

            for (int i = 0; i < uris.size(); i++) {
                int count = Clients.blobsClient.get(uris.get(i)).getNumberBlobs().value();
                if (temp > count) {
                    temp = count;
                    index = i;
                }
            }

            String blobUri = uris.get(index).toString();

            Result<User> success = Clients.usersClient.get().getUser(userId, password);

            if (!success.isOK()) {
                Log.info("User does not exist or password is incorrect.");
                return Result.error(ErrorCode.NOT_FOUND);
            }

            User user = success.value();
            if (!user.getPwd().equals(password)) {
                Log.info("Password is incorrect.");
                return Result.error(ErrorCode.FORBIDDEN);
            }

            String shortId = UUID.randomUUID().toString();
            String blobLocation = blobUri + "/blobs/" + UUID.randomUUID().toString();
            Short newShort = new Short(shortId, userId, blobLocation);

            session.persist(newShort);
            return Result.ok(newShort);
        } catch (Exception e) {
            Log.severe("An error occurred while creating the short: " + e.getMessage());
            return Result.error(ErrorCode.BAD_REQUEST);
        }
    }

    @Override
    public Result<Void> deleteShort(String shortId, String password) {

        if (emptyStringToNull(shortId) == null || emptyStringToNull(password) == null) {
            Log.info("Short ID and password cannot be null or empty.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            Short shortToDelete = getShortWithId(shortId);
            if (shortToDelete == null) {
                return Result.error(ErrorCode.NOT_FOUND);
            }

            Result<User> success = Clients.usersClient.get().getUser(shortToDelete.getOwnerId(), password);

            if (success.error() == ErrorCode.NOT_FOUND) {
                Log.info("User does not exist.");
                return Result.error(ErrorCode.NOT_FOUND);
            }

            if (success.error() == ErrorCode.FORBIDDEN) {
                Log.info("Password is incorrect.");
                return Result.error(ErrorCode.FORBIDDEN);
            }

            Clients.blobsClient.get().deleteBlob(getShort(shortId).value().getBlobUrl().split("/blobs/")[1]);


            session.delete(shortToDelete);

            return Result.ok();
        } catch (Exception e) {

            Log.info("An error occurred while deleting the short: " + e.getMessage());
            return Result.error(ErrorCode.BAD_REQUEST);
        }
    }

    @Override
    public Result<Short> getShort(String shortId) {
        if (emptyStringToNull(shortId) == null) {
            Log.info("Short ID cannot be null or empty.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            Short shortEntity = getShortWithId(shortId);
            if (shortEntity == null) {
                return Result.error(ErrorCode.NOT_FOUND);
            }
            return Result.ok(shortEntity);
        } catch (Exception e) {
            Log.info("An error occurred while retrieving the short: " + e.getMessage());
            return Result.error(ErrorCode.BAD_REQUEST);
        }
    }

    @Override
    public Result<List<String>> getShorts(String userId) {
        if (emptyStringToNull(userId) == null) {
            Log.info("User ID cannot be null or empty.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            Result<String> check1 = Clients.usersClient.get().userExists(userId);

            if (!check1.isOK()) {
                Log.info("User does not exist or password is incorrect.");
                return Result.error(ErrorCode.NOT_FOUND);
            }

            List<String> shortIds = session.sql("SELECT shortId FROM SHORT u WHERE u.ownerId = '" + userId + "'",
                    String.class);

            return Result.ok(shortIds);
        } catch (Exception e) {
            Log.severe("An error occurred while retrieving the shorts: " + e.getMessage());
            return Result.error(ErrorCode.BAD_REQUEST);
        }
    }

    @Override
    public Result<Void> follow(String userId1, String userId2, boolean isFollowing, String password) {
        if (emptyStringToNull(userId1) == null || emptyStringToNull(userId2) == null
                || emptyStringToNull(password) == null) {
            Log.info("User IDs and password cannot be null or empty.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {

            Result<User> success = Clients.usersClient.get().getUser(userId1, password);
            if (!success.isOK()) {
                Log.info("User does not exist or password is incorrect.");
                return Result.error(ErrorCode.NOT_FOUND);
            }

            User user1 = success.value();
            if (!user1.getPwd().equals(password)) {
                Log.info("Password is incorrect.");
                return Result.error(ErrorCode.FORBIDDEN);
            }

            Result<String> check2 = Clients.usersClient.get().userExists(userId2);

            if (!check2.isOK()) {
                Log.info("User does not exist or password is incorrect.");
                return Result.error(ErrorCode.NOT_FOUND);
            }

            List<Follow> follow = session.sql(
                    "SELECT * FROM FOLLOW f WHERE f.followed = '" + userId2 + "' AND f.follower ='" + userId1 + "'",
                    Follow.class);

            Follow followInsert = null;
            if (isFollowing) {

                if (follow.isEmpty()) {
                    followInsert = new Follow(userId2, userId1);
                    session.persist(followInsert);
                } else
                    return Result.error(ErrorCode.CONFLICT);
            } else {

                if (!follow.isEmpty()) {
                    followInsert = follow.get(0);
                    session.delete(followInsert);
                } else
                    return Result.ok();

            }

            return Result.ok();
        } catch (Exception e) {
            Log.severe("An error occurred while updating follow status: " + e.getMessage());
            return Result.error(ErrorCode.BAD_REQUEST);
        }
    }

    @Override
    public Result<List<String>> followers(String userId, String password) {
        if (emptyStringToNull(userId) == null || emptyStringToNull(password) == null) {
            Log.info("User ID and password cannot be null or empty.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            Result<User> success = Clients.usersClient.get().getUser(userId, password);

            if (!success.isOK()) {
                Log.info("User does not exist or password is incorrect.");
                return Result.error(ErrorCode.NOT_FOUND);
            }

            User user1 = success.value();
            if (!user1.getPwd().equals(password)) {
                Log.info("Password is incorrect.");
                return Result.error(ErrorCode.FORBIDDEN);
            }

            List<String> followers = session.sql("SELECT f.follower FROM FOLLOW f WHERE f.followed = '" + userId + "'",
                    String.class);
            if (followers.isEmpty()) {
                return Result.ok(new LinkedList<String>());
            } else
                return Result.ok(followers);
        } catch (Exception e) {
            Log.info("An error occurred while retrieving followers: " + e.getMessage());
            return Result.error(ErrorCode.BAD_REQUEST);
        }
    }

    @Override
    public Result<Void> like(String shortId, String userId, boolean isLiked, String password) {

        if (emptyStringToNull(shortId) == null || emptyStringToNull(userId) == null
                || emptyStringToNull(password) == null || password.isEmpty()) {
            Log.info("Short ID, user ID, and password cannot be null or empty.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            Result<User> success = Clients.usersClient.get().getUser(userId, password);

            if (!success.isOK()) {
                Log.info("User does not exist or password is incorrect.");
                return Result.error(ErrorCode.NOT_FOUND);
            }

            User user1 = success.value();
            if (!user1.getPwd().equals(password)) {
                Log.info("Password is incorrect.");
                return Result.error(ErrorCode.FORBIDDEN);
            }

            List<Liked> likes = session.sql(
                    "SELECT * FROM LIKED l WHERE l.shortId = '" + shortId + "' AND l.likedBy = '" + userId + "'",
                    Liked.class);
            Liked like = null;
            if (isLiked) {
                if (likes.isEmpty()) {
                    like = new Liked(shortId, userId);
                    session.persist(like);
                    Short shortExists = getShort(shortId).value();
                    shortExists.incrementLike();
                    session.update(shortExists);
                } else {
                    return Result.error(ErrorCode.CONFLICT);
                }
            } else {
                if (!likes.isEmpty()) {

                    session.delete(likes.get(0));
                    Short shortExists = getShort(shortId).value();
                    shortExists.decrementLike();
                    session.update(shortExists);
                } else {
                    return Result.error(ErrorCode.CONFLICT);
                }
            }

            return Result.ok();
        } catch (Exception e) {
            Log.severe("An error occurred while updating follow status: " + e.getMessage());
            return Result.error(ErrorCode.BAD_REQUEST);
        }
    }

    @Override
    public Result<List<String>> likes(String shortId, String password) {
        if (emptyStringToNull(shortId) == null || emptyStringToNull(password) == null) {
            Log.info("Short ID and password cannot be null or empty.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            Result<Short> shortResult = getShort(shortId);

            if (!shortResult.isOK()) {
                Log.info("Short does not exist.");
                return Result.error(ErrorCode.NOT_FOUND);
            }

            Short s = shortResult.value();

            Result<User> check = Clients.usersClient.get().getUser(s.getOwnerId(), password);

            if (!check.isOK())
                return Result.error(ErrorCode.NOT_FOUND);

            User user = check.value();

            if (!user.getPwd().equals(password)) {
                Log.info("Password is incorrect.");
                return Result.error(ErrorCode.FORBIDDEN);
            }
            List<String> likes = session.sql("SELECT l.likedBy FROM Liked l WHERE l.shortId = '" + shortId + "'",
                    String.class);
            if (likes.isEmpty()) {
                return Result.ok(new LinkedList<String>());
            } else
                return Result.ok(likes);
        } catch (Exception e) {
            Log.info("An error occurred while retrieving likes: " + e.getMessage());
            return Result.error(ErrorCode.BAD_REQUEST);
        }
    }

    @Override
    public Result<List<String>> getFeed(String userId, String password) {
        if (emptyStringToNull(userId) == null || emptyStringToNull(password) == null) {
            Log.info("User ID and password cannot be null or empty.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            Result<User> success = Clients.usersClient.get().getUser(userId, password);

            if (!success.isOK()) {
                Log.info("User does not exist or password is incorrect.");
                return Result.error(ErrorCode.NOT_FOUND);
            }

            User user1 = success.value();
            if (!user1.getPwd().equals(password)) {
                Log.info("Password is incorrect.");
                return Result.error(ErrorCode.FORBIDDEN);
            }

            List<String> followedUsers = session
                    .sql("SELECT f.followed FROM FOLLOW f WHERE f.follower = '" + userId + "'", String.class);

            List<Short> feedShorts = new ArrayList<Short>();

            feedShorts.addAll(session.sql("SELECT * FROM SHORT s WHERE s.ownerId = '" + userId + "'", Short.class));

            for (String s : followedUsers) {
                feedShorts.addAll(getShortsWithUserId(s));
            }

            if (feedShorts.isEmpty()) {
                return Result.ok(new LinkedList<String>());
            } else {
                feedShorts.sort(Comparator.comparingLong(Short::getTimestamp).reversed());
                return Result.ok(feedShorts.stream().map(Short::getShortId).toList());
            }
        } catch (Exception e) {
            Log.info("An error occurred while retrieving the feed: " + e.getMessage());
            return Result.error(ErrorCode.BAD_REQUEST);
        }

    }

    @Override
    public Result<Void> burnDownUser(String userId, String password) {

        try {
            List<String> shorts = getShorts(userId).value();
            for (String sId : shorts) {
                like(sId, userId, false, password);
                deleteShort(sId, password);
            }

            List<Follow> follow = session.sql(
                    "SELECT * FROM FOLLOW f WHERE f.followed = '" + userId + "' OR f.follower ='" + userId + "'",
                    Follow.class);

            for (Follow f : follow)
                session.delete(f);

            List<Liked> likes = getLikesWithUserId(userId);

            for (Liked l : likes) {
                Short s = getShortWithId(l.getShortId());
                like(s.getShortId(), userId, false, password);
            }

            return Result.ok();
        } catch (Exception e) {
            Log.info("An error occurred while retrieving the feed: " + e.getMessage());
            return Result.error(ErrorCode.BAD_REQUEST);
        }
    }

    private Short getShortWithId(String shortId) {

        List<Short> bdShort = session.sql("SELECT * FROM SHORT u WHERE u.shortId = '" + shortId + "'", Short.class);
        if (bdShort.isEmpty()) {
            Log.info("Short does not exist.");
            return null;
        }
        return bdShort.get(0);
    }

    private List<Liked> getLikesWithUserId(String userId) {
        return session.sql("SELECT * FROM LIKED l WHERE l.likedBy = '" + userId + "'", Liked.class);
    }

    private List<Short> getShortsWithUserId(String userId) {
        return session.sql("SELECT * FROM SHORT u WHERE u.ownerId = '" + userId + "'", Short.class);
    }

    private static String emptyStringToNull(String s) {
        return s == null || s.length() == 0 ? null : s;
    }

}
