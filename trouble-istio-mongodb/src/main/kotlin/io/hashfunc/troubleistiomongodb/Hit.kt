package io.hashfunc.troubleistiomongodb

import org.bson.types.ObjectId
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document

@Document(collection = "hit")
class Hit {

    @Id
    var id: ObjectId? = null
}
