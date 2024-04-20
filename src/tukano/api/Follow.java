package tukano.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Follow {
    @Id
    private String followed;
    @Id
    private String follower;

    public Follow() {
    }

    public Follow(String followed, String follower) {
        this.followed = followed;
        this.follower = follower;
    }

    @Override
    public String toString() {
        return "Follow [follower=" + follower + ", followed=" + followed + "]";
    }

}

