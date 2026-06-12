package com.timz.rag_platform.controller;

import com.timz.rag_platform.model.Document;
import com.timz.rag_platform.model.Question;
import com.timz.rag_platform.model.User;
import com.timz.rag_platform.repository.QuestionRepository;
import com.timz.rag_platform.repository.UserRepository;
import com.timz.rag_platform.service.DocumentService;
import com.timz.rag_platform.service.RagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ChatController {

    @Autowired
    private RagService ragService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @PostMapping("/chat")
    public Map<String, String> chat(@RequestBody Map<String, String> body,
                                     Authentication auth) {
        String question = body.get("question");

        if (question == null || question.trim().isEmpty()) {
            return Map.of("reponse", "Veuillez poser une question.",
                         "sources", "");
        }

        // Recuperer l'utilisateur
        Optional<User> userOpt = userRepository.findByEmail(auth.getName());
        if (userOpt.isEmpty()) {
            return Map.of("reponse", "Utilisateur non trouve.", "sources", "");
        }
        User user = userOpt.get();

        // Recuperer les documents de l'utilisateur
        List<Document> documents = documentService.getDocumentsParUser(user);
        if (documents.isEmpty()) {
            return Map.of(
                "reponse", "Aucun document trouve. Veuillez d'abord uploader des documents.",
                "sources", ""
            );
        }

        // Extraire le texte de tous les documents
        StringBuilder contexte = new StringBuilder();
        StringBuilder sources = new StringBuilder();
        for (Document doc : documents) {
            String texte = ragService.extraireTexte(doc.getCheminFichier(), doc.getType());
            if (texte != null && !texte.isEmpty()) {
                contexte.append("=== Document: ").append(doc.getNom()).append(" ===\n");
                contexte.append(texte, 0, Math.min(texte.length(), 2000));
                contexte.append("\n\n");
                sources.append(doc.getNom()).append(", ");
            }
        }

        // Poser la question a Groq
        String reponse = ragService.poserQuestion(question, contexte.toString());

        // Sauvegarder la question en base
        Question q = new Question();
        q.setQuestion(question);
        q.setReponse(reponse);
        q.setSources(sources.toString());
        q.setUser(user);
        questionRepository.save(q);

        return Map.of(
            "reponse", reponse,
            "sources", sources.toString()
        );
    }
}