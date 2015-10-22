namespace java edu.illinois.adsc.transport.generated



struct Query {
1: i64 query_id,
2: i64 query_type,
3: string stationId,
4: string timeStamp
}

struct QueryResult {
1: i64 query_id,
2: i64 query_type,
3: i64 result,
4: i64 result2
}

struct Matrix {
1: i64 rows,
2: i64 columns,
3: list<double> data
}

struct StationUpdate {
1: string stationId,
2: string timeStamp,
3: Matrix updateMatrix,
4: i64 queryType
}

service QueryService {
    list<i64> query(1:string stationID, 2:string timeStamp, 3: i64 queryType),
    Query takeQuery(),
    void finishQuery(1: QueryResult result),
    StationUpdate fetchStateUpdate(),
    void pushUpdate(1: StationUpdate update),
    void pushUpdateForce(1: StationUpdate update),
    string getCurrentTime(),
    bool setTimeStamp(1: string time)
}