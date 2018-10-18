package com.github.apiggy.handler;

import com.github.apiggy.schema.Tree;
import com.github.apiggy.Environment;

/**
 * 文档结构树访问器
 */
public interface TreeHandler {

    void handle(Tree tree, Environment env);

}
