package co.edu.dosw.arepaexpress.cocina.infrastructure.adapter.in.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import co.edu.dosw.arepaexpress.cocina.domain.model.PedidoCreadoEvent;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class CocinaListenerTest {

    private final CocinaListener listener = new CocinaListener();
    private final PedidoCreadoEvent evento = new PedidoCreadoEvent(
            "a1b2c3d4", "Daniel", List.of("Arepa de huevo"), "2026-11-05T12:00:00Z");

    @Test
    void preparaDuranteTresSegundosYRegistraElPedidoListo(CapturedOutput output) throws Exception {
        long inicio = System.nanoTime();

        listener.recibir(evento);

        long duracion = System.nanoTime() - inicio;
        assertThat(duracion).isGreaterThanOrEqualTo(3_000_000_000L);
        assertThat(output.getOut()).contains(
                "Preparando pedido a1b2c3d4 del cliente Daniel. Arepas: [Arepa de huevo]",
                "Pedido a1b2c3d4 listo");
        assertThat(output.getOut().indexOf("Preparando pedido"))
                .isLessThan(output.getOut().indexOf("Pedido a1b2c3d4 listo"));
    }

    @Test
    void noRegistraListoSiSeInterrumpeLaPreparacion(CapturedOutput output) {
        Thread.currentThread().interrupt();
        try {
            assertThatThrownBy(() -> listener.recibir(evento)).isInstanceOf(InterruptedException.class);
            assertThat(output.getOut()).contains("Preparando pedido a1b2c3d4");
            assertThat(output.getOut()).doesNotContain("Pedido a1b2c3d4 listo");
        } finally {
            Thread.interrupted();
        }
    }

}
