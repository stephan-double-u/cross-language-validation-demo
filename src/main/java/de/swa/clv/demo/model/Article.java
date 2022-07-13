package de.swa.clv.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.swa.clv.UseType;
import de.swa.clv.ValidationRules;
import de.swa.clv.constraints.*;
import de.swa.clv.demo.Util;
import de.swa.clv.demo.validation.ValidationRulesGettable;
import de.swa.clv.groups.ConditionsGroup;
import de.swa.clv.groups.ConditionsTopGroup;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;

import static de.swa.clv.demo.model.ArticleStatus.*;
import static de.swa.clv.demo.model.Permission.DecommissionAssets;
import static java.lang.Boolean.TRUE;

public final class Article implements ValidationRulesGettable<Article> {

    public static final int AMOUNT_MIN = 1;
    public static final int AMOUNT_MAX = 5;
    public static final int AMOUNT_SUM_MAX = 20;
    // The CLV client implementation has to support all used regex features as well!
    // Note: to transfer valid regex strings, the CLV Java implementation has to 'double' all backslashes during
    // serialization: regEx.replaceAll("\\\\", "\\\\\\\\") 🙄
    public static final String EXAMPLE_UNICODE_PROPERTY_CLASSES_REGEX = "^[\\p{L}][\\p{L}\\p{N} ]*$";

    /*
       Notes:
       (1) demo on how to add suffix to the error code
       (2) demo on how to replace the error code by own code resp. own message
           ".article.status" is needed here by frontend to assign the error message to the #statusErr element
       (3) Enums are objects in Java and serialized as strings by default. Therefore, nested properties of enums like
           "category.subCategories[*]" can't be validated in a Javascript frontend easily.
           With the help of the method "doNotSerialize()" it can be prevented that such rules are serialized.
           Besides, it is also likely not necessary to validate the rule in the frontend, because the synchronization of
           the select boxes, which is done in a frontend anyway, ensures that no wrong sub-category is transferred.
       (4) 'technical' rule for concurrent modification detection
     */
    public static final ValidationRules<Article> RULES = new ValidationRules<>(Article.class);

    static {
        RULES.mandatory("name");
        RULES.content("name", RegEx.any("^[^ ].{1,28}[^ ]$"));

        RULES.mandatory("number");

        RULES.mandatory("status");
        RULES.immutable("status",
                Condition.of("status", Equals.any(DECOMMISSIONED)));
        RULES.content("status", Equals.any(NEW),
                Condition.of("id", Equals.null_()))
                .errorCodeControl(UseType.AS_SUFFIX, "#initial"); // (1)
        RULES.update("status", Equals.any(NEW, ACTIVE, INACTIVE),
                Condition.of("status", Equals.any(NEW)));
        RULES.update("status", Equals.any(ACTIVE, INACTIVE),
                Condition.of("status", Equals.any(ACTIVE, INACTIVE)));
        RULES.update("status", Equals.any(ACTIVE, INACTIVE, DECOMMISSIONED),
                Permissions.any(DecommissionAssets),
                Condition.of("status", Equals.any(ACTIVE, INACTIVE)))
                .errorCodeControl(UseType.AS_REPLACEMENT, "mycode.for.article.status"); // (2)

        RULES.mandatory("maintenanceIntervalMonth",
                ConditionsGroup.AND(
                        Condition.of("maintenanceLastDate", Equals.notNull()),
                        Condition.of("maintenanceIntervalMonth", Equals.null_())));
        RULES.mandatory("maintenanceLastDate",
                ConditionsGroup.AND(
                        Condition.of("maintenanceIntervalMonth", Equals.notNull()),
                        Condition.of("maintenanceLastDate", Equals.null_())));

        RULES.content("category", Equals.any(Util.appendNull(Category.values()))); // better API needed?!
        RULES.mandatory("subCategory",
                Condition.of("category", Equals.notNull()));
        RULES.content("subCategory", Equals.anyRef("category.subCategories[*]", null))
                .doNotSerialize(); // (3)

        RULES.immutable("everLeftWarehouse",
                Condition.of("everLeftWarehouse", Equals.any(TRUE)));

        RULES.immutable("animalUse",
                ConditionsTopGroup.OR(
                        ConditionsGroup.AND(
                                Condition.of("animalUse", Equals.any(TRUE)),
                                Condition.of("everLeftWarehouse", Equals.any(TRUE))),
                        ConditionsGroup.AND(
                                Condition.of("medicalSet", Equals.notNull()))));

        RULES.content("accessories[*].name", RegEx.any(EXAMPLE_UNICODE_PROPERTY_CLASSES_REGEX));
        RULES.content("accessories[*].name#distinct", Equals.any(true));
        RULES.content("accessories[*].amount", Range.minMax(AMOUNT_MIN, AMOUNT_MAX));
        RULES.content("accessories[*].amount#sum", Range.max(AMOUNT_SUM_MAX));

        RULES.content("accessories", Size.max(3),
                Condition.of("id", Equals.null_()));
        RULES.content("accessories", Size.max(AMOUNT_MAX),
                Permissions.any(Permission.MANAGER));
        RULES.update("accessories", Size.max(3),
                Permissions.none(Permission.MANAGER),
                Condition.of("accessories", Size.max(3)));
        RULES.update("accessories", Size.max(4),
                Permissions.none(Permission.MANAGER),
                Condition.of("accessories", Size.minMax(4, 4)));
        RULES.update("accessories", Size.max(AMOUNT_MAX),
                Permissions.none(Permission.MANAGER),
                Condition.of("accessories", Size.minMax(AMOUNT_MAX, AMOUNT_MAX)));

        RULES.immutable("lastModifiedOn"); // (4)
    }

