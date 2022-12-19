package org.example;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class Main {
    static final String API_URL = "https://api.nasa.gov/planetary/apod";
    static final String API_KEY = "AjrONsMYLVHfveflstBnyBKuNUilzflho3T12BAf";
    static final String DATE_FORMAT = "yyyy-MM-dd";

    public static void main(String[] args) {
        getNasaMedia(false); //today image
        getNasaMedia("2022-12-4", true); // by date + mediaType = Video
        getNasaMedia("2019-02-10", "2019-02-12", false); //by dates range
    }

    public static void getNasaMedia(boolean saveToFolder) {
        getMedia(createRequestUrl(), saveToFolder);
    }

    public static void getNasaMedia(String date, boolean saveToFolder) {
        if (isDateValid(date)) {
            getMedia(createRequestUrl(date), saveToFolder);
        }
    }

    public static void getNasaMedia(String dateStart, String dateEnd, boolean saveToFolder) {
        if (isDatesValid(dateStart, dateEnd)) {
            getMedia(createRequestUrl(dateStart, dateEnd), saveToFolder);
        }
    }

    public static void getMedia(String requestUrl, boolean saveToFolder) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        final CloseableHttpClient httpClient = HttpClients.createDefault();
        final HttpUriRequest httpGet = new HttpGet(requestUrl);
        try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet)) {

            if (isResponse200(httpResponse)) { //если сервер вернул 200 Ок
                List<Apod> apods = mapper.readValue(
                        httpResponse.getEntity().getContent(),
                        new TypeReference<>() {
                        }
                );

                for (Apod apod : apods) {
                    String fileName = null;
                    String loadUrl = null;

                    if (apod.getMediaType().equals("video")) {
                        if (apod.getThumbnailUrl().length() > 0) {
                            loadUrl = apod.getThumbnailUrl();
                            fileName = getFileName(apod.getThumbnailUrl());
                        } else {
                            System.out.println("Не удалось получить превью для видео от " + apod.getDate());
                        }
                    }

                    if (apod.getMediaType().equals("image")) {
                        loadUrl = apod.getHdurl();
                        fileName = getFileName(apod.getHdurl());
                    }

                    if (loadUrl != null && fileName != null) {
                        HttpUriRequest httpGetImage = new HttpGet(loadUrl);
                        try (CloseableHttpResponse httpResponseFile = httpClient.execute(httpGetImage)) {

                            if (isResponse200(httpResponseFile)) {

                                if (saveToFolder) {
                                    File folder = createFolder(apod.getDate());
                                    fileName = folder.getPath() + "/" + fileName;
                                }

                                try (FileOutputStream fos = new FileOutputStream(fileName)) {
                                    fos.write(httpResponseFile.getEntity().getContent().readAllBytes());

                                    File newApodImage = new File(fileName);
                                    if (newApodImage.canRead() && newApodImage.canWrite()) {

                                        System.out.print("Изображение от " + reformDateString(apod.getDate()) + " \"" + newApodImage.getName() + "\" загружено");
                                        if (saveToFolder) {
                                            System.out.print(" в папку \"" + newApodImage.getParent() + "\"");
                                        }
                                        System.out.println();
                                    }

                                } catch (IOException ex) {
                                    System.out.println(ex.getMessage());
                                }
                            } else {
                                System.out.println("Что-то пошло не так, код ответа сервера: " + httpResponse.getStatusLine().getStatusCode());
                            }
                        }
                    }
                }

            } else {
                System.out.println("Что-то пошло не так... Код ответа сервера: " + httpResponse.getStatusLine().getStatusCode());
            }
            httpResponse.close();
            httpClient.close();

        } catch (StreamReadException streamReadException) {
            System.out.println(streamReadException.getMessage());
        } catch (IOException clientProtocolException) {
            System.out.println(clientProtocolException.getMessage());
        }
    }

    public static String getFileName(String url) {
        String[] data = url.split("/");
        String fileName = data[data.length - 1];
        return fileName;
    }

    public static String createRequestUrl() {
        String requestUrl = API_URL + "?api_key=" + API_KEY + "&thumbs=true";
        return requestUrl;
    }

    public static String createRequestUrl(String date) {
        String requestUrl = null;
        if (isDateValid(date)) {
            requestUrl = API_URL + "?" + "api_key=" + API_KEY + "&date=" + date + "&thumbs=true";
        }
        return requestUrl;
    }

    public static String createRequestUrl(String startDate, String endDate) {
        String requestUrl = null;
        if (isDateValid(startDate) && isDateValid(endDate)) {
            requestUrl = API_URL + "?" + "api_key=" + API_KEY + "&start_date=" + startDate + "&end_date=" + endDate + "&thumbs=true";
        }
        return requestUrl;
    }

    public static boolean isDateValid(String string) {
        //Date must be between Jun 16, 1995 and Dec 18, 2022

        Date startDateApi = new GregorianCalendar(1995, 05, 15).getTime();
        Date endDateApi = new GregorianCalendar().getTime();

        try {
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setLenient(false);
            Date date = df.parse(string);
            if (date.after(startDateApi)) {
                if (date.equals(endDateApi) || date.before(endDateApi)) {
                    return true;
                }
            }

            return false;
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean isDatesValid(String date1, String date2) {
        //Date must be between Jun 16, 1995 and Dec 18, 2022

        Date startDateApi = new GregorianCalendar(1995, 05, 15).getTime();
        Date endDateApi = new GregorianCalendar().getTime();

        try {
            DateFormat df = new SimpleDateFormat(DATE_FORMAT);
            df.setLenient(false);
            Date startDate = df.parse(date1);
            Date endDate = df.parse(date2);
            if (startDate.after(endDate)) {
                System.out.println("Дата начала периода больше даты окончания!");
                return false;
            }

            if (startDate.after(startDateApi) && endDate.after(startDateApi)) {
                if (endDate.equals(endDateApi) || endDate.before(endDateApi)) {
                    return true;
                }
            }

            return false;
        } catch (ParseException e) {
            return false;
        }
    }

    public static boolean isResponse200(CloseableHttpResponse httpResponse) {
        if (httpResponse.getStatusLine().getStatusCode() == 200) {
            return true;
        }
        return false;
    }

    public static File createFolder(String folderName) {
        File parentFolder = new File("APOD images");
        if (!parentFolder.exists()) {
            parentFolder.mkdir();
        }

        File folder = new File(parentFolder.getPath() + "/" + folderName); //папка изображений для даты
        if (!folder.exists()) {
            folder.mkdir();
        }
        return folder;
    }

    public static String reformDateString(String inputDate) {
        try {
            SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd");
            Date date = dt.parse(inputDate);
            SimpleDateFormat dt1 = new SimpleDateFormat("dd.MM.yyyy");
            return dt1.format(date);

        } catch (ParseException e) {
            return inputDate;
        }
    }
}


