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

struct Matrix {
1: i64 rows,
2: i64 columns,
3: list<double> data
}

struct StationUpdate {
1: string stationId,
2: string timeStamp,
3: Matrix updateMatrix
}

service QueryService {
    i64 getNumberOfPeople(1:string stationID, 2:string timeStamp),
    Query takeQuery(),
    void finishQuery(1: QueryResult result),
    StationUpdate fetchStateUpdate(),
    void pushUpdate(1: StationUpdate update),
    void pushUpdateForce(1: StationUpdate update),
    string getCurrentTime(),
    bool setTimeStamp(1: string time)
}