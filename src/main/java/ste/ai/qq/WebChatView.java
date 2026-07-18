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
package ste.ai.qq;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;

import dev.dirs.BaseDirectories;

/**
 * Self-contained JavaFX component that embeds a web view and a selectable list of web chat endpoints.
 * <p>
 * The component loads its own layout from {@code web-chat-view.fxml} and exposes the list of
 * available services through the {@link #webChatSetProperty()} property.
 */
public class WebChatView extends VBox {

    public static final String DEFAULT_STORAGE_DIR_NAME = "quick-question";
    public static final String DEFAULT_STORAGE_PATH_NAME = BaseDirectories.get().configDir;
    public static final String SELECTED_STYLE_CLASS = "selected";

    @FXML
    private ButtonBar serviceBar;

    @FXML
    private WebView webView;

    @FXML
    private StackPane webViewContainer;

    @FXML
    private Label warningLabel;

    @FXML
    private Label failureLabel;

    private final ListProperty<WebChat> webChatSetProperty;
    private final String defaultUrl;

    /**
     * Creates a web chat view using the default session storage directory.
     */
    public WebChatView() {
        this(null, null);
    }

    /**
     * Creates a web chat view using the supplied session storage directory.
     *
     * @param storageDirectory directory where WebView session state is persisted;
     *                                {@code null} to use the default directory
     */
    public WebChatView(File storageDirectory) {
        this(null, storageDirectory);
    }

    /**
     * Creates a web chat view with an explicit default URL and session storage directory.
     *
     * @param defaultUrl the URL to load when the component is displayed
     * @param storageDirectory directory where WebView session state is persisted;
     *                                {@code null} to use the default directory
     */
    public WebChatView(String defaultUrl, File storageDirectory) {
        this.defaultUrl = defaultUrl;
        this.webChatSetProperty = new SimpleListProperty<>(FXCollections.observableArrayList());

        loadFxml();

        VBox.setVgrow(webViewContainer, Priority.ALWAYS);

        this.failureLabel.setText("Navigation failed");
        this.warningLabel.setText("Google login is not supported, please use email login");

        this.webView.getEngine().setUserDataDirectory(resolveStorageDirectory(storageDirectory));
        configureSelectionHandling();
    }

    private void loadFxml() {
        FXMLLoader loader = new FXMLLoader(WebChatView.class.getResource("web-chat-view.fxml"));
        loader.setRoot(this);
        loader.setController(this);
        try {
            loader.load();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load web-chat-view.fxml", exception);
        }
    }

    /**
     * Returns the property that holds the available web chat services.
     *
     * @return the web chat set property
     */
    public ListProperty<WebChat> webChatSetProperty() {
        return webChatSetProperty;
    }

    /**
     * Sets the property that holds the available web chat services.
     *
     * @param property the property to use
     */
    public void webChatSetProperty(ListProperty<WebChat> property) {
        Objects.requireNonNull(property, "property must not be null");
        this.webChatSetProperty.bindBidirectional(property);
    }

    /**
     * Returns the current web chat services.
     *
     * @return the current services
     */
    public ObservableList<WebChat> webChatSet() {
        return webChatSetProperty.get();
    }

    /**
     * Loads the default URL or the first configured entry when the component is displayed.
     */
    public void onDisplayed() {
        if (defaultUrl != null) {
            navigateTo(defaultUrl);
        } else if (!webChatSetProperty.isEmpty()) {
            selectEntry(webChatSetProperty.getFirst());
        }
    }

    private File resolveStorageDirectory(File sessionStorageDirectory) {
        if (sessionStorageDirectory == null) {
            return defaultStorageDirectory().toFile();
        }
        if (!isValidStorageDirectory(sessionStorageDirectory)) {
            throw new IllegalArgumentException(
                "Invalid session storage directory: " + sessionStorageDirectory.getAbsolutePath());
        }
        return sessionStorageDirectory;
    }

    /**
     * Returns the default storage directory used for WebView session state and
     * persisted cookies.
     *
     * @return the default storage directory
     */
    public static Path defaultStorageDirectory() {
        Path userHome = Path.of(System.getProperty("user.home"));
        Path storageDirectory = userHome.resolve(DEFAULT_STORAGE_PATH_NAME).resolve(DEFAULT_STORAGE_DIR_NAME);
        try {
            Files.createDirectories(storageDirectory);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to create default session storage directory: " + storageDirectory, exception);
        }
        return storageDirectory;
    }

    private boolean isValidStorageDirectory(File directory) {
        if (!directory.exists()) {
            return directory.mkdirs();
        }
        return directory.isDirectory() && directory.canWrite();
    }

    private void configureSelectionHandling() {
        webChatSetProperty.addListener((ListChangeListener<WebChat>) change -> rebuildSelector());
    }

    private void rebuildSelector() {
        serviceBar.getButtons().clear();
        for (WebChat entry : webChatSetProperty) {
            Button button = new Button(entry.name());
            button.setOnAction(event -> selectEntry(entry));
            serviceBar.getButtons().add(button);
        }
    }

    private void selectEntry(WebChat entry) {
        for (javafx.scene.Node node : serviceBar.getButtons()) {
            if (node instanceof Button button) {
                boolean isSelected = button.getText().equals(entry.name());
                if (isSelected) {
                    if (!button.getStyleClass().contains(SELECTED_STYLE_CLASS)) {
                        button.getStyleClass().add(SELECTED_STYLE_CLASS);
                    }
                } else {
                    button.getStyleClass().remove(SELECTED_STYLE_CLASS);
                }
            }
        }
        warningLabel.setVisible(!entry.googleLogin());
        warningLabel.setManaged(!entry.googleLogin());
        navigateTo(entry.url());
    }

    private void navigateTo(String url) {
        WebEngine engine = webView.getEngine();
        try {
            new URI(url);
            failureLabel.setVisible(false);
            failureLabel.setManaged(false);
            engine.load(url);
        } catch (URISyntaxException | IllegalArgumentException exception) {
            failureLabel.setVisible(true);
            failureLabel.setManaged(true);
        }
    }

    /**
     * Returns the selector control.
     *
     * @return the selector
     */
    public ButtonBar selector() {
        return serviceBar;
    }

    /**
     * Returns the embedded web view.
     *
     * @return the web view
     */
    public WebView webView() {
        return webView;
    }

    /**
     * Returns the failure label used when navigation cannot be performed.
     *
     * @return the failure label
     */
    public Label failureLabel() {
        return failureLabel;
    }

    /**
     * Returns the warning label used when google login is not supported.
     *
     * @return the warning label
     */
    public Label warningLabel() {
        return warningLabel;
    }
}
