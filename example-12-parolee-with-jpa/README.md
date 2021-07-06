# SE325 Example 12 - Parolee web service *with* JPA / Hibernate
This project contains a parolee web service identical to [Example 11](../example-11-parolee-nojpa), except that it uses JPA / Hibernate to persist its state.

You can see how the [`ParoleeResource`](./se325-example-12-web-service/src/main/java/se325/example12/parolee/services/ParoleeResource.java) class uses `EntityManager` and various query / update operations. You can also see the use of various JPA annotations in the [domain model](./se325-example-12-domain-model).

In addition, in `ParoleeApplication`, you can see we're supplying the `ParoleeResource` *class*, rather than an *instance* of that class as we did before. This is because the application state is no longer contained within `ParoleeResource`, but is within the database. Therefore, we can allow JAX-RS to create new instances of `ParoleeResource` as required, to provide better multithreading performance.