package com.github.apigggy.handler;

import com.github.apigggy.schema.Tree;
import com.github.apigggy.Environment;

/**
 * 文档结构树访问器
 */
public interface TreeHandler {

    void handle(Tree tree, Environment env);

}
