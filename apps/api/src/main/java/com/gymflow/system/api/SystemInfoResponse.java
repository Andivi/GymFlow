package com.gymflow.system.api;

public record SystemInfoResponse(
    String name,
    String version,
    String apiVersion
) {}
