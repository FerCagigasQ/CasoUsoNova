/*
 * Copyright 2007-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.Properties;

public class MavenWrapperDownloader {

    private static final String WRAPPER_VERSION = "3.3.2";

    public static void main(String[] args) throws Exception {
        System.out.println("Apache Maven Wrapper Downloader " + WRAPPER_VERSION);
        File mavenWrapperPropertyFile = new File(".mvn/wrapper/maven-wrapper.properties");
        if (!mavenWrapperPropertyFile.getParentFile().exists()) {
            mavenWrapperPropertyFile.getParentFile().mkdirs();
        }
        System.out.println("- Downloading from: " + mavenWrapperPropertyFile.getAbsolutePath());

        File classLoaderSource = new File(System.getProperty("java.class.path").split(File.pathSeparator)[0]);
        String classLoaderSourcePath = classLoaderSource.getAbsolutePath().endsWith(".jar")
            ? classLoaderSource.getParent()
            : classLoaderSource.getAbsolutePath();
        String mavenWrapperPropertyPath = new File(classLoaderSourcePath).getAbsolutePath()
            + File.separator + ".." + File.separator + ".mvn" + File.separator + "wrapper" + File.separator + "maven-wrapper.properties";
        readMavenWrapperProperties(mavenWrapperPropertyPath);
    }

    private static String downloadMavenWrapperIfNotPresent(String mavenWrapperUrl) throws IOException {
        String basePath = System.getProperty("maven.home", "");
        String mavenWrapperDirectory = basePath + File.separator + ".mvn" + File.separator + "wrapper";
        File mavenWrapperDirectory_dir = new File(mavenWrapperDirectory);
        mavenWrapperDirectory_dir.mkdirs();

        String mavenWrapperPropertyFile = mavenWrapperDirectory + File.separator + "maven-wrapper.properties";

        File mavenWrapperPropertyFile_file = new File(mavenWrapperPropertyFile);
        if (!mavenWrapperPropertyFile_file.exists()) {
            System.out.println("Downloading maven wrapper ...");
            URL website = new URL(mavenWrapperUrl);
            ReadableByteChannel rbc = Channels.newChannel(website.openStream());
            FileOutputStream fos = new FileOutputStream(mavenWrapperPropertyFile);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            fos.close();
            rbc.close();
        }
        return mavenWrapperPropertyFile;
    }

    private static void readMavenWrapperProperties(String mavenWrapperPropertyPath) throws IOException {
        Properties mavenWrapperProperties = new Properties();
        FileInputStream fileInputStream = new FileInputStream(mavenWrapperPropertyPath);
        mavenWrapperProperties.load(fileInputStream);
        fileInputStream.close();
        System.out.println("- Using " + mavenWrapperProperties.getProperty("distributionUrl"));
    }
}
