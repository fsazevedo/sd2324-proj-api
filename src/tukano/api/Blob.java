package tukano.api;

public class Blob {

    private String blobId;
    private byte[] bytes;

    public Blob() {
    }

    public Blob(String blobId, byte[] bytes) {
        this.blobId = blobId;
        this.bytes = bytes;
    }

}