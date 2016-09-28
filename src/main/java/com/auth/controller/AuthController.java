package com.auth.controller;

import java.security.MessageDigest;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.auth.dto.request.UserLoginRequest;
import com.auth.dto.request.UserSignupRequest;
import com.auth.dto.response.GenericResponse;
import com.auth.dto.response.LoginResponse;
import com.auth.model.Accounts;
import com.auth.model.User;
import com.auth.service.AccountService;
import com.auth.service.UserService;
import com.restfb.Connection;
import com.restfb.DefaultFacebookClient;
import com.restfb.FacebookClient;
import com.restfb.Parameter;
import com.restfb.Version;

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
			response.setCode("V001");
			response.setMessage("Email Id is required");
			return new ResponseEntity<GenericResponse>(response, HttpStatus.NO_CONTENT);
		}

		if (userSignupRequest.getPassword() == null || "".equals(userSignupRequest.getPassword())) {
			response.setCode("V002");
			response.setMessage("password  is required");
			return new ResponseEntity<GenericResponse>(response, HttpStatus.NO_CONTENT);
		}

		try {
			User user = new User();
			Accounts account = new Accounts();

			user.setAppType(userSignupRequest.getAppType());

			user.setEmail(userSignupRequest.getEmail());
			user.setPassword(userSignupRequest.getPassword());
			user.setUserName(userSignupRequest.getUsername());
			user.setActive(true);
			// Set 32 bit User ID
			if (userSignupRequest.getId() != null) {
				user.setUserId(userSignupRequest.getId());
			} else {
				user.setUserId(UUID.randomUUID().toString());
			}
			// save user details to db
			userService.saveUser(user);
			// create account for user
			account.setAccountId(UUID.randomUUID().toString());
			account.setProvider_name(userSignupRequest.getUsername());
			account.setUserId(user);
			accountService.createAccount(account);

			response.setCode("S001");
			response.setMessage("User created succssfully");
			return new ResponseEntity<GenericResponse>(response, HttpStatus.OK);
		} catch (org.springframework.dao.DataIntegrityViolationException ex) {
			ex.printStackTrace();
			response.setCode("V002");
			response.setMessage("Email Id or username already used for signup. please try with other Email id and username");
			return new ResponseEntity<GenericResponse>(response, HttpStatus.CONFLICT);
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
						return new ResponseEntity<GenericResponse>(response, HttpStatus.EXPECTATION_FAILED);
					}
					return new ResponseEntity<LoginResponse>(loginResponse, HttpStatus.OK);
				} else {
					response.setCode("V001");
					response.setMessage("Email or Password doesn't matched");

					return new ResponseEntity<GenericResponse>(response, HttpStatus.EXPECTATION_FAILED);
				}
			} else {
				response.setCode("V002");
				response.setMessage("email id doesn't exist");

				return new ResponseEntity<GenericResponse>(response, HttpStatus.EXPECTATION_FAILED);
			}
		} catch (Exception ex) {
			ex.printStackTrace();
			response.setCode("E002");
			response.setMessage(ex.getMessage());

			return new ResponseEntity<GenericResponse>(response, HttpStatus.BAD_REQUEST);
		}

	}

	/**
	 * facebook signIn
	 * 
	 * @param userLoginRequest
	 * @return
	 */
	@Value("${facebook.app.secret}")
	private String APP_SECRET;

	@RequestMapping(value = "/user/facebook/signin", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE, consumes = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> facebokkSignin(@RequestParam("access_token") String accessToken) {
		GenericResponse response = new GenericResponse();
		LoginResponse loginResponse = new LoginResponse();
		try {
			System.out.println("==token====" + accessToken);
			System.out.println("==app====" + APP_SECRET);
			FacebookClient facebookClient = new DefaultFacebookClient(accessToken, "8e2cf44150f2bb506dc14946acdeccb7", Version.VERSION_2_7);
			com.restfb.types.User fbUser = facebookClient.fetchObject("me", com.restfb.types.User.class, Parameter.with("fields", "name,id,email,devices"));
			System.out.println("----------------------iii---");
			User user = userService.getUserByAccessToken(accessToken);
			System.out.println("==================device=--==" + fbUser.getDevices());
			if (user == null) {
				User newUser = new User();
				System.out.println("-----------------device--" + fbUser.getDevices() + "--00--" + fbUser.getDevices().get(0));
				if (fbUser.getDevices().size() != 0)
					newUser.setAppType(fbUser.getDevices().get(0).toString());
				else
					newUser.setAppType("not defined");
				newUser.setUserId(UUID.randomUUID().toString());
				newUser.setUserName(fbUser.getName());
				newUser.setEmail(fbUser.getEmail());
				newUser.setFbAuthToken(accessToken);
				System.out.println("()))=======");
				userService.saveUser(newUser);
				System.out.println("(77777)))=======");
				Accounts account = new Accounts();
				account.setAccountId(UUID.randomUUID().toString());
				// generate authtoken
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

				account.setProvider_token(sb.toString());
				account.setUserId(newUser);
				account.setProvider_name(fbUser.getName());
				System.out.println("())111111)=======");

				accountService.createAccount(account);
				System.out.println("===result===");

				loginResponse = new LoginResponse();
				loginResponse.setProviderToken(sb.toString());
				loginResponse.setUserId(account.getUserId().getUserId());
				loginResponse.setUserName(fbUser.getName());

				return new ResponseEntity<LoginResponse>(loginResponse, HttpStatus.OK);

			} else {
				System.out.println("--else======");
				Accounts accounts = new Accounts();
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
				accounts.setUserId(user);
				accounts.setProvider_name(user.getUserName());
				int result = accountService.updateAccount(accounts);
				System.out.println("=res11==" + result);
				if (result != 0) {

					loginResponse = new LoginResponse();
					loginResponse.setProviderToken(sb.toString());
					loginResponse.setUserId(accounts.getUserId().getUserId());
					loginResponse.setUserName(user.getUserName());
				} else {
					response.setCode("E002");
					response.setMessage("Account not activted yet");

					return new ResponseEntity<GenericResponse>(response, HttpStatus.BAD_REQUEST);
				}
			}

			System.out.println("-----email----" + fbUser.getEmail());
			System.out.println("-----1----" + fbUser);
			System.out.println("===========" + fbUser.getName() + fbUser.getType());
			System.out.println(fbUser.getBirthday());
			System.out.println("==" + fbUser.getBirthdayAsDate() + "==" + fbUser.getUsername());
			// user.getBirthday();
			return new ResponseEntity<LoginResponse>(loginResponse, HttpStatus.OK);

		} catch (Exception ex) {
			ex.printStackTrace();
			response.setCode("E002");
			response.setMessage(ex.getMessage());

			return new ResponseEntity<GenericResponse>(response, HttpStatus.BAD_REQUEST);
		}

	}

}
