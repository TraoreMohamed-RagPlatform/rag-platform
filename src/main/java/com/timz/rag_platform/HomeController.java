package com.timz.rag_platform;

import com.timz.rag_platform.model.User;
import com.timz.rag_platform.repository.DocumentRepository;
import com.timz.rag_platform.repository.QuestionRepository;
import com.timz.rag_platform.repository.UserRepository;
import com.timz.rag_platform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        model.addAttribute("nombreDocuments", documentRepository.count());
        model.addAttribute("nombreUsers", userRepository.count());
        model.addAttribute("nombreQuestions", questionRepository.count());
        return "dashboard";
    }

    @GetMapping("/chat")
    public String chat() {
        return "chat";
    }

    @GetMapping("/history")
    public String history(Model model, Authentication auth) {
        Optional<User> userOpt = userRepository.findByEmail(auth.getName());
        if (userOpt.isPresent()) {
            model.addAttribute("questions", questionRepository.findByUserOrderByCreatedAtDesc(userOpt.get()));
            model.addAttribute("nombreQuestions", questionRepository.countByUser(userOpt.get()));
        } else {
            model.addAttribute("questions", java.util.List.of());
            model.addAttribute("nombreQuestions", 0);
        }
        return "history";
    }

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("users", userService.getTousLesUsers());
        model.addAttribute("nombreUsers", userRepository.count());
        model.addAttribute("nombreDocuments", documentRepository.count());
        model.addAttribute("nombreQuestions", questionRepository.count());
        return "admin";
    }

    @PostMapping("/admin/ajouter")
    public String ajouterUser(@RequestParam String prenom,
                              @RequestParam String nom,
                              @RequestParam String email,
                              @RequestParam String password,
                              @RequestParam String role,
                              RedirectAttributes redirectAttributes) {
        try {
            userService.creerUser(email, password, nom, prenom, User.Role.valueOf(role));
            redirectAttributes.addFlashAttribute("success", "Utilisateur ajoute avec succes !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin";
    }

    @PostMapping("/admin/supprimer/{id}")
    public String supprimerUser(@PathVariable Long id,
                                RedirectAttributes redirectAttributes) {
        try {
            userService.supprimerUser(id);
            redirectAttributes.addFlashAttribute("success", "Utilisateur supprime !");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Erreur lors de la suppression.");
        }
        return "redirect:/admin";
    }
}