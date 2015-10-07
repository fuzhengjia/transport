namespace java edu.illinois.adsc.transport.generated

struct Query {
1: i64 query_id;
2: string stationId,
3: string timeStamp
}

struct QueryResult {
1: i64 query_id,
2: i64 result
}

service QueryService {
    i64 getNumberOfPeople(1:string stationID, 2:string timeStamp),
    Query takeQuery(),
    void finishQuery(1: QueryResult result)
}