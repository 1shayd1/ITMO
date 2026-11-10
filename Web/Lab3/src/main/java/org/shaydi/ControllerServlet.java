package org.shaydi;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/controller")
public class ControllerServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if ("true".equals(req.getParameter("clear"))) {
            ResultBean bean = (ResultBean) req.getSession().getAttribute("resultBean");
            if (bean != null) bean.clear();
            req.getRequestDispatcher("/WEB-INF/index.jsp").forward(req, resp);
            return;
        }
        if (hasCoordinates(req)) {
            req.getRequestDispatcher("/areaCheck").forward(req, resp);
        } else {
            req.getRequestDispatcher("/WEB-INF/index.jsp").forward(req, resp);
        }
    }

    private boolean hasCoordinates(HttpServletRequest request) {
        return isPresent(request.getParameter("x")) &&
                isPresent(request.getParameter("y")) &&
                isPresent(request.getParameter("r"));
    }

    private boolean isPresent (String value) {
        return (value != null) && (!value.isBlank());
    }
}
