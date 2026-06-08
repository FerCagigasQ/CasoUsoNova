package com.bbva.gdpd.pedidos.repository;

import com.bbva.gdpd.pedidos.model.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByCodigoCliente(String codigoCliente);
    List<Pedido> findByEstado(Pedido.EstadoPedido estado);
}
