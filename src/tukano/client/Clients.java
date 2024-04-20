package tukano.client;

import tukano.api.java.Blobs;
import tukano.api.java.Shorts;
import tukano.api.java.Users;
import tukano.clients.rest.RestBlobsClient;
import tukano.clients.rest.RestShortsClient;
import tukano.clients.rest.RestUsersClient;

import java.net.URI;

public class Clients {

    public static final ClientFactory<Users> usersClient = new ClientFactory<>(Users.NAME, u -> new RestUsersClient(URI.create(u)), u -> new GrpcUsersClient(URI.create(u)));

    public static final ClientFactory<Shorts> shortsClient = new ClientFactory<>(Shorts.NAME, u -> new RestShortsClient(URI.create(u)), u -> new GrpcShortsClient(URI.create(u)));

    public static final ClientFactory<Blobs> blobsClient = new ClientFactory<>(Blobs.NAME, u -> new RestBlobsClient(URI.create(u)), u -> new GrpcBlobsClient(URI.create(u)));

}