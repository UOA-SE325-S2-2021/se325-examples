package se325.example11.parolee.services;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Link;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import se325.example11.parolee.domain.*;
import se325.example11.parolee.dto.ParoleeDTO;

/**
 * Web service resource implementation for the Parolee application. An instance
 * of this class handles all HTTP requests for the Parolee Web service.
 */
@Path("/parolees")
public class ParoleeResource {

//    private static final Logger LOGGER = LoggerFactory.getLogger(ParoleeResource.class);

    private final ParoleeDB paroleeDB = new ParoleeDB();

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
    public Response createParolee(ParoleeDTO dtoParolee) {

        Parolee domainParolee = dtoParolee.toDomain();
        long id = paroleeDB.addParolee(domainParolee);

        // Return a Response that specifies a status code of 201 Created along
        // with the Location header set to the URI of the newly created Parolee.
        return Response.created(URI.create("/parolees/" + id)).build();
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
    public void createMovementForParolee(@PathParam("id") long id, Movement movement) {
        Parolee parolee = paroleeDB.getParolee(id);
        parolee.addMovement(movement);

        // JAX-RS will add the default response code to the HTTP response message.
    }

    /**
     * Updates an existing Parolee. The parts of a Parolee that can be updated
     * are those represented by a Parolee instance.
     *
     * @param incomingParolee the Parolee data included in the HTTP request body.
     */
    @PUT
    @Path("{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateParolee(ParoleeDTO incomingParolee) {
        // Get the Parolee object from the database.
        Parolee parolee = paroleeDB.getParolee(incomingParolee.getId());

        // Update the Parolee object in the database based on the data in parolee.
        incomingParolee.updateDomain(parolee);

        // JAX-RS will add the default response code (204 No Content) to the HTTP response message.
    }

    /**
     * Updates the set of disassociate Parolees for a given Parolee.
     *
     * @param id              the Parolee whose disassociates should be updated.
     * @param disassociateIds the new set of disassociates.
     */
    @PUT
    @Path("{id}/disassociates")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateDisassociates(@PathParam("id") long id, Set<Long> disassociateIds) {
        // Get the Parolee object from the database.
        Parolee parolee = paroleeDB.getParolee(id);

        Set<Parolee> disassociates = new HashSet<>();
        Parolee disassociate;
        for (Long dId : disassociateIds) {
            if ((disassociate = paroleeDB.getParolee(dId)) != null) {
                disassociates.add(disassociate);
            }
        }
        parolee.setDisassociates(disassociates);

        // JAX-RS will add the default response code (204 No Content) to the HTTP response message.
    }

    /**
     * Updates a Parolee's set of convictions.
     *
     * @param id          the unique identifier of the Parolee.
     * @param convictions the Parolee's updated criminal profile.
     */
    @PUT
    @Path("{id}/convictions")
    @Consumes(MediaType.APPLICATION_JSON)
    public void updateConvictions(@PathParam("id") long id, List<Conviction> convictions) {
        // Get the Parolee object from the database.
        Parolee parolee = paroleeDB.getParolee(id);

        // Update the Parolee's criminal profile.
        parolee.setConvictions(convictions);

        // JAX-RS will add the default response code (204 No Content) to the HTTP response message.
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
    public ParoleeDTO getParolee(@PathParam("id") long id) {
        // Get the Parolee object from the database.
        Parolee parolee = paroleeDB.getParolee(id);

        // JAX-RS will processed the returned value, marshalling it and storing
        // it in the HTTP response message body. It will use the default status
        // code of 200 Ok.
        return ParoleeDTO.fromDomain(parolee);
    }

    /**
     * Returns a view of the Parolee database, represented as a List of
     * Parolee objects.
     */
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getParolees(@DefaultValue("-1") @QueryParam("page") int pageNum,
                                @DefaultValue("-1") @QueryParam("size") int pageSize,
                                @Context UriInfo uriInfo) {

        URI uri = uriInfo.getAbsolutePath();

        Link previous = null;
        Link next = null;

        if (pageSize >= 0) {
            if (pageNum > 0) {
                // There are previous Parolees - create a previous link.
                previous = Link.fromUri(uri + "?page={page}&size={size}")
                        .rel("prev")
                        .build(pageNum - 1, pageSize);
            }
            if (pageNum * pageSize + pageSize <= paroleeDB.size()) {
                // There are successive parolees - create a next link.
                next = Link.fromUri(uri + "?page={page}&size={size}")
                        .rel("next")
                        .build(pageNum + 1, pageSize);
            }
        }

        // Create list of Parolees to return.
        List<Parolee> domainParolees;
        if (pageSize <= 0) {
            domainParolees = paroleeDB.getParolees();
        }
        else {
            domainParolees = paroleeDB.getParolees(pageNum * pageSize, pageSize);
        }
        List<ParoleeDTO> dtoParolees = domainParolees.stream()
                .map(ParoleeDTO::fromDomain).collect(Collectors.toList());

        // Create a GenericEntity to wrap the list of Parolees to return. This
        // is necessary to preserve generic type data when using any
        // MessageBodyWriter to handle translation to a particular data format.
        GenericEntity<List<ParoleeDTO>> entity = new GenericEntity<>(dtoParolees) {
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
        Parolee parolee = paroleeDB.getParolee(id);

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
    @Path("{id}/disassociates")
    @Produces(MediaType.APPLICATION_JSON)
    public List<ParoleeDTO> getParoleeDisassociates(@PathParam("id") long id) {

        // Get the Parolee object from the database.
        Parolee parolee = paroleeDB.getParolee(id);

        List<ParoleeDTO> disassociates = parolee.getDisassociates().stream()
                .map(ParoleeDTO::fromDomain).collect(Collectors.toList());

        // JAX-RS will process the returned value, marshalling it and storing
        // it in the HTTP response message body. It will use the default status
        // code of 200 Ok.
        return disassociates;
    }

    /**
     * Returns the CriminalProfile for a particular Parolee.
     *
     * @param id the unique identifier of the Parolee.
     */
    @GET
    @Path("{id}/convictions")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Conviction> getConvictions(@PathParam("id") long id) {
        // Get the Parolee object from the database.
        Parolee parolee = paroleeDB.getParolee(id);

        // JAX-RS will processed the returned value, marshalling it and storing
        // it in the HTTP response message body. It will use the default status
        // code of 200 Ok.
        return parolee.getConvictions();
    }

    /**
     * Method that adds clears and then adds some dummy data to the "database".
     */
    protected void reloadDatabase() {
        paroleeDB.reset();

        // === Initialise Parolee #1
        GeoPosition addressLocation = new GeoPosition(-36.865520, 174.859520);
        Address address = new Address("15", "Bermuda road", "St Johns", "Auckland", "1071", addressLocation);
        Parolee parolee = new Parolee(
                "Sinnen",
                "Oliver",
                Gender.MALE,
                LocalDate.of(1970, 5, 26),
                address);
        paroleeDB.addParolee(parolee);

        parolee.getConvictions().add(new Conviction(LocalDate.of(
                1994, 1, 19), "Crime of passion", Offence.MURDER));

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime earlierToday = now.minusHours(1);
        LocalDateTime yesterday = now.minusDays(1);
        GeoPosition position = new GeoPosition(-36.852617, 174.769525);

        parolee.addMovement(new Movement(yesterday, position));
        parolee.addMovement(new Movement(earlierToday, position));
        parolee.addMovement(new Movement(now, position));

        // === Initialise Parolee #2
        address = new Address("22", "Tarawera Terrace", "St Heliers", "Auckland", "1071");
        parolee = new Parolee("Watson",
                "Catherine",
                Gender.FEMALE,
                LocalDate.of(1970, 2, 9),
                address);
        paroleeDB.addParolee(parolee);

        // === Initialise Parolee #3
        address = new Address("67", "Drayton Gardens", "Oraeki", "Auckland", "1071");
        parolee = new Parolee("Giacaman",
                "Nasser",
                Gender.MALE,
                LocalDate.of(1980, 10, 19),
                address);
        paroleeDB.addParolee(parolee);
    }
}
