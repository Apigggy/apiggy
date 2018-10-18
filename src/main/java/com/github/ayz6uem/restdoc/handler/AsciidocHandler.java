package com.github.ayz6uem.restdoc.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.ayz6uem.restdoc.RestDoc;
import com.github.ayz6uem.restdoc.http.HttpMessage;
import com.github.ayz6uem.restdoc.http.HttpRequest;
import com.github.ayz6uem.restdoc.http.HttpResponse;
import com.github.ayz6uem.restdoc.schema.Cell;
import com.github.ayz6uem.restdoc.schema.Group;
import com.github.ayz6uem.restdoc.schema.Node;
import com.github.ayz6uem.restdoc.schema.Tree;
import com.github.ayz6uem.restdoc.util.AttributeAsciidocBuilder;
import com.github.ayz6uem.restdoc.util.ObjectMappers;

import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * adoc文件构建器
 */
public class AsciidocHandler implements RestDocHandler {

    AttributeAsciidocBuilder builder = AttributeAsciidocBuilder.newInstance();

    @Override
    public void handle(RestDoc restDoc) {
        Tree tree = restDoc.getTree();
        builder.documentTitle(tree.getName());
        if(Objects.nonNull(tree.getDescription())){
            builder.paragraph(tree.getDescription(),true);
        }

        for (int i = 0; i < tree.getGroups().size(); i++) {
            Group group = tree.getGroups().get(i);
            buildGroup(group, "", i + 1);
        }

        String adoc = restDoc.getEnv().getOut() + "/" + restDoc.getEnv().getProject();

        Path indexFile = Paths.get(adoc);
        builder.writeToFile(indexFile, StandardCharsets.UTF_8);
    }


    private void buildGroup(Group group, String prefix, int num) {
        builder.sectionTitleLevel1(prefix + num + " " + group.getName());
        if (Objects.nonNull(group.getDescription())) {
            builder.paragraph(group.getDescription(),true);
        }
        for (int i = 0; i < group.getNodes().size(); i++) {
            HttpMessage httpMessage = group.getNodes().get(i);
            buildHttpMessage(httpMessage, prefix + num + ".", i + 1);
        }
    }

    private void buildHttpMessage(HttpMessage message, String prefix, int num) {
        builder.sectionTitleLevel2(prefix + num + " " + message.getName());
        if (Objects.nonNull(message.getDescription())) {
            builder.paragraph(message.getDescription(),true);
        }

        HttpRequest request = message.getRequest();
        builder.block(builder -> {
            for (String uri : request.getUris()) {
                builder.textLine(request.getMethod()
                        + " "
                        + uri
                        + request.queryString()
                        + " "
                        + message.getVersion());
            }
            request.getHeaders().forEach((k,v) -> builder.textLine(k+": "+v));
            if(request.hasBody()){
                builder.newLine();
                builder.textLine(request.bodyString());
            }
        }, "REQUEST");

        table(request.getCells());

        HttpResponse response = message.getResponse();
        if (!response.isEmpty()) {
            builder.block(builder -> {
                builder.textLine(message.getVersion()+" " + response.getStatus());
                response.getHeaders().forEach((k,v) -> builder.textLine(k + ": "+v));
                if(response.hasBody()){
                    builder.newLine();
                    builder.textLine(response.bodyString());
                }
            }, "RESPONSE");
            table(response.getCells());
        }

    }

    private void table(List<Cell> cells){
        if (cells.size() > 0) {
            List<List<String>> responseTable = new ArrayList<>();
            responseTable.add(Arrays.asList("NAME", "TYPE", "DEFAULT", "DESCRIPTION"));
            cells.forEach(parameter -> responseTable.add(parameter.toList()));
            builder.table(responseTable);
        }

    }

}
