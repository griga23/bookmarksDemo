package jans.BookmarksGateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.RouteToRequestUrlFilter;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.net.URISyntaxException;

@SpringBootApplication
public class BookmarksGatewayApplication {

    private CustomerFilter filter = new CustomerFilter();

    public static void main(String[] args) {
        SpringApplication.run(BookmarksGatewayApplication.class, args);
    }

    @Bean
    public RouteLocator myRoutes(RouteLocatorBuilder builder) {

        return builder.routes().route(p -> p.
                path("/getOneBookmark/**").
                filters(f -> f.filter(filter)).
                uri("no://op")).build();
    }
}

class CustomerFilter implements GatewayFilter {

    public String getRightHost(String key) {
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response =
                restTemplate.getForEntity(
                        "http://bookmarksConsumer:8888/keyHost/" + key,
                        String.class);
        return response.getBody();
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

        String queryParams = exchange.getRequest().getURI().getPath();
        String newUrl = getRightHost(queryParams.substring(queryParams.lastIndexOf("/")));

        URI mynewURI = null;
        try {
            mynewURI = new URI("http://" + newUrl + ":8888" + queryParams);
        } catch (URISyntaxException e) {
            System.out.println(e.toString());
        }
        exchange.getAttributes().put(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR, mynewURI);
        return chain.filter(exchange);
    }

}
