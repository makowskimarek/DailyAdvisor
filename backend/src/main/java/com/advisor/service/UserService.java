package com.advisor.service;

import com.advisor.model.entity.User;
import com.advisor.model.entity.UserProfile;
import com.advisor.model.request.NewUserRequest;
import com.advisor.model.request.UserProfileRequest;
import com.advisor.model.response.UserProfileResponse;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserService {

	User findUserByEmail(String email);

	void saveUser(NewUserRequest newUserRequest);

	Optional<User> findById(UUID userId);

	UserProfileResponse createUserProfileResponseByUser(User user);

	void updateUserProfile(UserProfileRequest userProfileRequest, UUID userProfileId);

	void upgradeUserToCoach(User user);

    List<UserProfile> findByUsers(List<User> users);

	List<UserProfile> findByCity(String city);
}
