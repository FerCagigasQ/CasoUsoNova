package com.gdpd.pedidos.api.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

import com.gdpd.pedidos.api.client.model.*;

@Service
public class GdpdPedidosApiClient {
    private final WebClient webClient;
    private final String apiBasePath;

    public GdpdPedidosApiClient(WebClient.Builder webClientBuilder,
                                @Value("${gdpd.api.base-url:http://localhost:24000/gdpd/pedidos-api/1.0.0}") String apiBaseUrl) {
        this.apiBasePath = apiBaseUrl;
        this.webClient = webClientBuilder
                .baseUrl(apiBaseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    public Mono<PedidoDTO> crearPedido(CrearPedidoRequest request) {
        return webClient.post()
                .uri("/api/v1/pedidos")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(PedidoDTO.class);
    }

    public Mono<PedidoDTO> obtenerPedido(Long id) {
        return webClient.get()
                .uri("/api/v1/pedidos/{id}", id)
                .retrieve()
                .bodyToMono(PedidoDTO.class);
    }

    public Mono<PedidoDTO> actualizarPedido(Long id, String observaciones) {
        return webClient.put()
                .uri("/api/v1/pedidos/{id}", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new java.util.HashMap<String, String>() {{
                    put("observaciones", observaciones);
                }})
                .retrieve()
                .bodyToMono(PedidoDTO.class);
    }

    public Mono<Void> eliminarPedido(Long id) {
        return webClient.delete()
                .uri("/api/v1/pedidos/{id}", id)
                .retrieve()
                .bodyToMono(Void.class);
    }

    public Mono<PedidoDTO> cambiarEstado(Long id, EstadoPedido estado) {
        return webClient.patch()
                .uri("/api/v1/pedidos/{id}/estado", id)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new java.util.HashMap<String, Object>() {{
                    put("estado", estado);
                }})
                .retrieve()
                .bodyToMono(PedidoDTO.class);
    }

    public Mono<LineaPedidoDTO> anadirLinea(Long pedidoId, CrearLineaRequest request) {
        return webClient.post()
                .uri("/api/v1/pedidos/{id}/lineas", pedidoId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .retrieve()
                .bodyToMono(LineaPedidoDTO.class);
    }

    public String getApiBasePath() {
        return apiBasePath;
    }
}
