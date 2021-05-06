/*
 * Fakturama - Free Invoicing Software - http://www.fakturama.org
 * 
 * Copyright (C) 2021 www.fakturama.org
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: The Fakturama Team - initial API and implementation
 */

package org.fakturama.connectors.mail;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/**
 * Container class for Mail Settings
 */
public class MailSettings {
    private String user, password, host, templateText, subject, body, sender;
    private final List<String> receiversTo = new ArrayList<>();
    private final List<String> receiversCC = new ArrayList<>();
    private final List<String> receiversBCC = new ArrayList<>();
    private List<String> additionalDocs = new ArrayList<>();

    public MailSettings withUser(String user) {
        this.user = user;
        return this;
    }

    public MailSettings withPassword(String pasword) {
        this.password = pasword;
        return this;
    }

    public MailSettings withSender(String sender) {
        this.sender = sender;
        return this;
    }

    public MailSettings withAdditionalDocs(String... additionalDocs) {
        this.additionalDocs = Arrays.stream(additionalDocs).collect(Collectors.toList());
        return this;
    }

    public MailSettings withTemplateText(String templateText) {
        this.templateText = templateText;
        return this;
    }

    public MailSettings withReceiversTo(String... receiversTo) {
        this.receiversTo.addAll(Arrays.asList(receiversTo));
        return this;
    }

    public MailSettings withReceiversCC(String... receiversCC) {
        this.receiversCC.addAll(Arrays.asList(receiversCC));
        return this;
    }

    public MailSettings withReceiversBCC(String... receiversBCC) {
        this.receiversBCC.addAll(Arrays.asList(receiversBCC));
        return this;
    }

    public MailSettings withHost(String host) {
        this.host = host;
        return this;
    }

    public MailSettings withSubject(String subject) {
        this.subject = subject;
        return this;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String pasword) {
        this.password = pasword;
    }

    public List<String> getAdditionalDocs() {
        return additionalDocs;
    }

    public void addToAdditionalDocs(String... additionalDocs) {
        this.additionalDocs.addAll(Arrays.stream(additionalDocs).collect(Collectors.toList()));
    }

    public void setAdditionalDocs(List<String> additionalDocs) {
        this.additionalDocs = additionalDocs;
    }

    public String getTemplateText() {
        return templateText;
    }

    public void setTemplateText(String templateText) {
        this.templateText = templateText;
    }

    public String getReceiversTo() {
        return StringUtils.join(receiversTo, ';');
    }

    public void setReceiversTo(String receivers) {
        if (receivers != null) {
            receiversTo.clear();
            Arrays.stream(receivers.split(";")).forEach(r -> receiversTo.add(StringUtils.trim(r)));
        }
    }

    public String getReceiversCC() {
        return StringUtils.join(receiversCC, ';');
    }

    public String getReceiversBCC() {
        return StringUtils.join(receiversBCC, ';');
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getHost() {
        return host;
    }

    public String getSender() {
        return sender;
    }

    public void removeFromAdditionalDocs(String additionalDoc) {
        this.additionalDocs.remove(additionalDoc);
    }

}
