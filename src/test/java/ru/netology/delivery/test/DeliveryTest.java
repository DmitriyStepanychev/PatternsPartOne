package ru.netology.delivery.test;

import com.codeborne.selenide.Condition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.Keys;
import ru.netology.delivery.data.DataGenerator;

import java.time.Duration;

import static com.codeborne.selenide.Condition.appear;
import static com.codeborne.selenide.Selenide.open;
import static com.codeborne.selenide.Selenide.*;

class DeliveryTest {

    @BeforeEach
    void setup() {
        open("http://localhost:9999");
    }

    @Test
    @DisplayName("Should successful plan and replan meeting")
    void shouldSuccessfulPlanAndReplanMeeting() {
        var validUser = DataGenerator.Registration.generateUser("ru");
        var daysToAddForFirstMeeting = 4;
        var firstMeetingDate = DataGenerator.generateDate(daysToAddForFirstMeeting);
        var daysToAddForSecondMeeting = 7;
        var secondMeetingDate = DataGenerator.generateDate(daysToAddForSecondMeeting);

        $("[data-test-id=city] input").setValue(validUser.getCity());
        $("[data-test-id=date] input").sendKeys(Keys.SHIFT, Keys.HOME, Keys.DELETE);
        $("[data-test-id=date] input").sendKeys(firstMeetingDate);
        $("[data-test-id=name] input").setValue(validUser.getName());
        $("[data-test-id=phone] input").setValue(validUser.getPhone());
        $("[data-test-id=agreement]").click();
        $x("//span[text() = 'Запланировать']").click();

        /* т.к. Faker генерирует города не только из списка административных субъектов пришлось
         * добавить логику изменения сгенерированного города, чтобы тест не крашился по данной причине */
        if ($x("//span[text()='Доставка в выбранный город недоступна']").isDisplayed()) {
            $("[data-test-id=city] input").sendKeys(Keys.SHIFT, Keys.HOME, Keys.DELETE);
            $("[data-test-id=city] input").setValue(DataGenerator.generateCity("ru"));
            /* при использовании ValidUser.getCity, подставляется ранеее сгенерированные город,
             * который не подходит под параметры. Генерирую новый город с помощью прямого вызова
             * DataGenerator.generateCity*/
            $x("//span[text() = 'Запланировать']").click();
        }
        /* такую же логику написал на проверку имени, т.к. Faker генерирует имена и фамилии с буквой ё,
        * а форма анкеты не пропускает их с ошибкой */
        if ($x("//span[text()='Имя и Фамилия указаные неверно. Допустимы только русские буквы, пробелы и дефисы']").isDisplayed()) {
            $("[data-test-id=name] input").sendKeys(Keys.SHIFT, Keys.HOME, Keys.DELETE);
            $("[data-test-id=name] input").setValue(DataGenerator.generateName("ru"));
            $x("//span[text() = 'Запланировать']").click();
        }

        $("[data-test-id=success-notification]  .notification__content")
                .shouldBe(Condition.visible,
                        Duration.ofSeconds(15))
                .shouldHave(Condition.text("Встреча успешно запланирована на " + firstMeetingDate));

        $("[data-test-id=date] input").sendKeys(Keys.SHIFT, Keys.HOME, Keys.DELETE);
        $("[data-test-id=date] input").sendKeys(secondMeetingDate);
        $x("//span[text() = 'Запланировать']").click();

        $("[data-test-id=replan-notification]  .notification__content")
                .shouldBe(Condition.visible,
                        Duration.ofSeconds(15))
                .shouldHave(Condition.text("У вас уже запланирована встреча на другую дату. Перепланировать?"));
        $x("//span[text() = 'Перепланировать']").click();

        $("[data-test-id=success-notification]  .notification__content")
                .shouldBe(Condition.visible,
                        Duration.ofSeconds(15))
                .shouldHave(Condition.text("Встреча успешно запланирована на " + secondMeetingDate));
    }
}
