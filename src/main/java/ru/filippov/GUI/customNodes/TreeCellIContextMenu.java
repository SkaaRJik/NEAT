package ru.filippov.GUI.customNodes;

import javafx.scene.control.TreeCell;

public class TreeCellIContextMenu extends TreeCell {
    @Override
    protected void updateItem(Object item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
            setGraphic(null);
        } else {
            setText(getItem() == null ? "" : getItem().toString());
            setGraphic(getTreeItem().getGraphic());
            setContextMenu(((TreeItemContextMenu) getTreeItem()).getContextMenu());
        }
    }


}
