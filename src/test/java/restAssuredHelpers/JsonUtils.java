package restAssuredHelpers;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonUtils {

	private ObjectMapper objectMapper;

    public JsonUtils() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Reads a JSON file and converts it to a Map.
     *
     * @param filePath the path to the JSON file
     * @return Map<String, Object> representing the JSON file
     */
    public Map<String, Object> readJsonFile(String filePath) {
        try {
            return objectMapper.readValue(new File(filePath), new TypeReference<Map<String, Object>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Retrieves the object at a specific index from the 'data' list as a Map<String, Object>.
     *
     * @param jsonData the JSON data as a Map
     * @param index the index of the object to retrieve
     * @return Map<String, Object> representing the object at the given index, or null if not found
     */
    @SuppressWarnings("unchecked")
	public Map<String, Object> getObjectAtIndex(Map<String, Object> jsonData, int index) {
        if (jsonData == null) {
            System.out.println("JSON data is null");
            return null;
        }

        // Retrieve the 'data' list from the JSON map
        Object dataObject = jsonData.get("data");
        if (dataObject instanceof List) {
            List<?> dataList = (List<?>) dataObject;

            // Check if the index is within bounds
            if (index >= 0 && index < dataList.size()) {
                Object item = dataList.get(index);
                if (item instanceof Map) {
                    return (Map<String, Object>) item;
                } else {
                    System.out.println("Item at index " + index + " is not a Map");
                }
            } else {
                System.out.println("Index " + index + " is out of bounds");
            }
        } else {
            System.out.println("'data' is not a List");
        }

        return null;
    }

}
