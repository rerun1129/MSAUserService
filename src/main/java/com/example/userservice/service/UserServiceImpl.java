package com.example.userservice.service;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.vo.ResponseOrder;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MatchingStrategy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;

    @Override
    public UserDto createUser ( UserDto userDto ) {
        userDto.setUserId ( UUID.randomUUID ().toString () );
        ModelMapper modelMapper = new ModelMapper ( );
        modelMapper.getConfiguration ( ).setMatchingStrategy ( MatchingStrategies.STRICT );

        UserEntity entity = modelMapper.map ( userDto, UserEntity.class );

        entity.setEncryptedPassword ( encoder.encode ( userDto.getPassword () ) );

        userRepository.save ( entity );

        return modelMapper.map ( entity, UserDto.class );
    }

    @Override
    public UserDto getUserByUserId ( String userId ) {
        UserEntity userEntity = userRepository.findByUserId ( userId );
        if(userEntity == null) throw new UsernameNotFoundException ( "User not found" );
        UserDto userDto = new ModelMapper ( ).map ( userEntity, UserDto.class );
        List <ResponseOrder> orders = new ArrayList <> ( );
        userDto.setOrders ( orders );
        return userDto;
    }

    @Override
    public Iterable <UserEntity> getUserByAll ( ) {
        return userRepository.findAll ();
    }

    @Override
    public UserDetails loadUserByUsername ( String username ) throws UsernameNotFoundException {
        UserEntity entity = userRepository.findByEmail ( username );
        if(entity == null){
            throw new UsernameNotFoundException(username);
        }
        return new User (
                entity.getEmail (),
                entity.getEncryptedPassword (),
                true,
                true,
                true,
                true,
                new ArrayList<> () );
    }

    @Override
    public UserDto getUserDetailsByEmail ( String email ) {
        UserEntity entity = userRepository.findByEmail ( email );
        if ( entity == null ) {
            throw new UsernameNotFoundException ( email );
        }
        return new ModelMapper ().map ( entity, UserDto.class );
    }
}
