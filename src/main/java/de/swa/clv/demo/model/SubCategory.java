package de.swa.clv.demo.model;

public enum SubCategory {

    LARYNGOSCOPE("Laryngoscope"),
    SINUSCOPE("Sinuscope"),
    OTOSCOPE("Otoscopes"),
    CAMERAHEAD("Camera Head"),
    LIGHTSOURCE("Light Source"),
    VIDEOPROCESSOR("Video Processors");

    private String label;

    SubCategory(String label) {
        this.label = label;
    }

    public AsRecord asRecord() {
        return new AsRecord(label);
    }

    public record AsRecord(String label) {
    }
}
