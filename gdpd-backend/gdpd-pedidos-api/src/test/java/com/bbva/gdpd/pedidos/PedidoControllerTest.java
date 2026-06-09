package com.bbva.gdpd.pedidos;

import com.bbva.gdpd.pedidos.model.EstadoPedido;
import com.bbva.gdpd.pedidos.model.Pedido;
import com.bbva.gdpd.pedidos.service.PedidoService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = com.bbva.gdpd.pedidos.controller.PedidoController.class)
class PedidoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PedidoService pedidoService;

    @Test
    void getAll_returnsEmptyList() throws Exception {
        when(pedidoService.findAll()).thenReturn(List.of());
        mockMvc.perform(get("/api/pedidos"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getById_whenExists_returnsPedido() throws Exception {
        UUID id = UUID.randomUUID();
        Pedido pedido = new Pedido();
        pedido.setId(id);
        pedido.setReferencia("REF-001");
        pedido.setEstado(EstadoPedido.PENDIENTE);
        when(pedidoService.findById(id)).thenReturn(Optional.of(pedido));

        mockMvc.perform(get("/api/pedidos/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.referencia").value("REF-001"));
    }

    @Test
    void getById_whenNotFound_returns404() throws Exception {
        UUID id = UUID.randomUUID();
        when(pedidoService.findById(id)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/pedidos/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_returnsCreated() throws Exception {
        Pedido pedido = new Pedido();
        pedido.setReferencia("REF-002");
        pedido.setImporteTotal(BigDecimal.valueOf(100.00));
        pedido.setEstado(EstadoPedido.PENDIENTE);

        Pedido saved = new Pedido();
        saved.setId(UUID.randomUUID());
        saved.setReferencia("REF-002");
        saved.setImporteTotal(BigDecimal.valueOf(100.00));
        saved.setEstado(EstadoPedido.PENDIENTE);

        when(pedidoService.create(any())).thenReturn(saved);

        mockMvc.perform(post("/api/pedidos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pedido)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.referencia").value("REF-002"));
    }
}
