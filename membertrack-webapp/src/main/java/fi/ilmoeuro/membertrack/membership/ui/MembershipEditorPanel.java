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
package fi.ilmoeuro.membertrack.membership.ui;

import fi.ilmoeuro.membertrack.membership.Membership;
import fi.ilmoeuro.membertrack.membership.MembershipEditor;
import fi.ilmoeuro.membertrack.membership.MembershipBrowser;
import fi.ilmoeuro.membertrack.person.PhoneNumber;
import fi.ilmoeuro.membertrack.person.SecondaryEmail;
import fi.ilmoeuro.membertrack.service.PeriodTimeUnit;
import fi.ilmoeuro.membertrack.service.Subscription;
import fi.ilmoeuro.membertrack.service.SubscriptionPeriod;
import fi.ilmoeuro.membertrack.ui.MtButton;
import fi.ilmoeuro.membertrack.ui.MtForm;
import fi.ilmoeuro.membertrack.ui.MtLabel;
import fi.ilmoeuro.membertrack.ui.MtLink;
import fi.ilmoeuro.membertrack.ui.MtListView;
import fi.ilmoeuro.membertrack.ui.MtTextField;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponentLabel;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.resource.PackageResourceReference;

@Slf4j
public class MembershipEditorPanel extends Panel {
    private static final long serialVersionUID = 3l;

    private final IModel<MembershipEditor<?>> model;

    private final MtLink closeLink;
    private final FeedbackPanel feedbackPanel;

    private final MtForm<Membership> deleteForm; 
    private final MtButton confirmButton; 
    private final MtButton cancelButton; 
    private final MtLabel personNameLabel;

    private final MtForm<Membership> editorForm; 
    private final MtTextField<String> personFullNameField;
    private final FormComponentLabel personFullNameLabel;
    private final MtTextField<String> personEmailField;
    private final FormComponentLabel personEmailLabel;

    private final MtListView<PhoneNumber> numbersSection;
    private final MtButton addNumber;

    private final MtListView<SecondaryEmail> secondaryEmailsSection;
    private final MtButton addSecondaryEmail;

    private final MtListView<Subscription> subscriptionsSection;

    private final MtButton saveButton;
    private final MtButton deleteButton;

    // OK to register callbacks on methods, they're not called right away
    @SuppressWarnings("initialization")
    public MembershipEditorPanel(
        String id,
        IModel<MembershipEditor<?>> model
    ) {
        super(id, model);

        this.model = model;
        final IModel<Membership> membershipModel
            = new PropertyModel<>(model, "membership");

        feedbackPanel = new FeedbackPanel("feedbackPanel");
        closeLink = new MtLink("closeLink", this::close);

        deleteForm = new MtForm<>(
            "deleteForm",
            membershipModel);
        personNameLabel = new MtLabel(
            "person.fullName",
            membershipModel);
        confirmButton = new MtButton(
            "confirmButton",
            this::save);
        cancelButton = new MtButton(
            "cancelButton",
            this::unDelete);

        editorForm
            = new MtForm<>("editorForm", membershipModel);
        personFullNameField
            = new MtTextField<>("person.fullName", membershipModel);
        personFullNameField.setRequired(true);
        personFullNameLabel
            = new FormComponentLabel("person.fullName.label", personFullNameField);
        personEmailField
            = new MtTextField<>("person.email", membershipModel);
        personEmailField.setRequired(true);
        personEmailLabel
            = new FormComponentLabel("person.email.label", personEmailField);

        numbersSection = new MtListView<>(
            "phoneNumbers",
            membershipModel,
            (ListItem<PhoneNumber> item) -> {
                MtTextField<String> numberField
                    = new MtTextField<>("phoneNumber", item);
                numberField.setRequired(true);
                MtButton deleteNumber = new MtButton(
                    "deleteNumber",
                    () -> deletePhoneNumber(item),
                    false);
                if (item.getModelObject().isDeleted()) {
                    item.setVisible(false);
                }
                item.add(numberField);
                item.add(deleteNumber);
            });
        addNumber = new MtButton("addNumber", this::newPhoneNumber, false);

        secondaryEmailsSection = new MtListView<>(
            "secondaryEmails",
            membershipModel,
            (ListItem<SecondaryEmail> item) -> {
                MtTextField<String> emailField
                    = new MtTextField<>("email", item);
                emailField.setRequired(true);
                MtButton deleteSecondaryEmail = new MtButton(
                    "deleteSecondaryEmail",
                    () -> deleteSecondaryEmail(item),
                    false);
                if (item.getModelObject().isDeleted()) {
                    item.setVisible(false);
                }
                item.add(emailField);
                item.add(deleteSecondaryEmail);
            });
        addSecondaryEmail = new MtButton("addSecondaryEmail", this::newSecondaryEmail, false);

        subscriptionsSection = new MtListView<>(
            "subscriptions",
            membershipModel,
            (ListItem<Subscription> subItem) -> {
                MtLabel legend = new MtLabel("service.title", subItem);
                MtListView<SubscriptionPeriod> periodsSection = new MtListView<>(
                    "periods",
                    subItem,
                    (ListItem<SubscriptionPeriod> prdItem) -> {
                        if (prdItem.getModelObject().isDeleted()) {
                            prdItem.setVisible(false);
                        }
                        MtTextField<LocalDate> startDateField =
                            new MtTextField<>("startDate", prdItem);
                        MtTextField<Integer> lengthField =
                            new MtTextField<>("length", prdItem);
                        MtTextField<Double> paymentField =
                            new MtTextField<>("paymentFormatted", prdItem);
                        DropDownChoice<PeriodTimeUnit> lengthUnitField =
                            new DropDownChoice<PeriodTimeUnit>(
                                "lengthUnit",
                                new PropertyModel<PeriodTimeUnit>(
                                    prdItem.getModel(),
                                    "lengthUnit"),
                                new PropertyModel<List<PeriodTimeUnit>>(
                                    prdItem.getModel(),
                                    "possibleLengthUnits"));
                        CheckBox approvedField = new CheckBox(
                                "approved",
                                new PropertyModel<>(
                                    prdItem.getModel(),
                                    "approved"));

                        MtButton deletePeriod =
                            new MtButton(
                                "deletePeriod", 
                                () -> deleteSubscriptionPeriod(prdItem),
                                false);

                        startDateField.setRequired(true);
                        lengthField.setRequired(true);
                        paymentField.setRequired(true);
                        lengthUnitField.setRequired(true);
                        approvedField.setRequired(true);
                        prdItem.add(startDateField);
                        prdItem.add(lengthField);
                        prdItem.add(paymentField);
                        prdItem.add(lengthUnitField);
                        prdItem.add(approvedField);
                        prdItem.add(deletePeriod);
                    });
                MtButton addPeriod = new MtButton(
                    "addPeriod",
                    () -> newSubscriptionPeriod(subItem),
                    false);
                subItem.add(legend);
                subItem.add(periodsSection);
                subItem.add(addPeriod);
            });

        saveButton = new MtButton("saveButton", this::save);
        deleteButton = new MtButton("deleteButton", this::delete, false);
    }

