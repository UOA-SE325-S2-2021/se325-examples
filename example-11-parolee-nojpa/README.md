# SE325 Example 11 - Parolee web service *without* JPA / Hibernate
This project contains a parolee web service significantly more complex than the one shown in previous examples ([Example 05](../example-05-jax-rs), [Example 08](../example-08-jaxrs-json)). It shows how we can build a more comprehensive REST service using JAX-RS - including HATEOAS (pagination with "next" and "prev" links is implemented for getting a list of parolees).

In addition, this project serves as a good comparison to the next example - [Example 12](../example-12-parolee-with-jpa). This project doesn't use JPA / Hibernate for persistence, whereas Example 12 does. You can compare the domain models from each example to see the difference between the annotations.
