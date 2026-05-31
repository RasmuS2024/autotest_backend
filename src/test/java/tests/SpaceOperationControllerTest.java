package tests;

import base.BaseTest;
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

class SpaceOperationControllerTest extends BaseTest {

    private final List<String> createdSatelliteIds = new ArrayList<>();
    private final List<String> createdConstellationNames = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        for (String id : createdSatelliteIds) {
            apiClient.post("/api/satellites/" + id + "/deactivate");
            apiClient.delete("/api/satellites/" + id);
        }
        for (String name : createdConstellationNames) {
            apiClient.delete("/api/constellations/" + name);
        }
    }

    private String createSatellite() {
        String satelliteName = "Спутник-" + UUID.randomUUID();
        Map<String, Object> satellite = new HashMap<>();
        satellite.put("type", "IMAGE");
        satellite.put("name", satelliteName);
        satellite.put("batteryLevel", 1);
        satellite.put("resolution", 250);
        Response response = apiClient.post("/api/satellites", satellite);
        String id = response.jsonPath().getString("id");
        createdSatelliteIds.add(id);
        return id;
    }

    private String createConstellation() {
        String constellationName = "Группировка-" + UUID.randomUUID();
        Map<String, Object> body = new HashMap<>();
        body.put("name", constellationName);
        apiClient.post("/api/constellations", body);
        createdConstellationNames.add(constellationName);
        return constellationName;
    }

    @Test
    @DisplayName("[200] GET /api/overview - получение сводки по системе")
    void getSystemOverviewTest() {
        Response response = apiClient.get("/api/overview");
        response.then().spec(expectedStatusCode(200));
    }

    @Test
    @DisplayName("[200] POST /api/missions - выполнение миссии")
    void executeMissionTest() {
        String constellationName = createConstellation();
        String satelliteName = "Спутник-миссия-" + UUID.randomUUID();

        Map<String, Object> satellite = new HashMap<>();
        satellite.put("type", "IMAGE");
        satellite.put("name", satelliteName);
        satellite.put("batteryLevel", 1);
        satellite.put("resolution", 250);
        Response createResponse = apiClient.post("/api/satellites", satellite);
        createdSatelliteIds.add(createResponse.jsonPath().getString("id"));

        Map<String, Object> addBody = new HashMap<>();
        addBody.put("constellationName", constellationName);
        List<Map<String, Object>> params = new ArrayList<>();
        Map<String, Object> satParam = new HashMap<>();
        satParam.put("type", "IMAGE");
        satParam.put("name", satelliteName);
        satParam.put("batteryLevel", 1);
        satParam.put("resolution", 250);
        params.add(satParam);
        addBody.put("satelliteParams", params);
        apiClient.post("/api/add-satellites", addBody);

        Map<String, Object> body = new HashMap<>();
        body.put("targetType", "SINGLE_SATELLITE");
        body.put("constellationName", constellationName);
        body.put("satelliteName", satelliteName);

        Response response = apiClient.post("/api/missions", body);
        response.then().spec(expectedStatusCode(200));
    }

    @Test
    @DisplayName("[200] POST /api/add-satellites - добавление спутника в группировку")
    void addSatelliteTest() {
        String constellationName = createConstellation();
        String satelliteName = "Спутник-добавление-" + UUID.randomUUID();

        Map<String, Object> body = new HashMap<>();
        body.put("constellationName", constellationName);

        List<Map<String, Object>> params = new ArrayList<>();
        Map<String, Object> satParam = new HashMap<>();
        satParam.put("type", "IMAGE");
        satParam.put("name", satelliteName);
        satParam.put("batteryLevel", 1);
        satParam.put("resolution", 250);
        params.add(satParam);
        body.put("satelliteParams", params);

        Response response = apiClient.post("/api/add-satellites", body);
        response.then().spec(expectedStatusCode(200));

        apiClient.delete("/api/constellations/" + constellationName + "/satellites/" + satelliteName);
        createdConstellationNames.remove(constellationName);
    }

    @Test
    @DisplayName("[200] POST /api/satellites/{id}/mission - выполнение миссии спутника")
    void performSatelliteMissionTest() {
        String satelliteId = createSatellite();

        Response response = apiClient.post("/api/satellites/" + satelliteId + "/mission");
        response.then().spec(expectedStatusCode(200));
    }

    @Test
    @DisplayName("[200] POST /api/satellites/{id}/activate - активация спутника")
    void activateSatelliteTest() {
        String satelliteId = createSatellite();

        Response response = apiClient.post("/api/satellites/" + satelliteId + "/activate");
        response.then().spec(expectedStatusCode(200));
    }

    @Test
    @DisplayName("[200] POST /api/satellites/{id}/deactivate - деактивация спутника")
    void deactivateSatelliteTest() {
        String satelliteId = createSatellite();

        Response response = apiClient.post("/api/satellites/" + satelliteId + "/deactivate");
        response.then().spec(expectedStatusCode(200));
    }

    @Test
    @DisplayName("[200] GET /api/satellites/{id}/status - получение статуса спутника")
    void getSatelliteStatusTest() {
        String satelliteId = createSatellite();

        Response response = apiClient.get("/api/satellites/" + satelliteId + "/status");
        response.then().spec(expectedStatusCode(200));
        Assertions.assertNotNull(response.jsonPath().getString("id"));
    }

    @Test
    @DisplayName("[200] POST /api/constellations/{name}/mission - выполнение миссии группировки")
    void executeConstellationMissionTest() {
        String constellationName = createConstellation();

        Response response = apiClient.post("/api/constellations/" + constellationName + "/mission");
        response.then().spec(expectedStatusCode(200));
    }

    @Test
    @DisplayName("[200] POST /api/constellations/{name}/activate - активация группировки")
    void activateConstellationTest() {
        String constellationName = createConstellation();

        Response response = apiClient.post("/api/constellations/" + constellationName + "/activate");
        response.then().spec(expectedStatusCode(200));
    }

    @Test
    @DisplayName("[200] GET /api/constellations/{name}/status - получение статуса группировки")
    void getConstellationStatusTest() {
        String constellationName = createConstellation();

        Response response = apiClient.get("/api/constellations/" + constellationName + "/status");
        response.then().spec(expectedStatusCode(200));
        Assertions.assertNotNull(response.jsonPath().getString("id"));
    }

    @Test
    @DisplayName("[200] DELETE /api/constellations/{constellationName}/satellites/{satelliteName} - вывод спутника из эксплуатации")
    void decommissionSatelliteTest() {
        String constellationName = createConstellation();
        String satelliteName = "Спутник-вывод-" + UUID.randomUUID();

        Map<String, Object> addBody = new HashMap<>();
        addBody.put("constellationName", constellationName);
        List<Map<String, Object>> params = new ArrayList<>();
        Map<String, Object> satParam = new HashMap<>();
        satParam.put("type", "IMAGE");
        satParam.put("name", satelliteName);
        satParam.put("batteryLevel", 1);
        satParam.put("resolution", 250);
        params.add(satParam);
        addBody.put("satelliteParams", params);
        apiClient.post("/api/add-satellites", addBody);

        Response response = apiClient.delete("/api/constellations/" + constellationName + "/satellites/" + satelliteName);
        response.then().spec(expectedStatusCodeNoContent(204));

        createdConstellationNames.remove(constellationName);
    }

}
