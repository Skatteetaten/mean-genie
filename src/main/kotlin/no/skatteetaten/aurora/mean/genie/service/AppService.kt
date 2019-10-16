package no.skatteetaten.aurora.mean.genie.service

import no.skatteetaten.aurora.mean.genie.crd.ApplicationDeployment
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class AppService {
    private val apps = ConcurrentHashMap<String, ApplicationDeployment>()

    fun addApp(appName: String, app: ApplicationDeployment) {
        apps[appName] = app
    }
}