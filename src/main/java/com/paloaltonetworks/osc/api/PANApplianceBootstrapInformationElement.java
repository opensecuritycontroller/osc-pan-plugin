/* Copyright 2016 Palo Alto Networks Inc.
 * All Rights Reserved.
 *    Licensed under the Apache License, Version 2.0 (the "License"); you may
 *    not use this file except in compliance with the License. You may obtain
 *    a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 *    WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 *    License for the specific language governing permissions and limitations
 *    under the License.
 */
package com.paloaltonetworks.osc.api;

import java.util.ArrayList;
import java.util.List;

import org.osc.sdk.manager.element.ApplianceBootstrapInformationElement;

public class PANApplianceBootstrapInformationElement implements ApplianceBootstrapInformationElement {

    private List<BootstrapFileElement> bootstrapInfo;

    public PANApplianceBootstrapInformationElement() {
        bootstrapInfo = new ArrayList<BootstrapFileElement>();
    }

    @Override
    public List<BootstrapFileElement> getBootstrapFiles() {
        return bootstrapInfo;
    }

    private class PANBootstrapFileElement implements BootstrapFileElement {

        String localFileName;
        byte[] localFileData;

        PANBootstrapFileElement(String fileName, byte[] fileData) {
            localFileName = fileName;
            localFileData = fileData;
        }

        @Override
        public String getName() {

            return localFileName;
        }

        @Override
        public byte[] getContent() {
            return localFileData;
        }
    }

    public void addBootstrapFile(String filename, byte[] encodedData) {

        PANBootstrapFileElement fileElement = new PANBootstrapFileElement(filename, encodedData);
        bootstrapInfo.add(fileElement);
    }

}
