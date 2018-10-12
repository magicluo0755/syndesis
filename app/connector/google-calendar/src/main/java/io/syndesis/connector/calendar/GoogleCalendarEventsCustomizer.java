/*
 * Copyright (C) 2016 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.syndesis.connector.calendar;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.AddressException;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.util.ObjectHelper;
import org.apache.commons.lang3.StringUtils;

import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventAttendee;

import io.syndesis.integration.component.proxy.ComponentProxyComponent;
import io.syndesis.integration.component.proxy.ComponentProxyCustomizer;

public class GoogleCalendarEventsCustomizer implements ComponentProxyCustomizer {

    @Override
    public void customize(ComponentProxyComponent component, Map<String, Object> options) {
        component.setBeforeConsumer(this::beforeConsumer);
    }

    private void beforeConsumer(Exchange exchange) throws MessagingException, IOException {

        final Message in = exchange.getIn();
        final Event event = exchange.getIn().getBody(Event.class);
        GoogleCalendarEventModel model = new GoogleCalendarEventModel();
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        if (event != null) {

            if (ObjectHelper.isNotEmpty(event.getSummary())) {
                model.setTitle(event.getSummary());
            }
            if (ObjectHelper.isNotEmpty(event.getDescription())) {
                model.setDescription(event.getDescription());
            }
            if (ObjectHelper.isNotEmpty(event.getAttendees())) {
                model.setAttendees(getAttendeesString(event.getAttendees()));
            }
            if (ObjectHelper.isNotEmpty(event.getStart())) {
                model.setStartDate(dateFormat.format(new Date(event.getStart().getDateTime().getValue())));
                model.setStartTime(timeFormat.format(new Date(event.getStart().getDateTime().getValue())));
            }
            if (ObjectHelper.isNotEmpty(event.getEnd())) {
                model.setEndDate(dateFormat.format(new Date(event.getEnd().getDateTime().getValue())));
                model.setEndTime(timeFormat.format(new Date(event.getEnd().getDateTime().getValue())));
            }
            if (ObjectHelper.isNotEmpty(event.getLocation())) {
                model.setLocation(event.getLocation());
            }
            if (ObjectHelper.isNotEmpty(event.getId())) {
                model.setEventId(event.getId());
            }
        }

        in.setBody(model);
    }

    private String getAttendeesString(List<EventAttendee> attendees) throws AddressException {
        String attendeesString;
        List<String> attendeesList = new ArrayList<>();
        for (EventAttendee eventAttendee : attendees) {
            attendeesList.add(eventAttendee.getEmail());
        }
        attendeesString = StringUtils.join(attendeesList, ',');
        return attendeesString;
    }
}