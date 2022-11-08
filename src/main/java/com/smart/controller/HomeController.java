package com.smart.controller;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;

@Controller
public class HomeController {
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	
	@Autowired
	private UserRepository userRepository;
	
	@GetMapping("/")
	public String home()
	{
		return "home";
	}

	@GetMapping("/about")
	public String about()
	{
		return "about";
	}
	
	@GetMapping("/signup")
	public String signup(Model model)
	{
		model.addAttribute("user",new User());
		return "signup";
	}
	
	@GetMapping("/signin")
	public String signin(Model model)
	{
		return "login";
	}
	
	@GetMapping("/user_dashboard")
	public String userDashboard(Model model)
	{
		return "user_dashboard";
	}
	
//	@GetMapping("/logout")
//	public String logout(Model model)
//	{
//		return "login";
//	}
	
	
	@RequestMapping(value = {"/logout"}, method = RequestMethod.POST)
	public String logoutDo(HttpServletRequest request,HttpServletResponse response){
	HttpSession session= request.getSession(false);
	    SecurityContextHolder.clearContext();
	         session= request.getSession(false);
	        if(session != null) {
	            session.invalidate();
	        }
	        for(Cookie cookie : request.getCookies()) {
	            cookie.setMaxAge(0);
	        }

	    return "logout";
	}
	
	@PostMapping("/do_register")
	public String registerUser(@Valid @ModelAttribute("user") User user,BindingResult result,
			@RequestParam(value="agreement",defaultValue ="false")  boolean agreement,
			Model model,
			HttpSession session)
	
	{
		System.out.println("user is :" + user);
		try {
			if(!agreement)
				throw new Exception("Please agree terms and conditions");
			
			if(result.hasErrors())
			{
				System.out.println("Error "+result.toString());
				model.addAttribute("user",user); 
				return "signup";
			}
			user.setRole("ROLE_USER");
			user.setEnabled(true);
			user.setImageUrl("default.png");
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			System.out.println("Agreement "+agreement);
			System.out.println("User "+user);
			this.userRepository.save(user);
			model.addAttribute("user",new User());
			session.setAttribute("message", new Message("successfully registered ","alert-success"));
			return "signup";
		}
		catch(Exception e)
		{
			e.printStackTrace();
			model.addAttribute("user",user);
			session.setAttribute("message", new Message("something went wrong🤔 "+e.getMessage(),"alert-danger"));
			return "signup";
		}
		
	}
}
