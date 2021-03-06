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

import java.util.Objects;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.IModel;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

public class MtForm<T> extends Form<@NonNull T> {
    private static final long serialVersionUID = 0l;
    private @Nullable T oldModelObject = null;

    public MtForm(String id) {
        super(id);
    }

    public MtForm(String id, IModel<@NonNull T> model) {
        super(id, model);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();
        @Nullable T modelObject = getModelObject();

        if (!Objects.equals(oldModelObject, modelObject)) {
            clearInput();
            oldModelObject = modelObject;
        }
    }
}
