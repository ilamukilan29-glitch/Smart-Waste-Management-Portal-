package com.waste.management.util;

import java.io.PrintWriter;
import java.util.List;

/**
 * Utility for generating RFC-4180 compliant CSV streams.
 */
public class CsvExportUtil {

    public static void writeCsv(PrintWriter writer, List<String> headers, List<List<String>> rows) {
        // Write header
        writeRow(writer, headers);

        // Write rows
        if (rows != null) {
            for (List<String> row : rows) {
                writeRow(writer, row);
            }
        }
        writer.flush();
    }

    private static void writeRow(PrintWriter writer, List<String> fields) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < fields.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(escape(fields.get(i)));
        }
        writer.println(sb.toString());
    }

    private static String escape(String field) {
        if (field == null) {
            return "\"\"";
        }
        String escaped = field.replace("\"", "\"\"");
        return "\"" + escaped + "\"";
    }
}
