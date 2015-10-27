import storm
import station_crowd_online
import station_waiting_online
import train_crowd_online


def create_model_for_station_crowd_online(station):
    return station_crowd_online.Station_Crowd_Pred(station)

def create_mode_for_station_waiting_online(station):
    return station_waiting_online.Station_flow_Waiting_Pred(station)

def create_mode_for_train_crowd_online(station):
    return train_crowd_online.Station_flow_Waiting_Pred(station)

class CrowdPredicateBoltPython(storm.BasicBolt):
    def __init__(self):
        # self.model
        self.dictionary={}
        self.model_creation_mapping_table = {0: create_model_for_station_crowd_online,
                                    1: create_mode_for_station_waiting_online,
                                    2: create_mode_for_train_crowd_online}
        #storm.logInfo("CrowdPredicateBoltPython is initialized!");
        # self.dictionary["0243"]=station_crowd_online.Station_Crowd_Pred("0243")
        # self.model = station_crowd_online.Station_Crowd_Pred("0243")
        # storm.logInfo("model initialized!")
    # def prepare(self):
    #     print "aaa"
    def process(self, tup):
        station = tup.values[0];
        time = tup.values[1];
        value = tup.values[2];
        query_type = tup.values[3];

        self.init_model_when_necessary(station,query_type)

        result = self.dictionary[station][query_type].get_prediction([time,value])
        for item in result:
            storm.emit([station,query_type,item[0], item[1], item[2]],"update_stream")



    # For a certain query type on a station, the first update is responsible for initializing the model.
    def init_model_when_necessary(self, station, query_type):
        if not self.dictionary.has_key(station):
            self.dictionary[station] = {}
            storm.logInfo("station["+station+"] is initialized!")
        if not self.dictionary[station].has_key(query_type):
            self.dictionary[station][query_type] = self.model_creation_mapping_table[query_type](station)
            storm.logInfo("station["+station+","+ str(query_type)+"] is initialized!")

        # TODO: this function should return if there is any error in the model initialization.

        return 1



bolt = CrowdPredicateBoltPython()
bolt.run()