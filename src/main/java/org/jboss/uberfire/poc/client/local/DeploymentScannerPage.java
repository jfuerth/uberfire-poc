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

import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Widget;
import org.jboss.ballroom.client.widgets.forms.TextBoxItem;
import org.jboss.dmr.client.ModelDescriptionConstants;
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
    private TextItem name = new TextItem("name", "Name");
    private CheckBoxItem autoDepExploded = new CheckBoxItem("autoDeployExploded", "Auto-deploy Exploded");
    private CheckBoxItem autoDepXML = new CheckBoxItem("autoDeployXML", "Auto-deploy XML");
    private CheckBoxItem autoDepZip = new CheckBoxItem("autoDeployZipped", "Auto-deploy Zipped");
    private NumberBoxItem deployTimeout = new NumberBoxItem("deploymentTimeout", "Deployment Timeout");
    private TextBoxItem path = new TextBoxItem("path", "Deployments");
    private TextBoxItem relativeTo = new TextBoxItem("relativeTo", "Relative-to");
    private CheckBoxItem scanEnabled = new CheckBoxItem("enabled", "Scan Enabled");
    private NumberBoxItem scanInterval = new NumberBoxItem("scanInterval", "Scan Interval");

    public DeploymentScannerPage() {
        scannerForm.setToolsCallback(new FormCallback<DeploymentScanner>() {
            @Override
            public void onSave(Map<String, Object> changeset) {
                saveItemValuesToAutoBean();
                //Window.alert("Ready to save: " + changeset);
                ModelNode writeAllOp = writeAllAttributes();
                DMRRequest.sendRequest(writeAllOp, new DMRCallback() {
                    @Override
                    public void dmrResponse(ModelNode responseNode) {
                        System.out.println("****** Saved scanner *********");
                        System.out.println(responseNode.toString());
                        System.out.println("******************************");
                        scannerForm.edit(scanner);
                    }
                });
            }

            @Override
            public void onCancel(DeploymentScanner entity) {
                //Window.alert("Canceled edit on: " + entity);
            }
        });

        scannerForm.setFields(name, autoDepExploded, autoDepXML, autoDepZip, deployTimeout, path, relativeTo, scanEnabled, scanInterval);
        scannerDetail = scannerForm.asWidget();
    }

    private void saveItemValuesToAutoBean() {
        scanner.setAutoDeployExploded(autoDepExploded.getValue());
        scanner.setAutoDeployXML(autoDepXML.getValue());
        scanner.setAutoDeployZipped(autoDepZip.getValue());
        scanner.setDeploymentTimeout(deployTimeout.getValue().longValue());
        scanner.setPath(path.getValue());
        scanner.setRelativeTo(relativeTo.getValue());
        scanner.setEnabled(scanEnabled.getValue());
        scanner.setScanInterval(scanInterval.getValue().intValue());
    }

    @PostConstruct
    private void init() {
        ConsoleBeanFactory factory = consoleFramework.getBeanFactory();
        scanner = factory.deploymentScanner().as();
        scannerForm.setEnabled(false);
        fillScannerData();
    }

    private void fillScannerData() {
        DMRRequest.sendRequest(defaultScanner(), new DMRCallback() {
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
                scannerForm.edit(scanner);
            }
        });
    }

    private ModelNode defaultScanner() {
        ModelNode request = new ModelNode();
        request.get(ModelDescriptionConstants.OP_ADDR).set(scannerAddress("default"));
        request.get(ModelDescriptionConstants.OP).set("read-resource");
        request.get("attributes-only").set(true);
        return request;
    }

    private ModelNode writeAllAttributes() {
        System.out.println("writeAllAttributes()");
        try {
            ModelNode address = scannerAddress(scanner.getName());
            ModelNode steps = new ModelNode();

            ModelNode writeAttrOp = writeAttributeOp(address);
            writeAttrOp.get(ModelDescriptionConstants.NAME).set("auto-deploy-exploded");
            writeAttrOp.get("value").set(scanner.isAutoDeployExploded());
            steps.add(writeAttrOp);

            writeAttrOp = writeAttributeOp(address);
            writeAttrOp.get(ModelDescriptionConstants.NAME).set("auto-deploy-xml");
            writeAttrOp.get("value").set(scanner.isAutoDeployXML());
            steps.add(writeAttrOp);

            writeAttrOp = writeAttributeOp(address);
            writeAttrOp.get(ModelDescriptionConstants.NAME).set("auto-deploy-zipped");
            writeAttrOp.get("value").set(scanner.isAutoDeployZipped());
            steps.add(writeAttrOp);

            writeAttrOp = writeAttributeOp(address);
            writeAttrOp.get(ModelDescriptionConstants.NAME).set("deployment-timeout");
            writeAttrOp.get("value").set(scanner.getDeploymentTimeout());
            steps.add(writeAttrOp);

            writeAttrOp = writeAttributeOp(address);
            writeAttrOp.get(ModelDescriptionConstants.NAME).set("path");
            writeAttrOp.get("value").set(scanner.getPath());
            steps.add(writeAttrOp);

            writeAttrOp = writeAttributeOp(address);
            writeAttrOp.get(ModelDescriptionConstants.NAME).set("relative-to");
            writeAttrOp.get("value").set(scanner.getRelativeTo());
            steps.add(writeAttrOp);

            writeAttrOp = writeAttributeOp(address);
            writeAttrOp.get(ModelDescriptionConstants.NAME).set("scan-interval");
            writeAttrOp.get("value").set(scanner.getScanInterval());
            steps.add(writeAttrOp);

            writeAttrOp = writeAttributeOp(address);
            writeAttrOp.get(ModelDescriptionConstants.NAME).set("scan-enabled");
            writeAttrOp.get("value").set(scanner.isEnabled());
            steps.add(writeAttrOp);

            ModelNode composite = composite();
            composite.get(ModelDescriptionConstants.STEPS).set(steps);

            System.out.println("*********************");
            System.out.println("write all attributes");
            System.out.println(composite.toString());
            System.out.println("*********************");
            return composite;
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }

    }

    private ModelNode scannerAddress(String scannerName) {
        ModelNode address = new ModelNode();
        address.add("subsystem", "deployment-scanner");
        address.add("scanner", scannerName);
        return address;
    }

    private ModelNode writeAttributeOp(ModelNode address) {
        ModelNode op = new ModelNode();
        op.get(ModelDescriptionConstants.OP_ADDR).set(address);
        op.get(ModelDescriptionConstants.OP).set(ModelDescriptionConstants.WRITE_ATTRIBUTE_OPERATION);
        return op;
    }

    private ModelNode composite() {
        ModelNode composite = new ModelNode();
        composite.get(ModelDescriptionConstants.OP_ADDR).setEmptyList();
        composite.get(ModelDescriptionConstants.OP).set(ModelDescriptionConstants.COMPOSITE);
        return composite;
    }
}
