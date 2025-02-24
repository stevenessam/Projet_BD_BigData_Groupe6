package CRUDFiles;

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

import java.util.List;

import org.bson.Document;

public class AvisCRUD {

    // Méthode privée pour obtenir la collection Avis
    private static MongoCollection<Document> getAvisCollection(MongoDatabase database) {
        return database.getCollection("Avis");
    }

    // Méthode pour insérer un avis dans la collection Avis avec un ID spécifié
    public static void insertAvis(MongoDatabase database, int avisId, int clientID, int trottinetteID,
            String messageAvis) {
        MongoCollection<Document> avisCollection = getAvisCollection(database);

        // Créer le document pour l'avis avec l'ID spécifié
        Document nouvelAvis = new Document("_id", avisId)
                .append("ClientID", clientID)
                .append("TrottinetteID", trottinetteID)
                .append("MessageAvis", messageAvis);

        // Insérer le document dans la collection
        avisCollection.insertOne(nouvelAvis);

        System.out.println("Avis inséré avec succès.");
    }

    // Méthode pour lire un avis par son ID
    public static void readAvis(MongoDatabase database, int avisId) {
        MongoCollection<Document> avisCollection = getAvisCollection(database);

        // Rechercher l'avis par son ID
        Document query = new Document("_id", avisId);
        FindIterable<Document> result = avisCollection.find(query);

        // Vérifier si l'avis existe
        if (result.first() != null) {
            Document avis = result.first();
            System.out.println("Détails de l'avis :");
            System.out.println("_id: " + avis.getInteger("_id"));
            System.out.println("ClientID: " + avis.getInteger("ClientID"));
            System.out.println("TrottinetteID: " + avis.getInteger("TrottinetteID"));
            System.out.println("MessageAvis: " + avis.getString("MessageAvis"));
        } else {
            System.out.println("Avis non trouvé.");
        }
    }

    // Méthode pour mettre à jour un avis dans la collection Avis
    public static void updateAvis(MongoDatabase database, int avisId, int clientID, int trottinetteID,
            String nouveauMessage) {
        MongoCollection<Document> avisCollection = getAvisCollection(database);

        // Créer un filtre pour trouver l'avis à mettre à jour
        Document filtre = new Document("_id", avisId);

        // Créer un document de mise à jour avec tous les champs
        Document miseAJour = new Document("$set", new Document()
                .append("ClientID", clientID)
                .append("TrottinetteID", trottinetteID)
                .append("MessageAvis", nouveauMessage));

        // Effectuer la mise à jour
        avisCollection.updateOne(filtre, miseAJour);

        System.out.println("Avis mis à jour avec succès.");
    }

    // Méthode pour supprimer un avis de la collection Avis
    public static void deleteAvis(MongoDatabase database, int avisId) {
        MongoCollection<Document> avisCollection = getAvisCollection(database);

        // Supprimer l'avis de la collection Avis
        avisCollection.deleteOne(new Document("_id", avisId));

        // Mettre à jour la collection Trottinettes en supprimant l'avis de l'array
        // AvisID
        MongoCollection<Document> trottinettesCollection = database.getCollection("Trottinettes");
        Document miseAJourTrottinettes = new Document("$pull", new Document("AvisID", avisId));
        trottinettesCollection.updateMany(new Document("AvisID", avisId), miseAJourTrottinettes);

        System.out.println("Avis supprimé avec succès.");
    }

    // -----------------------------------------Fonction-MANY-------------------------------------------------------

    // Méthode pour insérer plusieurs avis dans la collection Avis
    public static void insertManyAvis(MongoDatabase database, List<Document> avisList) {
        MongoCollection<Document> avisCollection = getAvisCollection(database);

        // Insérer plusieurs documents dans la collection
        avisCollection.insertMany(avisList);

        System.out.println("Avis insérés avec succès.");
    }

    public static void readAllAvis(MongoDatabase database) {
        try {
            // Obtenez la collection Avis
            MongoCollection<Document> avisCollection = database.getCollection("Avis");

            // Récupérez tous les avis de la collection
            FindIterable<Document> result = avisCollection.find();

            // Affichez les détails de chaque avis
            System.out.println("Liste des avis :");
            for (Document avis : result) {
                System.out.println("ID : " + avis.getInteger("_id"));
                System.out.println("ClientID : " + avis.getInteger("ClientID"));
                System.out.println("TrottinetteID : " + avis.getInteger("TrottinetteID"));
                System.out.println("MessageAvis : " + avis.getString("MessageAvis"));
                System.out.println("------------------------");
            }
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
        }
    }

    // Méthode pour mettre à jour plusieurs avis dans la collection Avis
    public static void updateManyAvis(MongoDatabase database, List<Document> avisList) {
        MongoCollection<Document> avisCollection = getAvisCollection(database);

        for (Document avis : avisList) {
            // Récupérer l'ID de l'avis à mettre à jour
            int avisId = avis.getInteger("_id");

            // Créer un filtre pour trouver l'avis à mettre à jour
            Document filtre = new Document("_id", avisId);

            // Créer un document de mise à jour avec tous les champs
            Document miseAJour = new Document("$set", new Document()
                    .append("ClientID", avis.getInteger("ClientID"))
                    .append("TrottinetteID", avis.getInteger("TrottinetteID"))
                    .append("MessageAvis", avis.getString("MessageAvis")));

            // Effectuer la mise à jour
            avisCollection.updateOne(filtre, miseAJour);
        }

        System.out.println("Avis mis à jour avec succès.");
    }

    // Méthode pour supprimer plusieurs avis dans la collection Avis
    public static void deleteManyAvis(MongoDatabase database, List<Integer> avisIds) {
        MongoCollection<Document> avisCollection = getAvisCollection(database);

        for (int avisId : avisIds) {
            // Supprimer l'avis de la collection Avis
            avisCollection.deleteOne(new Document("_id", avisId));

            // Mettre à jour la collection Trottinettes en supprimant l'avis de l'array
            // AvisID
            MongoCollection<Document> trottinettesCollection = database.getCollection("Trottinettes");
            Document miseAJourTrottinettes = new Document("$pull", new Document("AvisID", avisId));
            trottinettesCollection.updateMany(new Document("AvisID", avisId), miseAJourTrottinettes);

            System.out.println("Avis avec l'ID " + avisId + " supprimé avec succès.");
        }
    }
}
