package org.shaydi;

import jakarta.inject.Inject;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jdk.jshell.execution.LoaderDelegate;

import java.io.IOException;

@WebServlet("/controller")
public class ControllerServlet extends HttpServlet {

    @Inject
    private ResultBean resultBean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("true".equals(req.getParameter("clear"))) {
            resultBean.clear();
            req.getRequestDispatcher("/WEB-INF/index.jsp").forward(req, resp);
            return;
        }

        if (hasAnyCoordinate(req)) {
            req.getRequestDispatcher("/areaCheck").forward(req, resp);
        } else {
            req.getRequestDispatcher("/WEB-INF/index.jsp").forward(req, resp);
        }
    }

    private boolean hasAnyCoordinate(HttpServletRequest req) {
        return req.getParameter("x") != null || req.getParameter("y") != null || req.getParameter("r") != null;
    }
}
