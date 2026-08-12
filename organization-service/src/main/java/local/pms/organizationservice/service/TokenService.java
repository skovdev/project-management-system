package local.pms.organizationservice.service;

public interface TokenService {
    void setToken(String token);
    String getToken();
    void clear();
}
