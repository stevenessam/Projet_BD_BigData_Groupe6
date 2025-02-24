package CRUDFiles;

import com.mongodb.client.AggregateIterable;
import com.mongodb.client.FindIterable;
import com.mongodb.client.ListIndexesIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Accumulators;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;

import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.Arrays;
import java.util.Iterator;

public class AgregationsCRUD {

    public static void getAvisByClientId(MongoDatabase database, int clientId) {
        try {
            // Obtenez la collection Avis
            AggregateIterable<Document> result = database.getCollection("Avis").aggregate(Arrays.asList(
                    Aggregates.match(Filters.eq("ClientID", clientId)),
                    Aggregates.lookup("Clients", "ClientID", "_id", "clientInfo"),
                    Aggregates.lookup("Trottinettes", "TrottinetteID", "_id", "trottinetteInfo"),
                    Aggregates.unwind("$clientInfo"),
                    Aggregates.unwind("$trottinetteInfo"),
                    Aggregates.project(
                            Document.parse(
                                    "{_id: 1, MessageAvis: 1, Nom: \"$clientInfo.Nom\", Prenom: \"$clientInfo.Prenom\", TrottinetteID: \"$trottinetteInfo._id\"}"))));

            // Affichez les résultats
            for (Document document : result) {
                System.out.println("Nom du client: " + document.getString("Nom"));
                System.out.println("Prénom du client: " + document.getString("Prenom"));
                System.out.println("ID de la trottinette: " + document.getInteger("TrottinetteID"));
                System.out.println("Message Avis: " + document.getString("MessageAvis"));
                System.out.println("--------------------");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void getTransactionCountByClientId(MongoDatabase database, int clientId) {
        try {
            // Obtenez la collection Transactions
            AggregateIterable<Document> result = database.getCollection("Transactions").aggregate(Arrays.asList(
                    Aggregates.match(Filters.eq("ClientID", clientId)),
                    Aggregates.group("$ClientID", Accumulators.sum("TransactionCount", 1)),
                    Aggregates.lookup("Clients", "_id", "_id", "clientInfo"),
                    Aggregates.project(
                            Document.parse(
                                    "{_id: 1, TransactionCount: 1, Email: { $arrayElemAt: [\"$clientInfo.Email\", 0] }}"))));

            // Affichez les résultats
            for (Document document : result) {
                System.out.println("Email du client: " + document.getString("Email"));
                System.out.println("Nombre de transactions: " + document.getInteger("TransactionCount"));
                System.out.println("--------------------");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void getClientReservationsInfo(MongoDatabase database, int clientId) {
        try {
            // Define the aggregation pipeline stages
            Bson matchStage = Aggregates.match(Filters.eq("ClientID", clientId));
            Bson lookupStage = Aggregates.lookup("Clients", "ClientID", "_id", "client_info");
            Bson unwindStage = Aggregates.unwind("$client_info");
            Bson groupStage = Aggregates.group(
                    "$ClientID",
                    Accumulators.sum("ReservationCount", 1),
                    Accumulators.sum("TotalTarif", "$Tarif"),
                    Accumulators.first("Nom", "$client_info.Nom"),
                    Accumulators.first("Prenom", "$client_info.Prenom"),
                    Accumulators.first("Email", "$client_info.Email"));

            // Execute the aggregation query
            AggregateIterable<Document> result = database.getCollection("Reservations")
                    .aggregate(Arrays.asList(matchStage, lookupStage, unwindStage, groupStage));

            // Print the result
            for (Document document : result) {
                System.out.println("Nom: " + document.getString("Nom"));
                System.out.println("Prenom: " + document.getString("Prenom"));
                System.out.println("Email: " + document.getString("Email"));
                System.out.println("Nombre de réservations : " + document.getInteger("ReservationCount"));
                System.out.println("Tarif total : " + document.getDouble("TotalTarif"));
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void searchClientsConditionnelle(String collectionName, Document filter, Document sort,
            Document projection,
            MongoDatabase database) {
        System.out.println("\n\n\n*********** in searchClients *****************");

        try {
            // Get the Clients collection
            MongoCollection<Document> collection = database.getCollection(collectionName);

            // Create the query
            FindIterable<Document> clientList = collection
                    .find(filter)
                    .sort(sort)
                    .projection(projection);

            // Getting the iterator
            Iterator it = clientList.iterator();
            while (it.hasNext()) {
                System.out.println(it.next());
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    public static void joinCollection1EtCollection2(
            String localCollectionName,
            String foreignCollectionName,
            String localColJoinFieldName,
            String foreigColJoinFieldName,
            Document filterFieldsOnLocalCollection,
            String namedJoinedElements,
            MongoDatabase database) {
        AggregateIterable<Document> outputColl;
        MongoCollection<Document> joinColl = database.getCollection(localCollectionName);
        System.out.println("\n\n\n****************************");

        outputColl = joinColl.aggregate(Arrays.asList(
                Aggregates.match(filterFieldsOnLocalCollection),
                Aggregates.lookup(foreignCollectionName, localColJoinFieldName, foreigColJoinFieldName,
                        namedJoinedElements)));

        for (Document colDoc : outputColl) {
            System.out.println(colDoc);
        }
    }

    // Group By Operation
    public static void groupBy(
            String collectionName,
            String groupOperator,
            Document groupFields,
            MongoDatabase database) {
        MongoCollection<Document> collection = database.getCollection(collectionName);
        Document groupStage = new Document(groupOperator, groupFields);

        AggregateIterable<Document> output = collection.aggregate(Arrays.asList(groupStage));

        System.out
                .println("\n\n\n*********** Group By Operation on " + collectionName + " Collection *****************");
        for (Document document : output) {
            System.out.println(document);
        }
    }

    public static void createClientIndexes(
            String localCollectionName,
            String indexName,
            String fieldName,
            boolean isAscendingIndex,
            boolean indexUnique,
            MongoDatabase database) {
        System.out.println("\n\n\n*********** Creating Indexes *****************");
        MongoCollection<Document> collection = database.getCollection(localCollectionName);
        IndexOptions indexOptions = new IndexOptions();

        if (indexName != null)
            indexOptions.unique(indexUnique).name(indexName);
        else
            indexOptions.unique(indexUnique);

        if (isAscendingIndex)
            collection.createIndex(new Document(fieldName, 1), indexOptions);
        else
            collection.createIndex(new Document(fieldName, -1), indexOptions);

        System.out.println("Index created successfully.");
    }

    public static void getAllIndexesOfACollection(String localCollectionName, MongoDatabase database) {
        System.out.println("\n\n\n*********** Get All Indexes of a Collection *****************");
        MongoCollection<Document> collection = database.getCollection(localCollectionName);
        ListIndexesIterable<Document> indexIterable = collection.listIndexes();
        Iterator<Document> iterator = indexIterable.iterator();

        while (iterator.hasNext()) {
            System.out.println(iterator.next());
        }
    }
}