package de.swa.clv.demo.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ArticleTest {

    @Test
    public void validationRulesAreValid_yesItIsThatSimple() {
        Assertions.assertDoesNotThrow(() -> Article.rules);
    }

}
