package org.shaydi;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@WebServlet("/areaCheck")
public class AreaCheckServlet extends HttpServlet {

    private static final List<Double> rOkay = List.of(1.0, 2.0, 3.0, 4.0, 5.0);
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        long start = System.nanoTime();

        double x, y, r;
        try {
            x = Double.parseDouble(req.getParameter("x").replace(',','.'));
            y = Double.parseDouble(req.getParameter("y").replace(',','.'));
            r = Double.parseDouble(req.getParameter("r").replace(',','.'));
        } catch (NumberFormatException e) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "x, y, and r must be doubles");
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

        HttpSession session = req.getSession();
        ResultBean bean = (ResultBean) session.getAttribute("resultBean");
        if (bean == null) {
            bean = new ResultBean();
            session.setAttribute("resultBean", bean);
        }
        bean.addResult(result);

        renderPage(resp, result);
    }


    private boolean isValid(double x, double y, double r) {
        boolean xOk = (-5 <= x) && (x < 5);
        boolean yOk = (-5 < y) && (y < 5);
        boolean rOk = rOkay.contains(r);
        return xOk && yOk && rOk;
    }

    private boolean checkHit(double x, double y, double r) {
        return ((x*x + y*y <= r*r) && x >= 0 && y <=0) ||       // quater of circle
                (x <= 0 && y >= 0 && x >= -r/2 && y <= r) ||    // rectangle
                (x <= 0 && y <= 0 && y >= -x - r/2);            // triangle
    }


    private void renderPage(HttpServletResponse resp, Result res) throws IOException {
        resp.setContentType("text/html;charset=UTF-8");
        PrintWriter out = resp.getWriter();
        out.println("<!DOCTYPE html><html lang='ru'><head><meta charset='UTF-8'>");
        out.println("<link rel='stylesheet' href='styles.css'><title>Result</title></head><body>");
        out.println("<table><tr><th>X</th><th>Y</th><th>R</th><th>Result</th></tr>");
        out.printf("<tr><td>%.2f</td><td>%.2f</td><td>%.0f</td><td>%s</td></tr>%n",
                res.getX(), res.getY(), res.getR(), res.getHit() ? "Hit" : "Miss");
        out.println("</table>");
        out.println("<a href='controller'>Back to form</a>");
        out.println("</body></html>");
    }
}
