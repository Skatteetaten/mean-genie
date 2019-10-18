package no.skatteetaten.aurora.mean.genie

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import io.fabric8.kubernetes.api.model.WatchEvent

@JsonDeserialize
@JsonIgnoreProperties(ignoreUnknown = true)
class ApplicationDeploymanetEventWatcher : WatchEvent()