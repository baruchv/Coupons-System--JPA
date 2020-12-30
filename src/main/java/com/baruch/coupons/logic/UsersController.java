package com.baruch.coupons.logic;

import java.util.Calendar;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;

import com.baruch.coupons.dataObjectsForPresentation.UserData;
import com.baruch.coupons.dto.SuccessfulLoginData;
import com.baruch.coupons.dto.UserDto;
import com.baruch.coupons.dto.UserLoginData;
import com.baruch.coupons.entities.Company;
import com.baruch.coupons.entities.User;
import com.baruch.coupons.enums.ErrorTypes;
import com.baruch.coupons.enums.UserType;
import com.baruch.coupons.exceptions.ApplicationException;
import com.baruch.coupons.repository.IUserRepository;

@Controller
public class UsersController {
	
	//PROPERTIES

	@Autowired
	private IUserRepository repository;

	@Autowired
	private CompaniesController companiesController;

	@Autowired
	private CacheController cache;

	private final String HASHINGEXTENTION = "DASF;lkpoi493i@@#$%*21"; 
	
	//PUBLIC-METHODS

	public long createUser(UserDto userDto) throws ApplicationException{
		validateCreateUser(userDto);
		String hashedPassword = getHashedPassword(userDto.getPassword());
		userDto.setPassword(hashedPassword);
		User user = generateEntity(userDto);
		try {
			repository.save(user);
			return user.getId();
		}
		catch(Exception e) {
			throw new ApplicationException("createUser failed for " + userDto, ErrorTypes.GENERAL_ERROR, e);
		}
	}
	
	@Transactional
	public void updateUser(UserDto userDto, UserLoginData userDetails) throws ApplicationException{
		long id = userDetails.getId();
		validateUserId(id);
		validatePassword(userDto.getPassword());
		String hashedPassword = getHashedPassword(userDto.getPassword());
		try {
			repository.updateUser(hashedPassword, id);
		}
		catch(Exception e) {
			throw new ApplicationException("updateUser() failed for " + userDto,ErrorTypes.GENERAL_ERROR,e);
		}
	}

	public void deleteUser(UserLoginData userDetails) throws ApplicationException{
		long id = userDetails.getId();
		try {
			repository.deleteById(id);
		}
		catch(Exception e) {
			throw new ApplicationException("deleteById() failed for userID = " + id, ErrorTypes.GENERAL_ERROR,e);
		}
	}

	public UserData getUser(long id) throws ApplicationException{
		validateUserId(id);
		try {
			return repository.getUser(id);
		}
		catch(Exception e) {
			throw new ApplicationException("findById() failed for userID = " + id, ErrorTypes.GENERAL_ERROR,e);
		}
	}
	
	public List<UserData> getAllUsers() throws ApplicationException{
		try {
			return repository.getAllUsers();
		}
		catch(Exception e) {
			throw new ApplicationException("findAll() failed for users table", ErrorTypes.GENERAL_ERROR,e);
		}
	}

	public List<UserData> getUsersByCompany(long companyID) throws ApplicationException{
		validateCompanyID(companyID);
		try {
			return repository.getUsersByCompany(companyID);
		}
		catch(Exception e) {
			throw new ApplicationException("findAll() failed for users table", ErrorTypes.GENERAL_ERROR,e);
		}
	}

	public List<UserData> getUsersByType(UserType type) throws ApplicationException{
		try {
			return repository.getUsersByType(type);
		}
		catch(Exception e) {
			throw new ApplicationException("findAll() failed for users table", ErrorTypes.GENERAL_ERROR,e);
		}
	}

	public SuccessfulLoginData login(String userName, String password) throws ApplicationException {

		UserLoginData userDetails = repository.login(userName, getHashedPassword(password));

		if(userDetails == null) {
			throw new ApplicationException(ErrorTypes.LOGIN_ERROR);
		}

		String token = generateToken(userName, password);

		cache.put(token, userDetails);

		return new SuccessfulLoginData(token, userDetails.getType());
	}

	public void logout(String token) {
		cache.remove(token);
	}

	//PRIVATE-METHODS

	private void validateCreateUser(UserDto userDto) throws ApplicationException{
		try {
			if(repository.existsByUserName(userDto.getUserName())) {
				throw new ApplicationException(ErrorTypes.EXISTING_USERNAME_ERROR);
			}
		}
		catch(Exception e) {
			throw new ApplicationException("existsByUserName() failed for " + userDto, ErrorTypes.GENERAL_ERROR,e);
		}
		validateUserName(userDto.getUserName());
		validatePassword(userDto.getPassword());
		if(userDto.getType() == UserType.COMPANY) {
			validateCompanyID(userDto.getCompanyID());
		}
	}

	private String getHashedPassword(String password ) {
		return String.valueOf((password + HASHINGEXTENTION ).hashCode());
	}

	private void validateUserName(String userName) throws ApplicationException{
		if(userName == null) {
			throw new ApplicationException(ErrorTypes.EMPTY_USERNAME_ERROR);
		}
		if(userName.length()<2) {
			throw new ApplicationException(ErrorTypes.INVALID_USERNAME_ERROR);
		}
	}

	private void validatePassword(String password) throws ApplicationException{
		if(password == null) {
			throw new ApplicationException(ErrorTypes.EMPTY_PASSWORD_ERROR);
		}
		if(password.length()<8) {
			throw new ApplicationException(ErrorTypes.INVALID_PASSWORD_ERROR);
		}
	}

	protected void validateUserId(long id) throws ApplicationException{
		try {
			if( ! repository.existsById(id)) {
				throw new ApplicationException("UsersController.validateUserID() failed for ID: " +id, ErrorTypes.NO_USER_ID);
			}
		}
		catch(Exception e) {
			throw new ApplicationException("existsById failed for userID = " +id, ErrorTypes.GENERAL_ERROR,e);
		}
	}

	private void validateCompanyID(Long id) throws ApplicationException{
		if(id == null) {
			throw new ApplicationException(ErrorTypes.EMPTY_COMPANYID_ERROR);
		}
		companiesController.validateCompanyID(id);
	}

	private String generateToken(String userName, String password) {
		Calendar now = Calendar.getInstance();
		int token = (userName + password + now.getTime().toString() + HASHINGEXTENTION).hashCode();
		return Integer.toString(token);
	}

	private User generateEntity(UserDto userDto) throws ApplicationException{
		try {
			User user = new User(userDto);
			Long companyID = userDto.getCompanyID();
			Company company = null;
			if (companyID != null) {
				company = companiesController.getCompanyEntity(companyID);
			}
			user.setCompany(company);
			return user;
		} catch (Exception e) {
			throw new ApplicationException("usersController.generateEntity() failed for " + userDto,ErrorTypes.GENERAL_ERROR,e);
		}
	}
	
	protected User getUserEntity(long userID) throws ApplicationException{
		try {
			return repository.findById(userID).get();
		}
		catch(Exception e) {
			throw new ApplicationException("repository.findById() failed for userID = " + userID, ErrorTypes.GENERAL_ERROR, e);
		}
	}


}
