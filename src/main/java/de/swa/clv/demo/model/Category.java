package de.swa.clv.demo.model;

import java.util.Arrays;
import java.util.List;

public enum Category {

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

    Category(String label, SubCategory... subCategories) {
        this.label = label;
        this.subCategories = Arrays.asList(subCategories);
    }

    public List<SubCategory> getSubCategories() {
        return subCategories;
    }

    public AsRecord asRecord() {
            return new AsRecord(label, subCategories);
    }

    public record AsRecord(String label, List<SubCategory> subCategories) {
    }
}
