module com.game.roundr {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;


    opens com.game.roundr to javafx.fxml;
    exports com.game.roundr;
}