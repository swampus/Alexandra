package io.github.swampus.alexandra.compiler.mainflow;

import io.github.swampus.alexandra.compiler.extensions.GraphContainsCyclesException;
import io.github.swampus.alexandra.compiler.model.NetworkModel;
import io.github.swampus.alexandra.compiler.model.layer.Layer;
import io.github.swampus.alexandra.compiler.validator.impl.CycleValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class DummyLayer extends Layer {
    private final List<Layer> outputs = new ArrayList<>();
    private final String name;
    public DummyLayer(String name) {
        super(name);
        this.name = name; }
    @Override public String getName() { return name; }
    @Override public List<Layer> getOutputs() { return outputs; }

    @Override
    public int getSize() {
        return 0;
    }

    @Override
    public String getActivation() {
        return null;
    }

    @Override
    public Object getShape() {
        return null;
    }

    public void addOutput(Layer l) { outputs.add(l); }
}

public class CycleValidatorTest {

    @Test
    void testNoCycle() {
        // A -> B -> C
        DummyLayer a = new DummyLayer("A");
        DummyLayer b = new DummyLayer("B");
        DummyLayer c = new DummyLayer("C");
        a.addOutput(b);
        b.addOutput(c);

        NetworkModel model = new NetworkModel();
        model.addLayer(a);
        model.addLayer(b);
        model.addLayer(c);

        CycleValidator validator = new CycleValidator();
        Assertions.assertDoesNotThrow(() -> validator.validate(model));
    }

    @Test
    void testWithCycle() {
        // X -> Y -> Z -> X (цикл!)
        DummyLayer x = new DummyLayer("X");
        DummyLayer y = new DummyLayer("Y");
        DummyLayer z = new DummyLayer("Z");
        x.addOutput(y);
        y.addOutput(z);
        z.addOutput(x); // цикл

        NetworkModel model = new NetworkModel();
        model.addLayer(x);
        model.addLayer(y);
        model.addLayer(z);

        CycleValidator validator = new CycleValidator();
        Assertions.assertThrows(GraphContainsCyclesException.class, () -> validator.validate(model));
    }

    @Test
    void testLargeNetworkNoCycle() {
        // A -> B -> C -> D
        // A -> E -> F
        // B -> F
        // F -> G -> H
        DummyLayer a = new DummyLayer("A");
        DummyLayer b = new DummyLayer("B");
        DummyLayer c = new DummyLayer("C");
        DummyLayer d = new DummyLayer("D");
        DummyLayer e = new DummyLayer("E");
        DummyLayer f = new DummyLayer("F");
        DummyLayer g = new DummyLayer("G");
        DummyLayer h = new DummyLayer("H");

        a.addOutput(b); a.addOutput(e);
        b.addOutput(c); b.addOutput(f);
        c.addOutput(d);
        e.addOutput(f);
        f.addOutput(g);
        g.addOutput(h);

        NetworkModel model = new NetworkModel();
        model.addLayer(a); model.addLayer(b); model.addLayer(c); model.addLayer(d);
        model.addLayer(e); model.addLayer(f); model.addLayer(g); model.addLayer(h);

        CycleValidator validator = new CycleValidator();
        Assertions.assertDoesNotThrow(() -> validator.validate(model));
    }

    @Test
    void testLargeNetworkWithDeepCycle() {
        // A -> B -> C -> D
        // A -> E -> F
        // B -> F
        // F -> G -> H -> I -> J
        // J -> F   // <<< цикл тут! J возвращает на F

        DummyLayer a = new DummyLayer("A");
        DummyLayer b = new DummyLayer("B");
        DummyLayer c = new DummyLayer("C");
        DummyLayer d = new DummyLayer("D");
        DummyLayer e = new DummyLayer("E");
        DummyLayer f = new DummyLayer("F");
        DummyLayer g = new DummyLayer("G");
        DummyLayer h = new DummyLayer("H");
        DummyLayer i = new DummyLayer("I");
        DummyLayer j = new DummyLayer("J");

        a.addOutput(b); a.addOutput(e);
        b.addOutput(c); b.addOutput(f);
        c.addOutput(d);
        e.addOutput(f);
        f.addOutput(g);
        g.addOutput(h);
        h.addOutput(i);
        i.addOutput(j);
        j.addOutput(f); // <<< цикл J -> F (а F уже был по пути)

        NetworkModel model = new NetworkModel();
        model.addLayer(a); model.addLayer(b); model.addLayer(c); model.addLayer(d);
        model.addLayer(e); model.addLayer(f); model.addLayer(g); model.addLayer(h);
        model.addLayer(i); model.addLayer(j);

        CycleValidator validator = new CycleValidator();
        Assertions.assertThrows(GraphContainsCyclesException.class, () -> validator.validate(model));
    }
}
