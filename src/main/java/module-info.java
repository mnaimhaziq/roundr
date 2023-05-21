module com.game.roundr {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    opens com.game.roundr to javafx.fxml;
    opens com.game.roundr.lobby to javafx.fxml;
    opens com.game.roundr.game to javafx.fxml;
    
    exports com.game.roundr;
}
