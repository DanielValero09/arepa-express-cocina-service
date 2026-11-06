package co.edu.dosw.arepaexpress.cocina.infrastructure.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import co.edu.dosw.arepaexpress.cocina.domain.model.PedidoCreadoEvent;
import co.edu.dosw.arepaexpress.cocina.infrastructure.adapter.in.messaging.CocinaListener;
import com.rabbitmq.client.Channel;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerEndpointRegistry;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.HandlerAdapter;
import org.springframework.amqp.rabbit.listener.adapter.MessagingMessageListenerAdapter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.messaging.handler.annotation.support.DefaultMessageHandlerMethodFactory;

@SpringBootTest(properties = {
        "spring.rabbitmq.dynamic=false",
        "spring.rabbitmq.listener.simple.auto-startup=false"
})
class RabbitConfigTest {

    @Autowired
    private TopicExchange exchange;
    @Autowired
    private Queue queue;
    @Autowired
    private Binding binding;
    @Autowired
    private Jackson2JsonMessageConverter converter;
    @Autowired
    private RabbitTemplate template;
    @Autowired
    private RabbitListenerEndpointRegistry registry;

    @Test
    void declaraExchangeColaDurableYBindingEsperados() {
        assertThat(exchange.getName()).isEqualTo("arepas.eventos");
        assertThat(exchange.getType()).isEqualTo("topic");
        assertThat(exchange.isDurable()).isTrue();
        assertThat(exchange.isAutoDelete()).isFalse();
        assertThat(queue.getName()).isEqualTo("cocina.pedidos");
        assertThat(queue.isDurable()).isTrue();
        assertThat(queue.isExclusive()).isFalse();
        assertThat(queue.isAutoDelete()).isFalse();
        assertThat(binding.getExchange()).isEqualTo("arepas.eventos");
        assertThat(binding.getDestination()).isEqualTo("cocina.pedidos");
        assertThat(binding.getDestinationType()).isEqualTo(Binding.DestinationType.QUEUE);
        assertThat(binding.getRoutingKey()).isEqualTo("pedido.*");
    }

    @Test
    void springUsaElConversorJsonYRegistraElListenerEnSuPropiaCola() {
        assertThat(template.getMessageConverter()).isSameAs(converter);
        assertThat(registry.getListenerContainers()).hasSize(1);
        var container = (SimpleMessageListenerContainer) registry.getListenerContainers().iterator().next();
        assertThat(container.getQueueNames()).containsExactly("cocina.pedidos");
        assertThat(container.isRunning()).isFalse();
        assertThat(container.getMessageListener()).isInstanceOf(MessagingMessageListenerAdapter.class);
    }

    @ParameterizedTest
    @ValueSource(booleans = {true, false})
    void infiereElRecordLocalConHeaderDelProductorOSinHeader(boolean conHeader) throws Exception {
        var listener = mock(CocinaListener.class);
        var method = CocinaListener.class.getMethod("recibir", PedidoCreadoEvent.class);
        var factory = new DefaultMessageHandlerMethodFactory();
        factory.afterPropertiesSet();
        var adapter = new MessagingMessageListenerAdapter(listener, method);
        adapter.setMessageConverter(converter);
        adapter.setHandlerAdapter(new HandlerAdapter(factory.createInvocableHandlerMethod(listener, method)));
        var properties = new MessageProperties();
        properties.setContentType(MessageProperties.CONTENT_TYPE_JSON);
        if (conHeader) {
            // Esta clase del productor no existe en el classpath del consumidor.
            properties.setHeader("__TypeId__", "co.edu.dosw.arepaexpress.pedidos.domain.model.PedidoCreadoEvent");
        }
        var message = new Message("""
                {"pedidoId":"a1b2c3d4","cliente":"Daniel","arepas":["Arepa de huevo"],"fecha":"2026-11-05T12:00:00Z"}
                """.getBytes(StandardCharsets.UTF_8), properties);

        adapter.onMessage(message, mock(Channel.class));

        verify(listener).recibir(new PedidoCreadoEvent(
                "a1b2c3d4", "Daniel", List.of("Arepa de huevo"), "2026-11-05T12:00:00Z"));
    }
}
