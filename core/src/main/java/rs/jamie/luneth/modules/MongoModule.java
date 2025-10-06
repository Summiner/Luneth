package rs.jamie.luneth.modules;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.ServerApi;
import com.mongodb.ServerApiVersion;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.*;
import org.bson.BsonBinary;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class MongoModule implements Module {

    private static final Charset charSet = StandardCharsets.UTF_8;
    private static final UpdateOptions options = new UpdateOptions().upsert(true);
    private final MongoClient client;
    private final MongoDatabase database;
    private final MongoCollection<Document> collection;

    public MongoModule(String url, String database_name, String collection_name) {
        ServerApi serverApi = ServerApi.builder()
                .version(ServerApiVersion.V1)
                .build();

        MongoClientSettings settings = MongoClientSettings.builder()
                .applyConnectionString(new ConnectionString(url))
                .serverApi(serverApi)
                .build();

        this.client = MongoClients.create(settings);
        this.database = client.getDatabase(database_name);
        this.collection = database.getCollection(collection_name);
    }

    @Override
    public CompletableFuture<ByteBuffer> getObject(ByteBuffer key, String identifier) {
        BsonBinary binaryID = new BsonBinary(addIdentifier(key, identifier).array());

        CompletableFuture<ByteBuffer> future = new CompletableFuture<>();

        collection.find(Filters.eq("_id", binaryID))
                .subscribe(new Subscriber<>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(1);
                    }

                    @Override
                    public void onNext(Document document) {
                        try {
                            Binary binaryData = document.get("data", Binary.class);
                            if (binaryData != null) {
                                ByteBuffer buffer = ByteBuffer.wrap(binaryData.getData());
                                future.complete(buffer);
                            } else {
                                future.complete(null);
                            }
                        } catch (Exception ex) {
                            future.completeExceptionally(ex);
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        future.completeExceptionally(t);
                    }

                    @Override
                    public void onComplete() {
                        future.complete(null);
                    }
                });

        return future;
    }

    @Override
    public CompletableFuture<Boolean> setObject(ByteBuffer key, ByteBuffer value, String identifier) {
        BsonBinary binaryID = new BsonBinary(addIdentifier(key, identifier).array());
        BsonBinary binaryData = new BsonBinary(value.array());

        List<Bson> data = new ArrayList<>();
        data.add(Updates.set("identifier", identifier));
        data.add(Updates.set("data", binaryData));

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        collection.updateOne(Filters.eq("_id", binaryID), data, options)
                .subscribe(new Subscriber<>() {
                    @Override
                    public void onSubscribe(Subscription sub) {
                        sub.request(1);
                    }

                    @Override
                    public void onNext(UpdateResult result) {
                        future.complete(true);
                    }

                    @Override
                    public void onError(Throwable t) {
                        future.completeExceptionally(t);
                    }

                    @Override
                    public void onComplete() {
                        future.complete(null);
                    }
                });

        return future;
    }

    @Override
    public CompletableFuture<Boolean> removeObject(ByteBuffer key, String identifier) {
        BsonBinary binaryID = new BsonBinary(addIdentifier(key, identifier).array());

        CompletableFuture<Boolean> future = new CompletableFuture<>();

        collection.deleteOne(Filters.eq("_id", binaryID))
                .subscribe(new Subscriber<>() {
                    @Override
                    public void onSubscribe(Subscription s) {
                        s.request(1);
                    }

                    @Override
                    public void onNext(DeleteResult result) {
                        future.complete(true);
                    }

                    @Override
                    public void onError(Throwable t) {
                        future.completeExceptionally(t);
                    }

                    @Override
                    public void onComplete() {
                        future.complete(null);
                    }
                });

        return future;
    }

    private ByteBuffer addIdentifier(ByteBuffer buffer, String identifier) {
        if(identifier==null) return null;
        byte[] id = identifier.getBytes(charSet);
        int size = id.length;
        ByteBuffer buf = ByteBuffer.allocate(buffer.remaining() + Integer.BYTES + size);
        buf.putInt(size);
        buf.put(id);
        buf.put(buffer);
        return buf.flip();
    }

}
