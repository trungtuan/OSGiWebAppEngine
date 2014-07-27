/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mycompany.osgiwebfelix;

import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import javax.servlet.ServletContext;
import org.apache.felix.framework.Felix;
import org.apache.felix.framework.util.FelixConstants;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

public final class FrameworkService {

    private final ServletContext context;
    private Felix felix;

    private List<BundleLoader> bundleLoaders;
    private Map<String, String> bundleLocationMapping = new HashMap<>();

    public FrameworkService(ServletContext context) {
        this.context = context;
        JspBundleLoader jspb = new JspBundleLoader(context);
        bundleLoaders = new ArrayList<BundleLoader>();
        bundleLoaders.add(jspb);
    }

    public void start() {
        try {
            doStart();
        } catch (Exception e) {
            log("Failed to start framework", e);
        }
    }

    public void stop() {
        try {
            doStop();
        } catch (Exception e) {
            log("Error stopping framework", e);
        }
    }

    private void doStart()
            throws Exception {
        Felix tmp = new Felix(createConfig());
        tmp.init();
        BundleContext bundleContext = tmp.getBundleContext();

        // This is mandatory for Felix http servlet bridge
        context.setAttribute(BundleContext.class.getName(), bundleContext);
        
        List<URL> list = findInternalBundles(context);
        Bundle httpBridgeBundle = null;
        
        for (URL url : list) {
            Bundle bundle = bundleContext.installBundle(url.toExternalForm());
            bundleLocationMapping.put(bundle.getBundleId() + ".0", bundle.getLocation());
            
            if ("org.apache.felix.http.bridge".equals(bundle.getSymbolicName())) {
                httpBridgeBundle = bundle;
            }
        }
        tmp.start();
        
        httpBridgeBundle.start();
        
        this.felix = tmp;
        log("OSGi framework started", null);
    }

    private void doStop()
            throws Exception {
        if (this.felix != null) {
            this.felix.stop();
        }

        log("OSGi framework stopped", null);
    }

    private Map<String, Object> createConfig()
            throws Exception {
        Properties props = new Properties();
        InputStream felixConfig = this.context.getResourceAsStream("/WEB-INF/framework.properties");
        props.load(felixConfig);

        HashMap<String, Object> map = new HashMap<String, Object>();
        for (Object key : props.keySet()) {
            map.put(key.toString(), props.get(key));
        }

        map.put(FelixConstants.SYSTEMBUNDLE_ACTIVATORS_PROP, Arrays.asList(new ProvisionActivator(this.context)));
        return map;
    }

    private void log(String message, Throwable cause) {
        this.context.log(message, cause);
    }
    
        /**
     * Find built-in/mandatory bundles
     *
     * @param servletContext
     * @return
     * @throws MalformedURLException
     */
    private List<URL> findInternalBundles(ServletContext servletContext) throws MalformedURLException {
        List<URL> list = new ArrayList<>();
        @SuppressWarnings("unchecked")
        Set<String> paths = servletContext.getResourcePaths("/bundles/");
        if (paths != null) {
            for (String path : paths) {
                if (path.endsWith(".jar")) {
                    URL url = servletContext.getResource(path);
                    if (url != null) {
                        list.add(url);
                    }
                }
            }
        }
        return list;
    }
}
