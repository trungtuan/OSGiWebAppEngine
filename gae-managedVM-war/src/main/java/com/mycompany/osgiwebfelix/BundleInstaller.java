/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mycompany.osgiwebfelix;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import javax.servlet.ServletContext;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

/**
 *
 * @author Trung Tuan trungtuan.me@gmail.com
 */
public class BundleInstaller {
    
    ServletContext sc;
    
    public BundleInstaller(ServletContext sc){
        this.sc = sc;
    }
    
    public Bundle installBundleFromFile(File savedBundleFile, boolean startBundle, boolean updateExistingBundle) throws IOException, BundleException {
        InputStream bundleInputStream = null;
        BundleContext bundleContext = (BundleContext) sc.getAttribute(BundleContext.class.getName());
        try {
            bundleInputStream = FileUtils.openInputStream(savedBundleFile);

            final String bundleFileLocationAsURL = savedBundleFile.toURI().toURL().toExternalForm();
            Bundle bundle = bundleContext.installBundle(bundleFileLocationAsURL, bundleInputStream);
            bundle.start();
            return bundle;
        } finally {
            IOUtils.closeQuietly(bundleInputStream);
        }
    }
}
