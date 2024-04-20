package tukano.servers.java;

import tukano.api.java.Blobs;
import tukano.api.java.Result;
import tukano.api.java.Result.ErrorCode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.logging.Logger;

public class JavaBlobs implements Blobs {

    private static final Logger Log = Logger.getLogger(JavaBlobs.class.getName());
    private static final Path blobStoragePath = Paths.get(System.getProperty("java.io.tmpdir"), "blobs");

    static {
        try {
            Files.createDirectories(blobStoragePath);
        } catch (IOException e) {
            Log.severe("Failed to create blob storage directory: " + e.getMessage());
        }
    }

    public Result<Void> upload(String blobId, byte[] bytes) {
        if (emptyStringToNull(blobId) == null || bytes == null || bytes.length == 0) {
            Log.info("Blob ID and bytes cannot be null or empty.");
            return Result.error(ErrorCode.FORBIDDEN);
        }

        try {
            Path blobPath = blobStoragePath.resolve(blobId);

            if (Files.exists(blobPath)) {
                byte[] existingBytes = Files.readAllBytes(blobPath);
                if (!Arrays.equals(existingBytes, bytes)) {
                    Log.info("Conflict in blob content for blobId: " + blobId);
                    return Result.error(ErrorCode.CONFLICT);
                }
            } else {
                Files.write(blobPath, bytes);
                Log.info("Blob uploaded successfully: " + blobId);
            }

            return Result.ok();
        } catch (Exception e) {
            Log.severe("An error occurred while uploading the blob: " + e.getMessage());
            return Result.error(ErrorCode.BAD_REQUEST);
        }
    }

    @Override
    public Result<byte[]> download(String blobId) {
        if (emptyStringToNull(blobId) == null) {
            Log.info("Blob ID cannot be null or empty.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            Path blobPath = blobStoragePath.resolve(blobId);
            if (Files.exists(blobPath)) {
                byte[] blobBytes = Files.readAllBytes(blobPath);
                Log.info("Blob downloaded successfully: " + blobId);
                return Result.ok(blobBytes);
            } else {
                Log.info("No blob found with ID: " + blobId);
                return Result.error(ErrorCode.NOT_FOUND);
            }
        } catch (IOException e) {
            Log.severe("Failed to read blob from disk: " + e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Void> deleteBlob(String blobId) {
        if (blobId == null || blobId.isEmpty()) {
            Log.info("Blob ID cannot be null or empty.");
            return Result.error(ErrorCode.BAD_REQUEST);
        }

        try {
            Path blobPath = blobStoragePath.resolve(blobId);
            if (Files.exists(blobPath)) {
                Files.delete(blobPath);
                Log.info("Blob deleted successfully: " + blobId);
                return Result.ok();
            } else {
                Log.info("No blob found with ID: " + blobId);
                return Result.error(ErrorCode.NOT_FOUND);
            }
        } catch (IOException e) {
            Log.severe("Failed to delete blob: " + e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Override
    public Result<Integer> getNumberBlobs() {
        int count = 0;
        try {
            count = (int) Files.list(blobStoragePath).filter(Files::isRegularFile).count();
            return Result.ok(count);
        } catch (IOException e) {
            Log.severe("Failed to delete blob: " + e.getMessage());
            return Result.error(ErrorCode.INTERNAL_ERROR);
        }
    }

    private static String emptyStringToNull(String s) {
        return s == null || s.length() == 0 ? null : s;
    }
}
