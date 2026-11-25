package org.shaydi;

import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Named("resultBean")
@SessionScoped
public class ResultBean implements Serializable {

    private final List<Result> results = new CopyOnWriteArrayList<>();

    public ResultBean(){}

    public void addResult(Result result) {
        results.add(result);
    }

    public List<Result> getResults() {
        return results;
    }

    public void clear(){
        results.clear();
    }
}
