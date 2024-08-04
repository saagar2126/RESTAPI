package sdetqa_day1;


import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.fail;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.testng.annotations.Test;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import restAssuredHelpers.GenerateResponseAsList;
import restAssuredHelpers.JsonUtils;
import restAssuredHelpers.RestHTTPMethods;

public class HTTPRequests {

	GenerateResponseAsList generateResponse = new GenerateResponseAsList();
	JsonUtils jsonUtils=new JsonUtils();
	public static String fileName="tricentis_data.xlsx";
	public static String jsonfileName="testData.json";
	public static String jsonfileNameMultipleNodes="testDataMultiple.json";
	public static String filePath=System.getProperty("user.dir") + "/src/test/resources/data" + File.separator+ fileName;
	public static String jsonFilePath=System.getProperty("user.dir") + "/src/test/resources/data" + File.separator+ jsonfileName;
	public static String jsonFilePathMultipleNodes=System.getProperty("user.dir") + "/src/test/resources/data" + File.separator+ jsonfileNameMultipleNodes;

	@Test
	void getUsers(){
		String URI="https://reqres.in/api/users?page=2";
		//String URI="https://reqres.in/api/users?id=84";
		given()
		.when()
		.get(URI)
		.then()
		.statusCode(200)
		.body("page",equalTo(2))
		.log().all();

	}

	@Test
	public void fetchValueFromList() {
		String URI="https://reqres.in/api/users?page=2";
		//String URI="https://reqres.in/api/users?id=84";
		Response response = RestAssured.get(URI);
		response.then().statusCode(200);
		List<Map<String, Object>> dataList = response.jsonPath().getList("data");
		int dynamicIndex=getDynamicIndex(dataList, "Lindsay");
		System.out.println("index at which the value present is"+" "+dynamicIndex);
		if (dynamicIndex >= 0 && dynamicIndex < dataList.size()) {
			Map<String, Object> itemAtIndex = dataList.get(dynamicIndex);
			String key = "first_name";
			Object value = itemAtIndex.get(key);
			System.out.println("Value at index " + dynamicIndex + " for key '" + key + "': " + value);
			String expectedValue = "Lindsay";
			assertEquals(expectedValue,value,"Value at index"+dynamicIndex +" is as expected.");
		} else {
			fail("Dynamic index " + dynamicIndex + " is out of bounds for the data list.");
		}
	}

	@Test
	public void createUserDataReturnedAsArray(){
		String URI="https://reqres.in/api/users";
		Map<String, String[]> data =generateResponse.readExcelFileAsArray(filePath, "postReqData");
		given()
		.contentType("application/json")
		.body(data)
		.when()
		.post(URI)
		.then()
		.statusCode(201)
		.log().all();

	}
	
	@Test
	public void createUserDataReturnedAsString(){
		String URI="https://reqres.in/api/users";
		Map<String, String> data =generateResponse.readExcelFile(filePath, "postReqData");
		given()
		.contentType("application/json")
		.body(data)
		.when()
		.post(URI)
		.then()
		.statusCode(201)
		.log().all();

	} 
	
	@Test
	public void updateUser(){
		//fetching the user id 
		String URI="https://reqres.in/api/users/";
		Map<String, String> data =generateResponse.readExcelFile(filePath, "postReqData");
		int id=given()
				.contentType("application/json")
				.body(data)
				.when()
				.post(URI)
				.jsonPath()
				.getInt("id");
		System.out.println(id);
		//updating the user details
		given()
		.contentType("application/json")
		.body(data)
		.when()
		.put(URI+id)
		.then()
		.statusCode(200)
		.log().all();
				
	}
	
	
	@Test
	public void validateGETRequestFromUtil(){
		String URI="https://reqres.in/api/users/";
		Response response =generateResponse.getResponse(RestHTTPMethods.GET, URI, "");
		response.then()
		.statusCode(200)
		.log().all();
		List<Map<String, Object>> data=generateResponse.getResponseAsListOfMap(response, "data");
		generateResponse.validateValueInList("George", "first_name", data);
	}
	
