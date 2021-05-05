package embedded.postgres

import grails.config.Config
import grails.plugins.Plugin
import groovy.util.logging.Slf4j
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.springframework.util.SocketUtils

@Slf4j
class EmbeddedPostgresGrailsPlugin extends Plugin {
    def grailsVersion   = "3.3.10 > *"
    def title           = "Embedded Postgres"
    def author          = "Alexey Chentsov"
    def authorEmail     = "alche8411@gmail.com"
    def description     = 'Plugin starts local instance of postgres and after program termination clears temporary data. Simulates embedded postgres.'
    def documentation   = "https://github.com/Relaximus/embedded-postgres-grails-plugin"
    def license         = "APACHE"
    def organization    = [name: "Alexey Chentsov", url: "https://github.com/Relaximus"]
    def issueManagement = [ system: "GITHUB", url: "https://github.com/Relaximus/embedded-postgres-grails-plugin/issues" ]
    def scm             = [ url: "https://github.com/Relaximus/embedded-postgres-grails-plugin" ]
    def pluginExcludes  = []
    def developers      = [ [name: 'Alexey Chentsov'] ]
    def dependsOn = [dataSource: grailsVersion]
    def loadBefore = ['dataSource', 'database-migration']
    def scopes = [excludes:'war']

    Closure doWithSpring() { {->
        def config = grailsApplication.config
        if (config.dataSource.embeddedPostgres) {
            def dataSourceName = "dataSource"
            def ep = startEmbeddedPostgres(config.dataSource, dataSourceName)
            embeddedPostgres(EmbeddedPostgresHolder, ep)
        }
        for (def entry: config.dataSources) {
            def dataSourceName = "dataSource_${entry.key}"
            def embeddedName = "embeddedPostgres_${entry.key}"
            if (entry.value.embeddedPostgres) {
                def ep = startEmbeddedPostgres(entry.value, dataSourceName)
                "$embeddedName"(EmbeddedPostgresHolder, ep)
            }
        }
    } }

    private static def startEmbeddedPostgres(sourceConfig, dataSourceName){

        if (!sourceConfig.embeddedPort) {
            sourceConfig.embeddedPort = SocketUtils.findAvailableTcpPort()
            log.debug("Embedded Postgres will use DEFAULT port: {}", sourceConfig.embeddedPort)
        }

        log.info("Embedded Postgres plugin is starting under ${dataSourceName} bean on ${sourceConfig.embeddedPort} port...")

        if (!sourceConfig.url) {
            sourceConfig.url="jdbc:postgresql://localhost:${sourceConfig.embeddedPort}/postgres?autoReconnect=true&characterEncoding=UTF-8"
            log.debug("Embedded Postgres will use DEFAULT url: {}", sourceConfig.url)
        }

        if (!sourceConfig.username) {
            sourceConfig.username='postgres'
        }

        if (!sourceConfig.password) {
            sourceConfig.password='postgres'
        }

        def builder = EmbeddedPostgres.builder()
        if (sourceConfig.embeddedPort) {
            builder.setPort(sourceConfig.embeddedPort as int)
        }

        if (sourceConfig?.locale?.config) {
            builder.setLocaleConfig('locale', sourceConfig?.locale?.config as String)
        } else {
            builder.setLocaleConfig('locale', 'de_DE')
        }

        builder.setCleanDataDirectory(true)

        builder.start()
    }
}
