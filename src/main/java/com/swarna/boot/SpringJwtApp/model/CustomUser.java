package com.swarna.boot.SpringJwtApp.model;

import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class CustomUser {

	@Id
	private int id;
	private String username;
	private String password;
	private String email;

}
