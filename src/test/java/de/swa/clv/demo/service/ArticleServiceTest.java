package de.swa.clv.demo.service;

import de.swa.clv.demo.User;
import de.swa.clv.demo.model.Article;
import de.swa.clv.demo.model.ArticleStatus;
import de.swa.clv.demo.model.Category;
import de.swa.clv.demo.model.SubCategory;
import de.swa.clv.demo.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @InjectMocks
    private ArticleService articleService;

    @Test
    void createArticle() {
        Article article = new Article();
        article.setCategory(Category.ENDOSCOPY);
        article.setSubCategory(SubCategory.LIGHTSOURCE);

        ValidationException validationErrors = assertThrows(ValidationException.class,
                () -> articleService.createArticle(article, new User()));

        List<String> fieldErrors = validationErrors.getFieldErrors();
        assertNotNull(fieldErrors);
        assertEquals(6, fieldErrors.size());
        assertTrue(fieldErrors.contains("error.validation.mandatory.article.name"));
        assertTrue(fieldErrors.contains("error.validation.mandatory.article.number"));
        assertTrue(fieldErrors.contains("error.validation.mandatory.article.status"));
        assertTrue(fieldErrors.contains("error.validation.content.regex_any.article.name"));
        assertTrue(fieldErrors.contains("error.validation.content.equals_any.article.status#initial"));
        assertTrue(fieldErrors.contains("error.validation.content.equals_any_ref.article.subCategory.label"));

    }
}
