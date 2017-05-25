package controllers;

import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.*;
import com.amazonaws.services.dynamodbv2.document.spec.DeleteItemSpec;
import com.amazonaws.services.dynamodbv2.document.spec.GetItemSpec;
import play.mvc.Controller;
import play.mvc.Result;
import views.html.index;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DynamoController extends Controller {

    AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
            .withRegion(Regions.US_WEST_1)
            .build();
    DynamoDB dynamoDB = new DynamoDB(client);
    Table table = dynamoDB.getTable("lists");

    public Result index() {
        return ok(index.render());
    }

    public Result create(String key, String value) {

        GetItemSpec getItemSpec = new GetItemSpec()
                .withPrimaryKey("key", key);
        Set<String> ss = new HashSet<String>();
        try {
            Item outcome = table.getItem(getItemSpec);
            if(outcome!=null) {
                ss = outcome.getStringSet("value");
            }
        }
        catch (Exception e) {
            System.err.println(e.getMessage());
        }

        ss.add(value);

        try {
            PutItemOutcome outcome = table.putItem(new Item()
                    .withPrimaryKey("key", key)
                    .withStringSet("value", ss));
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return ok(ss.toString());
    }

    public Result read(String key) {
        response().setHeader("Access-Control-Allow-Origin", "*");
        GetItemSpec getItemSpec = new GetItemSpec()
                .withPrimaryKey("key", key);
        try {
            Item outcome = table.getItem(getItemSpec);
            if(outcome==null){
                return ok(key + " not populated");
            }
            return ok(outcome.getStringSet("value").toString());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ok(e.getMessage());
        }
    }

    public Result update(String key, String value) {
        try {
            Set<String> h = new HashSet<String>();
            h.add(value);
            PutItemOutcome outcome = table.putItem(new Item()
                    .withPrimaryKey("key", key)
                    .withStringSet("value", value));
            return ok(h.toString());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ok(e.getMessage());
        }
    }

    public Result delete(String key) {
        DeleteItemSpec deleteItemSpec = new DeleteItemSpec()
                .withPrimaryKey(new PrimaryKey("key", key));
        try {
            table.deleteItem(deleteItemSpec);
            return ok(key + " deleted");
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ok(e.getMessage());
        }
    }

    public Result post(String key) {
        GetItemSpec getItemSpec = new GetItemSpec()
                .withPrimaryKey("key", key);
        try {
            String item = request().body().asText();
            if(item==null) {
                Map<String, String[]> form = request().body().asFormUrlEncoded();
                item = decode((String) form.keySet().toArray()[0]);
                if(item==null) {return null;}
            }
            Item outcome = table.getItem(getItemSpec);
            Set<String> result = outcome==null ? new HashSet<String>() : outcome.getStringSet("value");
            System.out.println("result: " + result + " item: " + item);
            if(item.contains("[") || item.contains("]")) {
                result.addAll(bodyToList(item));
            }
            else {
                result.add(item);
            }
            try {
                PutItemOutcome putter = table.putItem(new Item()
                        .withPrimaryKey("key", key)
                        .withStringSet("value", result));
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
            return ok(result.toString());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ok(e.getMessage());
        }
    }

    public Result put(String key) {
        try {
            String item = request().body().asText();
            if(item==null) {
                Map<String, String[]> form = request().body().asFormUrlEncoded();
                item = decode((String) form.keySet().toArray()[0]);
                if(item==null) {return null;}
            }
            Set<String> result = new HashSet<String>();
            if(item.contains("[") || item.contains("]")) {
                result = bodyToList(item);
            }
            else {
                result.add(item);
            }
            PutItemOutcome outcome = table.putItem(new Item()
                    .withPrimaryKey("key", key)
                    .withStringSet("value", result));
            return ok(result.toString());
        } catch (Exception e) {
            System.err.println(e.getMessage());
            return ok(e.getMessage());
        }
    }

    //Helper Functions
    public static String decode(String value) throws UnsupportedEncodingException {
        return URLDecoder.decode(value, "UTF-8");
    }

    public static Set<String> bodyToList(String body) {
        body = body.replace("[","").replace("]","");
        List<String> mapping = Arrays.asList(body.split(","));
        Stream<String> stream = mapping.stream().map(item -> {
            return item.charAt(0) == ' ' ? item.substring(1, item.length()) : item;
        });
        Set<String> result = new HashSet<String>(stream.collect(Collectors.toCollection(ArrayList::new)));
        return result;
    }
}
