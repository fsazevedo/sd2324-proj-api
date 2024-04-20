package tukano.client;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import tukano.api.java.Result;
import tukano.discovery.Discovery;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class ClientFactory<T> {
    private final String serviceName;

    private final Function<String, T> restClientFunction;

    private final Function<String, T> grpcClientFunction;

    public ClientFactory(String serviceName, Function<String, T> restClientFunction, Function<String, T> grpcClientFunction) {
        this.serviceName = serviceName;
        this.restClientFunction = restClientFunction;
        this.grpcClientFunction = grpcClientFunction;
    }

    LoadingCache<URI, T> clients = CacheBuilder.newBuilder().build(new CacheLoader<URI, T>() {
        @Override
        public T load(URI key) throws RuntimeException {
            String serverURI = key.toString();
            if (serverURI.endsWith("/rest")) {
                return restClientFunction.apply(serverURI);
            } else if (serverURI.endsWith("/grpc")) {
                return grpcClientFunction.apply(serverURI);
            } else {
                throw new RuntimeException("No such service type " + serverURI);
            }
        }
    });

    public T get() {
        return get(Discovery.getInstance().knownUrisOf(serviceName, 1)[0]);
    }

    public T get(URI uri) {
        try {
            return clients.get(uri);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(Result.ErrorCode.INTERNAL_ERROR.toString());
        }
    }

    public List<URI> getAllClients() {
        return Arrays.stream(Discovery.getInstance().knownUrisOf(serviceName, 1)).toList();
    }
}