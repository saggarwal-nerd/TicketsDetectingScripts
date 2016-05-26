package detector;

import java.io.File;
import java.io.IOException;

import java.util.Map;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonParseException;

import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

public class JsonGen {
	public static void main(String args[]) {

		// JsonGen tester = new JsonGen();

		try {
			JsonFactory jasonFactory = new JsonFactory();

			JsonGenerator jsonGenerator = jasonFactory.createJsonGenerator(
					new File("student.json"), JsonEncoding.UTF8);

			// {
			jsonGenerator.writeStartObject();

			// "name" : "Mahesh Kumar"
			jsonGenerator.writeStringField("name", "Mahesh Kumar");

			// "age" : 21
			jsonGenerator.writeNumberField("age", 21);

			// "verified" : false
			jsonGenerator.writeBooleanField("verified", false);

			// "marks" : [100, 90, 85]
			jsonGenerator.writeFieldName("marks");

			// [
			jsonGenerator.writeStartArray();
			// 100, 90, 85
			jsonGenerator.writeNumber(100);
			jsonGenerator.writeNumber(90);
			jsonGenerator.writeNumber(85);
			// ]

			jsonGenerator.writeEndArray();

			// }

			jsonGenerator.writeEndObject();
			jsonGenerator.close();

			// result student.json
			// {
			// "name":"Mahesh Kumar",
			// "age":21,
			// "verified":false,
			// "marks":[100,90,85]
			// }

			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> dataMap = mapper.readValue(new File(
					"student.json"), Map.class);

			System.out.println(dataMap.get("name"));
			System.out.println(dataMap.get("age"));
			System.out.println(dataMap.get("verified"));
			System.out.println(dataMap.get("marks"));

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}