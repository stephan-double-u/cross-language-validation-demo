package de.swa.clv.demo.rest;

import de.swa.clv.ValidationRules;
import de.swa.clv.demo.User;
import de.swa.clv.demo.model.Accessory;
import de.swa.clv.demo.model.Article;
import de.swa.clv.demo.model.Category;
import de.swa.clv.demo.model.SubCategory;
import de.swa.clv.demo.service.ArticleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Map.entry;

@RestController
public class AllInOneController {

    private final Logger log = LoggerFactory.getLogger(AllInOneController.class);

    private final User userMock = new User();

    @Autowired
    private ArticleService articleService;

    @PostMapping(value = "/article", produces = "application/json;charset=UTF-8")
    public Article createArticle(@RequestBody Article article) {
        return articleService.createArticle(article, userMock);
    }

    @PutMapping(value = "/article", produces = "application/json;charset=UTF-8")
    public Article updateArticle(@RequestBody Article article) {
        return articleService.updateArticle(article, userMock);
    }

    @GetMapping(value = "/validation-rules", produces = "application/json;charset=UTF-8")
    public String getValidationRules() {
        return ValidationRules.serializeToJson(Article.RULES, Accessory.RULES);
    }

    @GetMapping(value = "/validation-error-messages", produces = "application/json;charset=UTF-8")
    public Map<String, String> getValidationErrorCodeToMessageMap() {
        return Map.ofEntries(
                entry("error.validation.mandatory.article.name",
                        "The article name is a mandatory entry."),

                entry("error.validation.content.regex_any.article.name",
                        "The name needs to be 3 to 30 characters long (no leading or trailing spaces)."),

                entry("error.validation.content.not-unique.article.name",
                        "An article with that name already exist."),

                entry("error.validation.mandatory.article.number",
                        "The article number is a mandatory entry."),

                entry("error.validation.mandatory.article.status",
                        "The article status is a mandatory entry."),

                entry("error.validation.content.equals_any.article.status#initial",
                        "The initial status must be NEW."),

                entry("error.validation.update.equals_any.article.status",
                        "This status transition is not allowed - read the docu."),

                entry("mycode.for.article.status",
                        "Hey dude, you should know, that the status can not be reset to NEW"),

                entry("error.validation.mandatory.article.maintenanceIntervalMonth",
                        "The interval is needed, if the last maintenance date is given."),

                entry("error.validation.mandatory.article.maintenanceLastDate",
                        "The last maintenance date is needed, if the interval is given."),

                entry("error.validation.content.equals_any_ref.article.category",
                        "This category is not valid"),

                entry("error.validation.mandatory.article.subCategory",
                        "The sub-category is required if a category is selected"),

                entry("error.validation.content.equals_any_ref.article.subCategory",
                        "This sub-category is not valid for the selected category"),

                entry("error.validation.immutable.article.everLeftWarehouse",
                        "This article has already left the warehouse once. This Flag must never be reset."),

                entry("error.validation.immutable.article.animalUse",
                        "This article has already been used for animals. This Flag must never be reset."),

                entry("error.validation.content.regex_any.article.accessories[*].name",
                        "Accessory names must match '" + Article.EXAMPLE_UNICODE_PROPERTY_CLASSES_REGEX + "' 🙂."),

                entry("error.validation.content.equals_any.article.accessories[*].name#distinct",
                        "The accessories for this article must have unique names."),

                entry("error.validation.content.range.article.accessories[*].amount",
                        "The amount of an accessory is not within this range: [" + Article.AMOUNT_MIN + "," +
                                Article.AMOUNT_MAX + "]."),

                entry("error.validation.content.range.article.accessories[*].amount#sum",
                        "The sum of the quantities is too large (max " + Article.AMOUNT_SUM_MAX + ")."),

                entry("error.validation.content.size.article.accessories",
                        "The article has to many accessories."),

                entry("error.validation.update.size.article.accessories",
                        "The article has to many accessories."),

                entry("error.validation.immutable.article.lastModifiedOn",
                        "The article has been modified by another user in the meantime.")
                );
    }

    @GetMapping(value = "/category-mapping", produces = "application/json;charset=UTF-8")
    public Map<String, Map<String, Object>> getCategoryMapping() {
        return Map.of("category",
                Arrays.stream(Category.values())
                        .collect(Collectors.toMap(Category::name, Category::asRecord)),
                "subCategory",
                Arrays.stream(SubCategory.values())
                        .collect(Collectors.toMap(SubCategory::name, SubCategory::asRecord))
        );
    }

    @PutMapping(value = "/user-permissions", produces = "application/json;charset=UTF-8")
    public void putUserPermissions(@RequestBody String[] permissions) {
        userMock.setPermissions(permissions);
        log.info("Received user permissions: " + permissions);
    }

}
