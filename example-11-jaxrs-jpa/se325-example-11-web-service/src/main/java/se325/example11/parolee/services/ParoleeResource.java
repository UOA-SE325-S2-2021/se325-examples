package se325.example11.parolee.services;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import javax.persistence.EntityManager;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se325.example11.parolee.domain.*;
import se325.example11.parolee.dto.ParoleViolation;
import se325.example11.parolee.utils.GeoUtils;

/**
 * Web service resource implementation for the Parolee application. An instance
 * of this class handles all HTTP requests for the Parolee Web service.
 */
@Path("/parolees")
public class ParoleeResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParoleeResource.class);

    private Map<Long, Parolee> paroleeDB;
    private AtomicLong idCounter;

    // Stores pending AsyncResponse objects representing subscriptions for parole violations.
    private final List<AsyncResponse> paroleViolationSubscriptions = new Vector<>();

    public ParoleeResource() {
        reloadDatabase();
    }

    /**
     * Convenience method for testing the Web service. This method reinitialises
     * the state of the Web service to hold three Parolee objects.
     */
    @PUT
    public void reloadData() {
        reloadDatabase();
    }

    /**
     * Adds a new Parolee to the system. The state of the new Parolee is
     * described by a Parolee object.
     *
     * @param dtoParolee the Parolee data included in the HTTP request body.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createParolee(
            se325.example11.parolee.dto.Parolee dtoParolee) {

//        EntityManager em = PersistenceManager.instance().createEntityManager();
//        em.close();

        Parolee parolee = ParoleeMapper.toDomainModel(dtoParolee);
        parolee.setId(idCounter.incrementAndGet());
        paroleeDB.put(parolee.getId(), parolee);

        // Return a Response that specifies a status code of 201 Created along
        // with the Location header set to the URI of the newly created Parolee.
        return Response.created(URI.create("/parolees/" + parolee.getId()))
                .build();
    }

    /**
     * Records a new Movement for a particular Parolee.
     *
     * @param id       the unique identifier of the Parolee.
     * @param movement the timestamped latitude/longitude position of the Parolee.
     */
    @POST
    @Path("{id}/movements")
    @Consumes(MediaType.APPLICATION_JSON)
    public void createMovementForParolee(@PathParam("id") long id,
                                         Movement movement) {
        Parolee parolee = findParolee(id);
        parolee.addMovement(movement);

        processParoleViolation(parolee, movement);

        // JAX-RS will add the default response code to the HTTP response
        // message.
    }

    /**
     * Updates an existing Parolee. The parts of a Parolee that can be updated
     * are those represented by a Parolee
     * instance.
     *
     * @param dtoParolee the Parolee data included in the HTTP request body.
     */
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateParolee(
            se325.example11.parolee.dto.Parolee dtoParolee) {
        // Get the Parolee object from the database.
        Parolee parolee = findParolee(dtoParolee.getId());

        // Update the Parolee object in the database based on the data in
        // dtoParolee.
        parolee.setFirstName(dtoParolee.getFirstName());
        parolee.setLastName(dtoParolee.getLastName());
        parolee.setGender(dtoParolee.getGender());
        parolee.setDateOfBirth(dtoParolee.getDateOfBirth());
        parolee.setHomeAddress(dtoParolee.getHomeAddress());
        parolee.setCurfew(dtoParolee.getCurfew());

        // JAX-RS will add the default response code (204 No Content) to the
        // HTTP response message.
    }

    /**
     * Updates the set of a dissassociate Parolees for a given Parolee.
     *
     * @param id             the Parolee whose dissassociates should be updated.
     * @param dissassociates the new set of dissassociates.
     */
    @PUT
    @Path("{id}/dissassociates")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateDissassociates(@PathParam("id") long id, Set<se325.example11.parolee.dto.Parolee> dissassociates) {
        // Get the Parolee object from the database.
        Parolee parolee = findParolee(id);

        // Lookup the dissassociate Parolee instances in the database.
        Set<Parolee> dissassociatesInDatabase = new HashSet<>();
        for (se325.example11.parolee.dto.Parolee dtoParolee : dissassociates) {
            Parolee dissassociate = findParolee(dtoParolee.getId());
            dissassociatesInDatabase.add(dissassociate);
        }

        // Update the Parolee by setting its dissassociates.
        parolee.updateDissassociates(dissassociatesInDatabase);

        // JAX-RS will add the default response code (204 No Content) to the
        // HTTP response message.
    }

    /**
     * Updates a Parolee's CriminalProfile.
     *
     * @param id      the unique identifier of the Parolee.
     * @param profile the Parolee's updated criminal profile.
     */
    @PUT
    @Path("{id}/criminal-profile")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateCriminalProfile(@PathParam("id") long id, CriminalProfile profile) {
        // Get the Parolee object from the database.
        Parolee parolee = findParolee(id);

        // Update the Parolee's criminal profile.
        parolee.setCriminalProfile(profile);

        // JAX-RS will add the default response code (204 No Content) to the
        // HTTP response message.
    }

    /**
     * Returns a particular Parolee. The returned Parolee is represented by a
     * Parolee object.
     *
     * @param id the unique identifier of the Parolee.
     */
    @GET
    @Path("{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public se325.example11.parolee.dto.Parolee getParolee(
            @PathParam("id") long id) {
        // Get the Parolee object from the database.
        Parolee parolee = findParolee(id);

        // Convert the Parolee to a Parolee DTO.
        se325.example11.parolee.dto.Parolee dtoParolee = ParoleeMapper.toDto(parolee);

        // JAX-RS will processed the returned value, marshalling it and storing
        // it in the HTTP response message body. It will use the default status
        // code of 200 Ok.
        return dtoParolee;
    }

    /**
     * Returns a view of the Parolee database, represented as a List of
     * Parolee objects.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getParolees(@DefaultValue("1") @QueryParam("start") int start,
                                @DefaultValue("1") @QueryParam("size") int size,
                                @Context UriInfo uriInfo) {
        URI uri = uriInfo.getAbsolutePath();

        Link previous = null;
        Link next = null;

        if (start > 1) {
            // There are previous Parolees - create a previous link.
            previous = Link.fromUri(uri + "?start={start}&size={size}")
                    .rel("prev")
                    .build(start - 1, size);
        }
        if (start + size <= paroleeDB.size()) {
            // There are successive parolees - create a next link.
            next = Link.fromUri(uri + "?start={start}&size={size}")
                    .rel("next")
                    .build(start + 1, size);
        }

        // Create list of Parolees to return.
        List<se325.example11.parolee.dto.Parolee> parolees =
                new ArrayList<>();
        long paroleeId = start;
        for (int i = 0; i < size; i++) {
            Parolee parolee = paroleeDB.get(paroleeId);
            parolees.add(ParoleeMapper.toDto(parolee));
        }

        // Create a GenericEntity to wrap the list of Parolees to return. This
        // is necessary to preserve generic type data when using any
        // MessageBodyWriter to handle translation to a particular data format.
        GenericEntity<List<se325.example11.parolee.dto.Parolee>> entity =
                new GenericEntity<List<se325.example11.parolee.dto.Parolee>>(parolees) {
                };

        // Build a Response that contains the list of Parolees plus the link
        // headers.
        ResponseBuilder builder = Response.ok(entity);
        if (previous != null) {
            builder.links(previous);
        }
        if (next != null) {
            builder.links(next);
        }
        Response response = builder.build();

        // Return the custom Response. The JAX-RS run-time will process this,
        // extracting the List of Parolee objects and marshalling them into the
        // HTTP response message body. In addition, since the Response object
        // contains headers (previous and/or next), these will be added to the
        // HTTP response message. The Response object was created with the 200
        // Ok status code, and this too will be added for the status header.
        return response;
    }

    /**
     * Returns movement history for a particular Parolee.
     *
     * @param id the unique identifier of the Parolee.
     */
    @GET
    @Path("{id}/movements")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Movement> getMovements(@PathParam("id") long id) {
        // Get the Parolee object from the database.
        Parolee parolee = findParolee(id);

        // Return the Parolee's movements.
        return parolee.getMovements();

        // JAX-RS will processed the returned value, marshalling it and storing
        // it in the HTTP response message body. It will use the default status
        // code of 200 Ok.
    }

    /**
     * Returns the dissassociates associated directly with a particular Parolee.
     * Each dissassociate is represented as an instance of class
     * Parolee.
     *
     * @param id the unique identifier of the Parolee.
     */
    @GET
    @Path("{id}/dissassociates")
    @Produces(MediaType.APPLICATION_JSON)
    public List<se325.example11.parolee.dto.Parolee> getParoleeDissassociates(
            @PathParam("id") long id) {
        // Get the Parolee object from the database.
        Parolee parolee = findParolee(id);

        List<se325.example11.parolee.dto.Parolee> dissassociates = new ArrayList<>();

        for (Parolee dissassociate : parolee.getDissassociates()) {
            dissassociates.add(ParoleeMapper.toDto(dissassociate));
        }
        return dissassociates;

        // JAX-RS will process the returned value, marshalling it and storing
        // it in the HTTP response message body. It will use the default status
        // code of 200 Ok.
    }

    /**
     * Returns the CriminalProfile for a particular Parolee.
     *
     * @param id the unique identifier of the Parolee.
     */
    @GET
    @Path("{id}/criminal-profile")
    @Produces(MediaType.APPLICATION_JSON)
    public CriminalProfile getCriminalProfile(@PathParam("id") long id) {
        // Get the Parolee object from the database.
        Parolee parolee = findParolee(id);

        return parolee.getCriminalProfile();

        // JAX-RS will processed the returned value, marshalling it and storing
        // it in the HTTP response message body. It will use the default status
        // code of 200 Ok.
    }


    protected Parolee findParolee(long id) {
        return paroleeDB.get(id);
    }

    /**
     * Subscribe to be notified whenever the next parole violation occurs.
     *
     * @param sub
     */
    @GET
    @Path("/subscribeParoleViolations")
    @Produces(MediaType.APPLICATION_JSON)
    public void subscribeToParoleViolations(@Suspended AsyncResponse sub) {
        paroleViolationSubscriptions.add(sub);
    }

    /**
     * Checks if the given movement constitutes a parole violation for the given parolee.
     * If so, lets any subscribers know about the violation.
     * <p>
     *
     * @param parolee
     * @param movement
     */
    private void processParoleViolation(Parolee parolee, Movement movement) {
        if (isParoleViolation(parolee, movement)) {
            notifyParoleViolation(new ParoleViolation(parolee.getId(), movement.getGeoPosition()));
        }
    }

    /**
     * Notifies all subscribers of the given parole violation.
     *
     * @param violation
     */
    private void notifyParoleViolation(ParoleViolation violation) {
        synchronized (paroleViolationSubscriptions) {
            for (AsyncResponse sub : paroleViolationSubscriptions) {
                sub.resume(violation);
            }
            paroleViolationSubscriptions.clear();
        }
    }

    /**
     * Returns a value indicating whether the given movement is a parole violation for the given parolee.
     * <p>
     * Currently, this method only checks whether we're breaking curfew.
     * <p>
     * TODO Also be checking we're not near any disassociates.
     *
     * @param parolee
     * @param movement
     * @return
     */
    private boolean isParoleViolation(Parolee parolee, Movement movement) {

        // Can't have a parole violation if there's no curfew.
        if (parolee.getCurfew() == null) {
            return false;
        }

        Curfew curfew = parolee.getCurfew();
        LocalDate movementDate = movement.getTimestamp().toLocalDate();
        LocalDateTime curfewStart = LocalDateTime.of(movementDate, curfew.getStartTime());
        LocalDateTime curfewEnd = LocalDateTime.of(movementDate, curfew.getEndTime());
        if (curfewEnd.isBefore(curfewStart)) {
            curfewEnd = curfewEnd.plusDays(1);
        }

        boolean withinCurfewTime = (movement.getTimestamp().isAfter(curfewStart) &&
                movement.getTimestamp().isBefore(curfewEnd));

        if (withinCurfewTime) {

            boolean within50Meters = (GeoUtils.calculateDistanceInMeters(
                    curfew.getConfinementAddress().getLocation(), movement.getGeoPosition()) <= 50L);

            // If distance is > 50m, we have a parole violation.
            return !within50Meters;

        }

        // Can't violate parole if the movement is outside of curfew time
        else {
            return false;
        }
    }

    /**
     * Method that adds clears and then adds some dummy data to the "database".
     */
    protected void reloadDatabase() {
        paroleeDB = new ConcurrentHashMap<>();
        idCounter = new AtomicLong();

        synchronized (paroleViolationSubscriptions) {
            for (AsyncResponse sub : paroleViolationSubscriptions) {
                sub.cancel();
            }
            paroleViolationSubscriptions.clear();
        }

        // === Initialise Parolee #1
        long id = idCounter.incrementAndGet();
        ;
        GeoPosition addressLocation = new GeoPosition(-36.865520, 174.859520);
        Address address = new Address("15", "Bermuda road", "St Johns", "Auckland", "1071", addressLocation);
        Parolee parolee = new Parolee(id,
                "Sinnen",
                "Oliver",
                Gender.MALE,
                LocalDate.of(1970, 5, 26),
                address,
                new Curfew(address, LocalTime.of(20, 00), LocalTime.of(06, 30)));
        paroleeDB.put(id, parolee);

        CriminalProfile profile = new CriminalProfile();
        profile.addConviction(new Conviction(LocalDate.of(
                1994, 1, 19), "Crime of passion", Offence.MURDER,
                Offence.POSSESION_OF_OFFENSIVE_WEAPON));
        parolee.setCriminalProfile(profile);

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earlierToday = now.minusHours(1);
        LocalDateTime yesterday = now.minusDays(1);
        GeoPosition position = new GeoPosition(-36.852617, 174.769525);

        parolee.addMovement(new Movement(yesterday, position));
        parolee.addMovement(new Movement(earlierToday, position));
        parolee.addMovement(new Movement(now, position));

        // === Initialise Parolee #2
        id = idCounter.incrementAndGet();
        address = new Address("22", "Tarawera Terrace", "St Heliers", "Auckland", "1071");
        parolee = new Parolee(id,
                "Watson",
                "Catherine",
                Gender.FEMALE,
                LocalDate.of(1970, 2, 9),
                address,
                null);
        paroleeDB.put(id, parolee);

        // === Initialise Parolee #3
        id = idCounter.incrementAndGet();
        address = new Address("67", "Drayton Gardens", "Oraeki", "Auckland", "1071");
        parolee = new Parolee(id,
                "Giacaman",
                "Nasser",
                Gender.MALE,
                LocalDate.of(1980, 10, 19),
                address,
                null);
        paroleeDB.put(id, parolee);
    }
}
