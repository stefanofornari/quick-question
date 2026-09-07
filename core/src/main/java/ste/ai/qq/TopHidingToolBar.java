/*
 * Copyright 2026 QuickQuestion contributors
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

import javafx.animation.Interpolator;
import javafx.animation.TranslateTransition;
import javafx.beans.DefaultProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.util.Duration;

@DefaultProperty("items")
public class TopHidingToolBar extends VBox {

    private static final String ICON_HAMBURGER = "M3 6h18v2H3V6zm0 5h18v2H3v-2zm0 5h18v2H3v-2z";
    private static final String ICON_CLOSE = "M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z";

    private final ToolBar toolBar = new ToolBar();
    private final Pane barClipPane = new Pane();
    private final StackPane handle = new StackPane();
    private final SVGPath icon = new SVGPath();

    private final TranslateTransition slideTransition;
    private final Rectangle clipRect = new Rectangle();
    private boolean isExpanded = false;

    public TopHidingToolBar() {
        setAlignment(Pos.TOP_CENTER);
        // 1. Prevent StackPane from stretching this container to full window height
        setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        setPickOnBounds(false);
        getStyleClass().add("hiding-toolbar-container");

        // 2. Wrap ToolBar in a clipped viewport pane
        barClipPane.getChildren().add(toolBar);
        barClipPane.setClip(clipRect);
        barClipPane.setPickOnBounds(false);

        // Keep clip rectangle and wrapper size matched to the toolbar
        toolBar.layoutBoundsProperty().addListener((obs, oldBounds, newBounds) -> {
            double w = newBounds.getWidth();
            double h = newBounds.getHeight();
            barClipPane.setPrefSize(w, h);
            barClipPane.setMinSize(w, h);
            barClipPane.setMaxSize(w, h);
            clipRect.setWidth(w);
            clipRect.setHeight(h);

            // Keep hidden offset strictly synchronized
            if (!isExpanded) {
                toolBar.setTranslateY(-h);
                handle.setTranslateY(-h);
            }
        });

        // 3. Semicircle Handle & Icon
        icon.setContent(ICON_HAMBURGER);
        icon.getStyleClass().add("handle-icon");

        handle.getStyleClass().add("toolbar-handle");
        handle.getChildren().add(icon);

        getChildren().addAll(barClipPane, handle);

        // 4. Slide Animation (moves both toolbar and handle together)
        slideTransition = new TranslateTransition(Duration.millis(220));
        slideTransition.setInterpolator(Interpolator.EASE_BOTH);

        setupEvents();
    }

    private void setupEvents() {
        setOnMouseEntered(e -> showBar());
        setOnMouseExited(e -> hideBar());

        handle.setOnMouseClicked(e -> {
            if (isExpanded) {
                hideBar();
            } else {
                showBar();
            }
            e.consume();
        });
    }

    public void showBar() {
        if (isExpanded) return;
        isExpanded = true;
        icon.setContent(ICON_CLOSE);

        slideTransition.stop();
        animateTo(0);
    }

    public void hideBar() {
        if (!isExpanded) return;
        isExpanded = false;
        icon.setContent(ICON_HAMBURGER);

        slideTransition.stop();
        animateTo(-toolBar.getHeight());
    }

    private void animateTo(double targetY) {
        slideTransition.setNode(toolBar);
        slideTransition.setToY(targetY);

        // Move handle along with the toolbar
        TranslateTransition handleTransition = new TranslateTransition(Duration.millis(220), handle);
        handleTransition.setInterpolator(Interpolator.EASE_BOTH);
        handleTransition.setToY(targetY);

        slideTransition.play();
        handleTransition.play();
    }

    public ObservableList<Node> getItems() {
        return toolBar.getItems();
    }

    public ToolBar getToolBar() {
        return toolBar;
    }
}