package io.hashfunc.issueistiomongodb

import org.bson.types.ObjectId
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository

@Repository
interface HitRepository : MongoRepository<Hit, ObjectId>
