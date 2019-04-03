import grails.util.Holders
import nl.thehyve.gb.backend.NotificationsMailService

// Place your Spring DSL code here
beans = {
    notificationsMailService(NotificationsMailService) {
        clientApplicationName = Holders.config.getProperty(
                'nl.thehyve.gb.backend.notifications.clientApplicationName', String)
        maxNumberOfSets = Holders.config.getProperty(
                'nl.thehyve.gb.backend.notifications.maxNumberOfSets', Integer)
    }
}
