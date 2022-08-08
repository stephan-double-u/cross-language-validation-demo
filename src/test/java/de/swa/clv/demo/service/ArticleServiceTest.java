package de.swa.clv.demo.service;

import de.swa.clv.demo.User;
import de.swa.clv.demo.model.Accessory;
import de.swa.clv.demo.model.Article;
import de.swa.clv.demo.model.Category;
import de.swa.clv.demo.validation.ValidationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class ArticleServiceTest {

    @InjectMocks
    private ArticleService articleService;

    @Test
    void validateAllRulePropertiesAreValid() {
        try {
            new Article();
        } catch (Exception e) {
            fail();
        }
    }

    @Test
    void createArticle() {
        Article article = new Article();
        article.setMaintenanceNextDate(LocalDate.MAX);
        article.setCategory(Category.ENDOSCOPY);
        article.setSubCategory(Category.IMAGING_SYSTEM.getSubCategories().get(0));
        article.setAccessories(List.of(
                new Accessory("...", 1),
                new Accessory("Acc1", 1),
                new Accessory("Acc1", 1),
                new Accessory("Acc2", 20)));
        User user = new User();

        ValidationException validationErrors = assertThrows(ValidationException.class,
                () -> articleService.createArticle(article, user));

        List<String> fieldErrors = validationErrors.getFieldErrors();
        assertNotNull(fieldErrors);
        System.out.println(fieldErrors);
        assertEquals(12, fieldErrors.size());
        assertTrue(fieldErrors.contains("error.validation.mandatory.article.name"));
        assertTrue(fieldErrors.contains("error.validation.mandatory.article.number"));
        assertTrue(fieldErrors.contains("error.validation.mandatory.article.status"));
        assertTrue(fieldErrors.contains("error.validation.mandatory.article.maintenanceIntervalMonth"));
        assertTrue(fieldErrors.contains("error.validation.content.regex_any.article.name"));
        assertTrue(fieldErrors.contains("error.validation.content.equals_any.article.status#initial"));
        assertTrue(fieldErrors.contains("error.validation.content.future_days.article.maintenanceNextDate"));
        assertTrue(fieldErrors.contains("error.validation.content.equals_any_ref.article.subCategory"));
        assertTrue(fieldErrors.contains("error.validation.content.regex_any.article.accessories[*].name"));
        assertTrue(fieldErrors.contains("error.validation.content.range.article.accessories[*].amount"));
        assertTrue(fieldErrors.contains("error.validation.content.range.article.accessories[*].amount#sum"));
        assertTrue(fieldErrors.contains("error.validation.content.size.article.accessories"));
    }
}