    private Integer id;
    private Date lastModifiedOn;
    private String name;
    private String number;
    private ArticleStatus status;
    private String medicalSet;
    private boolean animalUse;
    private boolean everLeftWarehouse;
    private LocalDate maintenanceLastDate;
    private Short maintenanceIntervalMonth;
    private Category category;
    private SubCategory subCategory;
    private List<Accessory> accessories = List.of();

    public Article() { /**/
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public ArticleStatus getStatus() {
        return status;
    }

    public void setStatus(ArticleStatus status) {
        this.status = status;
    }

    public boolean isAnimalUse() {
        return animalUse;
    }

    public void setAnimalUse(boolean animalUse) {
        this.animalUse = animalUse;
    }

    public boolean isEverLeftWarehouse() {
        return everLeftWarehouse;
    }

    public void setEverLeftWarehouse(boolean everLeftWarehouse) {
        this.everLeftWarehouse = everLeftWarehouse;
    }

    public String getMedicalSet() {
        return medicalSet;
    }

    public void setMedicalSet(String medicalSet) {
        this.medicalSet = medicalSet;
    }

    public Date getLastModifiedOn() {
        return lastModifiedOn;
    }

    public void setLastModifiedOn(Date lastModifiedOn) {
        this.lastModifiedOn = lastModifiedOn;
    }

    public List<Accessory> getAccessories() {
        return accessories;
    }

    public void setAccessories(List<Accessory> accessories) {
        this.accessories = accessories;
    }

    public LocalDate getMaintenanceLastDate() {
        return maintenanceLastDate;
    }

    public void setMaintenanceLastDate(LocalDate maintenanceLastDate) {
        this.maintenanceLastDate = maintenanceLastDate;
    }

    public Short getMaintenanceIntervalMonth() {
        return maintenanceIntervalMonth;
    }

    public void setMaintenanceIntervalMonth(Short maintenanceIntervalMonth) {
        this.maintenanceIntervalMonth = maintenanceIntervalMonth;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public SubCategory getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(SubCategory subCategory) {
        this.subCategory = subCategory;
    }

    @JsonIgnore
    @Override
    public ValidationRules<Article> getValidationRules() {
        return RULES;
    }
}
