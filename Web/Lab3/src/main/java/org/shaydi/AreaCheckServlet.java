package org.shaydi;

import jakarta.inject.Inject;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet("/areaCheck")
public class AreaCheckServlet extends HttpServlet {

    private static final List<Double> rOkay = List.of(1.0, 2.0, 3.0, 4.0, 5.0);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @Inject
    private ResultBean resultBean;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        long start = System.nanoTime();

        if (req.getDispatcherType() != DispatcherType.FORWARD) {
            String query = req.getQueryString();
            resp.sendRedirect(req.getContextPath() + "/controller" + (query != null ? "?" + query : ""));
            return;
        }

        String xs = req.getParameter("x");
        String ys = req.getParameter("y");
        String rs = req.getParameter("r");
        if (isBlank(xs) || isBlank(ys) || isBlank(rs)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "x, y and r are required");
            return;
        }

        double x, y, r;
        try {
            x = Double.parseDouble(xs.replace(',', '.'));
            y = Double.parseDouble(ys.replace(',', '.'));
            r = Double.parseDouble(rs.replace(',', '.'));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "x, y and r must be numbers");
            return;
        }

        if (!isValid(x,y,r)) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "values out of range");
            return;
        }

        boolean hit = checkHit(x,y,r);
        long workTime = (System.nanoTime() - start) / 1000;
        String time = LocalDateTime.now().format(FMT);
        Result result = new Result(x,y,r,hit, time, workTime);

        resultBean.addResult(result);

        req.setAttribute("result", result);
        req.getRequestDispatcher("/WEB-INF/result.jsp").forward(req, resp);
    }


    private boolean isValid(double x, double y, double r) {
        boolean xOk = (-5
         <= x) && (x < 5);
        boolean yOk = (-5 < y) && (y < 5);
        boolean rOk = rOkay.contains(r);
        return xOk && yOk && rOk;
    }

    private boolean checkHit(double x, double y, double r) {
        return ((x*x + y*y <= r*r) && x >= 0 && y <=0) ||       // quater of circle
                (x <= 0 && y >= 0 && x >= -r/2 && y <= r) ||    // rectangle
                (x <= 0 && y <= 0 && y >= -x - r/2);            // triangle
    }

    private boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
