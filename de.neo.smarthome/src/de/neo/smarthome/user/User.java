package de.neo.smarthome.user;

import de.neo.persist.annotations.Domain;
import de.neo.persist.annotations.Id;
import de.neo.persist.annotations.Persist;

@Domain(name = "User")
public class User {

	public enum UserRole {
		ADMIN, USER
	};

	@Persist(name = "name")
	private String mName;

	@Persist(name = "password")
	private String mPassword;

	@Id(name = "id")
	private long mId;

	@Persist(name = "avatar")
	private String mAvatar;

	@Persist(name = "role")
	private UserRole mRole;

	public String getName() {
		return mName;
	}

	public void setName(String name) {
		mName = name;
	}

	public String getPassword() {
		return mPassword;
	}

	public void setPassword(String password) {
		mPassword = password;
	}

	public Long getId() {
		return mId;
	}

	public void setId(Long id) {
		mId = id;
	}

	public String getAvatar() {
		return mAvatar;
	}

	public void setAvatar(String avatar) {
		mAvatar = avatar;
	}

	public UserRole getRole() {
		return mRole;
	}

	public void setRole(UserRole role) {
		mRole = role;
	}

}
