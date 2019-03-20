package io.paratek.rs.deob;

import io.paratek.rs.util.JarHandler;

import java.util.ArrayList;
import java.util.List;

public class TransformationMediator {

    private final List<Transformer> transformers = new ArrayList<>();
    private final JarHandler jarHandler;

    public TransformationMediator(final JarHandler jarHandler) {
        this.jarHandler = jarHandler;
    }

    public void submit(final Transformer transformer) {
        this.transformers.add(transformer);
    }

    public void run() {
        this.transformers.forEach(transformer -> transformer.run(this.jarHandler.getClassMap()));
    }

}
