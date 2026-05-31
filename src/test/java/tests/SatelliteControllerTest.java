package tests;

import base.BaseTest;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static framework.specs.ResponseSpecification.expectedStatusCode;
import static framework.specs.ResponseSpecification.expectedStatusCodeNoContent;

class SatelliteControllerTest extends BaseTest {

    private final List<String> createdSatelliteIds = new ArrayList<>();
    private final List<String> createdConstellationNames = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        for (String id : createdSatelliteIds) {
            apiClient.delete("/api/satellites/" + id);
        }
        for (String name : createdConstellationNames) {
            apiClient.delete("/api/constellations/" + name);
        }
    }

    @Test
    @DisplayName("[200] GET /api/satellites - получение списка спутников")
    void getSatellitesTest() {
        Response response = apiClient.get("/api/satellites");
        response.then().spec(expectedStatusCode(200));
    }

    @Test
    @DisplayName("[200] POST /api/satellites - добавление спутника")
    void postSatellitesTest() {
        Map<String, Object> satellite = new HashMap<>();
        String satelliteName = "Спутник ДЗЗ-" + UUID.randomUUID();

        satellite.put("type", "IMAGE");
        satellite.put("name", satelliteName);
        satellite.put("batteryLevel", 1);
        satellite.put("resolution", 250);

        Response response = apiClient.post("/api/satellites", satellite);
        response.then().spec(expectedStatusCode(200));

        JsonPath json = response.jsonPath();
        String name = json.getString("name");
        Assertions.assertEquals(satelliteName, name);

        String id = json.getString("id");
        createdSatelliteIds.add(id);

        Response responseOrder = apiClient.get("/api/satellites/" + id);
        responseOrder.then().spec(expectedStatusCode(200));
        String responseOrderId = responseOrder.jsonPath().getString("id");
        Assertions.assertEquals(id, responseOrderId);


    }

    @Test
    @DisplayName("[200] GET /api/satellites/{id} - получение спутника по ID")
    void getSatelliteByIdTest() {
        Map<String, Object> satellite = new HashMap<>();
        String satelliteName = "Спутник ДЗЗ-" + UUID.randomUUID();
        satellite.put("type", "IMAGE");
        satellite.put("name", satelliteName);
        satellite.put("batteryLevel", 1);
        satellite.put("resolution", 250);

        Response createResponse = apiClient.post("/api/satellites", satellite);
        String id = createResponse.jsonPath().getString("id");
        createdSatelliteIds.add(id);

        Response response = apiClient.get("/api/satellites/" + id);
        response.then().spec(expectedStatusCode(200));
        Assertions.assertEquals(satelliteName, response.jsonPath().getString("name"));
    }

    @Test
    @DisplayName("[200] PUT /api/satellites/{id} - обновление спутника")
    void putSatelliteTest() {
        Map<String, Object> satellite = new HashMap<>();
        String satelliteName = "Спутник ДЗЗ-" + UUID.randomUUID();
        satellite.put("type", "IMAGE");
        satellite.put("name", satelliteName);
        satellite.put("batteryLevel", 1);
        satellite.put("resolution", 250);

        Response createResponse = apiClient.post("/api/satellites", satellite);
        String id = createResponse.jsonPath().getString("id");
        createdSatelliteIds.add(id);

        String updatedName = "Спутник ДЗЗ-обновленный-" + UUID.randomUUID();
        Map<String, Object> updateBody = new HashMap<>();
        updateBody.put("type", "IMAGE");
        updateBody.put("name", updatedName);
        updateBody.put("batteryLevel", 0.5);
        updateBody.put("resolution", 500);

        Response response = apiClient.put("/api/satellites/" + id, updateBody);
        response.then().spec(expectedStatusCode(200));
        Assertions.assertEquals(updatedName, response.jsonPath().getString("name"));

        Response getResponse = apiClient.get("/api/satellites/" + id);
        Assertions.assertEquals(updatedName, getResponse.jsonPath().getString("name"));
    }

    @Test
    @DisplayName("[204] DELETE /api/satellites/{id} - удаление спутника")
    void deleteSatelliteTest() {
        Map<String, Object> satellite = new HashMap<>();
        String satelliteName = "Спутник ДЗЗ-" + UUID.randomUUID();
        satellite.put("type", "IMAGE");
        satellite.put("name", satelliteName);
        satellite.put("batteryLevel", 1);
        satellite.put("resolution", 250);

        Response createResponse = apiClient.post("/api/satellites", satellite);
        String id = createResponse.jsonPath().getString("id");
        createdSatelliteIds.add(id);

        Response response = apiClient.delete("/api/satellites/" + id);
        response.then().spec(expectedStatusCodeNoContent(204));
    }

    @Test
    @DisplayName("[400] POST /api/satellites - проверка валидации (batteryLevel > 1)")
    void postSatelliteInvalidBatteryLevelTest() {
        Map<String, Object> satellite = new HashMap<>();
        satellite.put("type", "IMAGE");
        satellite.put("name", "Спутник ДЗЗ-" + UUID.randomUUID());
        satellite.put("batteryLevel", 1.01);
        satellite.put("resolution", 250);

        Response response = apiClient.post("/api/satellites", satellite);
        response.then().spec(expectedStatusCode(400));
    }

    @Test
    @DisplayName("[200] GET /api/satellites/name/{name} - получение спутника по имени")
    void getSatelliteByNameTest() {
        String satelliteName = "Спутник-поиск-" + UUID.randomUUID();
        Map<String, Object> satellite = new HashMap<>();
        satellite.put("type", "IMAGE");
        satellite.put("name", satelliteName);
        satellite.put("batteryLevel", 1);
        satellite.put("resolution", 250);

        Response createResponse = apiClient.post("/api/satellites", satellite);
        String id = createResponse.jsonPath().getString("id");
        createdSatelliteIds.add(id);

        Response response = apiClient.get("/api/satellites/name/" + satelliteName);
        response.then().spec(expectedStatusCode(200));
        Assertions.assertEquals(satelliteName, response.jsonPath().getString("name"));
    }

    @Test
    @DisplayName("[200] GET /api/satellites/constellation/{constellationId} - получение спутников группировки")
    void getSatellitesByConstellationTest() {
        String constellationName = "Группировка-" + UUID.randomUUID();

        Map<String, Object> constellationBody = new HashMap<>();
        constellationBody.put("name", constellationName);

        Response constellationResponse = apiClient.post("/api/constellations", constellationBody);
        constellationResponse.then().spec(expectedStatusCode(200));
        String constellationId = constellationResponse.jsonPath().getString("id");
        createdConstellationNames.add(constellationName);

        String satelliteName = "Спутник-группировка-" + UUID.randomUUID();
        Map<String, Object> satellite = new HashMap<>();
        satellite.put("type", "IMAGE");
        satellite.put("name", satelliteName);
        satellite.put("batteryLevel", 1);
        satellite.put("resolution", 250);

        Response createResponse = apiClient.post("/api/satellites", satellite);
        String satelliteId = createResponse.jsonPath().getString("id");
        createdSatelliteIds.add(satelliteId);

        Map<String, Object> addSatBody = new HashMap<>();
        addSatBody.put("constellationName", constellationName);
        List<Map<String, Object>> params = new ArrayList<>();
        Map<String, Object> satParam = new HashMap<>();
        satParam.put("type", "IMAGE");
        satParam.put("name", satelliteName);
        satParam.put("batteryLevel", 1);
        satParam.put("resolution", 250);
        params.add(satParam);
        addSatBody.put("satelliteParams", params);
        apiClient.post("/api/add-satellites", addSatBody);

        Response response = apiClient.get("/api/satellites/constellation/" + constellationId);
        response.then().spec(expectedStatusCode(200));
    }

}
