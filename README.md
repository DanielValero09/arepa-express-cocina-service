# Arepa Express - Cocina Service

Microservicio encargado de preparar los pedidos recibidos mediante RabbitMQ.

## Tecnologias
- Java 21
- Spring Boot
- Maven
- Spring AMQP
- Arquitectura hexagonal

## Funcionamiento
1. Escucha la cola durable `cocina.pedidos`.
2. Recibe eventos desde el exchange `arepas.eventos`.
3. Utiliza el binding `pedido.*`.
4. Registra el inicio de la preparacion del pedido.
5. Simula 3 segundos de preparacion.
6. Registra que el pedido esta listo.

## Variables de entorno
- `RABBIT_HOST`: servidor RabbitMQ.
- `RABBIT_USER`: usuario de RabbitMQ.
- `RABBIT_PASS`: contrasena de RabbitMQ.

## Ejecucion
Requiere Java 21, Maven y una instancia de RabbitMQ configurada.

```bash
mvn spring-boot:run
```

## Despliegue
Desplegado en Azure Container Apps como consumidor interno.
