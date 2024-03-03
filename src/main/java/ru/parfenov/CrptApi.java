package ru.parfenov;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@AllArgsConstructor
public class CrptApi {
    private static TimeUnit TIMEUNIT = TimeUnit.MINUTES;
    private static int REQUEST_LIMIT = 10;
    private final TimeUnit timeUnit;
    private final int requestLimit;

    public static void main(String[] args) throws InterruptedException {
        CrptApi crptApi = new CrptApi(TIMEUNIT, REQUEST_LIMIT);
        crptApi.toDo();
    }

    public void toDo() throws InterruptedException {
        InnerServlet servlet = new InnerServlet();
        InnerServer server = new InnerServer(servlet);
        for (long i = System.currentTimeMillis();
             i < Long.MAX_VALUE;
             i += TimeUnit.MILLISECONDS.convert(1L, timeUnit)) {
            ExecutorService pool = Executors.newFixedThreadPool(requestLimit);
            pool.submit(new Runnable() {
                @Override
                public void run() {
                    server.run();
                }
            });
        }

    }

    @AllArgsConstructor
    class InnerServer {
        private final InnerServlet servlet;

        public void run() {}
    }

    @WebServlet(name = "InnerServlet", urlPatterns = "https://ismp.crpt.ru/api/v3/lk/documents/create")
    class InnerServlet extends HttpServlet {
        private final ApiService service = new ApiService();

        @Override
        public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
            Scanner scanner = new Scanner(request.getInputStream());
            String userJson = scanner.useDelimiter("\\A").next();
            scanner.close();
            ObjectMapper objectMapper = new ObjectMapper();
            DocumentFromRequest doc = objectMapper.readValue(userJson, DocumentFromRequest.class);
            service.save(doc);
        }
    }

    class ApiService {
        public void save(DocumentFromRequest doc) {}
    }


    @AllArgsConstructor
    class Description {
        private final String participantInn;
    }

    enum DocType {
        LP_INTRODUCE_GOODS;
    }

    @AllArgsConstructor
    class Product {
        private final String certificateDocument;
        private final Date certificateDocumentDate;
        private final String certificateDocumentNumber;
        private final String ownerInn;
        private final String producerInn;
        private final Date productionDate;
        private final String tnvedCode;
        private final String uitCode;
        private final String uituCode;
    }

    @AllArgsConstructor
    class DocumentFromRequest {
        private final Description description;
        private final String docId;
        private final String docStatus;
        private final DocType docType;
        private final boolean importRequest;
        private final String ownerInn;
        private final String participantInn;
        private final String producerInn;
        private final Date productionDate;
        private final String productionType;
        private final List<Product> products;
        private final Date regDate;
        private final String regNumber;
    }
}
