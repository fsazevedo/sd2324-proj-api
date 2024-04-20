package tukano.clients.rest;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.rest.RestBlobs;

import java.net.URI;


public class RestBlobsClient extends RestClient implements Blobs {

    private final WebTarget target;

    public RestBlobsClient(URI serverURI) {
        super(serverURI);
        this.target = client.target(serverURI).path(RestBlobs.PATH);
    }

    @Override
    public Result<Void> upload(String blobId, byte[] bytes) {

        return super.reTry(() -> clt_upload(blobId, bytes));
    }

    private Result<Void> clt_upload(String blobId, byte[] bytes) {
        return super.toJavaResult(target.path(blobId).request().post(Entity.entity(bytes, MediaType.APPLICATION_OCTET_STREAM)), Void.class);

    }

    @Override
    public Result<byte[]> download(String blobId) {
        return super.reTry(() -> clt_download(blobId));
    }

    private Result<byte[]> clt_download(String blobId) {
        return super.toJavaResult(target.path(blobId).request(MediaType.APPLICATION_OCTET_STREAM).get(), byte[].class);
    }

    @Override
    public Result<Void> deleteBlob(String blobId) {
        return super.reTry(() -> clt_deleteBlob(blobId));
    }

    private Result<Void> clt_deleteBlob(String blobId) {
        return super.toJavaResult(target.path(blobId).request().delete(), Void.class);
    }

    @Override
    public Result<Integer> getNumberBlobs() {
        return super.reTry(() -> clt_getNumberBlobs());
    }

    private Result<Integer> clt_getNumberBlobs() {
        return super.toJavaResult(target.request().get(), Integer.class);
    }

}
