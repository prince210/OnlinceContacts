package com.smart.entities;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

import org.hibernate.validator.constraints.NotBlank;

@Entity
@Table(name="USER")
public class User {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private int id;
	
	@NotBlank(message="name should not be blank")
	@Size(min=2,max=20,message="min 2 and max 20 characters are allowed")
	private String name;
	
	@Column(unique = true)
	@Pattern(regexp="^(.+)@(.+)$",message="Invalid Email")
	private String email;
	
	@NotBlank(message="password should not be blank")
//	@Size(min=3,max=20,message="min 3 and max 20 characters are allowed")
//	@Pattern(regexp = "",
//	message="Password must contain atleast one digit"
//			+ " Password must contain atleast one lowercase "
//			+ " Password must contain atleast one uppercase "
//			+ " Password must contain atleast one special characters "
//			+ " Password must not contain white spaces")
	private String password;
	
	private String role;
	private boolean isEnabled;
	private String imageUrl;
	
	@Column(length = 500)
	private String about;
	
	@OneToMany(cascade = CascadeType.ALL,fetch = FetchType.EAGER,mappedBy = "users", orphanRemoval = true)
	private List<Contact> contacts = new ArrayList<>();
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public boolean isEnabled() {
		return isEnabled;
	}
	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}
	public String getImageUrl() {
		return imageUrl;
	}
	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}
	public String getAbout() {
		return about;
	}
	public void setAbout(String about) {
		this.about = about;
	}
	public User() {
		super();
		// TODO Auto-generated constructor stub
	}
	public List<Contact> getContacts() {
		return contacts;
	}
	public void setContacts(List<Contact> contacts) {
		this.contacts = contacts;
	}
	@Override
	public String toString() {
		return "User [id=" + id + ", name=" + name + ", email=" + email + ", password=" + password + ", role=" + role
				+ ", isEnabled=" + isEnabled + ", imageUrl=" + imageUrl + ", about=" + about + "]";
	}

	
	
}
