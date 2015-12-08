/*
 * Copyright (C) 2015 Ilmo Euro <ilmo.euro@gmail.com>
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
package fi.ilmoeuro.membertrack.ui;

import fi.ilmoeuro.membertrack.config.ConfigProvider;
import fi.ilmoeuro.membertrack.config.TypesafeConfigProvider;
import fi.ilmoeuro.membertrack.db.DataSourceInitializer;
import fi.ilmoeuro.membertrack.db.DatabaseInitializer;
import fi.ilmoeuro.membertrack.db.exampledata.DefaultExampleData;
import fi.ilmoeuro.membertrack.membership.ui.MembershipsPage;
import fi.ilmoeuro.membertrack.session.SessionRunner;
import fi.ilmoeuro.membertrack.session.UnitOfWorkFactory;
import fi.ilmoeuro.membertrack.session.db.DbSessionRunner;
import fi.ilmoeuro.membertrack.session.db.DbUnitOfWorkFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.jooq.DSLContext;

@Slf4j
public final class MembertrackApplication extends WebApplication {

    @Getter
    private final ConfigProvider configProvider;
    @Getter
    private final SessionRunner<DSLContext> sessionRunner;

    private final UnitOfWorkFactory<DSLContext> uowFactory;
    private final DataSourceInitializer dsInitializer;
    private final DatabaseInitializer dbInitializer;

    public MembertrackApplication() {
        configProvider
            = new TypesafeConfigProvider();
        sessionRunner
            = new DbSessionRunner(configProvider);
        uowFactory
            = new DbUnitOfWorkFactory();
        dsInitializer
            = new DataSourceInitializer(configProvider);
        dbInitializer
            = new DatabaseInitializer(configProvider,
                new DefaultExampleData<>(uowFactory));
    }

    @Override
    public void init() {
        dsInitializer.init();
        sessionRunner.run(dbInitializer::init);
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return MembershipsPage.class;
    }

    public static MembertrackApplication get() {
        try {
            return (MembertrackApplication) Application.get();
        } catch (ClassCastException ex) {
            String error = "Called get() from wrong app";
            log.error(error, ex);
            throw new RuntimeException(error, ex);
        }
    }
}
