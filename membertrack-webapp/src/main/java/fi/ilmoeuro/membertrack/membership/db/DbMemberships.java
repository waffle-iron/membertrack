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
package fi.ilmoeuro.membertrack.membership.db;

import com.relatejava.relate.RelationMapper_3__1;
import fi.ilmoeuro.membertrack.person.Person;
import fi.ilmoeuro.membertrack.person.PhoneNumber;
import org.jooq.DSLContext;
import org.jooq.Record;
import static fi.ilmoeuro.membertrack.schema.Tables.*;
import fi.ilmoeuro.membertrack.schema.tables.records.PersonRecord;
import fi.ilmoeuro.membertrack.schema.tables.records.PhoneNumberRecord;
import fi.ilmoeuro.membertrack.schema.tables.records.ServiceRecord;
import fi.ilmoeuro.membertrack.schema.tables.records.SubscriptionPeriodRecord;
import fi.ilmoeuro.membertrack.service.Service;
import fi.ilmoeuro.membertrack.service.SubscriptionPeriod;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.Map;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jooq.Condition;
import org.jooq.Cursor;
import org.jooq.SelectField;
import org.jooq.impl.DSL;
import fi.ilmoeuro.membertrack.membership.Membership;
import fi.ilmoeuro.membertrack.service.Subscription;
import lombok.RequiredArgsConstructor;
import fi.ilmoeuro.membertrack.membership.Memberships;
import fi.ilmoeuro.membertrack.person.SecondaryEmail;
import fi.ilmoeuro.membertrack.schema.tables.records.SecondaryEmailRecord;
import fi.ilmoeuro.membertrack.session.SessionToken;

@RequiredArgsConstructor
public final class
    DbMemberships
implements
    Memberships
{
    public static final class Factory implements Memberships.Factory<DSLContext> {
        private static final long serialVersionUID = 0L;

        @Override
        public Memberships create(SessionToken<DSLContext> token) {
            return new DbMemberships(token.getValue());
        }
    }

    private final DSLContext jooq;
    private static final int PAGE_SIZE = 5;

    @Override
    public int numMembershipsPages() {
        return (int) Math.ceil(jooq.select(DSL.count())
            .from(PERSON)
            .fetchAnyInto(Integer.class)
            / (double) PAGE_SIZE);
    }

    @Override
    public List<Membership> listMembershipsPage(int pageNum) {
        @Nullable String start = jooq
            .select(PERSON.FULL_NAME)
            .from(PERSON)
            .orderBy(PERSON.FULL_NAME)
            .limit((pageNum - 1)* PAGE_SIZE, 1)
            .fetchAny(this::getFullName);

        @Nullable String end = jooq
            .select(PERSON.FULL_NAME)
            .from(PERSON)
            .orderBy(PERSON.FULL_NAME)
            .limit(pageNum * PAGE_SIZE, 1)
            .fetchAny(this::getFullName);

        if (start != null) {
            if (end != null) {
                return listByConditions(
                    PERSON.FULL_NAME.ge(start),
                    PERSON.FULL_NAME.lt(end)
                );
            } else {
                return listByConditions(
                    PERSON.FULL_NAME.ge(start)
                );
            }
        }

        return Collections.emptyList();
    }

    @Override
    public List<Membership> listAllMemberships() {
        return listByConditions();
    }

    private List<Membership> listByConditions(Condition... conditions) {
        List<SelectField<?>> fields = new ArrayList<>();
        fields.addAll(Arrays.asList(PERSON.fields()));
        fields.addAll(Arrays.asList(PHONE_NUMBER.fields()));
        fields.addAll(Arrays.asList(SECONDARY_EMAIL.fields()));
        fields.addAll(Arrays.asList(SERVICE.fields()));
        fields.addAll(Arrays.asList(SUBSCRIPTION_PERIOD.fields()));
        RelationMapper_3__1<
            Person,
            Service,
            PhoneNumber,
            SecondaryEmail,
            SubscriptionPeriod>
            mapper = new RelationMapper_3__1<>();
        try (Cursor<? extends Record> records = jooq
            .selectDistinct(fields)
            .from(PERSON)
            .leftOuterJoin(PHONE_NUMBER)
            .on(PHONE_NUMBER.PERSON_ID.eq(PERSON.ID))
            .leftOuterJoin(SECONDARY_EMAIL)
            .on(SECONDARY_EMAIL.PERSON_ID.eq(PERSON.ID))
            .crossJoin(SERVICE)
            .leftOuterJoin(SUBSCRIPTION_PERIOD)
            .on(SUBSCRIPTION_PERIOD.PERSON_ID.eq(PERSON.ID),
                SUBSCRIPTION_PERIOD.SERVICE_ID.eq(SERVICE.ID))
            .where(conditions)
            .orderBy(
                PERSON.FULL_NAME.asc(), 
                PERSON.EMAIL.asc(), 
                PHONE_NUMBER.PHONE_NUMBER_.asc(),
                SECONDARY_EMAIL.EMAIL.asc(),
                SERVICE.TITLE.asc(), 
                SUBSCRIPTION_PERIOD.START_DATE.desc())
            .fetchLazy()) {
            for (Record r : records) {
                PersonRecord pr = r.into(PERSON);
                PhoneNumberRecord pnr = r.into(PHONE_NUMBER);
                SecondaryEmailRecord ser = r.into(SECONDARY_EMAIL);
                ServiceRecord sr = r.into(SERVICE);
                SubscriptionPeriodRecord spr = r.into(SUBSCRIPTION_PERIOD);
                if (pr.getId() != null) {
                    Person p = pr.into(Person.class);
                    mapper.root(p);
                    if (sr.getId() != null) {
                        Service s = sr.into(Service.class);
                        mapper.relate_1(p, s);
                        if (spr.getId() != null) {
                            SubscriptionPeriod sp = spr.into(SubscriptionPeriod.class);
                            mapper.relate_1_1(p, s, sp);
                        }
                    }
                    if (pnr.getId() != null) {
                        PhoneNumber pn = pnr.into(PhoneNumber.class);
                        mapper.relate_2(p, pn);
                    }
                    if (ser.getId() != null) {
                        SecondaryEmail se = ser.into(SecondaryEmail.class);
                        mapper.relate_3(p, se);
                    }
                }
            }
        }
        return mapper.<Membership>build(this::buildMembership);
    }

    private Membership buildMembership(
        Person person,
        Set<PhoneNumber> phoneNumbers,
        Set<SecondaryEmail> secondaryEmails,
        Map<Service, Set<SubscriptionPeriod>> periods
    ) {
        List<Subscription> subscriptions = new ArrayList<>();
        for (
            Map.Entry<Service, Set<SubscriptionPeriod>>
                entry : periods.entrySet()
        ) {
            subscriptions.add(
                new Subscription(
                    person,
                    entry.getKey(),
                    new ArrayList<>(entry.getValue())));
        }
        return new Membership(
            person,
            new ArrayList<>(phoneNumbers),
            new ArrayList<>(secondaryEmails),
            subscriptions);
    }

    private String getFullName(Record r) {
        return r.getValue(PERSON.FULL_NAME);
    }
}
