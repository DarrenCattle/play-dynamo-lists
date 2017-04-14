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

import java.util.HashSet;
import java.util.Set;

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
}
