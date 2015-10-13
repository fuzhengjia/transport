import storm
import station_crowd_online


class CrowdPredicateBoltPython(storm.BasicBolt):
    def __init__(self):
        # self.model
        self.model = station_crowd_online.Station_Crowd_Pred("243")
        # storm.logInfo("model initialized!")
    # def prepare(self):
    #     print "aaa"
    def process(self, tup):
        station = tup.values[0];
        time = tup.values[1];
        value = tup.values[2]



        result = self.model.get_prediction([time,value])

        for item in result:
            storm.emit([station,item[0], item[1]],"update_stream")
        # storm.emit([station,time,value],"update_stream")


bolt = CrowdPredicateBoltPython()
bolt.run()