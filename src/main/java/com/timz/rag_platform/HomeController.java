package com.timz.rag_platform;

import com.timz.rag_platform.model.User;
import com.timz.rag_platform.repository.DocumentRepository;
import com.timz.rag_platform.repository.QuestionRepository;
import com.timz.rag_platform.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

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
}