package embedded.postgres

import groovy.util.logging.Slf4j
import io.zonky.test.db.postgres.embedded.EmbeddedPostgres
import org.springframework.beans.factory.DisposableBean

@Slf4j
class EmbeddedPostgresHolder implements DisposableBean {
    private EmbeddedPostgres db

    EmbeddedPostgresHolder(EmbeddedPostgres db) {
        this.db = db
    }

    def getPort(){
        db.port
    }

    @Override
    void destroy() throws Exception {
        log.warn "Stopping ${db.toString()}"
        db.close()
    }
}