	@Test
	public void validatePOSTRequestFromUtil(){
		String URI="http://localhost:3000/students";
		Map<String, String> data =generateResponse.readExcelFile(filePath, "studentsData");
		Response response =generateResponse.getResponse(RestHTTPMethods.POST, URI, data);
		System.out.println(response.asString());
		response.then()
		.statusCode(201)
		.log().all();
	}
	
	@Test
	public void validatePOSTRequestWithListFromUtil() throws IOException{
		String URI="http://localhost:3000/students";
		Map<String, Object> data=generateResponse.getPostPayloadWithList(filePath, "postPayloadWithList");
		Response response =generateResponse.getResponse(RestHTTPMethods.POST, URI, data);
		//System.out.println(response.asString());
		response.then()
		.statusCode(201)
		.log().all();
	}
	
	@Test
	public void validateListOfMapValuesSingleList() throws IOException{
		String URI="http://localhost:3000/students/";
		Response response =generateResponse.getResponse(RestHTTPMethods.GET, URI, "");
		response.then()
		.statusCode(200)
		.log().all();
		Map<String, Object> excelMap=generateResponse.getPostPayloadWithList(filePath, "testDataForComparison_mario");
		List<Map<String, Object>> listOfMap=generateResponse.getResponseAsListOfMap(response, "$");
		Map<String, Object> responseMap=generateResponse.extractMapFromList("mario", "name", listOfMap);
		generateResponse.compareMaps(excelMap, responseMap);
	}
	
	@Test
	public void validateListOfMapValuesMultipleList() throws IOException{
		String URI="http://localhost:3000/students/";
		Response response =generateResponse.getResponse(RestHTTPMethods.GET, URI, "");
		response.then()
		.statusCode(200)
		.log().all();
		//Map<String, Object> excelMap=generateResponse.getPostPayloadwithMultipleLists(filePath, "testDataForComparison_lugi");
		Map<String, Object> jsonMap=generateResponse.readJsonToMap(jsonFilePath);
		List<Map<String, Object>> listOfMap=generateResponse.getResponseAsListOfMap(response, "$");
		Map<String, Object> responseMap=generateResponse.extractMapFromList("lugi", "name", listOfMap);
		generateResponse.compareMaps(jsonMap, responseMap);
	}
	
	@Test
	public void readJsonAsMapTest() throws IOException{
		String URI="https://reqres.in/api/users/";
		Response response =generateResponse.getResponse(RestHTTPMethods.GET, URI, "");
		response.then()
		.statusCode(200)
		.log().all();
		List<Map<String, Object>> listOfMap=generateResponse.getResponseAsListOfMap(response, "data");
		Map<String, Object> responseMap=generateResponse.extractMapFromList("Janet", "first_name", listOfMap);
		Map<String, Object> jsonData=jsonUtils.readJsonFile(jsonFilePathMultipleNodes);
		int index =GenerateResponseAsList.getDynamicIndex(jsonData,"data","Janet","first_name");
		Map<String, Object> jsonMapFromMultipleNodes=jsonUtils.getObjectAtIndex(jsonData, index);
		generateResponse.compareMaps(jsonMapFromMultipleNodes, responseMap);
	}

	private int getDynamicIndex(List<Map<String, Object>> dataList, String targetValue) {
		for (int i = 0; i < dataList.size(); i++) {
			Map<String, Object> item =dataList.get(i);
			String currentValue =item.get("first_name").toString();
			if (targetValue.equals(currentValue)) {
				return i;
			}
		}
		return -1; // Return -1 if the item is not found
	}
}

/*
given()-content type,set cookies,add auth,add param,set headers info 
when()-get,put,post,patch,delete
then()-validate status code,extract response , response body , extract headers , cookies
 */