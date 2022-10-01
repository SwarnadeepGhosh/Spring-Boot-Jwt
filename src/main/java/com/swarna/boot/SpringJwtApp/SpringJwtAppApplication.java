package com.swarna.boot.SpringJwtApp;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.swarna.boot.SpringJwtApp.model.CustomUser;
import com.swarna.boot.SpringJwtApp.repo.UserRepo;

@SpringBootApplication
public class SpringJwtAppApplication {
	
	@Autowired
	private UserRepo userRepo;
	
    @PostConstruct // This annotaion will make below method as init, so that it initialize on application startup
    public void initUsers() {
        List<CustomUser> users = Stream.of(
                new CustomUser(101, "javatechie", "password", "javatechie@gmail.com"),
                new CustomUser(102, "user1", "pwd1", "user1@gmail.com"),
                new CustomUser(103, "user2", "pwd2", "user2@gmail.com"),
                new CustomUser(104, "user3", "pwd3", "user3@gmail.com")
        ).collect(Collectors.toList());
        userRepo.saveAll(users);
    }

	public static void main(String[] args) {
		SpringApplication.run(SpringJwtAppApplication.class, args);
	}

}
