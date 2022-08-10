package de.swa.clv.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.swa.clv.UseType;
import de.swa.clv.ValidationRules;
import de.swa.clv.constraints.*;
import de.swa.clv.demo.validation.ValidationRulesGettable;
import de.swa.clv.groups.ConditionsGroup;
import de.swa.clv.groups.ConditionsTopGroup;

import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import static de.swa.clv.demo.model.Status.*;
import static de.swa.clv.demo.model.Permission.DecommissionAssets;
import static java.lang.Boolean.TRUE;
import static java.time.DayOfWeek.*;

public final class Article implements ValidationRulesGettable<Article> {

    private static final String TRIMMED_3_TO_30_REGEX = "^(?! ).{3,30}(?<! )$";
    public static final String EXAMPLE_UNICODE_PROPERTY_CLASSES_REGEX = "^[\\p{L}][\\p{L}\\p{N} ]*$";
    public static final int AMOUNT_MIN = 1;
    public static final int AMOUNT_MAX = 10;
    public static final int AMOUNT_SUM_MAX = 20;

    /*
       Remarks about the demo validation rules:
       (1) example on how to add suffix to the error code
       (2) example on how to replace the error code by own code (resp. own message)
           ".article.status" is needed here by frontend to assign the error message to the #statusErr element
       (3) example on how multiple rules for the same property (here: "maintenanceNextDate") and the same type
           (here: _content_) and different constraints can be defined
       (4) Enums are objects in Java and serialized as strings by default. Therefore, nested properties of enums like
           "category.subCategories[*]" can't be validated in a Javascript frontend easily.
           With the help of the method "doNotSerialize()" it can be prevented that such rules are serialized.
           Besides, it is also likely not necessary to validate the rule in the frontend, because the synchronization of
           the select boxes, which is done in a frontend anyway, ensures that no wrong sub-category is transferred.
       (5) example for a complex rule the multiple conditions needs to be logically linked with AND _and_ OR.
       (6) If rules with RegEx constraints should be validated, the CLV client implementation has to support all used
           regex features as well, e.g. unicode property escapes like "\p{L}".
       (7) 'technical' rule for concurrent modification detection
     */
    public static final ValidationRules<Article> RULES = new ValidationRules<>(Article.class);
    static {
        RULES.mandatory("name");
        RULES.content("name", RegEx.any(TRIMMED_3_TO_30_REGEX));

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
                ConditionsGroup.OR(
                        Condition.of("maintenanceNextDate", Equals.notNull()),
                        Condition.of("maintenanceIntervalMonth", Equals.notNull())));
        RULES.mandatory("maintenanceNextDate",
                ConditionsGroup.OR(
                        Condition.of("maintenanceIntervalMonth", Equals.notNull()),
                        Condition.of("maintenanceNextDate", Equals.notNull())));
        RULES.content("maintenanceNextDate", Future.minMaxDays(1, 365),
                Condition.of("maintenanceNextDate", Equals.notNull())); // (3)
        RULES.content("maintenanceNextDate", Weekday.anyOrNull(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)); // (3)
        RULES.content("maintenanceNextDate", Equals.none(getFakedCompanyVacationDates())); // (3)

        RULES.content("category", Equals.anyOrNull(Category.values()));
        RULES.mandatory("subCategory",
                Condition.of("category", Equals.notNull()));
        RULES.content("subCategory", Equals.anyRefOrNull("category.subCategories[*]"))
                .doNotSerialize(); // (4)

        RULES.immutable("everLeftWarehouse",
                Condition.of("everLeftWarehouse", Equals.any(TRUE)));

        RULES.immutable("animalUse",
                ConditionsTopGroup.OR(
                        ConditionsGroup.AND(
                                Condition.of("animalUse", Equals.any(TRUE)),
                                Condition.of("everLeftWarehouse", Equals.any(TRUE))),
                        ConditionsGroup.AND(
                                Condition.of("medicalSet", Equals.notNull())))); //(5)

        RULES.content("accessories", Size.max(3),
                Condition.of("id", Equals.null_()));
        RULES.content("accessories", Size.max(4),
                Permissions.any(Permission.MANAGER));
        RULES.update("accessories", Size.max(3),
                Permissions.none(Permission.MANAGER),
                Condition.of("accessories", Size.max(3)));
        RULES.update("accessories", Size.max(4),
                Permissions.none(Permission.MANAGER),
                Condition.of("accessories", Size.minMax(4, 4)));

        RULES.content("accessories[*].name", RegEx.any(EXAMPLE_UNICODE_PROPERTY_CLASSES_REGEX)); // (6)
        RULES.content("accessories[*].name#distinct", Equals.any(true));
        RULES.content("accessories[*].amount", Range.minMax(AMOUNT_MIN, AMOUNT_MAX));
        RULES.content("accessories[*].amount#sum", Range.max(AMOUNT_SUM_MAX));

        RULES.immutable("lastModifiedOn"); // (7)
    }

    private Integer id;
    private Date lastModifiedOn;
    private String name;
    private String number;
    private Status status;
    private String medicalSet;
    private boolean animalUse;
    private boolean everLeftWarehouse;
    private LocalDate maintenanceNextDate;
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

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
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

    public LocalDate getMaintenanceNextDate() {
        return maintenanceNextDate;
    }

    public void setMaintenanceNextDate(LocalDate maintenanceNextDate) {
        this.maintenanceNextDate = maintenanceNextDate;
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

    private static LocalDate[] getFakedCompanyVacationDates() {
        LocalDate today = LocalDate.now();
        List<LocalDate> augustDates = IntStream.rangeClosed(1, 31).boxed()
                .map(i -> {
                    LocalDate augustDate = LocalDate.of(today.getYear(), 8, i);
                    if (!augustDate.isAfter(today)) {
                        augustDate = LocalDate.of(today.getYear() + 1, 8, i);
                    }
                    return augustDate;
                }).toList();
        return augustDates.toArray(new LocalDate[0]);
    }
}
