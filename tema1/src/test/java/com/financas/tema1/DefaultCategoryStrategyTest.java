package com.financas.tema1;

import com.financas.tema1.domain.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import com.financas.tema1.application.DefaultCategoryStrategy;
import static org.assertj.core.api.Assertions.assertThat;

class DefaultCategoryStrategyTest {

    private DefaultCategoryStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new DefaultCategoryStrategy();
    }

    @Test
    @DisplayName("Deve retornar OTHER quando rawCategory for null")
    void shouldReturnOtherWhenNull() {
        assertThat(strategy.normalize(null)).isEqualTo(Category.OTHER);
    }

    @Test
    @DisplayName("Deve retornar OTHER quando rawCategory for string vazia")
    void shouldReturnOtherWhenEmpty() {
        assertThat(strategy.normalize("")).isEqualTo(Category.OTHER);
    }

    @Test
    @DisplayName("Deve retornar FOOD para 'food'")
    void shouldReturnFoodForFood() {
        assertThat(strategy.normalize("food")).isEqualTo(Category.FOOD);
    }

    @Test
    @DisplayName("Deve retornar FOOD para 'Food & Drinks' (case insensitive)")
    void shouldReturnFoodForFoodAndDrinks() {
        assertThat(strategy.normalize("Food & Drinks")).isEqualTo(Category.FOOD);
    }

    @Test
    @DisplayName("Deve retornar FOOD para 'alimentacao'")
    void shouldReturnFoodForAlimentacao() {
        assertThat(strategy.normalize("alimentacao")).isEqualTo(Category.FOOD);
    }

    @Test
    @DisplayName("Deve retornar FOOD para 'drink'")
    void shouldReturnFoodForDrink() {
        assertThat(strategy.normalize("drink")).isEqualTo(Category.FOOD);
    }

    @Test
    @DisplayName("Deve retornar FOOD para 'DRINK' (maiúsculas)")
    void shouldReturnFoodForDrinkUpperCase() {
        assertThat(strategy.normalize("DRINK")).isEqualTo(Category.FOOD);
    }

    @Test
    @DisplayName("Deve retornar TRANSPORT para 'transport'")
    void shouldReturnTransportForTransport() {
        assertThat(strategy.normalize("transport")).isEqualTo(Category.TRANSPORT);
    }

    @Test
    @DisplayName("Deve retornar TRANSPORT para 'transporte'")
    void shouldReturnTransportForTransporte() {
        assertThat(strategy.normalize("transporte")).isEqualTo(Category.TRANSPORT);
    }

    @Test
    @DisplayName("Deve retornar TRANSPORT para 'uber'")
    void shouldReturnTransportForUber() {
        assertThat(strategy.normalize("uber")).isEqualTo(Category.TRANSPORT);
    }

    @Test
    @DisplayName("Deve retornar TRANSPORT para 'Uber99' (case insensitive)")
    void shouldReturnTransportForUber99() {
        assertThat(strategy.normalize("Uber99")).isEqualTo(Category.TRANSPORT);
    }


    @Test
    @DisplayName("Deve retornar ENTERTAINMENT para 'entertain'")
    void shouldReturnEntertainmentForEntertain() {
        assertThat(strategy.normalize("entertain")).isEqualTo(Category.ENTERTAINMENT);
    }

    @Test
    @DisplayName("Deve retornar ENTERTAINMENT para 'lazer'")
    void shouldReturnEntertainmentForLazer() {
        assertThat(strategy.normalize("lazer")).isEqualTo(Category.ENTERTAINMENT);
    }

    @Test
    @DisplayName("Deve retornar ENTERTAINMENT para 'jogo'")
    void shouldReturnEntertainmentForJogo() {
        assertThat(strategy.normalize("jogo")).isEqualTo(Category.ENTERTAINMENT);
    }

    @Test
    @DisplayName("Deve retornar ENTERTAINMENT para 'ENTERTAINMENT' (maiúsculas)")
    void shouldReturnEntertainmentUpperCase() {
        assertThat(strategy.normalize("ENTERTAINMENT")).isEqualTo(Category.ENTERTAINMENT);
    }


    @Test
    @DisplayName("Deve retornar OTHER para categoria desconhecida")
    void shouldReturnOtherForUnknownCategory() {
        assertThat(strategy.normalize("utilities")).isEqualTo(Category.OTHER);
    }

    @Test
    @DisplayName("Deve retornar OTHER para 'saude'")
    void shouldReturnOtherForSaude() {
        assertThat(strategy.normalize("saude")).isEqualTo(Category.OTHER);
    }
}
