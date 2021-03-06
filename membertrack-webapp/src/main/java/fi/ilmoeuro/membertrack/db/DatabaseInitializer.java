/* 
 * Copyright (C) 2015 Ilmo Euro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package fi.ilmoeuro.membertrack.db;

import fi.ilmoeuro.membertrack.ResourceRoot;
import fi.ilmoeuro.membertrack.session.SessionToken;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.Charsets;
import org.apache.commons.io.IOUtils;
import org.jooq.DSLContext;
import org.jooq.exception.DataAccessException;

@Slf4j
@RequiredArgsConstructor
public final class DatabaseInitializer<SessionTokenType> {

    public static final @Data class Config implements Serializable {
        private static final long serialVersionUID = 0l;
        private String setupList = "";
        private String clearList = "";
        private boolean enabled = false;
        private boolean useExampleData = false;
    }

    private final Config config;
    private final ExampleData<DSLContext> exampleData;

    private void runSqlFiles(DSLContext jooq, List<String> fileNames) {
        try {
            for (String fileName : fileNames) {
                InputStream sqlStream
                        = ResourceRoot.class.getResourceAsStream(fileName);
                if (sqlStream == null) {
                    // TODO proper exception
                    throw new RuntimeException(
                        String.format(
                            "SQL file '%s'  not found",
                            fileName
                    ));
                }
                String sql = IOUtils.toString(sqlStream, Charsets.UTF_8);
                log.info("Executing: {}", sql);
                for (String part : sql.split(";")) {
                    jooq.execute(part);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Error executing sql files", e);
        }
    }

    public void init(SessionToken<DSLContext> sessionToken) {
        if (config.isEnabled()) {
            try (final InputStream clearStream =
                    ResourceRoot.class.getResourceAsStream(
                        config.getClearList()
            )) {
                if (clearStream != null) {
                    final List<String> clearFiles =
                        IOUtils.readLines(clearStream, Charsets.UTF_8);
                    runSqlFiles(sessionToken.getValue(), clearFiles);
                }
            } catch (DataAccessException ex) {
                log.info("Exception while clearing db", ex);
            } catch (IOException ex) {
                throw new RuntimeException("Error loading clear list", ex);
            }

            try (final InputStream setupStream =
                    ResourceRoot.class.getResourceAsStream(
                        config.getSetupList()
            )) {
                if (setupStream != null) {
                    final List<String> setupFiles =
                        IOUtils.readLines(setupStream, Charsets.UTF_8);
                    runSqlFiles(sessionToken.getValue(), setupFiles);
                }
            } catch (IOException ex) {
                throw new RuntimeException("Error loading setup list", ex);
            }

            if (config.isUseExampleData()) {
                exampleData.populate(sessionToken);
            }
        }
    }
}
