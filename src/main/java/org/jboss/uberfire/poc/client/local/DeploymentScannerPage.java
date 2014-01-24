/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @author tags. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.jboss.uberfire.poc.client.local;

import java.util.Map;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.ballroom.client.widgets.forms.Form;
import org.jboss.ballroom.client.widgets.forms.FormCallback;
import org.jboss.ballroom.client.widgets.forms.TextItem;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;
import org.jboss.uberfire.poc.client.ballroom.ConsoleBeanFactory;
import org.jboss.uberfire.poc.client.ballroom.ConsoleFramework;
import org.jboss.uberfire.poc.client.ballroom.DeploymentScanner;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 * @author Stan Silvert ssilvert@redhat.com (C) 2014 Red Hat Inc.
 */
@Templated
@Page
public class DeploymentScannerPage extends Composite {

    @DataField
    private Widget scannerDetail;
    private Form<DeploymentScanner> scannerForm = new Form(DeploymentScanner.class);

    @Inject
    private ConsoleFramework consoleFramework;

    public DeploymentScannerPage() {
        TextItem nameItem = new TextItem("name", "Name");
        scannerForm.setToolsCallback(new FormCallback<DeploymentScanner>() {

          @Override
          public void onSave(Map<String, Object> changeset) {
            Window.alert("Ready to save: " + changeset);
          }

          @Override
          public void onCancel(DeploymentScanner entity) {
            Window.alert("Canceled edit on: " + entity);
          }
        });
        scannerForm.setFields(nameItem);
        scannerDetail = scannerForm.asWidget();
    }

    @PostConstruct
    private void init() {
        ConsoleBeanFactory factory = consoleFramework.getBeanFactory();
        DeploymentScanner scanner = factory.deploymentScanner().as();
        scanner.setName("foo");
        scannerForm.edit(scanner);
        scannerForm.setEnabled(false);
    }
}
