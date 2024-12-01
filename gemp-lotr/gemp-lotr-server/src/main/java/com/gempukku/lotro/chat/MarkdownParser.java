package com.gempukku.lotro.chat;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.node.Image;
import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.html.HtmlWriter;

import java.util.*;
import java.util.regex.Pattern;

public class MarkdownParser {

    private final Parser _markdownParser;
    private final HtmlRenderer _markdownRenderer;

    private static Pattern QuoteExtender = Pattern.compile("^([ \t]*>[ \t]*.+)(?=\n[ \t]*[^>])", Pattern.MULTILINE);

    public MarkdownParser() {
        List<Extension> adminExt = Arrays.asList(StrikethroughExtension.create(), AutolinkExtension.create());
        _markdownParser = Parser.builder()
                .extensions(adminExt)
                .build();
        _markdownRenderer = HtmlRenderer.builder()
                .nodeRendererFactory(htmlContext -> new LinkShredder(htmlContext))
                .extensions(adminExt)
                .escapeHtml(true)
                .sanitizeUrls(true)
                .softbreak("<br />")
                .build();
    }

    public String renderMarkdown(String markdown) {
        String newMsg = markdown.trim().replaceAll("\n\n\n+", "\n\n\n");
        newMsg = QuoteExtender.matcher(newMsg).replaceAll("$1\n");
        //Escaping underscores so that URLs with lots of underscores (i.e. wiki links) aren't mangled
        // Besides, who uses _this_ instead of *this*?
        newMsg = newMsg.replace("_", "\\_");

        //Need to preserve any commands being made
        if(!newMsg.startsWith("/")) {
            newMsg = _markdownRenderer.render(_markdownParser.parse(newMsg));
            // Prevent quotes with newlines from displaying side-by-side
            newMsg = newMsg.replaceAll("</blockquote>[\n \t]*<blockquote>", "</blockquote><br /><blockquote>");
            //Make all links open in a new tab
            newMsg = newMsg.replaceAll("<(a href=\".*?\")>", "<$1 target=\"_blank\">");
        }

        return newMsg;
    }


    //This class is used to ensure that markdown links [like this](https://somewebsite.com) are rendered literally.
    // This is to ensure that people are unable to falsely represent links as other links.
    private class LinkShredder implements NodeRenderer {

        private final HtmlWriter html;

        LinkShredder(HtmlNodeRendererContext context) {
            this.html = context.getWriter();
        }

        @Override
        public Set<Class<? extends Node>> getNodeTypes() {
            // Return the node types we want to use this renderer for.
            return new HashSet<>(Arrays.asList(
                    Link.class,
                    Image.class
            ));
        }

        @Override
        public void render(Node node) {
            if(node instanceof Link link) {
                if(link.getTitle() != null) {
                    html.text(link.getTitle() + ": " + link.getDestination());
                }
                else {
                    if(link.getFirstChild() != null
                            && link.getFirstChild() instanceof Text text
                            && !text.getLiteral().equals(link.getDestination()))
                    {
                        html.text(text.getLiteral() + ": " + link.getDestination());
                    }
                    else {
                        html.tag("a", Collections.singletonMap("href", link.getDestination()));
                        html.text(link.getDestination());
                        html.tag("/a");
                    }
                }

            }
            else if(node instanceof Image image){
                html.text(image.getTitle() + ": " + image.getDestination());
            }
        }
    }

}
