package io.github.swampus.alexandra.networkapi.weight.application.usecase;

import io.github.swampus.alexandra.contract.model.ModelWithMeta;

import java.util.List;

/** Lists stored weight versions for the given model. */
public interface ListWeightsVersionsUseCase {

    List<String> list(ModelWithMeta model);
}
