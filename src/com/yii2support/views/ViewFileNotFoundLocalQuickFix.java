package com.yii2support.views;

import com.intellij.codeInspection.LocalQuickFix;
import com.intellij.codeInspection.ProblemDescriptor;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.jetbrains.php.lang.psi.elements.StringLiteralExpression;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Properties;

/**
 * Created by NVlad on 15.01.2017.
 */
public class ViewFileNotFoundLocalQuickFix implements LocalQuickFix {
    private String myName;

    ViewFileNotFoundLocalQuickFix(String name) {
        myName = name;
    }

    @Override
    public boolean startInWriteAction() {
        return false;
    }

    @Nls
    @NotNull
    @Override
    public String getName() {
        return "Create view for '%name%'".replace("%name%", myName);
    }

    @Nls
    @NotNull
    @Override
    public String getFamilyName() {
        return "Create view";
    }

    @Override
    public void applyFix(@NotNull Project project, @NotNull ProblemDescriptor descriptor) {
        PsiElement psiElement = descriptor.getPsiElement().getParent();
        final PsiFile psiFile = psiElement.getContainingFile().getOriginalFile();
        final PsiDirectory psiDirectory = ViewsUtil.getViewsPsiDirectory(psiFile, psiElement);
        if (psiDirectory != null) {
            String filename = ((StringLiteralExpression) psiElement).getContents() + ".php";
            if (filename.contains("/")) {
                filename = filename.substring(filename.lastIndexOf('/') + 1);
            }
            String finalFilename = filename;
            ApplicationManager.getApplication().runWriteAction(() -> {
                PsiFile viewPsiFile = psiDirectory.createFile(finalFilename);
                FileTemplate[] templates = FileTemplateManager.getDefaultInstance().getTemplates(FileTemplateManager.DEFAULT_TEMPLATES_CATEGORY);
                FileTemplate template = null;
                for (FileTemplate fileTemplate : templates) {
                    if (fileTemplate.getName().equals("PHP File")) {
                        template = fileTemplate;
                        break;
                    }
                }

                FileEditorManager.getInstance(project).openFile(viewPsiFile.getVirtualFile(), true);
                Properties properties = FileTemplateManager.getDefaultInstance().getDefaultProperties();
                if (viewPsiFile.getViewProvider().getDocument() != null && template != null) {
                    template.setLiveTemplateEnabled(true);
                    template.setReformatCode(true);
                    try {
                        viewPsiFile.getViewProvider().getDocument().insertString(0, template.getText(properties));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
