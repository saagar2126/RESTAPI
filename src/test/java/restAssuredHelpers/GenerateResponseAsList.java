package restAssuredHelpers;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class GenerateResponseAsList {

	public List<Map<String, Object>> getResponseAsListOfMap_(
			String endpoint,
			RestHTTPMethods method,
			String validationStringForAnyIndex,
			String keyForValidation,
			String nodeName,
			Object... requestBody) {

		// Send the request and get the response
		Response response;
		switch (method) {
		case GET:
			response = RestAssured.get(endpoint);
			break;
		case POST:
			response = RestAssured.given().body(requestBody.length > 0 ? requestBody[0] : "").post(endpoint);
			break;
		case PUT:
			response = RestAssured.given().body(requestBody.length > 0 ? requestBody[0] : "").put(endpoint);
			break;
		case DELETE:
			response = RestAssured.given().body(requestBody.length > 0 ? requestBody[0] : "").delete(endpoint);
			break;
		default:
			throw new IllegalArgumentException("Unsupported HTTP method: " + method);
		}

		// Check if the response status is 200 OK
		if (response.getStatusCode() != 200) {
			throw new RuntimeException("Failed: HTTP error code: " + response.getStatusCode());
		}

		List<Map<String, Object>> dataList = response.jsonPath().getList(nodeName);

		int index=getDynamicIndex(dataList, validationStringForAnyIndex,keyForValidation);

		System.out.println("index at which the value present is"+" "+index);

		if (index >= 0) {
			dataList = response.jsonPath().getList(nodeName);
		} else{
			System.out.println("node not found in the list of objects");
		}
		return dataList;
	}

	public Response getResponse(RestHTTPMethods method,String endpoint,Object... requestBody){
		Response response;
		switch (method) {
		case GET:
			response = RestAssured.get(endpoint);
			break;
		case POST:
			response = RestAssured.given().body(requestBody.length > 0 ? requestBody[0] : "").post(endpoint);
			break;
		case PUT:
			response = RestAssured.given().body(requestBody.length > 0 ? requestBody[0] : "").put(endpoint);
			break;
		case DELETE:
			response = RestAssured.given().body(requestBody.length > 0 ? requestBody[0] : "").delete(endpoint);
			break;
		default:
			throw new IllegalArgumentException("Unsupported HTTP method: " + method);
		}
		return response;
	}

	public static int getDynamicIndex(List<Map<String, Object>> dataList, String targetValue,String keyForValidation ) {
		for (int i = 0; i < dataList.size(); i++) {
			Map<String, Object> item =dataList.get(i);
			String currentValue =item.get(keyForValidation).toString();
			if (targetValue.equals(currentValue)) {
				return i;
			}
		}
		return -1; // Return -1 if the item is not found
	}
	
	public static int getDynamicIndex(Map<String, Object> jsonData, String keyForList, String targetValue, String keyForValidation) {
        // Retrieve the list of maps from the provided JSON data
        Object listObject = jsonData.get(keyForList);
        if (listObject instanceof List) {
            List<?> dataList = (List<?>) listObject;
            if (!dataList.isEmpty() && dataList.get(0) instanceof Map) {
                // Iterate over the list and check for the target value
                for (int i = 0; i < dataList.size(); i++) {
                    Map<String, Object> item = (Map<String, Object>) dataList.get(i);
                    Object valueObject = item.get(keyForValidation);
                    if (valueObject != null && targetValue.equals(valueObject.toString())) {
                        return i;
                    }
                }
            } else {
                System.out.println("The data is not a list of maps or is empty");
            }
        } else {
            System.out.println("The provided key does not map to a List");
        }
        return -1; // Return -1 if the item is not found
    }


	public List<Map<String, Object>> getResponseAsListOfMap(Response response,String nodeName){
		List<Map<String, Object>> dataList = response.jsonPath().getList(nodeName);
		return dataList;
	}

	public void validateValueInList(String targetValue,String keyForValidation,List<Map<String, Object>> dataList){
		int dynamicIndex=getDynamicIndex(dataList, targetValue,keyForValidation);
		System.out.println("index at which the value present is"+" "+dynamicIndex);
		if (dynamicIndex >= 0 && dynamicIndex < dataList.size()) {
			Map<String, Object> itemAtIndex = dataList.get(dynamicIndex);
			String key = keyForValidation;
			Object value = itemAtIndex.get(key);
			System.out.println("Value at index " + dynamicIndex + " for key '" + key + "': " + value);
			String expectedValue = "George";
			assertEquals(expectedValue,value,"Value at index"+dynamicIndex +" is as expected.");
		} else {
			fail("Dynamic index " + dynamicIndex + " is out of bounds for the data list.");
		}
	}

	public Map<String, Object> extractMapFromList(String targetValue,String keyForValidation,List<Map<String, Object>> dataList){
		Map<String, Object> itemAtIndex = null;
		int dynamicIndex=getDynamicIndex(dataList, targetValue,keyForValidation);
		System.out.println("index at which the value present is"+" "+dynamicIndex);
		if (dynamicIndex >= 0 && dynamicIndex < dataList.size()) {
			itemAtIndex = dataList.get(dynamicIndex);
		}
		return itemAtIndex;
	}

	public Map<String, Object> getPostPayloadWithList(String filePath,String sheetName) throws IOException {
		Map<String, Object> payload = new HashMap<>();

		try (FileInputStream fis = new FileInputStream(filePath);
				XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

			Sheet sheet = workbook.getSheet(sheetName);
			if (sheet == null) {
				throw new IllegalArgumentException("Sheet with name " + sheetName + " does not exist.");
			}

			// Read general fields (String: String)
			Row generalFieldsRow = sheet.getRow(0);
			if (generalFieldsRow == null) {
				throw new IllegalArgumentException("Row 0 is missing in sheet " + sheetName);
			}

			for (int i = 0; i < generalFieldsRow.getLastCellNum(); i += 2) {
				Cell keyCell = generalFieldsRow.getCell(i);

				String key = keyCell.getStringCellValue();
				Cell valueCell = generalFieldsRow.getCell(i + 1);
				String value = null;

				value = valueCell.getStringCellValue();

				if (key != null && value != null) {
					payload.put(key, value);
				}
			}

			// Read special field (String: List<String>)
			Row specialFieldRow = sheet.getRow(generalFieldsRow.getRowNum() + 1);
			if (specialFieldRow == null) {
				throw new IllegalArgumentException("Special field row is missing in sheet " + sheetName);
			}

			Cell specialFieldKeyCell = specialFieldRow.getCell(0);

			String specialFieldKey = specialFieldKeyCell.getStringCellValue();
			List<String> specialFieldValues = new ArrayList<>();

			for (int i = 1; i < specialFieldRow.getLastCellNum(); i++) {
				Cell cell = specialFieldRow.getCell(i);
				specialFieldValues.add(cell.getStringCellValue());
			}

			if (!specialFieldValues.isEmpty()) {
				payload.put(specialFieldKey, specialFieldValues);
			}
		}

		return payload;
	}

	/*public void compareListOfMapValues(Map<String, Object> excelTestDataMap, Map<String, Object> responseMap)  {

	}*/
	public void compareMaps(Map<String, Object> excelTestDataMap, Map<String, Object> responseMap) {
		// Check if both maps are null or if they are the same reference
		if (excelTestDataMap == responseMap) {
			System.out.println("both the maps are matching");
		}

		// Check if either map is null
		if (excelTestDataMap == null || responseMap == null) {
			System.out.println("no data present in the map");
		}

		// Check if maps have the same size
		if (excelTestDataMap.size() != responseMap.size()) {
			System.out.println("size of the map is not matching");
		}

		// Iterate through entries of the first map
		for (Map.Entry<String, Object> entry : excelTestDataMap.entrySet()) {
			String key = entry.getKey();
			Object value1 = entry.getValue();
			Object value2 = responseMap.get(key);

			// Check if the second map contains the key
			if (value2 == null) {
				System.out.println("one of second map the value is null");
			}

			// Compare values based on their type
			if (value1 instanceof String && value2 instanceof String) {
				// Compare strings
				if (!value1.equals(value2)) {
					System.out.println("string values of the maps are not matching");
				}
			} else if (value1 instanceof List && value2 instanceof List) {
				// Compare lists
				if (!compareLists((List<?>) value1, (List<?>) value2)) {
					System.out.println("list values of the map are not matching");
				}else if (value1 instanceof Integer && value2 instanceof Integer) {
					// Compare integers by converting them to strings
					if (!value1.toString().equals(value2.toString())) {
						System.out.println("Integer values for key '" + key + "' are not matching.");
					}
				} else {
					if (!value1.equals(value2)) {
						System.out.println("Values for key '" + key + "' are not matching.");
					}
				}
			}
		}
		System.out.println("maps and thier values are matching as expected");
	}

	private static boolean compareLists(List<?> list1, List<?> list2) {
		// Check if both lists are null or if they are the same reference
		if (list1 == list2) {
			return true;
		}

		// Check if either list is null
		if (list1 == null || list2 == null) {
			return false;
		}

		// Check if lists have the same size
		if (list1.size() != list2.size()) {
			return false;
		}

		// Compare list elements
		for (int i = 0; i < list1.size(); i++) {
			Object element1 = list1.get(i);
			Object element2 = list2.get(i);

			// Compare elements based on their type
			if (element1 instanceof String && element2 instanceof String) {
				// Compare strings
				if (!element1.equals(element2)) {
					return false;
				}
			} else {
				// Elements are of different types or unknown types
				return false;
			}
		}

		return true;
	}

	public Map<String, String[]> readExcelFileAsArray(String filePath,String sheetName) {
		Map<String, String[]> result = new HashMap<>();

		try (FileInputStream fis = new FileInputStream(filePath);
				XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

			Sheet sheet = workbook.getSheet(sheetName);

			// Read the headers (first row) to use as keys
			Row headerRow = sheet.getRow(0);
			if (headerRow == null) {
				return result; 
			}

			// Initialize the map with keys from the first row
			Map<Integer, String> headers = new HashMap<>();
			for (Cell cell : headerRow) {
				headers.put(cell.getColumnIndex(), cell.getStringCellValue());
			}

			// Initialize arrays to hold column values
			int numberOfColumns = headers.size();
			String[][] values = new String[numberOfColumns][];

			for (int i = 0; i < numberOfColumns; i++) {
				values[i] = new String[sheet.getLastRowNum()];
			}

			// Process each row after the header
			for (int rowIndex = 1; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
				Row row = sheet.getRow(rowIndex);
				if (row != null) {
					for (int colIndex = 0; colIndex < numberOfColumns; colIndex++) {
						Cell cell = row.getCell(colIndex);
						String value=" ";
						if (cell != null) {
							value = cell.toString();
						} else {
							value = "";
						}

						values[colIndex][rowIndex - 1] = value;
					}
				}
			}

			// Populate the result map with key-value pairs
			for (Map.Entry<Integer, String> entry : headers.entrySet()) {
				result.put(entry.getValue(), values[entry.getKey()]);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	public Map<String, String> readExcelFile(String filePath, String sheetName) {
		Map<String, String> result = new HashMap<>();

		try (FileInputStream fis = new FileInputStream(filePath);
				XSSFWorkbook workbook = new XSSFWorkbook(fis)) {

			Sheet sheet = workbook.getSheet(sheetName);

			if (sheet == null) {
				System.err.println("Sheet with name " + sheetName + " not found.");
				return result;
			}

			// Read the headers (first row) to use as keys
			Row headerRow = sheet.getRow(0);
			if (headerRow == null) {
				System.err.println("Header row is missing.");
				return result;
			}

			// Read the next row as values
			Row dataRow = sheet.getRow(headerRow.getRowNum()+1);
			if (dataRow == null) {
				System.err.println("Data row is missing.");
				return result; 
			}

			// Map headers to values
			for (Cell headerCell : headerRow) {
				int columnIndex = headerCell.getColumnIndex();
				String header = headerCell.getStringCellValue();
				Cell valueCell = dataRow.getCell(columnIndex);
				String value = "";

				if (valueCell != null) {
					value = valueCell.toString();
				} else {
					value = "";
				}
				result.put(header, value);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

		return result;
	}

	public Map<String, Object> readJsonToMap(String filePath) throws IOException {
		Gson gson = new Gson();
		Type mapType = new TypeToken<Map<String, Object>>(){}.getType();
		try (FileReader reader = new FileReader(filePath)) {
			return gson.fromJson(reader, mapType);
		}
	}
}



