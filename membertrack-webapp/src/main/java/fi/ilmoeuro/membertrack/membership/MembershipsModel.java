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
package fi.ilmoeuro.membertrack.membership;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import fi.ilmoeuro.membertrack.session.SessionToken;
import fi.ilmoeuro.membertrack.session.SessionJoinable;
import java.io.IOException;
import java.io.ObjectInputStream;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.nullness.qual.Nullable;

@RequiredArgsConstructor
public final class
    MembershipsModel<SessionTokenType>
implements
    Serializable,
    SessionJoinable<SessionTokenType>
{
    private static final long serialVersionUID = 0l;
        
    private final MembershipRepositoryFactory<SessionTokenType> mrf;

    @Getter
    private transient List<Membership> memberships = Collections.emptyList();

    @Getter
    private transient int numPages = 0;

    @Getter
    @Setter
    private int pageNumber = 0;

    @Getter
    @Setter
    private @Nullable Membership currentMembership = null;

    @Override
    public void join(SessionToken<SessionTokenType> token) {
        MembershipRepository repo = mrf.create(token);
        memberships = repo.listMembershipsPage(pageNumber);
        numPages = repo.numMembershipsPages();
    }

    private void readObject(ObjectInputStream is)
        throws IOException, ClassNotFoundException
    {
        is.defaultReadObject();
        memberships = Collections.emptyList();
        numPages = 0;
        currentMembership = null;
    }
}