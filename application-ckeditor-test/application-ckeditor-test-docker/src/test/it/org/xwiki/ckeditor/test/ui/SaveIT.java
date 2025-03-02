/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.ckeditor.test.ui;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.xwiki.ckeditor.test.po.CKEditor;
import org.xwiki.ckeditor.test.po.RichTextAreaElement;
import org.xwiki.panels.test.po.DocumentInformationPanel;
import org.xwiki.test.docker.junit5.TestReference;
import org.xwiki.test.docker.junit5.UITest;
import org.xwiki.test.ui.TestUtils;
import org.xwiki.test.ui.po.DocumentSyntaxPicker;
import org.xwiki.test.ui.po.DocumentSyntaxPicker.SyntaxConversionConfirmationModal;
import org.xwiki.test.ui.po.editor.WYSIWYGEditPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Integration tests for the Save plugin.
 * 
 * @version $Id$
 * @since 1.13
 */
@UITest
public class SaveIT
{
    @BeforeAll
    public static void configure(TestUtils setup) throws Exception
    {
        // Run the tests as a normal user. We make the user advanced only to enable the Edit drop down menu.
        setup.createUserAndLogin("alice", "pa$$word", "editor", "Wysiwyg", "usertype", "Advanced");
    }

    @Test
    public void save(TestUtils setup, TestReference testReference) throws Exception
    {
        WYSIWYGEditPage editPage = setup.gotoPage(testReference).editWYSIWYG();
        CKEditor editor = new CKEditor("content").waitToLoad();
        RichTextAreaElement textArea = editor.getRichTextArea();
        textArea.clear();
        textArea.sendKeys("xyz");
        editPage = editPage.clickSaveAndView().editWYSIWYG();
        assertEquals("<p>xyz</p>", editor.waitToLoad().getRichTextArea().getContent());
    }

    @Test
    public void saveAfterSyntaxChange(TestUtils setup, TestReference testReference)
    {
        setup.createPage(testReference, "[[label>>#target]]", "", "xwiki/2.0").edit();
        // Wait for the WYSIWYG edit page to load.
        WYSIWYGEditPage editPage = new WYSIWYGEditPage();

        // Type some text to verify that it isn't lost when we change the syntax.
        CKEditor editor = new CKEditor("content").waitToLoad();
        editor.getRichTextArea().sendKeys("test ");

        DocumentSyntaxPicker documentSyntaxPicker = new DocumentInformationPanel().getSyntaxPicker();
        assertEquals("xwiki/2.0", documentSyntaxPicker.getSelectedSyntax());

        SyntaxConversionConfirmationModal confirmationModal = documentSyntaxPicker.selectSyntaxById("xwiki/2.1");
        assertTrue(confirmationModal.getMessage()
            .contains("from the previous XWiki 2.0 syntax to the selected XWiki 2.1 syntax?"));
        confirmationModal.confirmSyntaxConversion();

        assertEquals("[[test label>>||anchor=\"target\"]]", editPage.clickSaveAndView().editWiki().getContent());
        assertEquals("xwiki/2.1", new DocumentInformationPanel().getSyntaxPicker().getSelectedSyntax());
    }
}
