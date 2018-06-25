package com.benblamey.saesneg.review;

import java.util.Properties;
import org.apache.velocity.app.VelocityEngine;

public class VelocityUtils {

    static VelocityEngine getVelocityEngine() {
        Properties velocityProperties = new Properties();
        velocityProperties.put("resource.loader", "class");
        velocityProperties.put("class.resource.loader.class", "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
        VelocityEngine ve = new VelocityEngine();
        ve.init(velocityProperties);
        return ve;
    }

}
