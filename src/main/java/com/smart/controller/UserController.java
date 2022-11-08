package com.smart.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.smart.dao.ContactRepository;
import com.smart.dao.UserRepository;
import com.smart.entities.Contact;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
@RequestMapping("/user")
public class UserController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;

	@Autowired
	private ContactRepository contactRepository;
	@ModelAttribute
	public void addCommonData(Model model,Principal principal)
	{
		String username = principal.getName();
		User user = this.userRepository.getUserByUserName(username);
		System.out.println("USER "+user);
		model.addAttribute("user",user);
	}
	
	@GetMapping("/index")
	public String dashboard(Model model,Principal principal)
	{
		
		return "norm/user_dashboard";
	}
	
//	@RequestMapping(value = {"/logout"}, method = RequestMethod.POST)
//	public String logoutDo(HttpServletRequest request,HttpServletResponse response){
//	HttpSession session= request.getSession(false);
//	    SecurityContextHolder.clearContext();
//	         session= request.getSession(false);
//	        if(session != null) {
//	            session.invalidate();
//	        }
//	        for(Cookie cookie : request.getCookies()) {
//	            cookie.setMaxAge(0);
//	        }
//
//	    return "logout";
//	}
	
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model)
	{
		model.addAttribute("contact",new Contact());
		return "norm/add_contact_form";
	}
	
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact, 
			@RequestParam("profileImage") MultipartFile file,
			Principal principal,
			HttpSession session)
	{
		try
		{
			System.out.println("contact FORM "+contact);
			String name = principal.getName();
			User user = this.userRepository.getUserByUserName(name);
			
			// processing file
			if(file.isEmpty())
			{
				System.out.println("File is empty");
				contact.setImage("contact.png");
			}else
			{
				contact.setImage(file.getOriginalFilename());
				File saveFile = new ClassPathResource("static/img").getFile();
				System.out.println("FilePath is ::: "+ saveFile.getAbsolutePath());
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				System.out.println("Image is uploaded");
			}
			
			contact.setUsers(user);
			user.getContacts().add(contact);
			this.userRepository.save(user);
			session.setAttribute("message", new Message("Your contact is added successfully!!", "success"));
		}
		catch (Exception e) {
			System.out.println("ERROR "+e.getMessage());
			e.printStackTrace();
			session.setAttribute("message", new Message("Something went wrong", "danger"));
		}
		return "norm/add_contact_form";
	}
	
	@GetMapping("/show-contacts")
	public String showContacts(Model model,Principal principal)
	{
		String name = principal.getName();
		User user = this.userRepository.getUserByUserName(name);
		List<Contact> contacts = this.contactRepository.findContactByUser(user.getId());
		System.out.println(contacts);
		model.addAttribute("contacts", contacts);
		return "norm/show-contacts";
	}
	
	@GetMapping("/{cId}/contact")
	public String showContactDetail(@PathVariable("cId") Integer cId,Model model,Principal principal)
	{
		
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		if(user.getId() == contact.getUsers().getId())
			model.addAttribute("contact", contact);
		
		return "norm/contact_detail";
	}
	
	
	@GetMapping("/delete/{cId}")
	public String deleteContact(@PathVariable("cId") Integer cId,Model model,Principal principal,
			HttpSession session) throws IOException
	{
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		
		String userName = principal.getName();
		User user = this.userRepository.getUserByUserName(userName);
		if(user.getId() == contact.getUsers().getId())
			{
				contact.setUsers(null);
				user.getContacts().remove(contact);
				this.userRepository.save(user);
//				System.out.println("parent of deleted user contacts "+user.getContacts().get(0).toString()+ " " + user.getContacts().get(1).toString());
				// image remove
		
				String image = contact.getImage();
				File parentFolderPath = new ClassPathResource("static/img").getFile();
				System.out.println("FilePath is ::: "+parentFolderPath.getAbsolutePath() + File.separator + image);
				Path path = Paths.get(parentFolderPath.getAbsolutePath() + File.separator + image);
				Files.delete(path);
			}
		session.setAttribute("message", new Message("Contact deleted successfully","successs"));
		return "redirect:/user/show-contacts";
	}
	
	@PostMapping("/update-contact/{cId}")
	public String updateForm(@PathVariable("cId") Integer cId, Model model)
	{
		Optional<Contact> contactOptional = this.contactRepository.findById(cId);
		Contact contact = contactOptional.get();
		model.addAttribute("contact",contact);
		return "norm/update_form";
	}
	
	// update contact handler
	
	@RequestMapping(value = "/process-update",method = RequestMethod.POST)
	public String updateHandler(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file,
			HttpSession session,Model model,Principal principal)
	{
		try {
			System.out.println("contact ------>  "+contact.getcId());
			Optional<Contact> oldContactDetail = this.contactRepository.findById(contact.getcId());
			if(!file.isEmpty())
			{
				// update image if selected
				//delete old Img
				String image = file.getOriginalFilename();
				File parentFolderPath = new ClassPathResource("static/img").getFile();
//				System.out.println("FilePath is ::: "+parentFolderPath.getAbsolutePath() + File.separator + image);
				Path path = Paths.get(parentFolderPath.getAbsolutePath() + File.separator + image);
				Files.delete(path);
				
				// update new Img
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				// updating contact image with new image name
				contact.setImage(file.getOriginalFilename());
			}
			else
			{
				contact.setImage(oldContactDetail.get().getImage());
			}
			
			User user = this.userRepository.getUserByUserName(principal.getName());
			contact.setUsers(user);
			session.setAttribute("message", new Message("Contact updated successfully","successs"));
			System.out.println("updated contact (( " + contact.getcId() + " " + contact.getPhone() +" " + contact.getName() + " " + contact.getSecondName() + " " + contact.getImage() +" ))");
			this.contactRepository.save(contact);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/user/show-contacts";
	}
	
	@GetMapping("/profile")
	public String yourProfile(Model model)
	{
		return "norm/profile";
	}
	
	@GetMapping("/settings")
	public String settings()
	{
		return "norm/settings";
	}
	
	@PostMapping("/process-setting")
	public String processSettings(@ModelAttribute User userModel,
			@RequestParam("confirmPassword") String confirmPassword,
			Model model,@RequestParam("profileImage") MultipartFile file) throws IOException
	{
		User user = this.userRepository.getUserByUserName(userModel.getEmail());
		System.out.println(userModel.toString());
		
		if(!file.isEmpty())
		{
			// update image if selected
			//delete old Img
			String image = file.getOriginalFilename();
			File parentFolderPath = new ClassPathResource("static/img").getFile();
//			System.out.println("FilePath is ::: "+parentFolderPath.getAbsolutePath() + File.separator + image);
			Path path = Paths.get(parentFolderPath.getAbsolutePath() + File.separator + image);
//			Files.delete(path);
			
			// update new Img
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			
			// updating contact image with new image name
			user.setImageUrl(file.getOriginalFilename());
		}

		user.setAbout(userModel.getAbout());
		user.setPassword(passwordEncoder.encode(userModel.getPassword()));
		System.out.println("updated user "+user.toString());
		this.userRepository.save(user);
		return "redirect:/user/profile";
	}
}
