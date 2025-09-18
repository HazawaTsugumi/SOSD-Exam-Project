package com.sosd.utils;

import com.hankcs.hanlp.HanLP;
import org.commonmark.node.AbstractVisitor;
import org.commonmark.node.Node;
import org.commonmark.node.Paragraph;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;

import java.util.ArrayList;
import java.util.List;

public class MarkdownUtil {

    public static final Parser parser = Parser.builder().build();

    //TODO:记
    //将Md文章按段拆分
    public static List<String> divideParagraphs(String content) {
        List<String> paragraphs = new ArrayList<>();
        Node document = parser.parse(content);
        document.accept(new AbstractVisitor() {
            @Override
            public void visit(Paragraph paragraph) {
                TextContentRenderer renderer = TextContentRenderer.builder().build();
                String renderText = renderer.render(paragraph);
                paragraphs.add(renderText);
            }
        });
        return paragraphs;
    }
    //TODO
//    List<String> abstractParagraph = HanLP.extractSummary(paragraphs.get(i), 1);
}
