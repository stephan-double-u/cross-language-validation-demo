package de.swa.clv.demo.service;

import de.swa.clv.demo.User;
import de.swa.clv.demo.model.*;
import de.swa.clv.demo.validation.ValidationException;
import de.swa.clv.demo.validation.ValidationRulesCheck;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static de.swa.clv.demo.validation.ValidatorProvider.VALIDATOR;

@Service
public class ArticleService implements ValidationRulesCheck {

    // The demo app simply stores the articles in a map
    private static AtomicInteger articleIdSeq = new AtomicInteger(0);
    private static Map<Integer, Article> idArticleMap = new HashMap<>();
    static {
        idArticleMap.put(articleIdSeq.incrementAndGet(), new Article(articleIdSeq.get(), "Diagnostic Video Colonoscope",
                "DVC-H123T/Z", Status.ACTIVE, (short) 9, LocalDate.of(2022, 12, 1), Category.ENDOSCOPY, SubCategory.SINUSCOPE,
                List.of(new Accessory("Biopsy Forcep", 3), new Accessory("Polyp Traps", 5)), new Date()));
    }

    public Article getArticle(int id) {
        Article article = idArticleMap.get(id);
        if (article != null) {
            return article;
        }
        throw new IllegalArgumentException("Article with id '" + id + "' does not exist.");
    }

    public Article createArticle(Article newArticle, User user) {
        newArticle.setId(null); // just a precautionary measure

        requireValidationRulesPass(newArticle, user.getPermissions());
        requireUniqueName(newArticle, false);
        // The next validation is superfluous, accessory names have been already checked!
        // It's just a demo on how to validate objects that don't implement ValidationRulesGettable
        newArticle.getAccessories()
                .forEach(acc -> requireValidationRulesPass(AccessoryRules.rules, acc, user.getPermissions()));

        newArticle.setId(articleIdSeq.incrementAndGet());
        newArticle.setLastModifiedOn(new Date());

        idArticleMap.put(newArticle.getId(), newArticle);
        return newArticle;
    }

    public Article updateArticle(Article editedArticle, User user) {
        Article currentArticle = idArticleMap.get(editedArticle.getId());

        requireValidationRulesPass(editedArticle, currentArticle, user.getPermissions());
        requireUniqueName(editedArticle, true);

        editedArticle.setLastModifiedOn(new Date());

        idArticleMap.put(editedArticle.getId(), editedArticle);
        return editedArticle;
    }

    private void requireUniqueName(Article article, boolean forUpdate) {
        idArticleMap.values().stream()
                .filter(existingArticle -> existingArticle.getName() != null)
                .filter(existingArticle -> !existingArticle.getName().equalsIgnoreCase(article.getName()) || !forUpdate)
                .filter(existingArticle -> existingArticle.getName().equalsIgnoreCase(article.getName()))
                .findFirst().ifPresent(ignore -> {
                    throw new ValidationException("error", List.of(
                            VALIDATOR.getDefaultContentMessagePrefix() + "not-unique.article.name"));
                });
    }

}
