package ru.filippov.GUI.customNodes;

import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeItem;

public class TreeItemContextMenu<T> extends TreeItem<T> {

    protected ContextMenu contextMenu;

    public TreeItemContextMenu() {

    }

    public TreeItemContextMenu(T value) {
        super(value);
        this.contextMenu = contextMenu;
    }

    public TreeItemContextMenu(T value, Node graphic) {
        super(value, graphic);
        this.contextMenu = contextMenu;
    }

    public TreeItemContextMenu(T value, Node graphic, ContextMenu contextMenu) {
        super(value, graphic);
        this.contextMenu = contextMenu;
    }

    public ContextMenu getContextMenu() {
        return contextMenu;
    }

    public void setContextMenu(ContextMenu contextMenu) {
        this.contextMenu = contextMenu;
    }
}
