package local.pms.notificationservice.service;

/**
 * Stores the raw JWT bearer token for the current request thread.
 */
public interface TokenService {

    /**
     * Stores the raw JWT bearer token for the current request.
     *
     * @param token the bearer token string
     */
    void setToken(String token);

    /**
     * Returns the raw JWT bearer token stored for the current request.
     *
     * @return the bearer token, or {@code null} if not yet set
     */
    String getToken();
}
