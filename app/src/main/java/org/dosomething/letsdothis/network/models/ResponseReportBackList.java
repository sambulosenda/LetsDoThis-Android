package org.dosomething.letsdothis.network.models;
import org.dosomething.letsdothis.data.ReportBack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by izzyoji :) on 4/23/15.
 */
public class ResponseReportBackList
{

    public ReportBack data[];
    public Pagination pagination;

    // If there's an error, the response will instead include this object
    public Error error;

    public static List<ReportBack> getReportBacks(ResponseReportBackList response) {
        return new ArrayList<>(Arrays.asList(response.data));
    }

    public static class Pagination {
        public int total;
        public int per_page;
        public int current_page;
        public int total_pages;
    }

    public static class Error {
        public String message;
    }
}
