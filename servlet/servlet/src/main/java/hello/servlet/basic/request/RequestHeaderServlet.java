package hello.servlet.basic.request;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet(name = "requestHeaderServlet", urlPatterns = "/request-header")
public class RequestHeaderServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

    }
    }
    //start line 정보
//    private void printStartLine(HttpServletRequest request) {
//        System.out.println("--- REQUEST-LINE - start ---");
//        System.out.println("request.getMethod() = " + request.getMethod()); //GET
//        System.out.println("request.getProtocol() = " + request.getProtocol()); //
//
//        System.out.println("request.getScheme() = " + request.getScheme()); //http
//        // http://localhost:8080/request-header
//        System.out.println("request.getRequestURL() = " + request.getRequestURL());
//        // /request-header
//        System.out.println("request.getRequestURI() = " + request.getRequestURI());
//        //username=hi
//        System.out.println("request.getQueryString() = " +
//                request.getQueryString());
//        System.out.println("request.isSecure() = " + request.isSecure()); //https 사용
//
//        System.out.println("--- REQUEST-LINE - end ---");
//        System.out.println();
//    }
//}
