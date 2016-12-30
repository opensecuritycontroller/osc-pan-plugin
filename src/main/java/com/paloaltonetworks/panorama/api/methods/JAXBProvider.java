package com.paloaltonetworks.panorama.api.methods;

import static javax.ws.rs.core.MediaType.APPLICATION_XML_TYPE;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;

public class JAXBProvider<T> implements MessageBodyReader<T>, MessageBodyWriter<T> {

    private boolean isJAXBSuitable(Class<?> type) {
        try {
            JAXBContext.newInstance(type);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return isEqual(APPLICATION_XML_TYPE, mediaType) && isJAXBSuitable(type);
    }

    @Override
    public long getSize(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(T t, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
            throws IOException, WebApplicationException {
        JAXB.marshal(t, entityStream);
    }

    @Override
    public boolean isReadable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return isEqual(APPLICATION_XML_TYPE, mediaType) && isJAXBSuitable(type);
    }

    @Override
    public T readFrom(Class<T> type, Type genericType, Annotation[] annotations, MediaType mediaType,
            MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
            throws IOException, WebApplicationException {
        return JAXB.unmarshal(entityStream, type);
    }

    private boolean isEqual(MediaType applicationXmlType, MediaType mediaType) {

        if (mediaType.toString().contains(applicationXmlType.toString())) {
            return true;
        }
        return false;
    }

}
