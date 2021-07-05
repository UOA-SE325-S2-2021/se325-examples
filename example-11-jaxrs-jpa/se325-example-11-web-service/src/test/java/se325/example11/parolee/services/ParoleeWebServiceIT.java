package se325.example11.parolee.services;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;

import org.junit.*;
import se325.example11.parolee.domain.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se325.example11.parolee.dto.ParoleeDTO;

import static org.junit.Assert.*;

public class ParoleeWebServiceIT {
    private static final String WEB_SERVICE_URI = "http://localhost:10000/services/parolees";

    private static final Logger LOGGER = LoggerFactory.getLogger(ParoleeWebServiceIT.class);

    private static Client CLIENT;

    /**
     * One-time setup method that creates a Web service CLIENT.
     */
    @BeforeClass
    public static void setUpClient() {
        CLIENT = ClientBuilder.newClient();
    }

    /**
     * Runs before each unit test to restore Web service database. This ensures
     * that each test is independent; each test runs on a Web service that has
     * been initialised with a common set of Parolees.
     */
    @Before
    public void reloadServerData() {
        Response response = CLIENT
                .target(WEB_SERVICE_URI).request()
                .put(null);
        response.close();

        // Pause briefly before running any tests. Test addParoleeMovement(),
        // for example, involves creating a timestamped value (a movement) and
        // having the Web service compare it with data just generated with
        // timestamps. Joda's Datetime class has only millisecond precision,
        // so pause so that test-generated timestamps are actually later than
        // timestamped values held by the Web service.
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
        }
    }

    /**
     * One-time finalisation method that destroys the Web service CLIENT.
     */
    @AfterClass
    public static void destroyClient() {
        CLIENT.close();
    }

    /**
     * Tests that the Web service can create a new Parolee.
     */
    @Test
    public void addParolee() {
        Address homeAddress = new Address("34", "Appleby Road", "Remuera",
                "Auckland", "1070");
        ParoleeDTO zoran = new ParoleeDTO("Salcic", "Zoran", Gender.MALE,
                LocalDate.of(1958, 5, 17), homeAddress);

        Response response = CLIENT
                .target(WEB_SERVICE_URI).request()
                .post(Entity.json(zoran));

        if (response.getStatus() != 201) {
            fail("Failed to create new Parolee");
        }

        String location = response.getLocation().toString();
        response.close();

        // Query the Web service for the new Parolee.
        ParoleeDTO zoranFromService = CLIENT.target(location).request()
                .accept(MediaType.APPLICATION_JSON).get(ParoleeDTO.class);

        // The original local Parolee object (zoran) should have a value equal
        // to that of the Parolee object representing Zoran that is later
        // queried from the Web service. The only exception is the value
        // returned by getId(), because the Web service assigns this when it
        // creates a Parolee.
        assertEquals(zoran.getLastName(), zoranFromService.getLastName());
        assertEquals(zoran.getFirstName(), zoranFromService.getFirstName());
        assertEquals(zoran.getGender(), zoranFromService.getGender());
        assertEquals(zoran.getDateOfBirth(), zoranFromService.getDateOfBirth());
        assertEquals(zoran.getHomeAddress(), zoranFromService.getHomeAddress());
        assertNull(zoranFromService.getLastKnownPosition());
    }

    /**
     * Tests that the Web serves can process requests to record new Parolee
     * movements.
     */
    @Test
    public void addParoleeMovement() {
        LocalDateTime now = LocalDateTime.now();
        Movement newLocation = new Movement(now, new GeoPosition(
                -36.848238, 174.762212));

        Response response = CLIENT
                .target(WEB_SERVICE_URI + "/1/movements")
                .request().post(Entity.json(newLocation));
        if (response.getStatus() != 204) {
            fail("Failed to create new Movement");
        }
        response.close();

        // Query the Web service for the Parolee whose location has been
        // updated.
        ParoleeDTO oliver = CLIENT
                .target(WEB_SERVICE_URI + "/1").request()
                .accept(MediaType.APPLICATION_JSON).get(ParoleeDTO.class);

        // Check that the Parolee's location was updated.
        Assert.assertEquals(newLocation, oliver.getLastKnownPosition());
    }

    /**
     * Tests that the Web service can process Parolee update requests.
     */
    @Test
    public void updateParolee() {
        final String targetUri = WEB_SERVICE_URI + "/2";

        // Query a Parolee (Catherine) from the Web service.
        ParoleeDTO catherine = CLIENT.target(targetUri).request()
                .accept(MediaType.APPLICATION_JSON).get(ParoleeDTO.class);

        Address originalAddress = catherine.getHomeAddress();

        // Update some of Catherine's details.
        Address newAddress = new Address("40", "Clifton Road", "Herne Bay",
                "Auckland", "1022");
        catherine.setHomeAddress(newAddress);

        Response response = CLIENT.target(targetUri).request()
                .put(Entity.json(catherine));

        if (response.getStatus() != 204) {
            fail("Failed to update Parolee");
        }
        response.close();

        // Requery Parolee from the Web service.
        ParoleeDTO updatedCatherine = CLIENT.target(targetUri).request()
                .accept(MediaType.APPLICATION_JSON).get(ParoleeDTO.class);

        // Parolee's home address should have changed.
        assertNotEquals(originalAddress, updatedCatherine.getHomeAddress());
        assertEquals(newAddress, updatedCatherine.getHomeAddress());
    }

    /**
     * Tests that the Web service can add disassociates to a Parolee.
     */
    @Test
    public void updateDisassociates() {
        // Query Parolee Catherine from the Web service.
//        ParoleeDTO catherine = CLIENT
//                .target(WEB_SERVICE_URI + "/2").request()
//                .accept(MediaType.APPLICATION_JSON).get(ParoleeDTO.class);

        // Query Catherines's disassociates.
        List<ParoleeDTO> disassociates = CLIENT
                .target(WEB_SERVICE_URI + "/2/disassociates")
                .request().accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<>() {
                });

        // Catherine should not yet have any recorded disassociates.
        assertTrue(disassociates.isEmpty());

        // Request that Nasser is added as a dissassociate to Catherine.
        Set<Long> newDisassociates = new HashSet<>();
        newDisassociates.add(3L);
        GenericEntity<Set<Long>> entity = new GenericEntity<>(newDisassociates) {
        };

        Response response = CLIENT
                .target(WEB_SERVICE_URI + "/2/disassociates")
                .request().put(Entity.json(entity));

        if (response.getStatus() != 204) {
            fail("Failed to update Parolee");
        }
        response.close();

        // Requery Catherine's dissassociates. The GET request is expected to
        // return a List<ParoleeDTO> object; since this is a parameterized type, a
        // GenericType wrapper is required so that the data can be unmarshalled.
        List<ParoleeDTO> updatedDisassociates = CLIENT
                .target(WEB_SERVICE_URI + "/2/disassociates")
                .request().accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<>() {
                });

        // The Set of Parolees returned in response to the request for
        // Catherine's dissassociates should contain one object with the same
        // state (value) as the Parolee instance representing Nasser.
        assertEquals(1, updatedDisassociates.size());
        assertEquals("Nasser", updatedDisassociates.get(0).getFirstName());

    }

    @Test
    public void updateConvictions() {
        final String targetUri = WEB_SERVICE_URI + "/1/convictions";

        List<Conviction> convictionsForOliver = CLIENT.target(targetUri).request()
                .accept(MediaType.APPLICATION_JSON).get(new GenericType<>() {
                });

        // Amend the criminal profile.
        convictionsForOliver.add(new Conviction(
                LocalDate.now(), "Shoplifting", Offence.THEFT));

        // Send a Web service request to update the profile.
        Response response = CLIENT.target(targetUri).request()
                .put(Entity.json(convictionsForOliver));

        if (response.getStatus() != 204) {
            fail("Failed to update CriminalProfile");
        }
        response.close();

        // Requery Oliver's criminal profile.
        List<Conviction> reQueriedConvictions = CLIENT.target(targetUri).request()
                .accept(MediaType.APPLICATION_JSON).get(new GenericType<>() {
                });

        // The locally updated copy of Oliver's CriminalProfile should have
        // the same value as the updated profile obtained from the Web service.
        assertEquals(convictionsForOliver.size(), reQueriedConvictions.size());
        for (int i = 0; i < convictionsForOliver.size(); i++) {
            assertEquals(convictionsForOliver.get(i), reQueriedConvictions.get(i));
        }
    }

