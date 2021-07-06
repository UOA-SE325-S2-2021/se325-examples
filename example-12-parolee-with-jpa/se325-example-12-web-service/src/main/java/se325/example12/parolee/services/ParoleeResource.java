package se325.example12.parolee.services;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se325.example12.parolee.domain.*;
import se325.example12.parolee.dto.ParoleeDTO;

/**
 * Web service resource implementation for the Parolee application. An instance
 * of this class handles all HTTP requests for the Parolee Web service.
 */
@Path("/parolees")
public class ParoleeResource {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParoleeResource.class);

    /**
     * Adds a new Parolee to the system. The state of the new Parolee is
     * described by a Parolee object.
     *
     * @param dtoParolee the Parolee data included in the HTTP request body.
     */
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createParolee(ParoleeDTO dtoParolee) {

        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {

            em.getTransaction().begin();
            Parolee domainParolee = dtoParolee.toDomain();
            em.persist(domainParolee);
            em.getTransaction().commit();

            // Return a Response that specifies a status code of 201 Created along
            // with the Location header set to the URI of the newly created Parolee.
            return Response.created(URI.create("/parolees/" + domainParolee.getId())).build();

        } finally {
            em.close();
        }
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

        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            em.getTransaction().begin();
            Parolee parolee = em.find(Parolee.class, id);
            parolee.addMovement(movement);
            em.persist(parolee);
            em.getTransaction().commit();

            // JAX-RS will add the default response code to the HTTP response message.
        } finally {
            em.close();
        }
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

        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            // Get the Parolee object from the database.
            em.getTransaction().begin();
            Parolee parolee = em.find(Parolee.class, incomingParolee.getId());

            // Update the Parolee object in the database based on the data in parolee.
            incomingParolee.updateDomain(parolee);
            em.persist(parolee);
            em.getTransaction().commit();

            // JAX-RS will add the default response code (204 No Content) to the HTTP response message.
        } finally {
            em.close();
        }
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

        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            // Get the Parolee object from the database.
            em.getTransaction().begin();
            Parolee parolee = em.find(Parolee.class, id);

            TypedQuery<Parolee> disassociatesQuery = em.createQuery(
                    "SELECT p FROM Parolee p WHERE p.id IN (:ids)", Parolee.class)
                    .setParameter("ids", disassociateIds);

            List<Parolee> disassociates = disassociatesQuery.getResultList();

            parolee.setDisassociates(new HashSet<>(disassociates));
            em.persist(parolee);
            em.getTransaction().commit();

            // JAX-RS will add the default response code (204 No Content) to the HTTP response message.
        } finally {
            em.close();
        }
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
    public void updateConvictions(@PathParam("id") long id, Set<Conviction> convictions) {

        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            // Get the Parolee object from the database.
            em.getTransaction().begin();
            Parolee parolee = em.find(Parolee.class, id);

            // Update the Parolee's criminal profile.
            parolee.setConvictions(convictions);
            em.persist(parolee);
            em.getTransaction().commit();

            // JAX-RS will add the default response code (204 No Content) to the HTTP response message.
        } finally {
            em.close();
        }
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

        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            // Get the Parolee object from the database.
            em.getTransaction().begin();
            Parolee parolee = em.find(Parolee.class, id);
            em.getTransaction().commit();

            // JAX-RS will processed the returned value, marshalling it and storing
            // it in the HTTP response message body. It will use the default status
            // code of 200 Ok.
            return ParoleeDTO.fromDomain(parolee);
        } finally {
            em.close();
        }
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

        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {

            // How many parolees are there in total? This is so we can calc what is the "last page".
            em.getTransaction().begin();
            TypedQuery<Long> countQuery = em.createQuery("SELECT count(p.id) FROM Parolee p", Long.class);
            long numParolees = countQuery.getSingleResult();

            // If the default values for either pageNum or pageSize were provided, then set them to proper values
            // equalling the total parolee list.
            if (pageNum < 0 || pageSize <= 0) {
                pageNum = 0;
                pageSize = (int) numParolees;
            }

            // Get all the parolees, using the pagination methods setFirstResult() and setMaxResults().
            TypedQuery<Parolee> paroleesQuery = em.createQuery("SELECT p FROM Parolee p", Parolee.class);
            paroleesQuery.setFirstResult(pageNum * pageSize);
            paroleesQuery.setMaxResults(pageSize);
            List<Parolee> domainParolees = paroleesQuery.getResultList();
            em.getTransaction().commit();

            URI uri = uriInfo.getAbsolutePath();
            Link previous = null;
            Link next = null;

            // If we didn't start from the first parolee, create a "prev" link.
            if (pageNum > 0) {
                // There are previous Parolees - create a previous link.
                previous = Link.fromUri(uri + "?page={page}&size={size}")
                        .rel("prev")
                        .build(pageNum - 1, pageSize);
            }

            // If we didn't include the last parolee, create a "next" link.
            if (pageNum * pageSize + pageSize <= numParolees) {
                // There are successive parolees - create a next link.
                next = Link.fromUri(uri + "?page={page}&size={size}")
                        .rel("next")
                        .build(pageNum + 1, pageSize);
            }

            // Create list of Parolees to return.
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

            // Return the custom Response. The JAX-RS run-time will process this,
            // extracting the List of Parolee objects and marshalling them into the
            // HTTP response message body. In addition, since the Response object
            // contains headers (previous and/or next), these will be added to the
            // HTTP response message. The Response object was created with the 200
            // Ok status code, and this too will be added for the status header.
            return builder.build();

        } finally {
            em.close();
        }
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

        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            // Get the Parolee object from the database.
            em.getTransaction().begin();
            Parolee parolee = em.find(Parolee.class, id);
            List<Movement> movements = parolee.getMovements();
            em.getTransaction().commit();

            // Return the Parolee's movements.
            return movements;

            // JAX-RS will processed the returned value, marshalling it and storing
            // it in the HTTP response message body. It will use the default status
            // code of 200 Ok.
        } finally {
            em.close();
        }
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

        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            // Get the Parolee object from the database.
            em.getTransaction().begin();
            Parolee parolee = em.find(Parolee.class, id);
            em.getTransaction().commit();

            List<ParoleeDTO> disassociates = parolee.getDisassociates().stream()
                    .map(ParoleeDTO::fromDomain).collect(Collectors.toList());

            // JAX-RS will process the returned value, marshalling it and storing
            // it in the HTTP response message body. It will use the default status
            // code of 200 Ok.
            return disassociates;
        } finally {
            em.close();
        }
    }

    /**
     * Returns the CriminalProfile for a particular Parolee.
     *
     * @param id the unique identifier of the Parolee.
     */
    @GET
    @Path("{id}/convictions")
    @Produces(MediaType.APPLICATION_JSON)
    public Set<Conviction> getConvictions(@PathParam("id") long id) {

        EntityManager em = PersistenceManager.instance().createEntityManager();
        try {
            // Get the Parolee object from the database.
            em.getTransaction().begin();
            Parolee parolee = em.find(Parolee.class, id);
            Set<Conviction> convictions = parolee.getConvictions();
            em.getTransaction().commit();

            // JAX-RS will processed the returned value, marshalling it and storing
            // it in the HTTP response message body. It will use the default status
            // code of 200 Ok.
            return convictions;
        } finally {
            em.close();
        }
    }
}
