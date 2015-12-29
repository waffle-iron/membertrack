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
package fi.ilmoeuro.membertrack.db.exampledata;

import fi.ilmoeuro.membertrack.db.ExampleData;
import fi.ilmoeuro.membertrack.person.Account;
import fi.ilmoeuro.membertrack.person.Person;
import fi.ilmoeuro.membertrack.person.PhoneNumber;
import fi.ilmoeuro.membertrack.service.PeriodTimeUnit;
import fi.ilmoeuro.membertrack.service.Service;
import fi.ilmoeuro.membertrack.service.SubscriptionPeriod;
import fi.ilmoeuro.membertrack.session.SessionToken;
import fi.ilmoeuro.membertrack.session.UnitOfWork;
import fi.ilmoeuro.membertrack.session.UnitOfWorkFactory;
import java.time.LocalDate;
import java.time.Month;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public final class
    DefaultExampleData<SessionTokenType>
implements
    ExampleData<SessionTokenType>
{
    private final UnitOfWorkFactory<SessionTokenType> uwf;
    
    @Override
    public void populate(SessionToken<SessionTokenType> session) {
        UnitOfWork uw = uwf.create(session);

        Service s = new Service("Tilankäyttö", "Tilankäyttömaksut");
        uw.addEntity(s);

        Person admin = new Person("Mr. Admin", "admin@example.com");
        Account adminAccount = Account.create(admin, "admin");

        uw.addEntity(admin);
        uw.addEntity(adminAccount);

        uw.execute();
        for (int i = 0; i < 100; i++) {
            Person p = new Person(
                "John Doe " + i,
                "john.doe." + i + "@example.com");
            PhoneNumber pn = new PhoneNumber(p.getId(), "+1234567890");
            SubscriptionPeriod pr1 = new SubscriptionPeriod(
                s.getId(),
                p.getId(),
                LocalDate.of(2000, Month.MARCH, 1),
                PeriodTimeUnit.DAY,
                30,
                2000,
                true
            );
            SubscriptionPeriod pr2 = new SubscriptionPeriod(
                s.getId(),
                p.getId(),
                LocalDate.of(2000, Month.APRIL, 1),
                PeriodTimeUnit.DAY,
                30,
                2000,
                true
            );

            uw = uwf.create(session);
            uw.addEntity(p);
            uw.addEntity(pn);
            uw.addEntity(pr1);
            uw.addEntity(pr2);
            uw.execute();
        }
    }
}
