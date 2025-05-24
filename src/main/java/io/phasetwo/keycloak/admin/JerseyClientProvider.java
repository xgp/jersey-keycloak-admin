package io.phasetwo.keycloak.admin;

import com.google.auto.service.AutoService;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import org.glassfish.jersey.client.proxy.WebResourceFactory;
import org.keycloak.admin.client.JacksonProvider;
import org.keycloak.admin.client.spi.ResteasyClientProvider;

@AutoService(ResteasyClientProvider.class)
public class JerseyClientProvider implements ResteasyClientProvider {

  @Override
  public Client newRestEasyClient(
      Object customJacksonProvider, SSLContext sslContext, boolean disableTrustManager) {
    ClientBuilder clientBuilder =
        ClientBuilder.newBuilder()
            .readTimeout(60, TimeUnit.SECONDS)
            .connectTimeout(10, TimeUnit.SECONDS);
    clientBuilder.sslContext(sslContext);

    if (disableTrustManager) {
      clientBuilder.trustStore(null);
    }

    if (customJacksonProvider != null) {
      clientBuilder.register(customJacksonProvider, 100);
    } else {
      clientBuilder.register(JacksonProvider.class, 100);
    }

    return clientBuilder.build();
  }

  @Override
  public <R> R targetProxy(WebTarget client, Class<R> targetClass) {
    return WebResourceFactory.newResource(targetClass, client);
  }
}
