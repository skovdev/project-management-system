package local.pms.apigateway.dto;

public record ErrorResponse(int status, String error, String message) {}
