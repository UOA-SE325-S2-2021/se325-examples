package se325.example11.parolee.services;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;

import java.util.HashSet;
import java.util.Set;

/**
 * Application subclass for the Parolee Web service.
 */
@ApplicationPath("/services")
public class ParoleeApplication extends Application {
    private Set<Object> singletons = new HashSet<>();
    private Set<Class<?>> classes = new HashSet<>();

    public ParoleeApplication() {
        singletons.add(new ParoleeResource());
    }

    @Override
    public Set<Object> getSingletons() {
        return singletons;
    }

    @Override
    public Set<Class<?>> getClasses() {
        return classes;
    }
}
