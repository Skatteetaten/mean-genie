package no.skatteetaten.aurora.mean.genie.crd

import io.fabric8.kubernetes.api.builder.Function
import io.fabric8.kubernetes.client.CustomResourceDoneable

class ApplicationDeploymentDoneable(
    resource: ApplicationDeployment,
    function: Function<ApplicationDeployment, ApplicationDeployment>
) :
    CustomResourceDoneable<ApplicationDeployment>(resource, function)