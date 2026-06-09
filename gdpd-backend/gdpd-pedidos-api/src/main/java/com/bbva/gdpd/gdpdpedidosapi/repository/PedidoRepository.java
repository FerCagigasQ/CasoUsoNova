package com.bbva.gdpd.gdpdpedidosapi.repository;

import com.bbva.gdpd.gdpdpedidosapi.domain.EstadoPedido;
import com.bbva.gdpd.gdpdpedidosapi.domain.Pedido;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PedidoRepository extends JpaRepository<Pedido, Long> {
    List<Pedido> findByEstado(EstadoPedido estado);
    Optional<Pedido> findByReferencia(String referencia);
}
