package io.github.swampus.alexandra.networkapi.weight;

import io.github.swampus.alexandra.contract.model.ModelWithMeta;
import io.github.swampus.alexandra.networkapi.weight.application.usecase.InitWeightsUseCase;
import io.github.swampus.alexandra.networkapi.weight.application.usecase.impl.InitWeightsUseCaseImpl;
import io.github.swampus.alexandra.networkapi.weight.domain.init.InitMode;
import io.github.swampus.alexandra.networkapi.weight.infrastructure.codec.BinaryWeightsCodec;
import io.github.swampus.alexandra.networkapi.weight.infrastructure.service.BasicWeightsValidator;
import io.github.swampus.alexandra.networkapi.weight.infrastructure.spec.NetworkModelShapeSpecProvider;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class CodecAndInitSmokeTest {

    @Test
    void binaryCodec_roundTrip() {
        var codec = new BinaryWeightsCodec();
        var w = new io.github.swampus.alexandra.networkapi.weight.domain.model.Weights(
                Map.of("a", new double[]{1.0, 2.0}, "b", new double[]{})
        );
        byte[] payload = codec.encode(w);
        var decoded = codec.decode(payload);
        assertArrayEquals(new double[]{1.0, 2.0}, decoded.flat().get("a"));
        assertArrayEquals(new double[]{}, decoded.flat().get("b"));
    }

    @Test
    void init_isDeterministicWithSeedAndKey() {
        ModelWithMeta m1 = new TestModel();
        ModelWithMeta m2 = new TestModel();

        Map<String, int[]> spec = new LinkedHashMap<>();
        spec.put("W1", new int[]{2, 3});
        spec.put("B1", new int[]{2});
        m1.meta().put(NetworkModelShapeSpecProvider.META_SHAPE_SPEC, spec);
        m2.meta().put(NetworkModelShapeSpecProvider.META_SHAPE_SPEC, spec);

        InitWeightsUseCase uc = new InitWeightsUseCaseImpl(new BasicWeightsValidator(), new NetworkModelShapeSpecProvider());
        uc.init(m1, new InitWeightsUseCase.InitWeightsCommand(InitMode.XAVIER_UNIFORM, 42L, "v1", null));
        uc.init(m2, new InitWeightsUseCase.InitWeightsCommand(InitMode.XAVIER_UNIFORM, 42L, "v1", null));

        var w1 = (io.github.swampus.alexandra.networkapi.weight.domain.model.Weights) m1.meta().get("weights.current");
        var w2 = (io.github.swampus.alexandra.networkapi.weight.domain.model.Weights) m2.meta().get("weights.current");

        assertNotNull(w1);
        assertNotNull(w2);
        assertArrayEquals(w1.flat().get("W1"), w2.flat().get("W1"));
        assertArrayEquals(w1.flat().get("B1"), w2.flat().get("B1"));
    }
}
