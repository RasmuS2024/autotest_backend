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

class ConstellationControllerTest extends BaseTest {

    private final List<String> createdConstellationNames = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        for (String name : createdConstellationNames) {
            apiClient.delete("/api/constellations/" + name);
        }
    }

    @Test
    @DisplayName("[200] GET /api/constellations - получение списка группировок")
    void getConstellationsTest() {
        Response response = apiClient.get("/api/constellations");
        response.then().spec(expectedStatusCode(200));
    }

    @Test
    @DisplayName("[200] POST /api/constellations - создание группировки")
    void postConstellationTest() {
        String constellationName = "Группировка-" + UUID.randomUUID();
        Map<String, Object> body = new HashMap<>();
        body.put("name", constellationName);

        Response response = apiClient.post("/api/constellations", body);
        response.then().spec(expectedStatusCode(200));

        String name = response.jsonPath().getString("name");
        Assertions.assertEquals(constellationName, name);

        String id = response.jsonPath().getString("id");
        Assertions.assertNotNull(id);

        createdConstellationNames.add(constellationName);
    }

    @Test
    @DisplayName("[200] GET /api/constellations/{name} - получение группировки по имени")
    void getConstellationByNameTest() {
        String constellationName = "Группировка-" + UUID.randomUUID();
        Map<String, Object> body = new HashMap<>();
        body.put("name", constellationName);

        Response createResponse = apiClient.post("/api/constellations", body);
        createdConstellationNames.add(constellationName);

        Response response = apiClient.get("/api/constellations/" + constellationName);
        response.then().spec(expectedStatusCode(200));
        Assertions.assertEquals(constellationName, response.jsonPath().getString("name"));
    }

    @Test
    @DisplayName("[204] DELETE /api/constellations/{name} - удаление группировки")
    void deleteConstellationTest() {
        String constellationName = "Группировка-" + UUID.randomUUID();
        Map<String, Object> body = new HashMap<>();
        body.put("name", constellationName);

        Response createResponse = apiClient.post("/api/constellations", body);

        Response response = apiClient.delete("/api/constellations/" + constellationName);
        response.then().spec(expectedStatusCodeNoContent(204));
    }

    @Test
    @DisplayName("[200] PATCH /api/constellations/{name} - переименование группировки")
    void renameConstellationTest() {
        String constellationName = "Группировка-" + UUID.randomUUID();
        String newName = "Группировка-переименована-" + UUID.randomUUID();

        Map<String, Object> createBody = new HashMap<>();
        createBody.put("name", constellationName);
        apiClient.post("/api/constellations", createBody);
        createdConstellationNames.add(constellationName);

        Map<String, Object> patchBody = new HashMap<>();
        patchBody.put("newName", newName);

        Response response = apiClient.patch("/api/constellations/" + constellationName, patchBody);
        response.then().spec(expectedStatusCode(200));

        String responseName = response.jsonPath().getString("name");
        Assertions.assertEquals(newName, responseName);

        createdConstellationNames.remove(constellationName);
        createdConstellationNames.add(newName);
    }

}
