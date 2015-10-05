namespace java edu.illinois.adsc.transport.generated

service QueryService {
    i64 getNumberOfPeople(1:string stationID, 2:string timeStamp);
}