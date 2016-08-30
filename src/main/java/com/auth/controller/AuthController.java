package com.auth.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.auth.dto.request.UserLoginRequest;
import com.auth.dto.request.UserSignupRequest;
import com.auth.dto.response.GenericResponse;
import com.auth.dto.response.LoginResponse;
import com.auth.model.Accounts;
import com.auth.model.User;
import com.auth.service.AccountService;
import com.auth.service.UserService;

/**
 * @author Nitesh
 */

@RestController
public class AuthController {

	@Autowired
	private UserService userService;

	@Autowired
	private AccountService accountService;

	@RequestMapping(value = "/user/signup", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<GenericResponse> signup(@RequestBody UserSignupRequest userSignupRequest) {

		GenericResponse response = new GenericResponse();

		if (userSignupRequest.getEmail() == null || "".equals(userSignupRequest.getEmail())) {
			response.setCode("V002");
			response.setMessage("Email Id is required");
			return new ResponseEntity<GenericResponse>(response, HttpStatus.OK);
		}

		try {
			User user = new User();
			user.setAppType(userSignupRequest.getAppType());
			user.setUserId(userSignupRequest.getUserId());
			user.setEmail(userSignupRequest.getEmail());
			user.setPassword(userSignupRequest.getPassword());
			user.setUserName(userSignupRequest.getUsername());
			userService.saveUser(user);
			response.setCode("S001");
			response.setMessage("User created succssfully");
			return new ResponseEntity<GenericResponse>(response, HttpStatus.OK);
		} catch (org.hibernate.exception.ConstraintViolationException ex) {
			response.setCode("V001");
			response.setMessage("Email Id already used for signup");
			return new ResponseEntity<GenericResponse>(response, HttpStatus.OK);
		} catch (Exception ex) {
			ex.printStackTrace();
			response.setCode("E001");
			response.setMessage(ex.getMessage());

			return new ResponseEntity<GenericResponse>(response, HttpStatus.BAD_REQUEST);
		}

	}

	/**
	 * user signup
	 * 
	 * @param userLoginRequest
	 * @return
	 */
	@RequestMapping(value = "/user/signin", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> signin(@RequestBody UserLoginRequest userLoginRequest) {
		GenericResponse response = new GenericResponse();

		try {
			LoginResponse loginResponse = null;
			int result;
			Accounts accounts = new Accounts();
			User usr = userService.getUser(userLoginRequest.getEmail());
			if (usr != null) {
				if (usr.getPassword().equals(userLoginRequest.getPassword())) {
					String token = UUID.randomUUID().toString();
					accounts.setProvider_token(token);
					accounts.setUser(usr);
					accounts.setProvider_name(userLoginRequest.getProvider_name());
					result = accountService.updateAccount(accounts);
					if (result != 0) {
						loginResponse = new LoginResponse();
						loginResponse.setProviderToken(token);
						loginResponse.setUserId(accounts.getUser().getUserId());
						loginResponse.setUserName(usr.getUserName());
					} else {
						response.setCode("E001");
						response.setMessage("Acoount not activted yet");

						return new ResponseEntity<GenericResponse>(response, HttpStatus.OK);
					}

					return new ResponseEntity<LoginResponse>(loginResponse, HttpStatus.OK);
				} else {
					response.setCode("V001");
					response.setMessage("Email or Password doesn't matched");

					return new ResponseEntity<GenericResponse>(response, HttpStatus.OK);
				}
			} else {
				response.setCode("V001");
				response.setMessage("Email or Password doesn't matched");

				return new ResponseEntity<GenericResponse>(response, HttpStatus.OK);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			response.setCode("E001");
			response.setMessage(ex.getMessage());

			return new ResponseEntity<GenericResponse>(response, HttpStatus.BAD_REQUEST);
		}

	}
}
