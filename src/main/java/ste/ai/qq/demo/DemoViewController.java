/*
 * Copyright 2026 Quick Question contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ste.ai.qq.demo;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import ste.ai.qq.WebChat;
import ste.ai.qq.WebChatView;

/**
 * Controller for the demo configuration UI.
 */
public class DemoViewController {

    @FXML
    protected TextField nameField;

    @FXML
    protected TextField urlField;

    @FXML
    private Button addButton;

    @FXML
    private Button applyButton;

    @FXML
    private TableView<WebChat> entriesTable;

    @FXML
    private TableColumn<WebChat, String> nameColumn;

    @FXML
    private TableColumn<WebChat, String> urlColumn;

    @FXML
    private WebChatView webChatView;

    private final ObservableList<WebChat> entries = FXCollections.observableArrayList();

    /**
     * Initializes the table and default entries.
     */
    @FXML
    public void initialize() {
        nameColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().name()));
        urlColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().url()));
        entries.addAll(new ste.ai.qq.DefaultProviders().providers());
        entriesTable.setItems(entries);
        webChatView.webChatSetProperty().set(entries);
    }

    /**
     * Adds a new entry from the form fields.
     */
    @FXML
    public void handleAddEntry() {
        entries.add(new WebChat(nameField.getText(), urlField.getText()));
        nameField.clear();
        urlField.clear();
    }

    /**
     * Applies the current configuration to the embedded component.
     */
    @FXML
    public void handleApplyChanges() {
        webChatView.webChatSetProperty().set(entries);
    }

    /**
     * Returns the embedded component.
     *
     * @return the embedded web chat view
     */
    public WebChatView getWebChatView() {
        return webChatView;
    }
}
