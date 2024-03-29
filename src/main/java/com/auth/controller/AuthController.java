package com.auth.controller;

import java.security.MessageDigest;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
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
import com.auth.model.Seller;
import com.auth.model.User;
import com.auth.service.AccountService;
import com.auth.service.SellerService;
import com.auth.service.UserService;
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

	@Autowired
	private SellerService sellerService;

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
			Seller seller = new Seller();
			seller.setId(user.getUserId());
			seller.setUserId(user);
			seller.setBanned("no");
			sellerService.saveSeller(seller);
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
			User user = userService.getUser(userLoginRequest.getEmail());
			if (user == null) {
				response.setCode("V001");
				response.setMessage("Email not valid please check.");
				return new ResponseEntity<GenericResponse>(response, HttpStatus.OK);
			}
			Seller seller = sellerService.getSellerById(user.getUserId());
			if (seller.getBanned().equalsIgnoreCase("banned")) {
				response.setCode("V002");
				response.setMessage("user banned can't login.");
				return new ResponseEntity<GenericResponse>(response, HttpStatus.OK);
			}

			if (user.getPassword().equals(userLoginRequest.getPassword())) {
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
				accounts.setUserId(user);
				accounts.setProvider_name(userLoginRequest.getProvider_name());
				result = accountService.updateAccount(accounts);
				if (result != 0) {

					loginResponse = new LoginResponse();
					loginResponse.setProviderToken(sb.toString());
					loginResponse.setUserId(accounts.getUserId().getUserId());
					loginResponse.setUserName(user.getUserName());
				} else {
					response.setCode("E001");
					response.setMessage("Account not activted yet");
					return new ResponseEntity<GenericResponse>(response, HttpStatus.EXPECTATION_FAILED);
				}
				return new ResponseEntity<LoginResponse>(loginResponse, HttpStatus.OK);
			} else {
				response.setCode("V003");
				response.setMessage("Email or Password doesn't matched");

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
			FacebookClient facebookClient = new DefaultFacebookClient(accessToken, "8e2cf44150f2bb506dc14946acdeccb7", Version.VERSION_2_7);
			com.restfb.types.User fbUser = facebookClient.fetchObject("me", com.restfb.types.User.class, Parameter.with("fields", "name,id,email,devices"));
			User user = userService.getUserByAccessToken(accessToken);
			if (user == null) {
				User newUser = new User();
				if (fbUser.getDevices().size() != 0)
					newUser.setAppType(fbUser.getDevices().get(0).toString());
				else
					newUser.setAppType("not defined");
				newUser.setUserId(UUID.randomUUID().toString());
				newUser.setUserName(fbUser.getName());
				newUser.setEmail(fbUser.getEmail());
				newUser.setFbAuthToken(accessToken);
				userService.saveUser(newUser);
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

				accountService.createAccount(account);

				loginResponse = new LoginResponse();
				loginResponse.setProviderToken(sb.toString());
				loginResponse.setUserId(account.getUserId().getUserId());
				loginResponse.setUserName(fbUser.getName());

				return new ResponseEntity<LoginResponse>(loginResponse, HttpStatus.OK);

			} else {
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
				// System.out.println("=res11==" + result);
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

	/**
	 * admin api to banned user
	 * 
	 * @param userId
	 * @return
	 */
	@RequestMapping(value = "/v1/admin/banneduser/{userId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<?> bannedUSer(@PathVariable("userId") String userId) {
		GenericResponse response = new GenericResponse();
		try {
			Seller seller = sellerService.getSellerById(userId);
			if (seller == null) {
				response.setCode("V001");
				response.setMessage("user ID not found. please check userId");
				return new ResponseEntity<GenericResponse>(response, HttpStatus.EXPECTATION_FAILED);
			}
			seller.setBanned("banned");
			sellerService.updateSeller(seller);
			response.setCode("S001");
			response.setMessage("seller banned");
			return new ResponseEntity<GenericResponse>(response, HttpStatus.OK);
		} catch (Exception e) {
			e.printStackTrace();
			response.setCode("E001");
			response.setMessage(e.getMessage());
			return new ResponseEntity<GenericResponse>(response, HttpStatus.BAD_REQUEST);
		}

	}

}
