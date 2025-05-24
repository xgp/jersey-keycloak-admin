package io.phasetwo.keycloak.admin;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;

public class JerseyAdminClientTest {

  public static final String KEYCLOAK_IMAGE =
      String.format(
          "quay.io/keycloak/keycloak:%s", System.getProperty("keycloak-version", "26.2.4"));
  public static final String REALM = "master";
  public static final String ADMIN_CLI = "admin-cli";

  public static Keycloak keycloak;

  public static final KeycloakContainer container =
      new KeycloakContainer(KEYCLOAK_IMAGE)
          .withContextPath("/auth")
          .withReuse(true)
          .withAccessToHost(true);

  static {
    container.start();
  }

  @BeforeAll
  public static void beforeAll() {
    keycloak =
        getKeycloak(REALM, ADMIN_CLI, container.getAdminUsername(), container.getAdminPassword());
  }

  public static Keycloak getKeycloak(String realm, String clientId, String user, String pass) {
    Client jerseyClient =
        ClientBuilder.newBuilder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS)
            .build();
    System.err.println(jerseyClient.getClass().getName());
    return KeycloakBuilder.builder()
        .serverUrl(getAuthUrl())
        .realm(realm)
        .username(user)
        .password(pass)
        .clientId(clientId)
        .resteasyClient(jerseyClient)
        .build();
  }

  public static String getAuthUrl() {
    return container.getAuthServerUrl();
  }

  @Test
  void testClient() throws Exception {
    UserRepresentation admin =
        keycloak.realm(REALM).users().search(container.getAdminUsername()).get(0);
    assertNotNull(admin);
  }
}
