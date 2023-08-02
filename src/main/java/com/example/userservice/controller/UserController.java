package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.jpa.UserEntity;
import com.example.userservice.service.UserService;
import com.example.userservice.vo.Greeting;
import com.example.userservice.vo.RequestUser;
import com.example.userservice.vo.ResponseUser;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {
    private final Environment environment;
    private final Greeting greeting;
    private final UserService userService;

    @GetMapping("/health_check")
    public String status (){
        return "It's working in User Service on Port"
                +", port(local.server.port) = "+ environment.getProperty ( "local.server.port" )
                +", port(server.port) = "+ environment.getProperty ( "server.port" )
                +", token secret = "+ environment.getProperty ( "token.secret" )
                +", token expiration time = "+ environment.getProperty ( "token.expiration_time" )
                ;
    }

    @GetMapping("/welcome")
    public String welcome (){
//        return environment.getProperty ( "greeting.message" );
        return greeting.getMessage ();
    }

    @PostMapping("/users")
    public ResponseEntity<ResponseUser> createdUser ( @RequestBody RequestUser requestUser ){
        ModelMapper modelMapper = new ModelMapper ( );
        modelMapper.getConfiguration ( ).setMatchingStrategy ( MatchingStrategies.STRICT );
        UserDto dto = modelMapper.map ( requestUser, UserDto.class );
        userService.createUser ( dto );
        ResponseUser responseUser = modelMapper.map ( dto, ResponseUser.class );
        return ResponseEntity.status ( HttpStatus.CREATED ).body ( responseUser );
    }

    @GetMapping("/users")
    public ResponseEntity<List <ResponseUser>> getUsers ( ){
        Iterable<UserEntity> userList = userService.getUserByAll ();
        List<ResponseUser> result = new ArrayList <> ();
        userList.forEach ( item -> result.add ( new ModelMapper ().map ( item, ResponseUser.class ) ) );
        return ResponseEntity.status ( HttpStatus.OK ).body ( result );
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<ResponseUser> getUser ( @PathVariable("userId") String userId ){
        UserDto userByUserId = userService.getUserByUserId ( userId );
        return ResponseEntity.status ( HttpStatus.OK )
                            .body ( new ModelMapper ().map ( userByUserId, ResponseUser.class ) );
    }
}
