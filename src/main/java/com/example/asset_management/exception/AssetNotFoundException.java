package com.example.asset_management.exception;

public class AssetNotFoundException extends RuntimeException {
    public AssetNotFoundException(String message) {
        super(message);
    }

    public AssetNotFoundException(Long assetId) {
        super("Asset not found with ID: " + assetId);
    }

    public AssetNotFoundException(String assetTag, String type) {
        super("Asset not found with " + type + ": " + assetTag);
    }
}
