package com.grupocaos.products.athletix.user.infrastructure.persistence.mapper;

import com.grupocaos.products.athletix.shared.domain.valueobjects.Address;
import com.grupocaos.products.athletix.shared.infrastructure.common.persistence.jpa.AddressEmbeddable;

public class AddressMapper {

    public static Address mapAddressToDomain(AddressEmbeddable addressEmbeddable) {
        return new Address(
                addressEmbeddable.getStreet(),
                addressEmbeddable.getCity(),
                addressEmbeddable.getState(),
                addressEmbeddable.getZipCode()
        );
    }

    public static AddressEmbeddable mapAddressToEntity(Address domain) {
        return AddressEmbeddable.builder()
                .street(domain.street())
                .city(domain.city())
                .state(domain.state())
                .zipCode(domain.zipCode())
                .build();
    }
}
