package tukano.api;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Liked {
    @Id
    private String shortId;

    @Id
    private String likedBy;

    public Liked() {
    }

    public Liked(String shortId, String likedBy) {
        this.shortId = shortId;
        this.likedBy = likedBy;
    }

	public String getShortId() {
		return shortId;
	}

}
