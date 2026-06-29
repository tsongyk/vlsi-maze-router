import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.assertTimeout;

public class RoutingTest {

    // Single net on a small grid.
    @Test
    public void testWire0() {
        assertTimeout(Duration.ofMillis(1000), () -> {
            Utilities.test("./test/inputs/wire0.in");
        });
    }

    // A net whose source and destination are the same vertex.
    @Test
    public void testSameSourceAndDestination() {
        assertTimeout(Duration.ofMillis(1000), () -> {
            Utilities.test("./test/inputs/wire_same_endpoint.in");
        });
    }

    // Nets must route around rectangular obstacle regions (marked -1).
    @Test
    public void testWithObstacles() {
        assertTimeout(Duration.ofMillis(1000), () -> {
            Utilities.test("./test/inputs/wire_obstacle.in");
        });
    }

    // Multiple nets on one grid with no overlapping paths.
    @Test
    public void testMultipleWires() {
        assertTimeout(Duration.ofMillis(1000), () -> {
            Utilities.test("./test/inputs/wire_multiple.in");
        });
    }

    // Five nets, no obstacles: exercises shortest-first net ordering.
    @Test
    public void testWire3() {
        assertTimeout(Duration.ofMillis(1000), () -> {
            Utilities.test("./test/inputs/wire3.in");
        });
    }

    // Five nets with one obstacle: exercises rip-up-and-reroute.
    @Test
    public void testWire5() {
        assertTimeout(Duration.ofMillis(1000), () -> {
            Utilities.test("./test/inputs/wire5.in");
        });
    }

}
