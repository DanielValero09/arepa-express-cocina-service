package co.edu.dosw.arepaexpress.cocina.infrastructure.adapter.in.messaging;

import co.edu.dosw.arepaexpress.cocina.domain.model.PedidoCreadoEvent;
import co.edu.dosw.arepaexpress.cocina.infrastructure.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class CocinaListener {

    private static final Logger log = LoggerFactory.getLogger(CocinaListener.class);

    @RabbitListener(queues = RabbitConfig.QUEUE)
    public void recibir(PedidoCreadoEvent evento) throws InterruptedException {
        log.info("Preparando pedido {} del cliente {}. Arepas: {}", evento.pedidoId(), evento.cliente(), evento.arepas());
        Thread.sleep(3000);
        log.info("Pedido {} listo", evento.pedidoId());
    }
}
