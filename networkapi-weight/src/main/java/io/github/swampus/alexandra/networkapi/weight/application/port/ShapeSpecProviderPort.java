package io.github.swampus.alexandra.networkapi.weight.application.port;

import java.util.Map;

/** Provides parameter shapes for a given model. */
public interface ShapeSpecProviderPort<T> {
    Map<String, int[]> paramShapes(T model);
}
