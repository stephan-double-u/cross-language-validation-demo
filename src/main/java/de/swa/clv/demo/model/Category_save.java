package de.swa.clv.demo.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Arrays;
import java.util.List;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum Category_save {

    ENDOSCOPY("Endoscopy",
            SubCategory.LARYNGOSCOPE,
            SubCategory.SINUSCOPE,
            SubCategory.OTOSCOPE),
    IMAGING_SYSTEM("Imaging System",
            SubCategory.CAMERAHEAD,
            SubCategory.LIGHTSOURCE,
            SubCategory.VIDEOPROCESSOR);

    private final String label;
    private final List<SubCategory> subCategories;

    Category_save(String label, SubCategory... subCategories) {
        this.label = label;
        this.subCategories = Arrays.asList(subCategories);
    }

    public String getLabel() {
        return this.label;
    }

    public List<SubCategory> getSubCategories() {
        return this.subCategories;
    }

    public static String[] labels() {
        return Arrays.asList(values()).stream().map(v -> v.label).toArray(String[]::new);
    }

    @JsonCreator
    public static Category_save forLabel(@JsonProperty("label") String label) {
        for (Category_save value : Category_save.values()) {
            if (value.label.equals(label) ) {
                return value;
            }
        }
        return null;
    }

    public AsRecord asRecord() {
            return new AsRecord(subCategories.stream().map(SubCategory::name).toList());
    }

    public record AsRecord(List<String> subCategories) {
    }
}
