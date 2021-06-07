import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.CsvToBean;
import com.opencsv.bean.CsvToBeanBuilder;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class Main {

    public static void main(String[] args) {
        String[] columnMapping = {"id", "firstName", "lastName", "country", "age"};
        String fileNameCSV = "data.csv";
        String fileNameJSON1 = "data1.json";
        String fileNameJSON2 = "data2.json";
        String fileNameXML = "data.xml";

        // Первая задача CSV - JSON парсер
        String[] person1 = "1,John,Smith,USA,25".split(",");
        String[] person2 = "2,Ivan,Petrov,RU,23".split(",");
        writeInCSV(person1, fileNameCSV);
        writeInCSV(person2, fileNameCSV);

        List<Employee> listOfEmployeesFromCSV = parseCSV(columnMapping, fileNameCSV);
        System.out.println(listOfEmployeesFromCSV);

        String json1 = listToJson(listOfEmployeesFromCSV);
        writeStringInJson(json1, fileNameJSON1);

        // Вторая задача XML - JSON парсер
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        Document document = builder.newDocument();
        Element root = document.createElement("root");
        document.appendChild(root);
        Element staff = addElement(document, root, "staff");

        Element employee1 = addElement(document, staff, "employee");
        addElementWithText(document, employee1, "id", "1");
        addElementWithText(document, employee1, "firstName", "John");
        addElementWithText(document, employee1, "lastName", "Smith");
        addElementWithText(document, employee1, "country", "USA");
        addElementWithText(document, employee1, "age", "25");

        Element employee2 = addElement(document, staff, "employee");
        addElementWithText(document, employee2, "id", "2");
        addElementWithText(document, employee2, "firstName", "Ivan");
        addElementWithText(document, employee2, "lastName", "Petrov");
        addElementWithText(document, employee2, "country", "RU");
        addElementWithText(document, employee2, "age", "23");

        writeInXML(document, fileNameXML);
        List<Employee> listOfEmployeesFromXML = parseXML(fileNameXML);
        System.out.println(listOfEmployeesFromXML);

        String json2 = listToJson(listOfEmployeesFromXML);
        writeStringInJson(json2, fileNameJSON2);

        // Третья задача JSON парсер

        String json3 = readStringFromJson("data1.json");
        List<Employee> listOfEmployeesFromJSON = jsonToList(json3);
        System.out.println(listOfEmployeesFromJSON);
    }

    public static void writeInCSV(String[] info, String fileNameCSV) {
        try (CSVWriter writer = new CSVWriter(new FileWriter(fileNameCSV, true))) {
            writer.writeNext(info);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static List<Employee> parseCSV(String[] columnMapping, String fileNameCSV) {
        List<Employee> list = new ArrayList<>();
        try (CSVReader csvReader = new CSVReader(new FileReader(fileNameCSV))) {
            ColumnPositionMappingStrategy<Employee> strategy = new ColumnPositionMappingStrategy<>();
            strategy.setType(Employee.class);
            strategy.setColumnMapping(columnMapping);
            CsvToBean<Employee> csv = new CsvToBeanBuilder<Employee>(csvReader)
                    .withMappingStrategy(strategy)
                    .build();
            list = csv.parse();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static String listToJson(List<Employee> list) {
        Type listType = new TypeToken<List<Employee>>() {
        }.getType();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        String json = gson.toJson(list, listType);
        return json;
    }

    public static void writeStringInJson(String json, String fileNameJSON) {
        try (FileWriter file = new FileWriter(fileNameJSON)) {
            file.write(json);
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Element addElement(Document document, Element parentElement, String nameOfElement) {
        Element newElement = document.createElement(nameOfElement);
        parentElement.appendChild(newElement);
        return newElement;
    }

    public static void addElementWithText(Document document, Element parentElement, String nameOfElement, String text) {
        Element newElement = document.createElement(nameOfElement);
        newElement.appendChild(document.createTextNode(text));
        parentElement.appendChild(newElement);
    }

    public static void writeInXML(Document document, String fileNameXML) {
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(new File(fileNameXML));
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = null;
        try {
            transformer = transformerFactory.newTransformer();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        }
        try {
            transformer.transform(domSource, streamResult);
        } catch (TransformerException e) {
            e.printStackTrace();
        }
    }

    public static List<Employee> parseXML(String fileNameXML) {
        List<Employee> list = new ArrayList<>();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        try {
            Document document = builder.parse(new File(fileNameXML));
            readXML(list, document.getDocumentElement());
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return list;
    }

    public static void readXML(List<Employee> list, Node node) {
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node currentNode = nodeList.item(i);
            if (Node.ELEMENT_NODE == currentNode.getNodeType()) {
                Element element = (Element) currentNode;
                try {
                    long id = Long.parseLong(element.getElementsByTagName("id").item(0).getTextContent());
                    String firstName = element.getElementsByTagName("firstName").item(0).getTextContent();
                    String lastName = element.getElementsByTagName("lastName").item(0).getTextContent();
                    String country = element.getElementsByTagName("country").item(0).getTextContent();
                    int age = Integer.parseInt(element.getElementsByTagName("age").item(0).getTextContent());
                    list.add(new Employee(id, firstName, lastName, country, age));
                } catch (NumberFormatException e) {
                    e.getMessage();
                }
            }
        }
    }

    public static String readStringFromJson(String fileNameJSON) {
        String json = null;
        try (BufferedReader br = new BufferedReader(new FileReader(fileNameJSON))) {
            while ((json = br.readLine()) != null) {
                return json;
            }
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }
        return json;
    }

    public static List<Employee> jsonToList(String json) {
        List<Employee> list = new ArrayList<>();
        JSONParser parser = new JSONParser();
        GsonBuilder builder = new GsonBuilder();
        Gson gson = builder.create();
        try {
            JSONArray arrayOfEmployees = (JSONArray) parser.parse(json);
            for (Object emp : arrayOfEmployees) {
                Employee employee = gson.fromJson(((JSONObject) emp).toJSONString(), Employee.class);
                list.add(employee);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return list;
    }
}
