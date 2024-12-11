import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import java.net.*;
import java.io.*;
import java.awt.Desktop;

public class WikiSearch {

    public static void main(String[] args) {
        try {
            // Считывание ввода пользователя
            BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
            System.out.println("Введите запрос для поиска в Википедии:");
            String query = reader.readLine();

            // Генерация запроса для Википедии с URL-кодированием
            String encodedQuery = URLEncoder.encode(query, "UTF-8");
            String urlString = "https://ru.wikipedia.org/w/api.php?action=query&list=search&utf8=&format=json&srsearch=" + encodedQuery;

            // Получение ответа от Википедии
            String jsonResponse = sendRequest(urlString);

            // Парсинг JSON-ответа
            WikiSearchResponse response = parseJsonResponse(jsonResponse);

            // Вывод результатов поиска
            System.out.println("Результаты поиска:");
            JsonArray searchResults = response.query.search;
            for (int i = 0; i < searchResults.size(); i++) {
                JsonObject article = searchResults.get(i).getAsJsonObject();
                String title = article.get("title").getAsString();
                int pageId = article.get("pageid").getAsInt();
                System.out.println((i + 1) + ". " + title + " (Page ID: " + pageId + ")");
            }

            // Выбор статьи
            System.out.println("Введите номер статьи для открытия:");
            int choice = Integer.parseInt(reader.readLine()) - 1;

            if (choice >= 0 && choice < searchResults.size()) {
                int pageId = searchResults.get(choice).getAsJsonObject().get("pageid").getAsInt();
                openPageInBrowser(pageId);
            } else {
                System.out.println("Неверный номер статьи.");
            }

        } catch (Exception e) {
            System.err.println("Произошла ошибка: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Отправка HTTP-запроса
    private static String sendRequest(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        return response.toString();
    }

    // Парсинг JSON-ответа
    private static WikiSearchResponse parseJsonResponse(String jsonResponse) {
        Gson gson = new Gson();
        return gson.fromJson(jsonResponse, WikiSearchResponse.class);
    }

    // Открытие страницы в браузере
    private static void openPageInBrowser(int pageId) throws IOException {
        String url = "https://ru.wikipedia.org/w/index.php?curid=" + pageId;
        Desktop desktop = Desktop.getDesktop();
        desktop.browse(URI.create(url));
    }

    // Классы для парсинга JSON-ответа
    private static class WikiSearchResponse {
        Query query;
    }

    private static class Query {
        JsonArray search;
    }
}