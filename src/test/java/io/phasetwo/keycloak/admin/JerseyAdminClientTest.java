package io.phasetwo.keycloak.admin;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.ws.rs.core.Response.Status;
import org.junit.jupiter.api.BeforeAll;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.protocol.oidc.OIDCLoginProtocol;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.testcontainers.Testcontainers;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

public class JerseyAdminClientTest {

  public static final String KEYCLOAK_IMAGE =
      String.format(
          "quay.io/keycloak/keycloak:%s", System.getProperty("keycloak-version", "26.2.4"));
  public static final String REALM = "master";
  public static final String ADMIN_CLI = "admin-cli";

  public static Keycloak keycloak;
  public static Client resteasyClient;
  
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
    resteasyClient =
        ClientBuilder.newBuilder()
        .readTimeout(60, TimeUnit.SECONDS)
        .connectTimeout(10, TimeUnit.SECONDS)
        .build();
    keycloak =
        getKeycloak(REALM, ADMIN_CLI, container.getAdminUsername(), container.getAdminPassword());
  }

  public static Keycloak getKeycloak(String realm, String clientId, String user, String pass) {
    return KeycloakBuilder.builder()
        .serverUrl(getAuthUrl())
        .realm(realm)
        .username(user)
        .password(pass)
        .clientId(clientId)
        .resteasyClient(resteasyClient)
        .build()
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
