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

import static de.swa.clv.demo.validation.ValidatorProvider.VALIDATOR;
import static java.util.Map.entry;

@RestController
public class AllInOneController {

    private final Logger log = LoggerFactory.getLogger(AllInOneController.class);

    private static final String DEFAULT_MANDATORY_MESSAGE_PREFIX = VALIDATOR.getDefaultMandatoryMessagePrefix();
    private static final String DEFAULT_CONTENT_MESSAGE_PREFIX = VALIDATOR.getDefaultContentMessagePrefix();
    private static final String DEFAULT_UPDATE_MESSAGE_PREFIX = VALIDATOR.getDefaultUpdateMessagePrefix();
    private static final String DEFAULT_IMMUTABLE_MESSAGE_PREFIX = VALIDATOR.getDefaultImmutableMessagePrefix();

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
        // Alternative:
        // return Article.RULES.serializeToJson();
    }

    @GetMapping(value = "/validation-error-messages", produces = "application/json;charset=UTF-8")
    public Map<String, String> getValidationErrorCodeToMessageMap() {
        return Map.ofEntries(
                entry(DEFAULT_MANDATORY_MESSAGE_PREFIX + "article.name",
                        "The article name is a mandatory entry."),
                entry(DEFAULT_CONTENT_MESSAGE_PREFIX + "regex_any.article.name",
                        "The name needs to be 3 to 30 characters long (no leading or trailing spaces)."),
                entry(DEFAULT_CONTENT_MESSAGE_PREFIX + "not-unique.article.name",
                        "An article with that name already exist."),
                entry(DEFAULT_MANDATORY_MESSAGE_PREFIX + "article.number",
                        "The article number is a mandatory entry."),
                entry(DEFAULT_MANDATORY_MESSAGE_PREFIX + "article.status",
                        "The article status is a mandatory entry."),
                entry(DEFAULT_CONTENT_MESSAGE_PREFIX + "equals_any.article.status#initial",
                        "The initial status must be NEW."),
                entry(DEFAULT_UPDATE_MESSAGE_PREFIX + "equals_any.article.status",
                        "This status transition is not allowed - read the docu."),
                entry("mycode.for.article.status",
                        "Hey dude, you should know, that the status can not be reset to NEW"),
                entry(DEFAULT_MANDATORY_MESSAGE_PREFIX + "article.maintenanceIntervalMonth",
                        "The interval is required when the last maintenance date is specified."),
                entry(DEFAULT_MANDATORY_MESSAGE_PREFIX + "article.maintenanceLastDate",
                        "The last maintenance date is required when the interval is specified."),
                entry(DEFAULT_CONTENT_MESSAGE_PREFIX + "date_past.article.maintenanceLastDate",
                        "The last maintenance date must not be in the future."),
                entry(DEFAULT_CONTENT_MESSAGE_PREFIX + "equals_any_ref.article.category",
                        "This category is not valid"),
                entry(DEFAULT_MANDATORY_MESSAGE_PREFIX + "article.subCategory",
                        "The sub-category is required if a category is selected"),
                entry(DEFAULT_CONTENT_MESSAGE_PREFIX + "equals_any_ref.article.subCategory",
                        "This sub-category is not valid for the selected category"),
                entry(DEFAULT_IMMUTABLE_MESSAGE_PREFIX + "article.everLeftWarehouse",
                        "This article has already left the warehouse once. This Flag must never be reset."),
                entry(DEFAULT_IMMUTABLE_MESSAGE_PREFIX + "article.animalUse",
                        "This article has already been used for animals. This Flag must never be reset."),
                entry(DEFAULT_CONTENT_MESSAGE_PREFIX + "regex_any.article.accessories[*].name",
                        "Accessory names must match '" + Article.EXAMPLE_UNICODE_PROPERTY_CLASSES_REGEX + "' ????."),
                entry(DEFAULT_CONTENT_MESSAGE_PREFIX + "equals_any.article.accessories[*].name#distinct",
                        "The accessories for this article must have unique names."),
                entry(DEFAULT_CONTENT_MESSAGE_PREFIX + "range.article.accessories[*].amount",
                        "The amount of an accessory is not within this range: [" + Article.AMOUNT_MIN + "," +
                                Article.AMOUNT_MAX + "]."),
                entry(DEFAULT_CONTENT_MESSAGE_PREFIX + "range.article.accessories[*].amount#sum",
                        "The sum of the quantities is too large (max " + Article.AMOUNT_SUM_MAX + ")."),
                entry(DEFAULT_CONTENT_MESSAGE_PREFIX + "size.article.accessories",
                        "The article has to many accessories."),
                entry(DEFAULT_UPDATE_MESSAGE_PREFIX + "size.article.accessories",
                        "The article has to many accessories."),
                entry(DEFAULT_IMMUTABLE_MESSAGE_PREFIX + "article.lastModifiedOn",
                        "The article has been modified by another user in the meantime."));
    }

    @GetMapping(value = "/category-mapping", produces = "application/json;charset=UTF-8")
    public Map<String, Map<String, Object>> getCategoryMapping() {
        return Map.of(
                "category", Arrays.stream(Category.values())
                        .collect(Collectors.toMap(Category::name, Category::asRecord)),
                "subCategory", Arrays.stream(SubCategory.values())
                        .collect(Collectors.toMap(SubCategory::name, SubCategory::asRecord)));
    }

    @PutMapping(value = "/user-permissions", produces = "application/json;charset=UTF-8")
    public void putUserPermissions(@RequestBody String[] permissions) {
        userMock.setPermissions(permissions);
        log.info("Received user permissions: " + permissions);
    }

}
