package com.example.userservice.service;

import com.example.userservice.client.OrderServiceClient;
import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.jpa.UserRepository;
import com.example.userservice.vo.ResponseOrder;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.core.env.Environment;
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
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder encoder;
    private final Environment env;
//    private final RestTemplate restTemplate;
    private final OrderServiceClient client;
    private final CircuitBreakerFactory circuitBreakerFactory;

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
//        List <ResponseOrder> orders = new ArrayList <> ( );
        //첫번째 방법
//        String orderUrl = String.format ( env.getProperty ( "order_service.url" ), userId );
//        ResponseEntity <List <ResponseOrder>> result = restTemplate.exchange ( orderUrl, HttpMethod.GET, null,
//                                                                    new ParameterizedTypeReference <> ( ) { }
//                                                                        );
//      List <ResponseOrder> orderList = result.getBody ();
        //두번째 방법
            //트라이 캐치
//        List <ResponseOrder> orderList = null;
//        try{
//            orderList = client.getOrders ( userId );
//        }catch ( FeignException ex ){
//            log.error(ex.getMessage ());
//        }
            //에러 디코더
//        List <ResponseOrder> orderList = client.getOrders ( userId );
        //서킷 브레이커
        log.info ( "Before call orders MS" );
        CircuitBreaker circuitbreaker = circuitBreakerFactory.create ( "circuitbreaker" );
        List <ResponseOrder> orderList = circuitbreaker.run ( () -> client.getOrders ( userId ),
                                                                throwable -> new ArrayList <> () );
        log.info ( "After call orders MS" );
        userDto.setOrders ( orderList );
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
