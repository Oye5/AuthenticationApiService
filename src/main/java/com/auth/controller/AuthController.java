package com.auth.controller;

import java.security.MessageDigest;
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
			Accounts account = new Accounts();

			user.setAppType(userSignupRequest.getAppType());

			user.setEmail(userSignupRequest.getEmail());
			user.setPassword(userSignupRequest.getPassword());
			user.setUserName(userSignupRequest.getUsername());

			// Set 32 bit User ID
			String userid = UUID.randomUUID().toString();
			user.setUserId(userid);
			// save user details to db
			userService.saveUser(user);
			// create account for user
			account.setAccountId(UUID.randomUUID().toString());
			account.setProvider_name(userSignupRequest.getAppType());
			account.setUserId(user);
			accountService.createAccount(account);

			response.setCode("S001");
			response.setMessage("User created succssfully");
			return new ResponseEntity<GenericResponse>(response, HttpStatus.OK);
		} catch (org.hibernate.exception.ConstraintViolationException ex) {
			response.setCode("V001");
			response.setMessage("Email Id already used for signup");
			return new ResponseEntity<GenericResponse>(response, HttpStatus.BAD_REQUEST);
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
					// generate random token and convert this generated token to sha256 64 bit auth token

					String token = UUID.randomUUID().toString();
					MessageDigest md = MessageDigest.getInstance("SHA-256");
					md.update(token.getBytes());

					byte byteData[] = md.digest();

					// convert the byte to hex format
					StringBuffer sb = new StringBuffer();
					for (int i = 0; i < byteData.length; i++) {
						sb.append(Integer.toString((byteData[i] & 0xff) + 0x100, 16).substring(1));
					}

					// System.out.println("Hex format : " + sb.toString());

					accounts.setProvider_token(sb.toString());
					accounts.setUserId(usr);
					accounts.setProvider_name(userLoginRequest.getProvider_name());
					result = accountService.updateAccount(accounts);
					if (result != 0) {
						loginResponse = new LoginResponse();
						loginResponse.setProviderToken(sb.toString());
						loginResponse.setUserId(accounts.getUserId().getUserId());
						loginResponse.setUserName(usr.getUserName());
					} else {
						response.setCode("E001");
						response.setMessage("Account not activted yet");

						return new ResponseEntity<GenericResponse>(response, HttpStatus.OK);
					}

					return new ResponseEntity<LoginResponse>(loginResponse, HttpStatus.OK);
				} else {
					response.setCode("V001");
					response.setMessage("Email or Password doesn't matched");

					return new ResponseEntity<GenericResponse>(response, HttpStatus.OK);
				}
			} else {
				response.setCode("V002");
				response.setMessage("Email or Password doesn't matched");

				return new ResponseEntity<GenericResponse>(response, HttpStatus.OK);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			response.setCode("E002");
			response.setMessage(ex.getMessage());

			return new ResponseEntity<GenericResponse>(response, HttpStatus.BAD_REQUEST);
		}

	}
}
