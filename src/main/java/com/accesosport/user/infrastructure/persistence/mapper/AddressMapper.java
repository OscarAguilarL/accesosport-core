package com.accesosport.user.infrastructure.persistence.mapper;

import com.accesosport.shared.domain.valueobjects.Address;
import com.accesosport.shared.infrastructure.common.persistence.jpa.AddressEmbeddable;

/**
 * Provides static utility methods for mapping between Address domain objects
 * and AddressEmbeddable entities. This class facilitates conversion of address
 * data between different layers of the application (e.g., persistence and domain layers).
 */
public class AddressMapper {

    /**
     * Maps an AddressEmbeddable object to its corresponding Address domain model.
     *
     * @param addressEmbeddable the AddressEmbeddable object to be mapped.
     *                          Must contain all required address components such as
     *                          street, external number, neighborhood, city, state,
     *                          country, and zip code.
     * @return a new Address object constructed from the provided AddressEmbeddable data.
     */
    public static Address mapAddressToDomain(AddressEmbeddable addressEmbeddable) {
        return new Address(
                addressEmbeddable.getStreet(),
                addressEmbeddable.getExternalNumber(),
                addressEmbeddable.getInternalNumber(),
                addressEmbeddable.getNeighborhood(),
                addressEmbeddable.getCity(),
                addressEmbeddable.getState(),
                addressEmbeddable.getCountry(),
                addressEmbeddable.getZipCode()
        );
    }

    /**
     * Maps an Address domain object to its corresponding AddressEmbeddable entity.
     * This method constructs an AddressEmbeddable object using the relevant fields
     * from the provided Address domain model.
     *
     * @param domain the Address domain object containing the address details
     *               to be mapped to an entity. Must include values for street,
     *               city, state, and zip code.
     * @return a new AddressEmbeddable entity constructed from the provided
     * Address domain model.
     */
    public static AddressEmbeddable mapAddressToEntity(Address domain) {
        return AddressEmbeddable.builder()
                .street(domain.street())
                .externalNumber(domain.externalNumber())
                .internalNumber(domain.internalNumber())
                .neighborhood(domain.neighborhood())
                .city(domain.city())
                .state(domain.state())
                .country(domain.country())
                .zipCode(domain.zipCode())
                .build();
    }
}
