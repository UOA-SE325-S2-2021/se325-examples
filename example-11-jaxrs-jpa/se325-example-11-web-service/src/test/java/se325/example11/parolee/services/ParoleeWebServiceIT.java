package se325.example11.parolee.services;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import se325.example11.parolee.dto.Parolee;
import se325.example11.parolee.dto.ParoleViolation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        Parolee zoran = new Parolee("Salcic", "Zoran", Gender.MALE,
                LocalDate.of(1958, 5, 17), homeAddress, null);

        Response response = CLIENT
                .target(WEB_SERVICE_URI).request()
                .post(Entity.json(zoran));
        if (response.getStatus() != 201) {
            fail("Failed to create new Parolee");
        }

        String location = response.getLocation().toString();
        response.close();

        // Query the Web service for the new Parolee.
        Parolee zoranFromService = CLIENT.target(location).request()
                .accept(MediaType.APPLICATION_JSON).get(Parolee.class);

        // The original local Parolee object (zoran) should have a value equal
        // to that of the Parolee object representing Zoran that is later
        // queried from the Web service. The only exception is the value
        // returned by getId(), because the Web service assigns this when it
        // creates a Parolee.
        assertEquals(zoran.getLastName(), zoranFromService.getLastName());
        assertEquals(zoran.getFirstName(), zoranFromService.getFirstName());
        Assert.assertEquals(zoran.getGender(), zoranFromService.getGender());
        assertEquals(zoran.getDateOfBirth(), zoranFromService.getDateOfBirth());
        assertEquals(zoran.getHomeAddress(), zoranFromService.getHomeAddress());
        Assert.assertEquals(zoran.getCurfew(), zoranFromService.getCurfew());
        Assert.assertEquals(zoran.getLastKnownPosition(),
                zoranFromService.getLastKnownPosition());
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
        Parolee oliver = CLIENT
                .target(WEB_SERVICE_URI + "/1").request()
                .accept(MediaType.APPLICATION_JSON).get(Parolee.class);

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
        Parolee catherine = CLIENT.target(targetUri).request()
                .accept(MediaType.APPLICATION_JSON).get(Parolee.class);

        Address originalAddress = catherine.getHomeAddress();
        assertNull(catherine.getCurfew());

        // Update some of Catherine's details.
        Address newAddress = new Address("40", "Clifton Road", "Herne Bay",
                "Auckland", "1022");
        catherine.setHomeAddress(newAddress);
        Curfew newCurfew = new Curfew(newAddress, LocalTime.of(21, 00),
                LocalTime.of(7, 00));
        catherine.setCurfew(newCurfew);

        Response response = CLIENT.target(targetUri).request()
                .put(Entity.json(catherine));
        if (response.getStatus() != 204)
            fail("Failed to update Parolee");
        response.close();

        // Requery Parolee from the Web service.
        Parolee updatedCatherine = CLIENT.target(targetUri).request()
                .accept(MediaType.APPLICATION_JSON).get(Parolee.class);

        // Parolee's home address should have changed.
        assertFalse(originalAddress.equals(updatedCatherine.getHomeAddress()));
        assertEquals(newAddress, updatedCatherine.getHomeAddress());

        // Curfew should now be set.
        Assert.assertEquals(newCurfew, updatedCatherine.getCurfew());
    }

    /**
     * Tests that the Web service can add dissassociates to a Parolee.
     */
    @Test
    public void updateDissassociates() {
        // Query Parolee Catherine from the Web service.
        Parolee catherine = CLIENT
                .target(WEB_SERVICE_URI + "/2").request()
                .accept(MediaType.APPLICATION_JSON).get(Parolee.class);

        // Query a second Parolee, Nasser.
        Parolee nasser = CLIENT
                .target(WEB_SERVICE_URI + "/3").request()
                .accept(MediaType.APPLICATION_JSON).get(Parolee.class);

        // Query Catherines's dissassociates.
        Set<Parolee> dissassociates = CLIENT
                .target(WEB_SERVICE_URI + "/1/dissassociates")
                .request().accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<Set<Parolee>>() {
                });

        // Catherine should not yet have any recorded dissassociates.
        assertTrue(dissassociates.isEmpty());

        // Request that Nasser is added as a dissassociate to Catherine.
        // Because an object of a parameterized type is being sent to the Web
        // service, it must be wrapped in a GenericEntity, so that the generic
        // type information necessary for marshalling is preserved.
        dissassociates.add(nasser);
        GenericEntity<Set<Parolee>> entity = new GenericEntity<Set<Parolee>>(
                dissassociates) {
        };

        Response response = CLIENT
                .target(WEB_SERVICE_URI + "/1/dissassociates")
                .request().put(Entity.json(entity));
        if (response.getStatus() != 204)
            fail("Failed to update Parolee");
        response.close();

        // Requery Catherine's dissassociates. The GET request is expected to
        // return a List<Parolee> object; since this is a parameterized type, a
        // GenericType wrapper is required so that the data can be
        // unmarshalled.
        Set<Parolee> updatedDissassociates = CLIENT
                .target(WEB_SERVICE_URI + "/1/dissassociates")
                .request().accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<Set<Parolee>>() {
                });
        // The Set of Parolees returned in response to the request for
        // Catherine's dissassociates should contain one object with the same
        // state (value) as the Parolee instance representing Nasser.
        assertTrue(updatedDissassociates.contains(nasser));
        assertEquals(1, updatedDissassociates.size());
    }

    @Test
    public void updateCriminalProfile() {
        final String targetUri = WEB_SERVICE_URI + "/1/criminal-profile";

        CriminalProfile profileForOliver = CLIENT.target(targetUri).request()
                .accept(MediaType.APPLICATION_JSON).get(CriminalProfile.class);

        // Amend the criminal profile.
        profileForOliver.addConviction(new Conviction(
                LocalDate.now(), "Shoplifting", Offence.THEFT));

        // Send a Web service request to update the profile.
        Response response = CLIENT.target(targetUri).request()
                .put(Entity.json(profileForOliver));
        if (response.getStatus() != 204)
            fail("Failed to update CriminalProfile");
        response.close();

        // Requery Oliver's criminal profile.
        CriminalProfile reQueriedProfile = CLIENT.target(targetUri).request()
                .accept(MediaType.APPLICATION_JSON).get(CriminalProfile.class);

        // The locally updated copy of Oliver's CriminalProfile should have
        // the same value as the updated profile obtained from the Web service.
        assertEquals(profileForOliver, reQueriedProfile);
    }

    /**
     * Tests that the Web service can handle requests to query a particular
     * Parolee.
     */
    @Test
    public void queryParolee() {
        Parolee parolee = CLIENT
                .target(WEB_SERVICE_URI + "/1").request()
                .accept(MediaType.APPLICATION_JSON).get(Parolee.class);

        assertEquals(1, parolee.getId());
        assertEquals("Sinnen", parolee.getLastName());
    }

    /**
     * Similar to queryParolee(), but this method retrieves the Parolee using
     * via a Response object. Because a Response object is used, headers and
     * other HTTP response message data can be examined.
     */
    @Test
    public void queryParoleeWithResponse() {
        Response response = CLIENT
                .target(WEB_SERVICE_URI + "/1").request().get();
        Parolee parolee = response.readEntity(Parolee.class);

        // Get the headers and print them out.
        MultivaluedMap<String, Object> headers = response.getHeaders();
        LOGGER.info("Dumping HTTP response message headers ...");
        for (String key : headers.keySet()) {
            LOGGER.info(key + ": " + headers.getFirst(key));
        }
        response.close();
    }

    /**
     * Tests that the Web service processes requests for all Parolees.
     */
    @Test
    public void queryAllParolees() {
        List<Parolee> parolees = CLIENT
                .target(WEB_SERVICE_URI + "?start=1&size=3").request()
                .accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Parolee>>() {
                });
        assertEquals(3, parolees.size());
    }

    /**
     * Tests that the Web service processes requests for Parolees using header
     * links for HATEOAS.
     */
    @Test
    public void queryAllParoleesUsingHATEOAS() {
        // Make a request for Parolees (note that the Web service has default
        // values of 1 for the query parameters start and size.
        Response response = CLIENT
                .target(WEB_SERVICE_URI).request().get();

        // Extract links and entity data from the response.
        Link previous = response.getLink("prev");
        Link next = response.getLink("next");
        List<Parolee> parolees = response.readEntity(new GenericType<List<Parolee>>() {
        });
        response.close();

        // The Web service should respond with a list containing only the
        // first Parolee.
        assertEquals(1, parolees.size());
        assertEquals(1, parolees.get(0).getId());

        // Having requested the only the first parolee (by default), the Web
        // service should respond with a Next link, but not a previous Link.
        assertNull(previous);
        assertNotNull(next);

        // Invoke next link and extract response data.
        response = CLIENT
                .target(next).request().get();
        previous = response.getLink("prev");
        next = response.getLink("next");
        parolees = response.readEntity(new GenericType<List<Parolee>>() {
        });
        response.close();

        // The second Parolee should be returned along with Previous and Next
        // links to the adjacent Parolees.
        assertEquals(1, parolees.size());
        assertEquals(2, parolees.get(0).getId());
        assertEquals("<" + WEB_SERVICE_URI + "?start=1&size=1>; rel=\"prev\"", previous.toString());
        assertNotNull("<" + WEB_SERVICE_URI + "?start=1&size=1>; rel=\"prev\"", next.toString());
    }

    /**
     * Tests that the Web service can process requests for a particular
     * Parolee's movements.
     */
    @Test
    public void queryParoleeMovements() {
        List<Movement> movementsForOliver = CLIENT
                .target(WEB_SERVICE_URI + "/1/movements")
                .request().accept(MediaType.APPLICATION_JSON)
                .get(new GenericType<List<Movement>>() {
                });

        // Oliver has 3 recorded movements.
        assertEquals(3, movementsForOliver.size());
    }

    /**
     * Tests that the Web service can accept subscriptions for parole violations, and that subscribers are notified
     * when such a violation occurs.
     */
    @Test
    public void testSubscribeToParoleeViolation() throws InterruptedException, ExecutionException, TimeoutException {

        // Create an async request to the subscription service and set it going
        Future<Response> future = CLIENT.target(WEB_SERVICE_URI + "/subscribeParoleViolations")
                .request().async().get();

        // Create and start the thread that will POST the violating movement after 1s.
        Thread movementThread = createParoleeMovementThread();
        movementThread.start();

        // Wait for the published violation for five seconds max. If none received, fail.
        Response response = future.get(5, TimeUnit.SECONDS);

        // Check details are correct.
        assertEquals(200, response.getStatus());
        ParoleViolation violation = response.readEntity(ParoleViolation.class);
        assertEquals(1L, violation.getParoleeId());
        assertEquals(-36.870618, violation.getLocation().getLatitude(), 1e-10);
        assertEquals(174.772172, violation.getLocation().getLongitude(), 1e-10);
    }

    // Create a thread that will send a parolee movement to the server after one second.
    private Thread createParoleeMovementThread() {
        return new Thread(() -> {

            try {

                // Wait for a bit, to give the subscription request time to get through.
                Thread.sleep(1000);

                // Create offending movement
                long paroleeId = 1;
                GeoPosition movementPosition = new GeoPosition(-36.870618, 174.772172);
                LocalDate movementDate = LocalDate.now();
                LocalTime movementTime = LocalTime.of(22, 00);
                LocalDateTime movementTimestamp = LocalDateTime.of(movementDate, movementTime);
                Movement movement = new Movement(movementTimestamp, movementPosition);

                Client paroleeMovementClient = ClientBuilder.newClient();
                paroleeMovementClient.target(WEB_SERVICE_URI + "/1/movements")
                        .request().post(Entity.json(movement));

                paroleeMovementClient.close();

            } catch (Exception ex) {
                ex.printStackTrace();
            }

        });
    }
}
