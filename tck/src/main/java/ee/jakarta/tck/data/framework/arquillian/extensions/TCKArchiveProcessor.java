/*
 * Copyright (c) 2022, 2023 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */
package ee.jakarta.tck.data.framework.arquillian.extensions;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Logger;

import org.jboss.arquillian.container.test.spi.client.deployment.ApplicationArchiveProcessor;
import org.jboss.arquillian.test.spi.TestClass;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.container.ClassContainer;
import org.jboss.shrinkwrap.api.container.LibraryContainer;
import org.jboss.shrinkwrap.api.container.ResourceContainer;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;

import ee.jakarta.tck.data.framework.junit.anno.Full;
import ee.jakarta.tck.data.framework.junit.anno.Signature;
import ee.jakarta.tck.data.framework.junit.anno.Web;
import ee.jakarta.tck.data.framework.junit.anno.ReadOnlyTest;
import ee.jakarta.tck.data.framework.read.only.Populator;
import ee.jakarta.tck.data.framework.servlet.TestServlet;
import ee.jakarta.tck.data.framework.signature.DataSignatureTestRunner;


/**
 * This extension will intercept archives before they are deployed to the container and append 
 * the following packages:
 * 
 * <p>The read-only tests require the ee.jakarta.tck.data.framework.read.only package in the container.</p>
 * 
 * <p>The web/full profile tests require the ee.jakarta.tck.data.framework.servlet package in the container.</p>
 * 
 * <p>The signature tests require the ee.jakarta.tck.data.framework.signature package in the container.</p>
 */
public class TCKArchiveProcessor implements ApplicationArchiveProcessor {
    private static final Logger log = Logger.getLogger(TCKArchiveProcessor.class.getCanonicalName());

    private static final Package servletPackage = TestServlet.class.getPackage();
    private static final Package readOnlyPackage = Populator.class.getPackage();
    
    @Override
    public void process(Archive<?> applicationArchive, TestClass testClass) {
        String applicationName = applicationArchive.getName() == null 
                ? applicationArchive.getId() 
                : applicationArchive.getName();
        
        // NOTE: ClassContainer is a superclass of ResourceContainer
        if (applicationArchive instanceof ClassContainer) {
            
            //Add readonly packages to readonly tests
            if(testClass.isAnnotationPresent(ReadOnlyTest.class)) {
                log.info("Application Archive [" + applicationName + "] is being appended with packages [" + readOnlyPackage +"]");
                ((ClassContainer<?>) applicationArchive).addPackage(readOnlyPackage);
            }

            // Add servlet packages to web/full profile tests
            if(testClass.isAnnotationPresent(Web.class) || testClass.isAnnotationPresent(Full.class)) {
                log.info("Application Archive [" + applicationName + "] is being appended with packages [" + servletPackage +"]");
                ((ClassContainer<?>) applicationArchive).addPackage(servletPackage);
            }
            
            appendSignaturePackages(applicationArchive, testClass, applicationName);
        }
    }
    
    private static void appendSignaturePackages(final Archive<?> applicationArchive, final TestClass testClass, final String applicationName) {
        if (!testClass.isAnnotationPresent(Signature.class)) {
            return; //Nothing to append
        }
        
        final boolean isJava21orAbove = Integer.parseInt(System.getProperty("java.specification.version")) >= 21;
        final Package signaturePackage = DataSignatureTestRunner.class.getPackage();

        if (applicationArchive instanceof ClassContainer) {
            
            // Add the Concurrency runner
            log.info("Application Archive [" + applicationName + "] is being appended with packages [" + signaturePackage + "]");
            ((ClassContainer<?>) applicationArchive).addPackage(signaturePackage);

            // Add the sigtest plugin library
            File sigTestDep = Maven.resolver().resolve("org.netbeans.tools:sigtest-maven-plugin:1.6").withoutTransitivity().asSingleFile();
            log.info("Application Archive [" + applicationName + "] is being appended with library " + sigTestDep.getName());
            ((LibraryContainer<?>) applicationArchive).addAsLibrary(sigTestDep);
            
            // Add signature resources
            log.info("Application Archive [" + applicationName + "] is being appended with resources "
                    + Arrays.asList(DataSignatureTestRunner.SIG_RESOURCES));
            ((ResourceContainer<?>) applicationArchive).addAsResources(signaturePackage,
                    DataSignatureTestRunner.SIG_MAP_NAME, DataSignatureTestRunner.SIG_PKG_NAME);
            ((ResourceContainer<?>) applicationArchive).addAsResource(signaturePackage,
                    // Get local resource based on JDK level
                    isJava21orAbove ? DataSignatureTestRunner.SIG_FILE_NAME + "_21"
                            : DataSignatureTestRunner.SIG_FILE_NAME + "_17",
                    // Target same package as test
                    signaturePackage.getName().replace(".", "/") + "/" + DataSignatureTestRunner.SIG_FILE_NAME);
        }
    }
}
