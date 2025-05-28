module sad.gruppo11 {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    opens sad.gruppo11 to javafx.fxml;
    exports sad.gruppo11;
}
