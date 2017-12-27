package com.paloaltonetworks.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLStreamException;

import org.junit.Test;

public class QuickXmlizerUtilTest {

    @Test
    public void testWriteXml_WithFieldAccess_ShouldSucceed() throws JAXBException, IOException, XMLStreamException, FactoryConfigurationError {

        // Arrange.
        TestXMLEntry1 expected = new TestXMLEntry1("bob",
                                        Arrays.asList("A", "B"), "whatever");

        // Act.
        String xmlString = QuickXmlizerUtil.xmlString(expected);

        // Assert.
        TestXMLEntry1 result;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(xmlString.getBytes())) {
            Unmarshaller unmarshaller = JAXBContext.newInstance(TestXMLEntry1.class).createUnmarshaller();
            result = (TestXMLEntry1) unmarshaller.unmarshal(bis);
        }

        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getDescription(), result.getDescription());
        assertArrayEquals(expected.getPenguinNames().toArray(), result.getPenguinNames().toArray());
    }

    @Test
    public void testWriteXml_WithPropertyAccess_ShouldSucceed() throws JAXBException, IOException, XMLStreamException, FactoryConfigurationError {

        // Arrange.
        TestXMLEntry2 expected = new TestXMLEntry2("bob",
                                        Arrays.asList("A", "B"), "whatever");

        // Act.
        String xmlString = QuickXmlizerUtil.xmlString(expected);

        // Assert.
        TestXMLEntry2 result;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(xmlString.getBytes())) {
            Unmarshaller unmarshaller = JAXBContext.newInstance(TestXMLEntry2.class).createUnmarshaller();
            result = (TestXMLEntry2) unmarshaller.unmarshal(bis);
        }

        assertEquals(expected.getName(), result.getName());
        assertEquals(expected.getDescription(), result.getDescription());
        assertArrayEquals(expected.getPenguinNames().toArray(), result.getPenguinNames().toArray());
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "entry")
    @XmlRootElement(name = "entry")
    private static class TestXMLEntry1 {

        @XmlAttribute(name = "name")
        private String name;

        @XmlElementWrapper(name = "penguins")
        @XmlElement(name = "member", type = String.class)
        private List<String> penguinNames;

        @XmlElement(name = "description")
        private String description;

        // Required for JAXB
        @SuppressWarnings("unused")
        public TestXMLEntry1() {}

        public TestXMLEntry1(String name, List<String> penguinNames, String description) {
            super();
            this.name = name;
            this.penguinNames = penguinNames;
            this.description = description;
        }

        public String getName() {
            return this.name;
        }

        public List<String> getPenguinNames() {
            return this.penguinNames;
        }

        public String getDescription() {
            return this.description;
        }

        // With field access, we can live without setters, apparently.
    }

    @XmlAccessorType(XmlAccessType.PROPERTY)
    @XmlType(name = "entry")
    @XmlRootElement(name = "entry")
    private static class TestXMLEntry2 {

        private String name;

        private List<String> penguinNames;

        private String description;

        // Required for JAXB
        @SuppressWarnings("unused")
        public TestXMLEntry2() {}

        public TestXMLEntry2(String name, List<String> penguinNames, String description) {
            this.name = name;
            this.penguinNames = penguinNames;
            this.description = description;
        }

        @XmlAttribute(name = "name")
        public String getName() {
            return this.name;
        }

        // Required for JAXB with properties
        @SuppressWarnings("unused")
        public void setName(String name) {
            this.name = name;
        }

        @XmlElementWrapper(name = "penguins")
        @XmlElement(name = "member", type = String.class)
        public List<String> getPenguinNames() {
            return this.penguinNames;
        }

        @SuppressWarnings("unused")
        public void setPenguinNames(List<String> penguinNames) {
            this.penguinNames = penguinNames;
        }

        @XmlElement(name = "description")
        public String getDescription() {
            return this.description;
        }

        @SuppressWarnings("unused")
        public void setDescription(String description) {
            this.description = description;
        }
    }
}
