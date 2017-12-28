package com.paloaltonetworks.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * Quickly produce an xml string from a JAXB-annotated object.
 *
 * @see  javax.xml.bind.annotation.XmlRootElement
 */
public class QuickXmlizerUtil {

    /**
     * Quickly produce an XML string from a JAXB-annotated object.
     *
     * @param object The JAXB-annotated object
     * @return XML string.
     * @throws JAXBException
     * @throws IOException
     *
     * @see  javax.xml.bind.annotation.XmlRootElement
     */
    public static String xmlString(Object object) throws JAXBException, IOException {
        Marshaller marshaller = JAXBContext.newInstance(object.getClass()).createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            marshaller.marshal(object, bos);
            return new String(bos.toByteArray(), "UTF-8");
        }
    }
}
