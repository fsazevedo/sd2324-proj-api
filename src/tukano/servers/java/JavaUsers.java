package tukano.servers.java;


import tukano.api.User;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;
import tukano.api.java.Users;
import tukano.client.Clients;
import tukano.persistence.Hibernate;

import java.util.List;
import java.util.logging.Logger;

public class JavaUsers implements Users {

    private static Logger Log = Logger.getLogger(JavaUsers.class.getName());
    private final Hibernate session = Hibernate.getInstance();

    @Override
    public Result<String> createUser(User user) {
        Log.info("createUser : " + user);

        if (emptyStringToNull(user.userId()) == null || emptyStringToNull(user.pwd()) == null || emptyStringToNull(user.displayName()) == null
                || emptyStringToNull(user.email()) == null) {
            Log.info("User object invalid.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            List<User> existingUser = session.sql("SELECT * FROM USER u WHERE u.userId = '" + user.getUserId() + "'", User.class);

            if (!existingUser.isEmpty()) {
                Log.info("User already exists");
                return Result.error(ErrorCode.CONFLICT);
            }

            session.persist(user);
            return Result.ok(user.getUserId());
        } catch (Exception e) {
            return Result.error(ErrorCode.CONFLICT);
        }
    }

    @Override
    public Result<User> getUser(String userId, String pwd) {

        if (emptyStringToNull(userId) == null || emptyStringToNull(pwd) == null) {
            Log.info("User ID or password cannot be null or empty.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        try {
            List<User> existingUser = session.sql("SELECT * FROM USER u WHERE u.userId = '" + userId + "'", User.class);
            if (existingUser.isEmpty()) {
                Log.info("No user exists with the provided userId.");
                return Result.error(ErrorCode.NOT_FOUND);
            }

            if (!(existingUser.get(0).getPwd().equals(pwd))) {
                Log.info("Password is incorrect.");
                return Result.error(ErrorCode.FORBIDDEN);
            }

            return Result.ok(existingUser.get(0));
        } catch (Exception e) {
            Log.info("Error retrieving user: " + e.getMessage());
            return Result.error(ErrorCode.BAD_REQUEST);
        }
    }

    @Override
    public Result<User> updateUser(String userId, String pwd, User user) {
        if (user == null || emptyStringToNull(userId) == null || emptyStringToNull(pwd) == null || (emptyStringToNull(user.getUserId()) != null && !emptyStringToNull(user.getUserId()).equals(userId))) {
            Log.info("Updated user information cannot be null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            List<User> existingUser = session.sql("SELECT * FROM USER u WHERE u.userId = '" + userId + "'", User.class);
            if (existingUser.isEmpty()) {
                Log.info("No user exists with the provided userId.");
                return Result.error(ErrorCode.NOT_FOUND);
            }
            User userNew = existingUser.get(0);

            if (!userNew.getPwd().equals(pwd)) {
                Log.info("Password is incorrect.");
                return Result.error(ErrorCode.FORBIDDEN);
            }

            if (emptyStringToNull(user.getDisplayName()) != null) {
                userNew.setDisplayName(user.getDisplayName());
            }

            if (emptyStringToNull(user.getEmail()) != null) {
                userNew.setEmail(user.getEmail());
            }
            if (emptyStringToNull(user.getPwd()) != null) {
                if (!user.getPwd().isEmpty())
                    userNew.setPwd(user.getPwd());
            }

            session.update(userNew);
            return Result.ok(userNew);
        } catch (Exception e) {
            Log.info("Error updating user: " + e.getMessage());
            return Result.error(ErrorCode.BAD_REQUEST);
        }
    }

    @Override
    public Result<User> deleteUser(String userId, String pwd) {
        if (emptyStringToNull(userId) == null) {
            Log.info("User ID must not be null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        if (emptyStringToNull(pwd) == null) {
            Log.info("Password must not be null.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        try {
            List<User> existingUser = session.sql("SELECT * FROM USER u WHERE u.userId = '" + userId + "'", User.class);
            if (existingUser.isEmpty()) {
                Log.info("No user exists with the provided userId.");
                return Result.error(ErrorCode.NOT_FOUND);
            }
            if (!existingUser.get(0).getPwd().equals(pwd)) {
                Log.info("Password is incorrect.");
                return Result.error(ErrorCode.FORBIDDEN);
            }
            Clients.shortsClient.get().burnDownUser(userId, pwd);

            session.delete(existingUser.get(0));
            return Result.ok(existingUser.get(0));
        } catch (Exception e) {
            Log.info("Error deleting user: " + e.getMessage());
            return Result.error(ErrorCode.BAD_REQUEST);
        }
    }

    @Override
    public Result<List<User>> searchUsers(String pattern) {
        if (emptyStringToNull(pattern) == null) {
            Log.info("Pattern cannot be null or empty.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            String find = pattern.toLowerCase();
            List<User> existingUser = session.sql(
                    "SELECT * FROM USER u WHERE u.userId LIKE '%" + find + "%'", User.class);

            for (User user : existingUser) {
                user.setPwd("");
            }

            return Result.ok(existingUser);
        } catch (Exception e) {
            Log.info("Error searching users: " + e.getMessage());
            return Result.error(ErrorCode.BAD_REQUEST);
        }
    }

    public Result<String> userExists(String userId) {
        if (emptyStringToNull(userId) == null) {
            Log.info("User ID and password must not be null.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            List<User> existingUser = session.sql("SELECT * FROM USER u WHERE u.userId = '" + userId + "'", User.class);
            if (!existingUser.isEmpty()) {
                String out = existingUser.get(0).getUserId();
                return Result.ok(out);
            } else
                return Result.error(ErrorCode.NOT_FOUND);

        } catch (Exception e) {
            Log.info("Error searching users: " + e.getMessage());
            return Result.error(ErrorCode.BAD_REQUEST);
        }
    }

    private static String emptyStringToNull(String s) {
        return s == null || s.length() == 0 ? null : s;
    }

}
