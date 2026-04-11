package local.pms.apigateway.exception;

public class AwsSecretsRetrievalException extends RuntimeException {

    public AwsSecretsRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
