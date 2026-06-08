module com.fitify {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.media;
    requires java.sql;
    requires java.net.http;
    requires mysql.connector.j;
    requires json.simple;
    requires jdk.httpserver;
    requires java.desktop; 
    opens com.fitify            to javafx.graphics, javafx.fxml;
    opens com.fitify.ui         to javafx.graphics, javafx.fxml;
    opens com.fitify.model      to javafx.base;
    opens com.fitify.service    to javafx.base;
    opens com.fitify.dao        to javafx.base;
    opens com.fitify.util       to javafx.base;
    opens com.fitify.interfaces to javafx.base;

    exports com.fitify;
    exports com.fitify.model;
    exports com.fitify.service;
    exports com.fitify.dao;
    exports com.fitify.interfaces;
    exports com.fitify.ui;
    exports com.fitify.util;
}
