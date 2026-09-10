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
package ste.netbeans.ai.qq.nb;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;
import javafx.application.Platform;
import javafx.embed.swing.JFXPanel;
import javafx.scene.Scene;
import org.netbeans.api.settings.ConvertAsProperties;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.util.NbBundle.Messages;
import org.openide.windows.TopComponent;
import ste.ai.qq.WebChat;

@ConvertAsProperties(
    dtd = "-//ste.netbeans.ai.qq.nb//QuickQuestion//EN",
    autostore = false
)
@TopComponent.Description(
    preferredID = QuickQuestionTopComponent.PREFERRED_ID,
    iconBase = "ste/ai/qq/logo-qq-16x16.png",
    persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(
    mode = "explorer",
    openAtStartup = false
)
@ActionID(
    category = "Window",
    id = "ste.netbeans.ai.qq.nb.QuickQuestionTopComponent"
)
@ActionReference(
    path = "Menu/Window",
    position = 901
)
@TopComponent.OpenActionRegistration(
    displayName = "#CTL_QuickQuestionAction",
    preferredID = QuickQuestionTopComponent.PREFERRED_ID
)
@Messages({
    "CTL_QuickQuestionAction=QuickQuestion",
    "CTL_QuickQuestionTopComponent=QuickQuestion Window",
    "HINT_QuickQuestionTopComponent=This is a QuickQuestion window"
})
public class QuickQuestionTopComponent extends TopComponent {

    public static final String PREFERRED_ID = "ste_netbeans_qq_QuickQuestionTopComponent";

    //
    // A fully transparent cursor
    //
    private static final java.awt.Cursor CURSOR_BLANK = Toolkit.getDefaultToolkit().createCustomCursor(
        new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB), new Point(0, 0), "blank_cursor"
    );

    private JFXPanel fxPanel;
    private WebChat webChat;

    public QuickQuestionTopComponent() {
        setName("QuickQuestion");
        setToolTipText("Quick access LLM WebChat");
    }

    @Override
    protected void componentOpened() {
        super.componentOpened();
        SwingUtilities.invokeLater(this::initFx);
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
    }

    private void initFx() {
        if (fxPanel == null) {
            fxPanel = new JFXPanel();
            Platform.setImplicitExit(false);  // TODO: remove when using NetBeans JavaFX Toolkit
        }

        // ---
        //
        // Note: we do not want to render the cursor, the remote will render it
        //
        setCursor(cursor());// hide the swing cursor
        Platform.runLater(() -> {
            if (webChat == null) {
                webChat = new WebChat();
                webChat.setPrefWidth(600);
                webChat.setPrefHeight(800);
                final Scene scene = new Scene(webChat);
                scene.getStylesheets().add(
                    getClass().getResource("QuickQuestion.css").toExternalForm()
                );
                fxPanel.setScene(scene);
            }

            setLayout(new BorderLayout());
            add(fxPanel, BorderLayout.CENTER);
            revalidate();
            repaint();
        });
    }

    public static QuickQuestionTopComponent findDefault() {
        return new QuickQuestionTopComponent();
    }

    private java.awt.Cursor cursor() {
        return CURSOR_BLANK;
    }
}
