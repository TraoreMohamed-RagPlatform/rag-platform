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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping
public class ChatController {

    @Autowired
    private RagService ragService;

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @GetMapping("/chat")
    public String chatPage(@RequestParam(value = "doc", required = false) Long docId,
                            Model model,
                            Authentication auth) {
        Optional<User> userOpt = userRepository.findByEmail(auth.getName());
        if (userOpt.isEmpty()) {
            model.addAttribute("documents", List.of());
            return "chat";
        }

        User user = userOpt.get();
        List<Document> documents = documentService.getDocumentsParUser(user);
        model.addAttribute("documents", documents);

        if (docId != null) {
            Optional<Document> docOpt = documents.stream()
                    .filter(d -> d.getId().equals(docId))
                    .findFirst();
            docOpt.ifPresent(d -> model.addAttribute("docSelectionne", d));
        }

        return "chat";
    }

    @PostMapping("/api/chat")
    @ResponseBody
    public Map<String, String> chat(@RequestBody Map<String, Object> body,
                                     Authentication auth) {
        String question = (String) body.get("question");
        Object docIdRaw = body.get("documentId");

        if (question == null || question.trim().isEmpty()) {
            return Map.of("reponse", "Veuillez poser une question.", "sources", "");
        }

        Optional<User> userOpt = userRepository.findByEmail(auth.getName());
        if (userOpt.isEmpty()) {
            return Map.of("reponse", "Utilisateur non trouve.", "sources", "");
        }
        User user = userOpt.get();

        List<Document> documents = documentService.getDocumentsParUser(user);
        if (documents.isEmpty()) {
            return Map.of(
                "reponse", "Aucun document trouve. Veuillez d'abord uploader des documents.",
                "sources", ""
            );
        }

        List<Document> documentsACibler;

        if (docIdRaw != null) {
            Long documentId = Long.valueOf(docIdRaw.toString());
            Optional<Document> docCible = documents.stream()
                    .filter(d -> d.getId().equals(documentId))
                    .findFirst();

            if (docCible.isEmpty()) {
                return Map.of("reponse", "Document introuvable ou n'appartient pas a cet utilisateur.", "sources", "");
            }
            documentsACibler = List.of(docCible.get());
        } else {
            documentsACibler = documents;
        }

        StringBuilder contexte = new StringBuilder();
        StringBuilder sources = new StringBuilder();
        for (Document doc : documentsACibler) {
            String texte = ragService.extraireTexte(doc.getCheminFichier(), doc.getType());
            if (texte != null && !texte.isEmpty()) {
                contexte.append("=== Document: ").append(doc.getNom()).append(" ===\n");
                contexte.append(texte, 0, Math.min(texte.length(), 2000));
                contexte.append("\n\n");
                sources.append(doc.getNom()).append(", ");
            }
        }

        String reponse = ragService.poserQuestion(question, contexte.toString());

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