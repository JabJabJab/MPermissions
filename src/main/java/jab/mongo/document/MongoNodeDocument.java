/*
 * Copyright 2018 Joshua Edwards
 *
 * Licensed under the Apache License, Version 2.0.
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jab.mongo.document;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import jab.mongo.MongoCollection;

import java.util.*;

public abstract class MongoNodeDocument extends MongoDocument {

  /** The Map storing MongoNodes by their node value as the key. */
  private Map<String, MongoNode> mapNodes;

  /**
   * Main constructor.
   *
   * @param collection The MongoCollection storing the document.
   * @param fieldId The String identifier for the document.
   */
  public MongoNodeDocument(MongoCollection collection, String fieldId) {
    super(collection, fieldId);
    mapNodes = new HashMap<>();
  }

  /**
   * Handles loading nodes.
   *
   * @param object The DBObject storing the node data.
   */
  public void loadNodes(DBObject object) {
    mapNodes.clear();
    @SuppressWarnings({"rawtypes"})
    List objectList = (List) object.get("nodes");
    for (Object nextObject : objectList) {
      DBObject nextDBObject = (DBObject) nextObject;
      MongoNode mongoNode = new MongoNode(this);
      mongoNode.onLoad(nextDBObject);
      addNode(mongoNode, false);
    }
  }

  /**
   * Handles saving nodes.
   *
   * @param object The DBObject that stores the nodes.
   */
  public void saveNodes(DBObject object) {
    // Create a list of objects for the export.
    List<DBObject> listNodes = new ArrayList<>();
    // Go through each node in the document.
    for (MongoNode mongoNode : getNodes()) {
      // Create the object to contain the saved data.
      DBObject objectMongoNode = new BasicDBObject();
      // Save the MongoNode to the object.
      mongoNode.onSave(objectMongoNode);
      // Add the result object to the list.
      listNodes.add(objectMongoNode);
    }
    // Place the nodes into the main document object provided.
    object.put("nodes", listNodes);
  }

  /**
   * Adds a MongoNode to the document, if the entry does not exist.
   *
   * <p>(All nodes are stored and compared in lower-case automatically)
   *
   * @param mongoNode The MongoNode being added to the document.
   * @param save Flag to save the document after adding the MongoNode.
   * @return Returns true if the MongoNode is added to the document.
   */
  public boolean addNode(MongoNode mongoNode, boolean save) {
    boolean returned = false;
    if (!hasNode(mongoNode)) {
      mapNodes.put(mongoNode.getNode(), mongoNode);
      returned = true;
      if (save) save();
    }
    return returned;
  }

  /**
   * Removes a node from the document, if the entry exists.
   *
   * <p>(All nodes are stored and compared in lower-case automatically)
   *
   * @param mongoNode The MongoNode being removed from the document.
   * @param save Flag to save the document after removing the node.
   * @return Returns true if the MongoNode is removed from the document.
   */
  public boolean removeNode(MongoNode mongoNode, boolean save) {
    boolean returned = false;
    if (hasNode(mongoNode)) {
      mapNodes.remove(mongoNode.getNode());
      returned = true;
      if (save) save();
    }
    return returned;
  }

  /**
   * Checks if a MongoNode is assigned to the document.
   *
   * @param mongoNode The MongoNode being tested.
   * @return Returns true if the document contains the node.
   */
  public boolean hasNode(MongoNode mongoNode) {
    return mapNodes.containsKey(mongoNode.getNode());
  }

  /** @return Returns a List of the String nodes assigned to the document. */
  public Collection<MongoNode> getNodes() {
    return mapNodes.values();
  }
}
