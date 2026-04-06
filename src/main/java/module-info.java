module cz.cvut.fel.pjv {
    requires javafx.controls;
    requires java.sql;
    requires java.desktop;
    requires jdk.jshell;

    exports cz.cvut.fel.pjv;
    exports cz.cvut.fel.pjv.entities;
    exports cz.cvut.fel.pjv.managers;
    exports cz.cvut.fel.pjv.levelGraph;
    exports cz.cvut.fel.pjv.chunks;
}
