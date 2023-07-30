package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
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

@RestController
@RequestMapping("/")
@RequiredArgsConstructor
public class UserController {
    private final Environment environment;
    private final Greeting greeting;
    private final UserService userService;

    @GetMapping("/health_check")
    public String status (){
        return "It's working in User Service";
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
}
