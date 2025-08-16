package com.bidpro.util;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class BidProUtils {
    @Autowired
    private ModelMapper modelMapper;

    /**
     * Maps a source object to a destination class using ModelMapper.
     * @param source The source object
     * @param destinationClass The destination class type
     * @param <S> Source type
     * @param <D> Destination type
     * @return Mapped object of the destination type
     */
    public <S, D> D map(S source, Class<D> destinationClass) {
        return modelMapper.map(source, destinationClass);
    }

    /**
     * Maps a source object to an existing destination object using ModelMapper.
     * @param source The source object
     * @param destination The destination object
     * @param <S> Source type
     * @param <D> Destination type
     * @return The updated destination object
     */
    public <S, D> D map(S source, D destination) {
        modelMapper.map(source, destination);
        return destination;
    }
}