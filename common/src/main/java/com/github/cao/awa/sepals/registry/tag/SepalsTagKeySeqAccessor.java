package com.github.cao.awa.sepals.registry.tag;

public interface SepalsTagKeySeqAccessor {
    default int getSeq() {
        return sepals$getSeq();
    }

    int sepals$getSeq();
}
