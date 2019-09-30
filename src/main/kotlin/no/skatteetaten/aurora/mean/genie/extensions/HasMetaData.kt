package no.skatteetaten.aurora.mean.genie.extensions

import io.fabric8.kubernetes.api.model.HasMetadata

const val AFFILIATION_LABEL = "affiliation"

fun HasMetadata.affiliation(): String {
    return this.metadata.labels[AFFILIATION_LABEL] ?: throw IllegalStateException("affiliation is not set or invalid")
}
