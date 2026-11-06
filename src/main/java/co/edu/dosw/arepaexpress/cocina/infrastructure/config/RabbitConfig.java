package co.edu.dosw.arepaexpress.cocina.infrastructure.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.support.converter.Jackson2JavaTypeMapper;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class RabbitConfig {

    public static final String EXCHANGE = "arepas.eventos";
    public static final String QUEUE = "cocina.pedidos";
    public static final String ROUTING_KEY = "pedido.*";

    @Bean
    public TopicExchange eventosExchange() {
        return new TopicExchange(EXCHANGE, true, false);
    }

    @Bean
    public Queue pedidosQueue() {
        return new Queue(QUEUE, true, false, false);
    }

    @Bean
    public Binding pedidosBinding(Queue pedidosQueue, TopicExchange eventosExchange) {
        return BindingBuilder.bind(pedidosQueue).to(eventosExchange).with(ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter(ObjectMapper objectMapper) {
        var converter = new Jackson2JsonMessageConverter(objectMapper);
        // El record del consumidor tiene un paquete distinto al __TypeId__ del productor.
        converter.setTypePrecedence(Jackson2JavaTypeMapper.TypePrecedence.INFERRED);
        return converter;
    }
}