//    /**
//     * Tests that the Web service can handle requests to query a particular
//     * Parolee.
//     */
//    @Test
//    public void queryParolee() {
//        Parolee parolee = CLIENT
//                .target(WEB_SERVICE_URI + "/1").request()
//                .accept(MediaType.APPLICATION_JSON).get(Parolee.class);
//
//        assertEquals(1, parolee.getId());
//        assertEquals("Sinnen", parolee.getLastName());
//    }
//
//    /**
//     * Similar to queryParolee(), but this method retrieves the Parolee using
//     * via a Response object. Because a Response object is used, headers and
//     * other HTTP response message data can be examined.
//     */
//    @Test
//    public void queryParoleeWithResponse() {
//        Response response = CLIENT
//                .target(WEB_SERVICE_URI + "/1").request().get();
//        Parolee parolee = response.readEntity(Parolee.class);
//
//        // Get the headers and print them out.
//        MultivaluedMap<String, Object> headers = response.getHeaders();
//        LOGGER.info("Dumping HTTP response message headers ...");
//        for (String key : headers.keySet()) {
//            LOGGER.info(key + ": " + headers.getFirst(key));
//        }
//        response.close();
//    }

//    /**
//     * Tests that the Web service processes requests for all Parolees.
//     */
//    @Test
//    public void queryAllParolees() {
//        List<Parolee> parolees = CLIENT
//                .target(WEB_SERVICE_URI + "?start=1&size=3").request()
//                .accept(MediaType.APPLICATION_JSON)
//                .get(new GenericType<List<Parolee>>() {
//                });
//        assertEquals(3, parolees.size());
//    }
//
//    /**
//     * Tests that the Web service processes requests for Parolees using header
//     * links for HATEOAS.
//     */
//    @Test
//    public void queryAllParoleesUsingHATEOAS() {
//        // Make a request for Parolees (note that the Web service has default
//        // values of 1 for the query parameters start and size.
//        Response response = CLIENT
//                .target(WEB_SERVICE_URI).request().get();
//
//        // Extract links and entity data from the response.
//        Link previous = response.getLink("prev");
//        Link next = response.getLink("next");
//        List<Parolee> parolees = response.readEntity(new GenericType<List<Parolee>>() {
//        });
//        response.close();
//
//        // The Web service should respond with a list containing only the
//        // first Parolee.
//        assertEquals(1, parolees.size());
//        assertEquals(1, parolees.get(0).getId());
//
//        // Having requested the only the first parolee (by default), the Web
//        // service should respond with a Next link, but not a previous Link.
//        assertNull(previous);
//        assertNotNull(next);
//
//        // Invoke next link and extract response data.
//        response = CLIENT
//                .target(next).request().get();
//        previous = response.getLink("prev");
//        next = response.getLink("next");
//        parolees = response.readEntity(new GenericType<List<Parolee>>() {
//        });
//        response.close();
//
//        // The second Parolee should be returned along with Previous and Next
//        // links to the adjacent Parolees.
//        assertEquals(1, parolees.size());
//        assertEquals(2, parolees.get(0).getId());
//        assertEquals("<" + WEB_SERVICE_URI + "?start=1&size=1>; rel=\"prev\"", previous.toString());
//        assertNotNull("<" + WEB_SERVICE_URI + "?start=1&size=1>; rel=\"prev\"", next.toString());
//    }
//
//    /**
//     * Tests that the Web service can process requests for a particular
//     * Parolee's movements.
//     */
//    @Test
//    public void queryParoleeMovements() {
//        List<Movement> movementsForOliver = CLIENT
//                .target(WEB_SERVICE_URI + "/1/movements")
//                .request().accept(MediaType.APPLICATION_JSON)
//                .get(new GenericType<List<Movement>>() {
//                });
//
//        // Oliver has 3 recorded movements.
//        assertEquals(3, movementsForOliver.size());
//    }
//
//    /**
//     * Tests that the Web service can accept subscriptions for parole violations, and that subscribers are notified
//     * when such a violation occurs.
//     */
//    @Test
//    public void testSubscribeToParoleeViolation() throws InterruptedException, ExecutionException, TimeoutException {
//
//        // Create an async request to the subscription service and set it going
//        Future<Response> future = CLIENT.target(WEB_SERVICE_URI + "/subscribeParoleViolations")
//                .request().async().get();
//
//        // Create and start the thread that will POST the violating movement after 1s.
//        Thread movementThread = createParoleeMovementThread();
//        movementThread.start();
//
//        // Wait for the published violation for five seconds max. If none received, fail.
//        Response response = future.get(5, TimeUnit.SECONDS);
//
//        // Check details are correct.
//        assertEquals(200, response.getStatus());
//        ParoleViolation violation = response.readEntity(ParoleViolation.class);
//        assertEquals(1L, violation.getParoleeId());
//        assertEquals(-36.870618, violation.getLocation().getLatitude(), 1e-10);
//        assertEquals(174.772172, violation.getLocation().getLongitude(), 1e-10);
//    }
//
//    // Create a thread that will send a parolee movement to the server after one second.
//    private Thread createParoleeMovementThread() {
//        return new Thread(() -> {
//
//            try {
//
//                // Wait for a bit, to give the subscription request time to get through.
//                Thread.sleep(1000);
//
//                // Create offending movement
//                long paroleeId = 1;
//                GeoPosition movementPosition = new GeoPosition(-36.870618, 174.772172);
//                LocalDate movementDate = LocalDate.now();
//                LocalTime movementTime = LocalTime.of(22, 00);
//                LocalDateTime movementTimestamp = LocalDateTime.of(movementDate, movementTime);
//                Movement movement = new Movement(movementTimestamp, movementPosition);
//
//                Client paroleeMovementClient = ClientBuilder.newClient();
//                paroleeMovementClient.target(WEB_SERVICE_URI + "/1/movements")
//                        .request().post(Entity.json(movement));
//
//                paroleeMovementClient.close();
//
//            } catch (Exception ex) {
//                ex.printStackTrace();
//            }
//
//        });
//    }
}
