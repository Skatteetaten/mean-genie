package no.skatteetaten.aurora.mean.genie.extensions

import io.fabric8.kubernetes.api.model.HasMetadata
import io.fabric8.kubernetes.api.model.ObjectMeta

const val AFFILIATION_LABEL = "affiliation"
const val MEAN_GENIE_INSTANCE_ANNOTATION = "mean-genie.skatteetaten.no/instance"

fun HasMetadata.affiliation(): String {
    return this.metadata.labels[AFFILIATION_LABEL] ?: throw IllegalStateException("affiliation is not set or invalid")
}

fun ObjectMeta.meanGenieInstance(): String? {
    return this.annotations[MEAN_GENIE_INSTANCE_ANNOTATION] ?: "Not set"
}
