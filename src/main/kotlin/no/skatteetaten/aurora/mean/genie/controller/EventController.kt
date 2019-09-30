package no.skatteetaten.aurora.mean.genie.controller

import no.skatteetaten.aurora.mean.genie.service.OpenShiftService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api")
class EventController(
    val openShiftService: OpenShiftService
) {

    @GetMapping("/applicationDeployments")
    fun listApplicationDeployments() = openShiftService.findApplicationDeployments()
}