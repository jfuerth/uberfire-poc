package org.jboss.uberfire.poc.client.ballroom;

import com.google.gwt.core.client.GWT;
import com.google.web.bindery.autobean.shared.AutoBeanFactory;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import javax.enterprise.context.ApplicationScoped;
import org.jboss.ballroom.client.rbac.SecurityService;
import org.jboss.ballroom.client.spi.Framework;

/**
 *
 */
@ApplicationScoped
public class ConsoleFramework implements Framework {

    private final static ConsoleBeanFactory factory = GWT.create(ConsoleBeanFactory.class);
    private final static SecurityService secService = new BallroomSecurityService();

    @Override
    public ConsoleBeanFactory getBeanFactory() {
        return factory;
    }

    @Override
    public EventBus getEventBus() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public PlaceManager getPlaceManager() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public SecurityService getSecurityService() {
        return secService;
    }

}
