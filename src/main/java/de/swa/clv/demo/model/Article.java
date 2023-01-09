package de.swa.clv.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import de.swa.clv.UseType;
import de.swa.clv.ValidationRules;
import de.swa.clv.constraints.*;
import de.swa.clv.demo.validation.ValidationRulesGettable;
import de.swa.clv.groups.ConditionsGroup;
import de.swa.clv.groups.ConditionsTopGroup;

import java.time.*;
import java.util.Date;
import java.util.List;
import java.util.stream.IntStream;

import static de.swa.clv.demo.model.Permission.MANAGER;
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
       (3) example on how multiple rules for the same property (here: "maintenanceNextDate") and the same rule type
           (here: 'content') and different constraints can be defined
       (4) Enums are objects in Java and serialized as strings by default. Therefore, nested properties of enums like
           "category.subCategories[*]" can't be validated in a Javascript frontend easily.
           With the help of the method "doNotSerialize()" it can be prevented that such rules are serialized.
           Besides, it is also likely not necessary to validate the rule in the frontend, because the synchronization of
           the select boxes, which is done in a frontend anyway, ensures that no wrong sub-category is transferred.
       (5) example for a complex rule the multiple conditions needs to be logically linked with AND _and_ OR.
       (6) If rules with RegEx constraints should be validated, the CLV client implementation has to support all used
           regex features as well, e.g. unicode property escapes like "\p{L}".
       (7) the 'index definition' [*] is just a shortcut for [0/1] (start/step definition)
       (8) 'technical' rule for concurrent modification detection
     */
    public static final ValidationRules<Article> rules = new ValidationRules<>(Article.class);
    static {
        rules.mandatory("name");
        rules.immutable("name",
                Condition.of("status", Equals.any(DECOMMISSIONED)));
        rules.content("name", RegEx.any(TRIMMED_3_TO_30_REGEX));

        rules.mandatory("number");
        rules.immutable("number",
                Condition.of("status", Equals.any(DECOMMISSIONED)));

        rules.mandatory("status");
        rules.immutable("status",
                Condition.of("status", Equals.any(DECOMMISSIONED)));
        rules.content("status", Equals.any(NEW),
                Condition.of("id", Equals.null_()))
                .errorCodeControl(UseType.AS_SUFFIX, "#initial"); // (1)
        rules.update("status", Equals.any(NEW, ACTIVE, INACTIVE),
                Condition.of("status", Equals.any(NEW)));
        rules.update("status", Equals.any(ACTIVE, INACTIVE),
                Permissions.none(DecommissionAssets),
                Condition.of("status", Equals.any(ACTIVE, INACTIVE)));
        rules.update("status", Equals.any(ACTIVE, INACTIVE, DECOMMISSIONED),
                Permissions.any(DecommissionAssets),
                Condition.of("status", Equals.any(ACTIVE, INACTIVE)))
                .errorCodeControl(UseType.AS_REPLACEMENT, "mycode.for.article.status"); // (2)

        rules.mandatory("maintenanceIntervalMonth",
                ConditionsGroup.OR(
                        Condition.of("maintenanceNextDate", Equals.notNull()),
                        Condition.of("maintenanceIntervalMonth", Equals.notNull())));
        rules.mandatory("maintenanceNextDate",
                ConditionsGroup.OR(
                        Condition.of("maintenanceIntervalMonth", Equals.notNull()),
                        Condition.of("maintenanceNextDate", Equals.notNull())));
        rules.content("maintenanceNextDate", Future.minMaxDays(1, 365),
                ConditionsGroup.AND(
                        Condition.of("maintenanceNextDate", Equals.notNull()),
                        Condition.of("id", Equals.null_()))); // (3)
        rules.content("maintenanceNextDate", Weekday.anyOrNull(MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY)); // (3)
        rules.content("maintenanceNextDate", Equals.none(getFakedCompanyVacationDates())); // (3)
        rules.update("maintenanceNextDate", Future.minMaxDays(1, 365),
                        Condition.of("maintenanceNextDate", Value.changed()));

        rules.content("category", Equals.anyOrNull(Category.values()));
        rules.mandatory("subCategory",
                Condition.of("category", Equals.notNull()));
        rules.content("subCategory", Equals.anyRefOrNull("category.subCategories[*]"))
                .doNotSerialize(); // (4)

        rules.immutable("everLeftWarehouse",
                Condition.of("everLeftWarehouse", Equals.any(TRUE)));

        rules.immutable("animalUse",
                ConditionsTopGroup.OR(
                        ConditionsGroup.AND(
                                Condition.of("animalUse", Equals.any(TRUE)),
                                Condition.of("everLeftWarehouse", Equals.any(TRUE))),
                        ConditionsGroup.AND(
                                Condition.of("medicalSet", Equals.notNull())))); //(5)

        rules.content("accessories", Size.max(3),
                Permissions.none(MANAGER),
                Condition.of("id", Equals.null_()));
        rules.content("accessories", Size.max(4),
                Permissions.any(MANAGER));
        rules.update("accessories", Size.max(3),
                Permissions.none(MANAGER),
                Condition.of("accessories", Size.max(3)));
        rules.update("accessories", Size.max(4),
                Permissions.none(MANAGER),
                Condition.of("accessories", Size.min(4)));

        rules.content("accessories[*].name", RegEx.any(EXAMPLE_UNICODE_PROPERTY_CLASSES_REGEX)); // (6)
        rules.content("accessories[*].name#distinct", Equals.any(true));
        rules.content("accessories[*].amount", Range.minMax(AMOUNT_MIN, AMOUNT_MAX));
        rules.content("accessories[0/1].amount#sum", Range.max(AMOUNT_SUM_MAX)); // (7)

        rules.immutable("lastModifiedOn"); // (8)
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

    public Article() {
    }

    public Article(Integer id, String name, String number, Status status, Short maintenanceIntervalMonth,
            LocalDate maintenanceNextDate, Category category, SubCategory subCategory, List<Accessory> accessories, Date lastModifiedOn) {
        this.id = id;
        this.name = name;
        this.number = number;
        this.status = status;
        this.maintenanceIntervalMonth = maintenanceIntervalMonth;
        this.maintenanceNextDate = maintenanceNextDate;
        this.category = category;
        this.subCategory = subCategory;
        this.accessories = accessories;
        this.lastModifiedOn = lastModifiedOn;
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
        return rules;
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
