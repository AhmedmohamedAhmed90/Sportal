package com.example.Sportal.mapper;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
public class GlobalMapper {
    private final ModelMapper modelMapper;

    public GlobalMapper(ModelMapper modelMapper) {
        this.modelMapper = modelMapper;
    }

    public <S, T> T mapTo(S source, Class<T> targetClass) {
        return modelMapper.map(source, targetClass);
    }

    public <S,T> T mapFrom(S dto, Class<T> soruceClass) {
        return modelMapper.map(dto,soruceClass);
    }
}
