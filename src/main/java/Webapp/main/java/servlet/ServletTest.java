package Webapp.main.java.servlet;

import Http.HttpRequest;
import Http.HttpResponse;
import Loader.HttpServlet;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class ServletTest extends HttpServlet {

    private String message;

    @Override
    protected void doPost(HttpRequest request, HttpResponse response) throws IOException {

    }
    @Override
    protected void doGet(HttpRequest request, HttpResponse response) throws IOException {
        // 设置:响应内容类型
        response.setContentType("text/html");
        //System.out.println("hello");

        // 输出文本
        response.write("WebContent/index.html");
    }

}