    @Override
    protected void onInitialize() {
        super.onInitialize();

        deleteForm.add(personNameLabel);
        deleteForm.add(confirmButton);
        deleteForm.add(cancelButton);

        editorForm.add(personFullNameField);
        editorForm.add(personFullNameLabel);
        editorForm.add(personEmailField);
        editorForm.add(personEmailLabel);
        editorForm.add(numbersSection);
        editorForm.add(addNumber);
        editorForm.add(secondaryEmailsSection);
        editorForm.add(addSecondaryEmail);
        editorForm.add(subscriptionsSection);
        editorForm.add(saveButton);
        editorForm.setDefaultButton(saveButton);
        editorForm.add(deleteButton);

        add(deleteForm);
        add(editorForm);
        add(closeLink);
        add(feedbackPanel);
    }

    @Override
    protected void onConfigure() {
        super.onConfigure();

        setVisible(
            model.getObject().getMembership() != null
        );
        editorForm.setVisible(
            !model.getObject().isCurrentDeleted()
        );
        deleteForm.setVisible(
            model.getObject().isCurrentDeleted()
        );
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        PackageResourceReference cssRef = 
            new PackageResourceReference(
                MembershipEditorPanel.class,
                "MembershipEditorPanel.css");
        CssHeaderItem pageCss = CssHeaderItem.forReference(cssRef);
        response.render(pageCss);
    }

    private void save() {
        try {
            model.getObject().saveCurrent();
        } catch (MembershipBrowser.NonUniqueEmailException ex) {
            error("Email is already in use");
        }
    }

    private void close() {
        model.getObject().close();
    }

    private void delete() {
        model.getObject().deleteCurrent();
    }

    private void unDelete() {
        model.getObject().unDeleteCurrent();
    }

    private void newPhoneNumber() {
        model.getObject().addPhoneNumber();
    }

    private void newSecondaryEmail() {
        model.getObject().addSecondaryEmail();
    }

    private void newSubscriptionPeriod(ListItem<Subscription> li) {
        li.getModelObject().addPeriod();
    }

    private void deleteSubscriptionPeriod(ListItem<SubscriptionPeriod> li) {
        li.getModelObject().delete();
    }

    private void deletePhoneNumber(ListItem<PhoneNumber> li) {
        li.getModelObject().delete();
    }

    private void deleteSecondaryEmail(ListItem<SecondaryEmail> li) {
        li.getModelObject().delete();
    }
}
