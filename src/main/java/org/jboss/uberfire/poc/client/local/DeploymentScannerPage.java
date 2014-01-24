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
import org.jboss.ballroom.client.widgets.forms.FormCallback;
import org.jboss.ballroom.client.widgets.forms.CheckBoxItem;
import org.jboss.ballroom.client.widgets.forms.Form;
import org.jboss.ballroom.client.widgets.forms.NumberBoxItem;
import org.jboss.ballroom.client.widgets.forms.TextItem;
import org.jboss.dmr.client.ModelNode;
import org.jboss.uberfire.poc.client.local.ballroom.ConsoleBeanFactory;
import org.jboss.uberfire.poc.client.local.ballroom.ConsoleFramework;
import org.jboss.uberfire.poc.client.local.ballroom.DeploymentScanner;
import org.jboss.errai.ui.nav.client.local.Page;
import org.jboss.errai.ui.shared.api.annotations.DataField;
import org.jboss.errai.ui.shared.api.annotations.Templated;

import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.uberfire.poc.client.local.dmr.DMRCallback;
import org.jboss.uberfire.poc.client.local.dmr.DMRRequest;

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
    private DeploymentScanner scanner;

    public DeploymentScannerPage() {
        TextItem name = new TextItem("name", "Name");
        CheckBoxItem autoDepExploded = new CheckBoxItem("auto-deploy-exploded", "Auto-deploy Exploded");
        CheckBoxItem autoDepXML = new CheckBoxItem("auto-deploy-xml", "Auto-deploy XML");
        CheckBoxItem autoDepZip = new CheckBoxItem("auto-deploy-zipped", "Auto-deploy Zipped");
        NumberBoxItem deployTimeout = new NumberBoxItem("deployment-timeout", "Deployment Timeout");
        TextItem path = new TextItem("deployments", "Deployments");
        TextItem relativeTo = new TextItem("relative-to", "Relative-to");
        CheckBoxItem scanEnabled = new CheckBoxItem("scan-enabled", "Scan Enabled");
        NumberBoxItem scanInterval = new NumberBoxItem("scan-interval", "Scan Interval");

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

        scannerForm.setFields(name, autoDepExploded, autoDepXML, autoDepZip, deployTimeout, path, relativeTo, scanEnabled, scanInterval);
        scannerDetail = scannerForm.asWidget();
    }



    @PostConstruct
    private void init() {
        ConsoleBeanFactory factory = consoleFramework.getBeanFactory();
        scanner = factory.deploymentScanner().as();
        scannerForm.edit(scanner);
        scannerForm.setEnabled(false);
        fillScannerData();
    }

    private void fillScannerData() {
        DMRRequest.sendRequest(scannerRequest(), new DMRCallback() {
            @Override
            public void dmrResponse(ModelNode responseNode) {
                System.out.println("***** Got response *******");
                System.out.println(responseNode.toString());
                System.out.println("**************************");

                scanner.setName("default");

                ModelNode result = responseNode.get("result");
                scanner.setAutoDeployExploded(result.get("auto-deploy-exploded").asBoolean());
                scanner.setAutoDeployXML(result.get("auto-deploy-xml").asBoolean());
                scanner.setAutoDeployZipped(result.get("auto-deploy-zipped").asBoolean());
                scanner.setDeploymentTimeout(result.get("deployment-timeout").asLong());
                scanner.setPath(result.get("path").asString());
                scanner.setRelativeTo(result.get("relative-to").asString());
                scanner.setScanInterval(result.get("scan-interval").asInt());
                scanner.setEnabled(result.get("scan-enabled").asBoolean());
            }
        });
    }

    private ModelNode scannerRequest() {
        ModelNode request = new ModelNode();
        ModelNode address = new ModelNode();
        address.add("subsystem", "deployment-scanner");
        address.add("scanner", "default");
        request.get("address").set(address);
        request.get("operation").set("read-resource");
        request.get("attributes-only").set(true);
        return request;
    }
}
