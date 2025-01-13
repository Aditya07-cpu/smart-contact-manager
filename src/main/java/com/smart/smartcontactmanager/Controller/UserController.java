package com.smart.smartcontactmanager.Controller;

import com.smart.smartcontactmanager.Helper.Message;
import com.smart.smartcontactmanager.Model.Contact;
import com.smart.smartcontactmanager.Model.User;
import com.smart.smartcontactmanager.Repository.ContactRepository;
import com.smart.smartcontactmanager.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @ModelAttribute
    public void addCommonData(Model model, Principal principal) {
        String userName = principal.getName();

        User user = userRepository.getUserByUsername(userName);

        model.addAttribute("user", user);
    }

    @GetMapping("/index")
    public String dashboard(Model model, Principal principal) {
        model.addAttribute("title", "User Dashboard");
        return "normal/user_dashboard";
    }

    @GetMapping("/add-contact")
    public String openAddContactForm(Model model) {
        model.addAttribute("title", "Add Contact");
        model.addAttribute("contact", new Contact());
        return "normal/add_contact_form";
    }

    @PostMapping("/process-contact")
    public String processContract(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Principal principal, HttpSession session) {
        try {
            String userName = principal.getName();
            User user = userRepository.getUserByUsername(userName);
            contact.setUser(user);

            if (file.isEmpty()) {
                System.out.println("File Not Found");
                contact.setImage("contact.png");
            }
            else {
                contact.setImage(file.getOriginalFilename());

                File saveFile = new ClassPathResource("static/img").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                System.out.println("Image Uploaded Successfully");
            }

            user.getContacts().add(contact);
            userRepository.save(user);

            session.setAttribute("message", new Message("Contact Added Successfully.", "alert-success"));
        }
        catch (Exception exception) {
            exception.printStackTrace();
            session.setAttribute("message", new Message("Something Went Wrong. Try Again !!", "alert-danger"));
        }
        return "normal/add_contact_form";
    }

    @GetMapping("/show-contacts/{page}")
    public String showContacts(@PathVariable Integer page, Model model, Principal principal) {
        model.addAttribute("title", "Show Contacts");

        String userName = principal.getName();

        User user = userRepository.getUserByUsername(userName);

        Pageable pageable = PageRequest.of(page, 5);

        Page<Contact> contactList = contactRepository.findContactsByUser(user.getId(), pageable);

        model.addAttribute("contacts", contactList);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPage", contactList.getTotalPages());

        return "normal/show_contacts";
    }

    @GetMapping("/{cId}/contact")
    public String showContactDetails(@PathVariable Integer cId, Model model, Principal principal) {

        Optional<Contact> contactOptional = contactRepository.findById(cId);
        Contact contact = contactOptional.get();

        String userName = principal.getName();
        User user = userRepository.getUserByUsername(userName);

        if(user.getId() == contact.getUser().getId()) {
            model.addAttribute("contact", contact);
            model.addAttribute("title", contact.getName());
        }

        return "normal/contact_details";
    }

    @GetMapping("/delete/{cId}")
    public String deleteContact(@PathVariable Integer cId, Model model, HttpSession session) {
        Contact contact = contactRepository.findById(cId).get();

        contact.setUser(null);

        contactRepository.delete(contact);

        session.setAttribute("message", new Message("Contact Deleted Successfully", "alert-success"));

        return "redirect:/user/show-contacts/0";
    }

    @PostMapping("/update-contact/{cid}")
    public String updateForm(@PathVariable Integer cid, Model model) {
        model.addAttribute("title", "Update Contact");

        Contact contact = contactRepository.findById(cid).get();

        model.addAttribute("contact", contact);

        return "normal/update_form";
    }

    @PostMapping("/process-update")
    public String updateContact(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, Model model, Principal principal, HttpSession session) {
        try {

            Contact oldContact = contactRepository.findById(contact.getCId()).get();

            if(!file.isEmpty()) {

                File saveFile = new ClassPathResource("static/img").getFile();

                Path path = Paths.get(saveFile.getAbsolutePath() + File.separator + file.getOriginalFilename());

                Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

                contact.setImage(file.getOriginalFilename());
            }
            else {
                contact.setImage(oldContact.getImage());
            }

            User user = userRepository.getUserByUsername(principal.getName());

            contact.setUser(user);

            contactRepository.save(contact);

            session.setAttribute("message", new Message("Contact Updated Successfully", "alert-success"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return "redirect:/user/" + contact.getCId() + "/contact";
    }

    @GetMapping("/profile")
    public String yourProfile(Model model) {
        model.addAttribute("title", "Profile Page");
        return "normal/profile";
    }

    @GetMapping("/settings")
    public String openSettings(Model model) {
        model.addAttribute("title", "Settings");
        return "normal/settings";
    }

    @PostMapping("/change-password")
    public String changePassword(@RequestParam("oldPassword") String oldPassword, @RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {
        User user = userRepository.getUserByUsername(principal.getName());

        if (passwordEncoder.matches(oldPassword, user.getPassword())) {
            user.setPassword(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            session.setAttribute("message", new Message("Your Password Has Been Changed Successfully", "alert-success"));
        }
        else {
            session.setAttribute("message", new Message("Please Enter Correct Old Password", "alert-danger"));
            return "redirect:/user/settings";
        }

        return "redirect:/user/index";
    }
}
