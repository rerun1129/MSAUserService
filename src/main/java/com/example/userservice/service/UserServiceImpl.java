package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MatchingStrategy;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;

    @Override
    public UserDto createUser ( UserDto userDto ) {
        userDto.setUserId ( UUID.randomUUID ().toString () );
        ModelMapper modelMapper = new ModelMapper ( );
        modelMapper.getConfiguration ( ).setMatchingStrategy ( MatchingStrategies.STRICT );

        UserEntity entity = modelMapper.map ( userDto, UserEntity.class );

        entity.setEncryptedPassword ( "encrypted" );

        userRepository.save ( entity );

        return modelMapper.map ( entity, UserDto.class );
    }
}
