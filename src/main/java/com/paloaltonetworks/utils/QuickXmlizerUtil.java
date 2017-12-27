package com.paloaltonetworks.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

public class QuickXmlizerUtil {
    public static String xmlString(Object object) throws JAXBException, IOException {
        Marshaller marshaller = JAXBContext.newInstance(object.getClass()).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            marshaller.marshal(object, bos);
            return new String(bos.toByteArray(), "UTF-8");
        }
    }
}
